"""
Tests for the FastAPI health endpoints.
"""
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))


@pytest.fixture
def client():
    with patch("app.worker.start_consuming"):  # Don't actually connect to RabbitMQ in tests
        from app.main import app
        return TestClient(app)


def test_health_endpoint_returns_healthy(client):
    """Health check should return 200 with healthy status."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["service"] == "flowforge-python-worker"


def test_risk_endpoint_exists(client):
    """Risk endpoint should exist and be callable."""
    with patch("app.main.calculate_risk", return_value={"taskId": 1, "risk": "LOW", "riskScore": 10, "reasons": []}):
        response = client.get("/api/risk/1")
        assert response.status_code == 200


def test_workload_endpoint_exists(client):
    """Workload endpoint should exist and be callable."""
    with patch("app.main.calculate_workload", return_value={"userId": 1, "workloadLevel": "LOW", "workloadScore": 5}):
        response = client.get("/api/workload/1")
        assert response.status_code == 200
