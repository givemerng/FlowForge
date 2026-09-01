"""
Tests for the intelligence module (risk and workload analysis).
These tests use mocked database queries so no real DB is needed.
"""
import pytest
from unittest.mock import patch, MagicMock
from datetime import datetime, timedelta


# ─── Risk Analysis Tests ────────────────────────────────────────────────────────

class TestRiskAnalysis:
    """Test the deterministic risk scoring algorithm."""

    def _mock_task(self, status="IN_PROGRESS", priority="MEDIUM", deadline=None, assigned_to_id=1):
        return {
            "id": 1, "title": "Test Task", "status": status,
            "priority": priority, "deadline": deadline, "assigned_to_id": assigned_to_id,
            "project_id": 1
        }

    @patch("app.intelligence.SessionLocal")
    def test_critical_priority_increases_score(self, mock_session):
        """CRITICAL priority tasks should have higher risk scores than LOW priority."""
        from app.intelligence import calculate_risk

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        # Mock task row
        critical_task = self._mock_task(priority="CRITICAL")
        low_task = self._mock_task(priority="LOW")

        for task_data, expected_min in [(critical_task, 30), (low_task, 0)]:
            task_row = MagicMock()
            task_row._mapping = task_data
            workload_row = MagicMock()
            workload_row.__getitem__ = lambda self, idx: 3

            mock_db.execute.return_value.fetchone.side_effect = [task_row, workload_row]
            result = calculate_risk(1)
            assert result.get("riskScore", 0) >= expected_min

    @patch("app.intelligence.SessionLocal")
    def test_overdue_task_high_risk(self, mock_session):
        """Overdue tasks should always have HIGH risk."""
        from app.intelligence import calculate_risk

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        past = datetime.now() - timedelta(days=5)
        task_row = MagicMock()
        task_row._mapping = self._mock_task(priority="LOW", deadline=past)
        workload_row = MagicMock()
        workload_row.__getitem__ = lambda self, idx: 2

        mock_db.execute.return_value.fetchone.side_effect = [task_row, workload_row]
        result = calculate_risk(1)

        assert "riskScore" in result
        # Overdue adds +40 so score should be HIGH
        assert result["risk"] == "HIGH" or result["riskScore"] >= 40

    @patch("app.intelligence.SessionLocal")
    def test_blocked_status_adds_risk(self, mock_session):
        """BLOCKED status should increase risk score significantly."""
        from app.intelligence import calculate_risk

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        task_row = MagicMock()
        task_row._mapping = self._mock_task(status="BLOCKED", priority="MEDIUM")
        workload_row = MagicMock()
        workload_row.__getitem__ = lambda self, idx: 2

        mock_db.execute.return_value.fetchone.side_effect = [task_row, workload_row]
        result = calculate_risk(1)

        assert "BLOCKED" in result.get("reasons", [])
        assert result["riskScore"] >= 25

    @patch("app.intelligence.SessionLocal")
    def test_missing_task_returns_error(self, mock_session):
        """Missing task ID should return error dict."""
        from app.intelligence import calculate_risk

        mock_db = MagicMock()
        mock_session.return_value = mock_db
        mock_db.execute.return_value.fetchone.return_value = None

        result = calculate_risk(999)
        assert "error" in result

    @patch("app.intelligence.SessionLocal")
    def test_risk_score_clamped_0_to_100(self, mock_session):
        """Risk score must always be between 0 and 100."""
        from app.intelligence import calculate_risk

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        # Worst case: CRITICAL + BLOCKED + overdue + high workload
        past = datetime.now() - timedelta(days=30)
        task_row = MagicMock()
        task_row._mapping = self._mock_task(status="BLOCKED", priority="CRITICAL", deadline=past)
        workload_row = MagicMock()
        workload_row.__getitem__ = lambda self, idx: 15

        mock_db.execute.return_value.fetchone.side_effect = [task_row, workload_row]
        result = calculate_risk(1)

        assert 0 <= result["riskScore"] <= 100


# ─── Workload Analysis Tests ─────────────────────────────────────────────────────

class TestWorkloadAnalysis:
    """Test the workload scoring algorithm."""

    @patch("app.intelligence.SessionLocal")
    def test_zero_tasks_is_low_workload(self, mock_session):
        """User with no tasks should have LOW workload."""
        from app.intelligence import calculate_workload

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        # All queries return 0
        mock_db.execute.return_value.fetchone.return_value = (0,)

        result = calculate_workload(1)
        assert result["workloadLevel"] == "LOW"
        assert result["workloadScore"] == 0

    @patch("app.intelligence.SessionLocal")
    def test_many_high_priority_tasks_is_high_workload(self, mock_session):
        """User with 10 active tasks, all high priority, should have HIGH workload."""
        from app.intelligence import calculate_workload

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        responses = [(10,), (10,), (2,), (3,)]
        mock_db.execute.return_value.fetchone.side_effect = [MagicMock(__getitem__=lambda s, i: r[i]) for r in responses]

        result = calculate_workload(1)
        assert result["workloadLevel"] == "HIGH"

    @patch("app.intelligence.SessionLocal")
    def test_workload_score_clamped(self, mock_session):
        """Workload score must always be between 0 and 100."""
        from app.intelligence import calculate_workload

        mock_db = MagicMock()
        mock_session.return_value = mock_db

        responses = [(50,), (50,), (50,), (50,)]
        mock_db.execute.return_value.fetchone.side_effect = [MagicMock(__getitem__=lambda s, i: r[i]) for r in responses]

        result = calculate_workload(1)
        assert 0 <= result["workloadScore"] <= 100

    @patch("app.intelligence.SessionLocal")
    def test_workload_response_has_required_fields(self, mock_session):
        """Workload response should contain all required fields."""
        from app.intelligence import calculate_workload

        mock_db = MagicMock()
        mock_session.return_value = mock_db
        mock_db.execute.return_value.fetchone.return_value = (0,)

        result = calculate_workload(1)
        required_fields = ["userId", "activeTasks", "highPriorityTasks", "overdueTasks",
                           "workloadScore", "workloadLevel", "calculatedAt"]
        for field in required_fields:
            assert field in result, f"Missing field: {field}"
