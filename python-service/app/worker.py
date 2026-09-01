"""
RabbitMQ Job Worker
Processes jobs from the flowforge.jobs queue.
Implements idempotency to safely handle redelivered messages.
"""
import json
import logging
import pika
import os
from datetime import datetime
from sqlalchemy import text
from app.database import SessionLocal
from app.intelligence import calculate_risk, calculate_workload

logger = logging.getLogger(__name__)

RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@rabbitmq:5672/")
JOBS_QUEUE = os.getenv("JOBS_QUEUE", "flowforge.jobs")
MAX_RETRIES = 3


def publish_job_event(ch, job_id: int, status: str):
    if not ch or not job_id:
        return
    event_payload = json.dumps({"jobId": job_id, "status": status})
    try:
        ch.basic_publish(
            exchange='',
            routing_key='flowforge.job.events',
            body=event_payload,
            properties=pika.BasicProperties(delivery_mode=2) # Persistent
        )
    except Exception as e:
        logger.error(f"Failed to publish job event: {e}")

def update_job_status(db, job_id: int, status: str, result: str = None, error: str = None, ch=None):
    now = datetime.utcnow()
    db.execute(text(
        "UPDATE jobs SET status=:status, last_error=:error, updated_at=:now WHERE id=:job_id"
    ), {"status": status, "error": error, "now": now, "job_id": job_id})
    db.commit()
    if ch:
        publish_job_event(ch, job_id, status)



def process_report(payload: dict, db) -> str:
    """Generate a project report by computing aggregate task statistics."""
    project_id = payload.get("projectId")
    if not project_id:
        raise ValueError("Missing projectId in payload")

    rows = db.execute(text(
        "SELECT status, priority, deadline FROM tasks WHERE project_id = :pid"
    ), {"pid": project_id}).fetchall()

    total = len(rows)
    done = sum(1 for r in rows if r[0] == "DONE")
    in_progress = sum(1 for r in rows if r[0] == "IN_PROGRESS")
    blocked = sum(1 for r in rows if r[0] == "BLOCKED")
    overdue = sum(1 for r in rows if r[2] and r[2] < datetime.utcnow() and r[0] != "DONE")

    by_priority = {}
    for r in rows:
        by_priority[r[1]] = by_priority.get(r[1], 0) + 1

    report_data = {
        "generatedAt": datetime.utcnow().isoformat(),
        "projectId": project_id,
        "summary": {
            "totalTasks": total,
            "completedTasks": done,
            "inProgressTasks": in_progress,
            "blockedTasks": blocked,
            "overdueTasks": overdue,
            "completionPercentage": round(done / total * 100, 1) if total > 0 else 0
        },
        "tasksByPriority": by_priority
    }

    # Save report content
    db.execute(text(
        "UPDATE reports SET content=:content WHERE job_id=(SELECT id FROM jobs WHERE idempotency_key=:key)"
    ), {"content": json.dumps(report_data), "key": payload.get("idempotencyKey")})
    db.commit()

    return json.dumps(report_data)


def process_risk(payload: dict, db) -> str:
    task_id = payload.get("taskId")
    result = calculate_risk(task_id)
    return json.dumps(result)


def process_analytics(payload: dict, db) -> str:
    user_id = payload.get("userId")
    if user_id:
        result = calculate_workload(user_id)
    else:
        result = {"message": "No userId provided"}
    return json.dumps(result)


def process_job(ch, method, properties, body):
    db = SessionLocal()
    job_id = None
    try:
        payload = json.loads(body)
        idempotency_key = payload.get("idempotencyKey")
        job_type = payload.get("type", "REPORT").upper()

        # Idempotency check: skip if already completed
        if idempotency_key:
            existing = db.execute(text(
                "SELECT id, status FROM jobs WHERE idempotency_key = :key"
            ), {"key": idempotency_key}).fetchone()
            if existing:
                job_id = existing[0]
                if existing[1] == "COMPLETED":
                    logger.info(f"Job {job_id} already COMPLETED (idempotency), skipping.")
                    ch.basic_ack(delivery_tag=method.delivery_tag)
                    return
                # Mark as processing
                update_job_status(db, job_id, "PROCESSING", ch=ch)

        logger.info(f"Processing job {job_id} type={job_type}")

        if job_type == "REPORT":
            result = process_report(payload, db)
        elif job_type == "RISK":
            result = process_risk(payload, db)
        elif job_type == "ANALYTICS":
            result = process_analytics(payload, db)
        else:
            raise ValueError(f"Unknown job type: {job_type}")

        if job_id:
            update_job_status(db, job_id, "COMPLETED", ch=ch)

        logger.info(f"Job {job_id} completed successfully.")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        logger.error(f"Job processing failed: {e}", exc_info=True)
        if job_id:
            # Check retry count
            retry_row = db.execute(text(
                "SELECT attempt_count FROM jobs WHERE id = :jid"
            ), {"jid": job_id}).fetchone()
            attempt_count = retry_row[0] if retry_row else 0
            if attempt_count < MAX_RETRIES:
                db.execute(text(
                    "UPDATE jobs SET attempt_count = attempt_count + 1, status='QUEUED', last_error=:err, updated_at=:now WHERE id=:jid"
                ), {"err": str(e), "now": datetime.utcnow(), "jid": job_id})
                db.commit()
                publish_job_event(ch, job_id, "QUEUED")
                # Requeue
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
            else:
                update_job_status(db, job_id, "FAILED", error=str(e), ch=ch)
                ch.basic_ack(delivery_tag=method.delivery_tag)
        else:
            ch.basic_ack(delivery_tag=method.delivery_tag)
    finally:
        db.close()


def start_consuming():
    import time
    while True:
        try:
            params = pika.URLParameters(RABBITMQ_URL)
            connection = pika.BlockingConnection(params)
            channel = connection.channel()
            channel.queue_declare(queue=JOBS_QUEUE, durable=True)
            channel.queue_declare(queue='flowforge.job.events', durable=True)
            channel.basic_qos(prefetch_count=1)
            channel.basic_consume(queue=JOBS_QUEUE, on_message_callback=process_job)
            logger.info(f"Worker started. Listening on queue: {JOBS_QUEUE}")
            channel.start_consuming()
        except Exception as e:
            logger.error(f"RabbitMQ connection error: {e}. Retrying in 5s...")
            time.sleep(5)
