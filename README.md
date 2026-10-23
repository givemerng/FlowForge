# FlowForge

**Intelligent Job & Workflow Management Platform**

A full-stack enterprise application for managing projects, tasks, background jobs, and intelligent risk analysis.

---

## Architecture

```
React Frontend  →  Spring Boot API  →  MySQL 8
                        ↓
                    RabbitMQ
                        ↓
                  Python Workers  →  Risk/Workload Analysis
                        ↓
                  Job Status Update
```

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| Backend API | Java 21, Spring Boot 3.2, Spring Security, JWT |
| Database | MySQL 8.0 (JPA/Hibernate, Flyway migrations) |
| Cache | Redis 7 (rate limiting) |
| Message Broker | RabbitMQ 3 (async job queue) |
| Background Workers | Python 3.11, FastAPI, SQLAlchemy |
| Monitoring | Spring Actuator, Prometheus, Grafana |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Infrastructure | Docker, Docker Compose |

---

## Quick Start

### Prerequisites
- Docker Desktop
- (Optional) Node.js 20+ and Java 21 for local development

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env with your secrets (JWT_SECRET is required)
```

### 2. Start all services

```bash
docker compose up -d
```

### 3. Access the application

| Service | URL | Credentials |
|---|---|---|
| Frontend | http://localhost:5173 | - |
| Backend API | http://localhost:8080 | - |
| API Docs (Swagger) | http://localhost:8080/swagger-ui.html | - |
| Python worker | Internal Compose network only | - |
| RabbitMQ Dashboard | http://localhost:15672 | guest / guest |
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | - |

### 4. Seed demo data (optional)

The Compose stack defaults to the production profile and does not create demo accounts. To seed local demo data, set `SPRING_PROFILES_ACTIVE=dev` in `.env`, then start the stack:

```bash
docker compose up -d
```

**Demo accounts (dev only — never use in production):**
- `admin / admin123!` — full access
- `manager / manager123!` — project management
- `alice / alice123!` — member

---

## Authentication

FlowForge uses stateless JWT authentication.

- `POST /api/auth/register` — create account
- `POST /api/auth/login` — returns a JWT token (24h expiry by default)
- All other endpoints require `Authorization: Bearer <token>`
- Role-based access: `ADMIN`, `MANAGER`, `MEMBER`

---

## API Overview

| Domain | Endpoint |
|---|---|
| Auth | `/api/auth/register`, `/api/auth/login` |
| Projects | `/api/projects` (CRUD) |
| Tasks | `/api/projects/{id}/tasks`, `/api/tasks/{id}` |
| Comments | `/api/tasks/{id}/comments` |
| Jobs | `/api/jobs`, `/api/jobs/{id}/retry` |
| Reports | `/api/reports` (async via RabbitMQ) |
| Analytics | `/api/analytics/overview`, `/api/analytics/project/{id}` |
| Notifications | `/api/notifications`, `/api/notifications/read-all` |
| Search | `/api/search?q=query` |
| Monitoring health | `/api/monitoring/health` (authenticated) |

Full documentation: http://localhost:8080/swagger-ui.html

---

## Background Jobs

1. User requests a report/risk analysis from the frontend
2. Java API creates a `Job` record in MySQL (status: `QUEUED`)
3. Java API publishes a message to the `flowforge.jobs` RabbitMQ queue
4. Python worker consumes the message
5. Worker checks idempotency key — skips if already processed
6. Worker computes result and updates `Job` status to `COMPLETED`
7. Java publishes a STOMP event and the frontend updates in real time

**Job types:** `REPORT`, `RISK`, `ANALYTICS`
**Max retries:** 3 (tracked in `JobAttempt` table)

---

## Intelligent Risk Analysis

The Python service implements a deterministic, explainable risk scoring algorithm:

**Risk Factors:**
- Task priority (CRITICAL/HIGH add significant score)
- Deadline proximity (overdue adds 40 points, within 24h adds 30)
- Task status (BLOCKED adds 25 points)
- Assignee workload (high active task count adds score)

**Output:**
```json
{
  "risk": "HIGH",
  "riskScore": 87,
  "reasons": [
    "Task has critical priority",
    "Task is overdue by 3 day(s)",
    "Assignee has high workload (9 active tasks)"
  ]
}
```

---

## Redis

- Rate limiting: 100 requests/minute per user with an IP fallback.
- Project and task reads are membership-scoped; administrators can access all projects.

---

## Monitoring

- Spring Boot Actuator exposes only `/actuator/health` and `/actuator/prometheus` on the internal management port `9091`.
- The authenticated monitoring UI reads `/api/monitoring/health`; the management port is not published by Compose.
- Prometheus scrapes metrics every 15 seconds from both Java API and Python service
- Python metrics include RabbitMQ connectivity and job outcomes
- Grafana dashboards available at http://localhost:3000

---

## Testing

### Java (JUnit + Spring Boot Test)
```bash
cd backend
mvn test
```

### Python (Pytest)
```bash
cd python-service
python -m venv .venv
.venv/Scripts/python -m pip install -r requirements-dev.txt
.venv/Scripts/python -m pytest tests/ -v
```

### Frontend (React Testing Library)
```bash
cd frontend
npm test
```

### End-to-end (Playwright + Docker Compose)
```bash
docker compose -f docker-compose.yml -f docker-compose.e2e.yml up --build --wait
cd frontend
npx playwright install chromium
npm run test:e2e
```

The E2E override enables dev-only seed accounts without changing the production-default stack. The test proves login, project and task creation, queued report processing by the Python worker, WebSocket status delivery, and the completed report in the UI.

---

## Development Workflow

```bash
# Backend dev
cd backend && mvn spring-boot:run -Dspring.profiles.active=dev

# Frontend dev
cd frontend && npm run dev

# Python worker dev
cd python-service && uvicorn app.main:app --reload --port 8000
```

---

## Future Improvements

- Machine learning model replacing the rule-based risk engine
- Multi-tenant organization support
- OAuth2/SSO integration
- Mobile application

---

## Production Notes

- Generate a production-only environment file with cryptographically random secrets:

```bash
python ops/generate_env.py --domain flowforge.example.com --email ops@example.com
```

- Point the domain's DNS records at the server, then start the hardened stack:

```bash
docker compose --env-file .env.production -f docker-compose.yml -f docker-compose.prod.yml up -d --build --wait
```

- Caddy terminates HTTPS and renews certificates automatically. MySQL, Redis, RabbitMQ, Prometheus, Grafana, the Java API, and the frontend are not directly published by the production override.
- MySQL is dumped daily to the `mysql_backups` volume. Dumps older than `BACKUP_RETENTION_DAYS` are removed automatically. Restore with `mysql --host=mysql --user="$MYSQL_USER" "$MYSQL_DATABASE" < backup.sql` from a MySQL client attached to the Compose network.
- Prometheus data defaults to 30-day retention in `prometheus_data`; container JSON logs rotate at 10 MB with five retained files. Both limits are environment configurable.
- Keep `SPRING_PROFILES_ACTIVE=prod`; the `dev` profile and its demo credentials are enabled only by the explicit E2E override.
- Add schema changes as new files under `backend/src/main/resources/db/migration`; Hibernate only validates the schema.
- GitHub Actions runs backend, frontend, Python, Compose validation, and the complete Playwright flow on every push and pull request.
