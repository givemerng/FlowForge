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
| Backend API | Java 17, Spring Boot 3.2, Spring Security, JWT |
| Database | MySQL 8.0 (JPA/Hibernate, auto-migrated) |
| Cache | Redis 7 (project/analytics caching, rate limiting) |
| Message Broker | RabbitMQ 3 (async job queue) |
| Background Workers | Python 3.11, FastAPI, SQLAlchemy |
| Monitoring | Spring Actuator, Prometheus, Grafana |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Infrastructure | Docker, Docker Compose |

---

## Quick Start

### Prerequisites
- Docker Desktop
- (Optional) Node.js 18+ and Java 17+ for local development

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env with your secrets (especially JWT_SECRET)
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
| Python Service | http://localhost:8000 | - |
| RabbitMQ Dashboard | http://localhost:15672 | guest / guest |
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | - |

### 4. Seed demo data (optional)

Start the Java API with the `dev` profile to auto-seed users, projects, and tasks:

```bash
SPRING_PROFILES_ACTIVE=dev docker compose up java-api
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
| Health | `/actuator/health` |

Full documentation: http://localhost:8080/swagger-ui.html

---

## Background Jobs

1. User requests a report/risk analysis from the frontend
2. Java API creates a `Job` record in MySQL (status: `QUEUED`)
3. Java API publishes a message to the `flowforge.jobs` RabbitMQ queue
4. Python worker consumes the message
5. Worker checks idempotency key — skips if already processed
6. Worker computes result and updates `Job` status to `COMPLETED`
7. Frontend polls for job status update

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

## Redis Caching

- `GET /api/projects` — cached under `"projects"` key
- `GET /api/projects/{id}` — cached under `"project::{id}"`
- Cache is evicted on create/update/delete
- Rate limiting: 100 requests/minute per user (IP-based fallback)

---

## Monitoring

- Spring Boot Actuator exposed: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- Prometheus scrapes metrics every 15 seconds from both Java API and Python service
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
pip install -r requirements.txt
pytest tests/ -v
```

### Frontend (React Testing Library)
```bash
cd frontend
npm test
```

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

- WebSocket real-time job status updates (Spring STOMP configured)
- Drag-and-drop Kanban board
- Machine learning model replacing the rule-based risk engine
- Multi-tenant organization support
- OAuth2/SSO integration
- Mobile application
