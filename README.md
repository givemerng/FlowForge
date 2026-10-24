# FlowForge

FlowForge is a full-stack project, task, background-job, reporting, and risk-analysis platform. It combines a React frontend, a secured Spring Boot API, asynchronous Python workers, and production-oriented Docker infrastructure.

## Current Status

The main application flow is implemented and continuously verified:

1. Authenticate with JWT.
2. Create a project and tasks.
3. Request an asynchronous report.
4. Process the job through RabbitMQ and the Python worker.
5. Persist the result and deliver a real-time UI update.

The repository includes authorization boundary tests, Flyway migration tests against MySQL 8, frontend unit tests, Python tests, and a Playwright end-to-end test. GitHub Actions runs the complete suite on every push and pull request.

## Technology

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| Backend | Java 21, Spring Boot 3.2, Spring Security, JWT |
| Data | MySQL 8, Spring Data JPA, Flyway |
| Messaging | RabbitMQ 3, STOMP WebSockets |
| Worker | Python 3.11, FastAPI, SQLAlchemy |
| Cache and limits | Redis 7 |
| Observability | Actuator, Prometheus, Grafana |
| Infrastructure | Docker Compose, Caddy |

```text
React UI -> Spring Boot API -> MySQL
                |              |
                v              |
             RabbitMQ ---------+
                |
                v
          Python worker
                |
                v
        job event -> WebSocket -> React UI
```

See [Architecture](docs/ARCHITECTURE.md) for component boundaries and request flows.

## Quick Start

### Requirements

- Docker Desktop with Compose v2
- Optional for local development: Java 21, Node.js 20+, Python 3.11+

### Start the stack

```bash
cp .env.example .env
docker compose up -d --build --wait
```

PowerShell equivalent:

```powershell
Copy-Item .env.example .env
docker compose up -d --build --wait
```

The base Compose file uses development-safe defaults, but `JWT_SECRET` should be changed before sharing an environment.

### Local services

| Service | Address |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |
| RabbitMQ management | http://localhost:15672 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

The Python worker and Actuator management port remain internal to the Compose network. The production override also removes the development host ports for MySQL, Redis, RabbitMQ, Prometheus, Grafana, the API, and the frontend.

### Optional demo accounts

Demo data is disabled by default. Enable it only for local development by setting this in `.env`:

```dotenv
SPRING_PROFILES_ACTIVE=dev
```

Then restart the stack. The development-only accounts are:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123!` | Administrator |
| `manager` | `manager123!` | Manager |
| `alice` | `alice123!` | Member |

Never enable the `dev` profile in production.

## Major Capabilities

- JWT authentication and role-based access.
- Project membership enforcement for project, task, label, comment, report, job, analytics, and ID-based lookups.
- Kanban project boards, task assignment, labels, comments, and notifications.
- Asynchronous report jobs with retry tracking and transaction-safe publication.
- Deterministic task risk and workload analysis.
- Persisted profile, password, and notification settings.
- Database-side analytics aggregation and bounded job/report/audit pagination.
- Real-time job updates over authenticated WebSockets.
- OpenAPI documentation and operational dashboards.

## API Summary

| Domain | Main endpoints |
|---|---|
| Authentication | `/api/auth/register`, `/api/auth/login` |
| Projects | `/api/projects` |
| Tasks | `/api/projects/{projectId}/tasks`, `/api/tasks/{taskId}` |
| Comments and labels | `/api/tasks/{taskId}/comments`, `/api/tasks/{taskId}/labels` |
| Jobs and reports | `/api/jobs`, `/api/reports` |
| Analytics | `/api/analytics/overview`, `/api/analytics/project/{projectId}` |
| Intelligence | `/api/insights/tasks/{taskId}/risk` |
| Notifications | `/api/notifications` |
| User settings | `/api/users/me`, `/api/users/me/settings`, `/api/users/me/password` |
| Monitoring | `/api/monitoring/health` |

Use Swagger UI for request and response schemas.

## Testing

Run the complete local checks as documented in [Testing](docs/TESTING.md).

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm ci && npm run lint && npm test && npm run build

# Python
cd python-service
python -m venv .venv
# activate the environment, then:
pip install -r requirements-dev.txt
pytest -q
```

The end-to-end suite uses `docker-compose.e2e.yml`, which enables isolated demo data without weakening the default runtime profile.

## Production Deployment

Production uses required secrets, automatic TLS, internal-only service ports, database backups, metric retention, and log rotation.

```bash
python ops/generate_env.py --domain flowforge.example.com --email ops@example.com
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  up -d --build --wait
```

Before starting, point the selected domain to the server and permit inbound TCP ports `80` and `443` plus UDP `443` if HTTP/3 is desired. See [Production Deployment](docs/DEPLOYMENT.md) for backup, restore, monitoring, upgrade, and rollback procedures.

## Development

```bash
# Backend
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend
npm ci
npm run dev

# Python API/worker service
cd python-service
uvicorn app.main:app --reload --port 8000
```

Database schema changes must be added as versioned migrations under `backend/src/main/resources/db/migration`. Hibernate runs with schema validation rather than schema mutation.

## Documentation

- [Architecture and security model](docs/ARCHITECTURE.md)
- [Testing and CI](docs/TESTING.md)
- [Production deployment and operations](docs/DEPLOYMENT.md)
- [Troubleshooting history](error_log.md)
- [Frontend development](frontend/README.md)

## Roadmap

Potential future product extensions include organization-level multi-tenancy, OAuth2/SSO, a learned risk model, and native mobile clients. These are enhancements rather than current functional blockers.
