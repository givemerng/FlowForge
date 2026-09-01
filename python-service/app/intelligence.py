"""
Risk Analysis Worker
Implements deterministic, explainable rule-based risk scoring.
Designed so an ML model can replace the scoring logic later.
"""
from datetime import datetime, timezone
from sqlalchemy import text
from app.database import SessionLocal
import logging

logger = logging.getLogger(__name__)

def calculate_risk(task_id: int) -> dict:
    """
    Calculate risk score for a task based on:
    - Deadline proximity
    - Task priority
    - Current status (blocking, in-progress)
    - Assignee workload
    Returns a dict with risk level, score, and reasons.
    """
    db = SessionLocal()
    try:
        # Fetch task data
        task_row = db.execute(text(
            "SELECT t.id, t.title, t.status, t.priority, t.deadline, t.assigned_to_id, t.project_id "
            "FROM tasks t WHERE t.id = :task_id"
        ), {"task_id": task_id}).fetchone()

        if not task_row:
            return {"error": f"Task {task_id} not found"}

        task = dict(task_row._mapping)
        reasons = []
        score = 0

        # Factor 1: Priority weight
        priority_scores = {"CRITICAL": 30, "HIGH": 20, "MEDIUM": 10, "LOW": 5}
        priority = task.get("priority", "MEDIUM")
        score += priority_scores.get(priority, 10)
        if priority in ("CRITICAL", "HIGH"):
            reasons.append(f"Task has {priority.lower()} priority")

        # Factor 2: Deadline proximity
        deadline = task.get("deadline")
        if deadline:
            now = datetime.now()
            days_left = (deadline - now).days if isinstance(deadline, datetime) else None
            if days_left is not None:
                if days_left < 0:
                    score += 40
                    reasons.append(f"Task is overdue by {abs(days_left)} day(s)")
                elif days_left <= 1:
                    score += 30
                    reasons.append("Deadline is within 24 hours")
                elif days_left <= 3:
                    score += 20
                    reasons.append(f"Deadline is in {days_left} days")
                elif days_left <= 7:
                    score += 10
                    reasons.append(f"Deadline is in {days_left} days")
        else:
            score += 5
            reasons.append("No deadline set — tracking risk unknown")

        # Factor 3: Status (blocked is highest risk)
        status = task.get("status", "TODO")
        status_scores = {"BLOCKED": 25, "TODO": 10, "IN_PROGRESS": 5, "REVIEW": 2, "DONE": -20}
        score += status_scores.get(status, 0)
        if status == "BLOCKED":
            reasons.append("Task is currently blocked")

        # Factor 4: Assignee workload
        assignee_id = task.get("assigned_to_id")
        if assignee_id:
            workload_row = db.execute(text(
                "SELECT COUNT(*) as cnt FROM tasks WHERE assigned_to_id = :uid AND status NOT IN ('DONE') "
            ), {"uid": assignee_id}).fetchone()
            active_tasks = workload_row[0] if workload_row else 0
            if active_tasks > 8:
                score += 15
                reasons.append(f"Assignee has high workload ({active_tasks} active tasks)")
            elif active_tasks > 5:
                score += 8
                reasons.append(f"Assignee has moderate workload ({active_tasks} active tasks)")
        else:
            score += 10
            reasons.append("Task is unassigned")

        # Clamp score 0-100
        score = max(0, min(100, score))

        # Determine risk level
        if score >= 70:
            risk_level = "HIGH"
        elif score >= 40:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        if not reasons:
            reasons.append("No significant risk factors identified")

        return {
            "taskId": task_id,
            "risk": risk_level,
            "riskScore": score,
            "reasons": reasons,
            "calculatedAt": datetime.utcnow().isoformat()
        }

    except Exception as e:
        logger.error(f"Risk calculation failed for task {task_id}: {e}")
        return {"error": str(e)}
    finally:
        db.close()


def calculate_workload(user_id: int) -> dict:
    """
    Calculate workload score for a user based on active tasks,
    priorities, and deadlines.
    """
    db = SessionLocal()
    try:
        now = datetime.now()

        active_tasks_row = db.execute(text(
            "SELECT COUNT(*) FROM tasks WHERE assigned_to_id = :uid AND status NOT IN ('DONE')"
        ), {"uid": user_id}).fetchone()
        active_tasks = active_tasks_row[0] if active_tasks_row else 0

        high_priority_row = db.execute(text(
            "SELECT COUNT(*) FROM tasks WHERE assigned_to_id = :uid AND status NOT IN ('DONE') "
            "AND priority IN ('HIGH', 'CRITICAL')"
        ), {"uid": user_id}).fetchone()
        high_priority = high_priority_row[0] if high_priority_row else 0

        overdue_row = db.execute(text(
            "SELECT COUNT(*) FROM tasks WHERE assigned_to_id = :uid AND status NOT IN ('DONE') "
            "AND deadline IS NOT NULL AND deadline < :now"
        ), {"uid": user_id, "now": now}).fetchone()
        overdue_tasks = overdue_row[0] if overdue_row else 0

        upcoming_row = db.execute(text(
            "SELECT COUNT(*) FROM tasks WHERE assigned_to_id = :uid AND status NOT IN ('DONE') "
            "AND deadline IS NOT NULL AND deadline BETWEEN :now AND :soon"
        ), {"uid": user_id, "now": now, "soon": now.replace(day=now.day+7) if now.day <= 24 else now}).fetchone()
        upcoming_deadlines = upcoming_row[0] if upcoming_row else 0

        # Compute workload score
        score = 0
        score += min(active_tasks * 8, 40)      # up to 40 points for volume
        score += min(high_priority * 10, 30)    # up to 30 points for urgency
        score += min(overdue_tasks * 15, 30)    # up to 30 points for overdue

        score = max(0, min(100, score))

        if score >= 70:
            level = "HIGH"
        elif score >= 40:
            level = "MEDIUM"
        else:
            level = "LOW"

        return {
            "userId": user_id,
            "activeTasks": active_tasks,
            "highPriorityTasks": high_priority,
            "overdueTasks": overdue_tasks,
            "upcomingDeadlines": upcoming_deadlines,
            "workloadScore": score,
            "workloadLevel": level,
            "calculatedAt": datetime.utcnow().isoformat()
        }

    except Exception as e:
        logger.error(f"Workload calculation failed for user {user_id}: {e}")
        return {"error": str(e)}
    finally:
        db.close()
