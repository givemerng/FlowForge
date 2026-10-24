# Testing and CI

## Backend

Requirements: Java 21, Maven, and Docker for the MySQL Testcontainers test.

```bash
cd backend
mvn test
```

The suite covers authentication errors, project and task operations, cross-user authorization boundaries, database analytics, transaction-safe job publication, and Flyway migrations against MySQL 8.

## Frontend

```bash
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Vitest uses the configured worker pool to avoid the previous Windows worker hang. Unit tests cover task cards, role-aware navigation, and login behavior.

## Python

PowerShell:

```powershell
cd python-service
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements-dev.txt
.\.venv\Scripts\python.exe -m pytest -q
```

Bash:

```bash
cd python-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements-dev.txt
pytest -q
```

## End-to-End

```bash
docker compose -p flowforge-e2e \
  -f docker-compose.yml \
  -f docker-compose.e2e.yml \
  up -d --build --wait --wait-timeout 240

cd frontend
npx playwright install chromium
PLAYWRIGHT_SKIP_WEBSERVER=1 npm run test:e2e
```

PowerShell uses `$env:PLAYWRIGHT_SKIP_WEBSERVER='1'` before `npm run test:e2e`.

Clean up after the run:

```bash
docker compose -p flowforge-e2e \
  -f docker-compose.yml \
  -f docker-compose.e2e.yml \
  down -v
```

The scenario proves login, project creation, task creation, report queueing, Python processing, WebSocket delivery, and completed report rendering.

## Compose Validation

```bash
docker compose config --quiet
docker compose -f docker-compose.yml -f docker-compose.e2e.yml config --quiet
docker compose --env-file .env.production \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  config --quiet
```

## GitHub Actions

`.github/workflows/ci.yml` runs five jobs:

1. Backend tests on Java 21.
2. Frontend lint, unit tests, and production build.
3. Python tests.
4. Base, E2E, and production Compose validation.
5. The complete Playwright flow after all component jobs pass.

Container logs are printed when E2E completes or fails, and the CI stack is always removed.
