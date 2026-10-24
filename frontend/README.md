# FlowForge Frontend

The frontend is a React 18 and TypeScript application built with Vite and Tailwind CSS. It provides role-aware navigation, project and task management, analytics, reporting, monitoring, notifications, and persisted user settings.

## Requirements

- Node.js 20 or newer
- npm
- A FlowForge backend at `VITE_API_BASE_URL`

## Commands

```bash
npm ci
npm run dev       # local Vite server
npm run lint      # ESLint with zero warnings
npm test          # Vitest unit tests
npm run build     # TypeScript and production bundle
npm run test:e2e  # Playwright against the Compose stack
```

## Configuration

The default development API address is `http://localhost:8080/api`. Override it through `VITE_API_BASE_URL` when needed.

The production container is built with `VITE_API_BASE_URL=/api`, allowing Caddy to serve the frontend and API from the same HTTPS origin.

## Structure

| Path | Purpose |
|---|---|
| `src/pages` | Route-level application screens |
| `src/components` | Shared layout and UI components |
| `src/services` | Typed API clients |
| `src/hooks` | Shared hooks, including WebSocket handling |
| `src/types.ts` | Shared domain and API types |
| `tests` | Playwright end-to-end scenarios |

## Authentication

The login response is stored in browser local storage and attached to API requests as a bearer token. Navigation visibility is role-aware, but backend authorization remains the security boundary. WebSocket connections also include the JWT.

## Testing

Unit tests use Vitest, React Testing Library, and jsdom. The current coverage includes task rendering, role-based sidebar visibility, and login success/error behavior.

The Playwright scenario validates the complete flow through Docker Compose: login, project creation, task creation, report submission, Python worker processing, and completed report display.
