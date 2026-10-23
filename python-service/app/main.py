from fastapi import FastAPI
from fastapi.responses import Response
from prometheus_client import CONTENT_TYPE_LATEST, generate_latest
import threading
import logging
import os
from contextlib import asynccontextmanager

from app.worker import start_consuming
from app.intelligence import calculate_risk, calculate_workload

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(_app: FastAPI):
    worker_thread = threading.Thread(target=start_consuming, daemon=True)
    worker_thread.start()
    logger.info("RabbitMQ worker thread started.")
    yield

app = FastAPI(
    title="FlowForge Python Service",
    description="Background processing, risk analysis, and workload intelligence",
    version="1.0.0",
    lifespan=lifespan,
)

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "flowforge-python-worker",
        "version": "1.0.0"
    }


@app.get("/metrics", include_in_schema=False)
def metrics():
    return Response(content=generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.get("/api/risk/{task_id}")
def get_risk_analysis(task_id: int):
    """Get risk analysis for a specific task."""
    return calculate_risk(task_id)


@app.get("/api/workload/{user_id}")
def get_workload(user_id: int):
    """Get workload analysis for a specific user."""
    return calculate_workload(user_id)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
