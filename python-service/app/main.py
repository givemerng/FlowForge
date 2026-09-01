from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import threading
import logging
import os

from app.worker import start_consuming
from app.intelligence import calculate_risk, calculate_workload

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="FlowForge Python Service",
    description="Background processing, risk analysis, and workload intelligence",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "flowforge-python-worker",
        "version": "1.0.0"
    }


@app.get("/api/risk/{task_id}")
def get_risk_analysis(task_id: int):
    """Get risk analysis for a specific task."""
    return calculate_risk(task_id)


@app.get("/api/workload/{user_id}")
def get_workload(user_id: int):
    """Get workload analysis for a specific user."""
    return calculate_workload(user_id)


@app.on_event("startup")
def startup_event():
    worker_thread = threading.Thread(target=start_consuming, daemon=True)
    worker_thread.start()
    logger.info("RabbitMQ worker thread started.")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
