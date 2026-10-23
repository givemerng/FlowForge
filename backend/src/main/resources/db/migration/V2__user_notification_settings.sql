ALTER TABLE users
    ADD COLUMN task_assigned_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN task_completed_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN job_failed_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN report_ready_notifications BOOLEAN NOT NULL DEFAULT TRUE;
