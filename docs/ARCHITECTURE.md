# Architecture

## Components

| Component | Responsibility |
|---|---|
| React frontend | User interface, typed REST clients, authenticated STOMP updates |
| Spring Boot API | Authentication, authorization, domain operations, job orchestration |
| MySQL | Durable users, projects, tasks, jobs, reports, notifications, and audit data |
| RabbitMQ | Job commands and completion events |
| Python service | Report and intelligence processing plus worker metrics |
| Redis | Request-rate counters and cache infrastructure |
| Prometheus and Grafana | Metrics collection, retention, and dashboards |
| Caddy | Production TLS termination, security headers, and reverse proxying |

## Core Report Flow

1. An authenticated user submits a report request to the Java API.
2. The API verifies project membership and creates the report and job records in one transaction.
3. `JobQueuePublisher` publishes only after the transaction commits, preventing workers from observing an uncommitted job.
4. The Python worker consumes the command and uses the idempotency key to avoid duplicate processing.
5. The worker stores the result and publishes a job event.
6. The Java listener broadcasts the authorized status update over STOMP.
7. The frontend refreshes the report state.

## Authorization Model

- `ADMIN` users may access all projects and administrative screens.
- Project owners and project members may access project-scoped resources.
- Write operations enforce the required project role rather than relying on frontend visibility.
- Resource-ID endpoints resolve the parent project before authorizing access.
- Jobs and reports are authorized through their associated project.
- Negative integration tests verify that one user cannot read, modify, or delete another user's resources.

## Data and Migrations

Flyway owns the database schema. Migrations execute in version order from `backend/src/main/resources/db/migration`, and Hibernate uses `ddl-auto: validate`.

Analytics totals and grouped status/priority values are calculated by database queries. Job, report, and audit-list endpoints enforce bounded page sizes to prevent unbounded responses.

## Runtime Profiles

- `prod` is the default Compose profile and does not seed demo users.
- `dev` enables local demo data.
- `docker-compose.e2e.yml` enables `dev` only for the isolated end-to-end stack.

## Observability

The Java management server listens internally on port `9091` and exposes only health and Prometheus endpoints. The Python service exposes `/metrics` internally. Prometheus scrapes both services and Grafana is provisioned with a FlowForge dashboard.

Production keeps management services off public host ports. The authenticated application monitoring endpoint provides a restricted health view to the frontend.
