# Implementation Plan: Youth Organisation Event Staffing System (OYCI)

## Overview
This is a Quarkus backend REST API that manages event staffing for a youth organisation. It uses PostgreSQL, JWT-based RBAC, Hibernate ORM with Panache, Flyway for migrations, and Quarkus Mailer for email.

---

## Phase 1: Project Foundation & Dependencies

**Goal:** Upgrade the bare-bones Quarkus project with all required dependencies.

Add to `pom.xml`:
- `quarkus-hibernate-orm-panache` — ORM / Panache repositories
- `quarkus-jdbc-postgresql` — PostgreSQL JDBC driver
- `quarkus-flyway` — Database migrations
- `quarkus-smallrye-jwt` & `quarkus-smallrye-jwt-build` — JWT auth
- `quarkus-rest-jackson` — JSON serialisation
- `quarkus-mailer` — Email sending
- `quarkus-security` — `@RolesAllowed` support

Configure `application.properties` with datasource, JWT issuer, and mailer settings (with dev/test profiles using H2 or a Testcontainers PostgreSQL).

Add `docker-compose.yml` with `db` (PostgreSQL) and `backend` services.

---

## Phase 2: Database Schema (Flyway Migrations)

**Goal:** Create the full relational schema as described in the data model spec.

Create `src/main/resources/db/migration/V1__initial_schema.sql` defining:

| Table | Key fields |
|---|---|
| `users` | id, name, email, password_hash, role (ADMIN/STAFF/PARTICIPANT), max_hours_per_week |
| `tags` | id, name (unique) |
| `user_tags` | user_id, tag_id (composite PK) |
| `user_availability` | id, user_id, day_of_week (1–7), start_time, end_time |
| `user_holidays` | id, user_id, start_date, end_date |
| `locations` | id, name, address fields, contact fields, default_capacity |
| `event_types` | id, name, description, duration_minutes |
| `event_type_tags` | event_type_id, tag_id (composite PK) |
| `event_instances` | id, event_type_id, location_id, event_date, start_time, status (DRAFT/PUBLISHED/STAFFED/COMPLETED/CANCELLED), capacity_override |
| `event_assignments` | id, event_instance_id, user_id (unique constraint on pair) |

Seed with an initial Admin user (`V2__seed_admin.sql`).

---

## Phase 3: JPA Entities & Panache Repositories

**Goal:** Create Java entity classes mapped to each table.

Package: `org.scottishtecharmy.oyci.quarkus`

Entities (all extend `PanacheEntity`):
- `User` — with `Role` enum (ADMIN, STAFF, PARTICIPANT)
- `Tag`
- `UserAvailability`
- `UserHoliday`
- `Location`
- `EventType`
- `EventInstance` — with `EventStatus` enum
- `EventAssignment`

---

## Phase 4: Authentication

**Goal:** Secure endpoints with JWT and RBAC.

- `AuthResource` (`POST /api/auth/login`, `POST /api/auth/register`)
- `AuthService` — validates credentials (BCrypt password hashing via `quarkus-elytron-security-common`), builds JWT using `JWTClaimsSet` (SmallRye JWT Build), including `sub`, `name`, `email`, and `role` claims
- Login returns `{ token, name, email, role }`
- Register creates a PARTICIPANT user by default

---

## Phase 5: Resource Management (Admin CRUD APIs)

**Goal:** Implement the simple CRUD endpoints for configuration entities.

Each resource follows a standard pattern (list, create, update by id, delete by id):

- `TagResource` → `/api/tags` — `@RolesAllowed("ADMIN")`
- `LocationResource` → `/api/locations` — `@RolesAllowed("ADMIN")`
- `EventTypeResource` → `/api/event-types` — `@RolesAllowed("ADMIN")`

Each backed by a corresponding `*Service` class with basic Panache operations.

---

## Phase 6: Staff Management APIs

**Goal:** Staff CRUD (Admin) and self-service endpoints.

- `StaffResource` → `/api/staff`
  - Admin CRUD: GET all, GET by id, POST, PUT, DELETE
  - Self-service (ADMIN or own user):
    - `PUT /staff/{id}/availability` — replace weekly availability windows
    - `PUT /staff/{id}/max-hours` — update max hours
    - `PUT /staff/{id}/tags` — update qualifications
    - `GET /staff/{id}/schedule` — upcoming assigned events
    - `GET/POST/DELETE /staff/{id}/holidays` — manage holiday date ranges

---

## Phase 7: Event Instance APIs

**Goal:** Implement event scheduling and lifecycle management.

- `EventInstanceResource` → `/api/event-instances`
  - `GET` — list with optional `from`, `to`, `status` filters
  - `POST` — create (status defaults to DRAFT)
  - `GET /{id}` — get details
  - `PUT /{id}` — update
  - `DELETE /{id}` — delete
  - `POST /{id}/assign` — assign a staff member
  - `DELETE /{id}/assign/{staffId}` — unassign
  - `POST /{id}/publish` — transition DRAFT → PUBLISHED (validates minimum staffing)
  - `GET /{id}/available-staff` — triggers the Assignment Engine (see Phase 8)

---

## Phase 8: Assignment Engine Service

**Goal:** Core business logic — the most complex piece.

`AssignmentEngineService` evaluates every staff member against a given event instance and returns a list of `StaffSuitabilityDTO` objects, each containing:

- **Staff details** (id, name, email)
- **Status**: `PERFECT_MATCH` or `WARNING`
- **Warning flags** (can be multiple):
  - `MISSING_QUALIFICATION` — staff tags don't fully cover event type's required tags
  - `OUTSIDE_AVAILABLE_HOURS` — event time not within staff's availability window for that day of week
  - `EXCEEDS_WEEKLY_HOUR_LIMIT` — current assigned hours + event duration > max_hours_per_week
  - `SCHEDULING_CONFLICT` — staff already assigned to another event overlapping this time slot
  - `ON_HOLIDAY` — event date falls within a staff holiday range

---

## Phase 9: Participant Portal APIs

**Goal:** Allow participants to discover and register for events.

- `ParticipantResource` → `/api/participant`
  - `GET /events` — list PUBLISHED upcoming events (`@RolesAllowed({"PARTICIPANT", "ADMIN"})`)
  - `POST /events/{id}/register` — register self for an event
  - `DELETE /events/{id}/register` — cancel registration

---

## Phase 10: Communications API

**Goal:** Batch email sending for weekly schedules.

- `CommunicationsResource` → `/api/communications`
  - `POST /notify-staff` — `@RolesAllowed("ADMIN")`, accepts a date range, queries all assignments in that period, groups by staff member, and sends a formatted email to each via Quarkus Mailer

---

## Phase 11: Infrastructure Files

**Goal:** Complete the deployment setup.

- `docker-compose.yml` — `db` (postgres:16), `backend` (Quarkus JVM image)
- `Caddyfile` — reverse proxy, route `/api/*` to backend, static files to frontend
- Update `application.properties` with all required config keys (datasource URL, JWT secret, SMTP settings) using `${ENV_VAR}` references for secrets

---

## Proposed Build Order

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7 → Phase 8 → Phase 9 → Phase 10 → Phase 11
```

Each phase builds on the previous. Phases 5–7 can largely proceed in parallel once Phases 3 & 4 are done. Phase 8 (Assignment Engine) depends on Phases 5–7 being in place.

