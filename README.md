# Datech MVP

Datech is a simple MVP system for freelancers to manage clients, projects, time tracking and invoicing in one place.

## Stack

- Frontend: React (SPA, Vite)
- Backend: Spring Boot (Java 17)
- Database: PostgreSQL
- API: REST
- Hosting later: AWS or Azure

## Features Implemented

- Client management (CRUD)
- Project management (CRUD)
- Time entry tracking (CRUD)
- Invoice management (CRUD)
- Payment records (CRUD API)
- Budget overflow check per project
- Profitability calculation from tracked hours and hourly rate
- Overdue invoice detection
- Dashboard alerts endpoint

## Project Structure

```
.
|- backend/          # Spring Boot REST API
|- frontend/         # React SPA
`- docker-compose.yml
```

## Run With Docker (DB + Backend)

From repository root:

```bash
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5432`
- Backend API on `localhost:8080`

## Run Frontend Locally

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173` and calls backend at `http://localhost:8080/api`.

## Run Backend Locally (without Docker for backend)

Ensure PostgreSQL is available (for example via `docker compose up db`) and then:

```bash
cd backend
mvn spring-boot:run
```

## Main API Endpoints

- `GET/POST /api/clients`
- `GET/PUT/DELETE /api/clients/{id}`
- `GET/POST /api/projects`
- `GET/PUT/DELETE /api/projects/{id}`
- `GET /api/projects/{id}/profitability`
- `GET /api/projects/{id}/budget-status`
- `GET/POST /api/time-entries`
- `GET/PUT/DELETE /api/time-entries/{id}`
- `GET/POST /api/invoices`
- `GET/PUT/DELETE /api/invoices/{id}`
- `GET /api/invoices/overdue`
- `GET/POST /api/payments`
- `GET/PUT/DELETE /api/payments/{id}`
- `GET /api/dashboard/alerts`

## Notes

- This is an MVP baseline and uses simple entity-level JSON payloads.
- Authentication/authorization is not yet implemented.
- Deployment configs for AWS/Azure can be added as next step (for example with Terraform and CI/CD).
