# Crear Code Cesar — Corporate Site & Lead Platform

Full-stack monorepo for the corporate site of **Crear Code Cesar S.A.S.**, built on the current
generation of the Java and Angular stacks: **Spring Boot 4.1 / Java 25** on the backend and
**Angular 22 with SSR** on the frontend, deployed as two containerized services.

It is a working product — lead capture, customer accounts with email verification, an
authenticated admin panel and an AI assistant — but it is also where I keep my architecture
practice honest: every non-obvious decision is written down as an ADR, and every limit in the
system has a reason next to it in the code.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 25 · Spring Boot 4.1 · Spring Security · JWT (HS256) |
| Frontend | Angular 22 · TypeScript · SSR · SCSS |
| Database | PostgreSQL 18 (shared instance on the company VPS) |
| AI | Groq · `openai/gpt-oss-120b` |
| Mail | SMTP (Mailpit locally · Resend in production) |
| Infrastructure | Docker · Kamal 2 on a Netcup VPS · Caddy for the static site |
| CI | GitHub Actions |
| Testing | Unit · integration · end-to-end · `axe` accessibility checks |

---

## Repository layout

```
.
├── .github/workflows/   CI pipelines
├── backend/             Spring Boot 4.1 service (Java 25)
├── frontend/            Angular 22 application (SSR)
├── docs/                Architecture, ADRs and deployment guides
├── config/              Kamal deploy files (api and web)
├── .kamal/secrets       Secret *names* — values live in GitHub Actions secrets
└── docker-compose.yml   Local stack (Postgres + Mailpit, optional full stack)
```

---

## Engineering decisions worth pointing at

**Secrets never live in the repository.** `.kamal/secrets` holds only the *names* of the
credentials; the values are GitHub Actions secrets injected at deploy time and never
committed. The defaults in `application.properties` are deliberately useless in production —
they read `cambiar-en-produccion`. The Groq API key is environment-only, and the assistant's
provider URL is configurable so tests can point at a local stub instead.

**Rate limiting is layered, not global.** Different endpoints face different threats, so each
one gets its own budget instead of a single blanket rule:

| Endpoint | Limit |
|---|---|
| Contact form | 20 / 10 min |
| Admin login | 5 / 15 min |
| Account registration | 5 / 60 min |
| Email verification | 10 / 15 min |
| Resend verification | 3 / 15 min |
| Password recovery | 3 / 15 min |
| Password reset | 5 / 15 min |
| AI assistant | 30 / 15 min |

The assistant additionally enforces a daily ceiling **before** calling the provider — 800
requests globally, 50 for a registered user, 10 for an anonymous one — so a burst of traffic
can never burn through the upstream quota.

**Deployment is reproducible.** `config/deploy.api.yml` and `config/deploy.web.yml` describe
both Kamal apps, their health checks and their environment contracts. They share one host:
the static site takes every path and the API is routed by prefix (`/api`, `/actuator`), so
the browser sees a single origin and the project needs no CORS. The deploy job depends on the
full test suite and only rebuilds the app whose files changed.

**The database is shared, the app is disposable.** PostgreSQL runs on the same VPS with a
role and database per project; the application itself holds no state.

**Decisions are documented.** `docs/` holds the architecture notes and ADRs — why the admin
username had to become an email address, why the frontend proxies `/api`, why Groq was picked
as the assistant provider. The reasoning survives even when the person who made it does not
remember it.

---

## Running it locally

Requirements: Docker, and — if you want hot reload — JDK 25 and Node.

```bash
# Postgres + Mailpit only, then run backend and frontend natively (hot reload)
docker compose up -d
cd backend  && ./mvnw spring-boot:run
cd frontend && npm start

# or the entire stack containerized, using the production Dockerfiles
docker compose --profile full up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 (`4300` in full profile) |
| Backend | http://localhost:8080 |
| Mailpit inbox | http://localhost:8025 |
| Postgres | `localhost:5433` |

Port `5433` is intentional: `5432` is usually taken by another local project. Override
`BACKEND_PORT` or `FRONTEND_PORT` if they collide on your machine.

---

## Testing

Unit and integration tests run against the real persistence layer. The end-to-end suite covers
the flows that are easy to break and expensive to get wrong:

- **Account flow** — registration through to email verification, with the test reading the
  actual link out of the Mailpit inbox over its REST API rather than mocking the email away.
- **AI assistant** — driven against a local Groq stub, so the suite is deterministic and costs
  nothing to run.
- **Accessibility** — `axe` assertions across 12 pages, including the
  authenticated admin panel.

---

## Deployment

Two Kamal apps on a company VPS, built from their production Dockerfiles by GitHub Actions and
pushed to GHCR: the Angular site prerendered and served by Caddy, and the Spring Boot API.
Health checks at `/actuator/health` for the backend and `/up` for the site. TLS from Let's
Encrypt via kamal-proxy. See `docs/09-despliegue.md` for the full walkthrough and ADR-13 for
the reasoning.

---

## Status

Active. Work is tracked as numbered issues and grouped into phases — the most recent being
account management with email verification, and the AI assistant verified end to end against
the live Groq API.

---

## Author

**Juan Carlos Gutiérrez Huérfano** — Backend engineer, Java · Spring Boot · Microservices.
Bogotá, Colombia.

[GitHub](https://github.com/JuanCarlosGuti) · [LinkedIn](https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582)
