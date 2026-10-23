CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);
CREATE INDEX idx_projects_owner ON projects (owner_id);

CREATE TABLE project_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_project_members_user_project UNIQUE (user_id, project_id),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    project_id BIGINT NOT NULL,
    assigned_to_id BIGINT,
    deadline DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assigned_to_id) REFERENCES users (id)
);
CREATE INDEX idx_tasks_project_assignee_status_priority_deadline
    ON tasks (project_id, assigned_to_id, status, priority, deadline);

CREATE TABLE labels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_labels_project FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE task_labels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_task_label UNIQUE (task_id, label_id),
    CONSTRAINT fk_task_labels_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_labels_label FOREIGN KEY (label_id) REFERENCES labels (id)
);

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    body TEXT NOT NULL,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);

CREATE TABLE jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL,
    payload TEXT,
    status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_jobs_idempotency_key UNIQUE (idempotency_key)
);
CREATE INDEX idx_jobs_status_created_at ON jobs (status, created_at);

CREATE TABLE job_attempts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    attempt_number INT NOT NULL,
    error_message TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_job_attempts_job FOREIGN KEY (job_id) REFERENCES jobs (id)
);

CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    job_id BIGINT,
    type VARCHAR(255) NOT NULL,
    content TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_reports_job UNIQUE (job_id),
    CONSTRAINT fk_reports_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_reports_creator FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT fk_reports_job FOREIGN KEY (job_id) REFERENCES jobs (id)
);

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    resource VARCHAR(255) NOT NULL,
    resource_id BIGINT,
    metadata TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
);
