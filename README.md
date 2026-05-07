# OYCI Scheduling & Assignment Service (Quarkus)

Backend scaffold for OYCI staff scheduling and event assignment workflows.  
This service is intended to replace manual spreadsheet-based coordination with API-driven scheduling, staffing, and historical reporting.

## Purpose and scope

The service focuses on:
- Maintaining core scheduling master data (staff, qualifications, event types, locations)
- Managing scheduled sessions/events within schedule periods
- Assigning staff to events with eligibility/conflict rules
- Providing historical assignment views for admin/coordinator users

Current scaffold scope is **very early MVP foundation** with a working event API shape and in-memory relational storage.

## Bounded contexts and package architecture

Target modular-monolith architecture (from planning):
- `masterdata` — Staff, Qualification, EventType, Location
- `scheduling` — SchedulePeriod, SessionEvent lifecycle
- `assignment` — eligibility, staffing, workload/conflict checks
- `history` — list/calendar read models
- `notification` — weekly assignment communications

Planned package layers:
- `api` (REST resources + DTOs)
- `application` (use-case services)
- `domain` (entities/rules)
- `infrastructure` (persistence/integrations/messaging)
- `shared` (cross-cutting concerns)

### Current scaffold reality

The code now includes the initial bounded-context split:
- Existing starter package (`resources`, `services`, `models`) still powers the original `/events` API.
- New scaffold contexts are in place:
  - `masterdata` (`api`, `domain`)
  - `scheduling` (`api`, `domain`)
  - `assignment` (`api`, `domain`, `application`)

These new context endpoints currently return in-memory scaffold data/contracts and are ready to be backed by persistent services.

## Core entities and relationships (ER-style)

### Implemented in scaffold
- `Event`
  - fields: `eventId`, `eventType`, `eventDate`, `eventLocation`
- `EventType` enum
  - values: `CONFERENCE`, `WORKSHOP`, `MEETUP`, `WEBINAR`, `OTHER`

Persistence currently uses table `events` with columns:
`event_id`, `event_type`, `event_date`, `event_location`, `event_epoch_millis`.

### Planned MVP domain model
- `StaffMember` ↔ `Qualification` = N:M (via `StaffQualification`)
- `SessionEvent` ↔ `StaffMember` = N:M (via `EventStaffAssignment`)
- `SessionEvent` → `EventType`, `Location`, `SchedulePeriod` = N:1

## Endpoint groups

## Implemented now (scaffold)
- Existing events API:
  - `GET /events` — list upcoming events only
  - `POST /events` — create event
  - `GET /events/{eventId}` — get event by UUID
- New masterdata scaffold:
  - `GET /staff`
  - `GET /qualifications`
  - `GET /event-types`
  - `GET /locations`
- New scheduling scaffold:
  - `GET /schedule-periods`
- New assignment scaffold:
  - `GET /events/{eventId}/eligibility`
  - `GET /events/{eventId}/assignments`
  - `POST /events/{eventId}/assignments`
  - `GET /assignments`
- `GET /hello` — starter sample endpoint

## MVP target groups (planned)
- **Master data**: `/staff`, `/qualifications`, `/event-types`, `/locations`
- **Scheduling**: `/schedule-periods`, `/events`, event filtering/status
- **Assignment**: eligibility, assign/unassign, weekly-hour views
- **History**: list and calendar query endpoints

## Phase-2 roadmap summary
- assignment suggestion/scoring endpoints
- conflict simulation tools
- Excel import pipeline endpoints
- email preview/send/scheduling endpoints
- auth/role management
- booking + Zoho integration endpoints

## Persistence decision and rationale

Decision: **Relational-first** (PostgreSQL target; H2 in current scaffold).

Rationale:
- Assignment workflows need strong consistency and transactional safety
- Scheduling/reporting uses join-heavy query shapes
- Many-to-many relations are first-class (staff qualifications, event assignments)
- Historical audit/reporting is simpler with relational constraints

Non-relational/document storage remains useful for raw import staging, but not as the primary system of record.

## Local development, run, and test

Prerequisites:
- Java 17+

Commands:

```bash
# run in dev mode (hot reload)
./mvnw quarkus:dev

# run unit/integration test lifecycle configured by Maven
./mvnw test

# package runnable JVM artifact
./mvnw package

# run packaged app
java -jar target/quarkus-app/quarkus-run.jar

# optional native build
./mvnw package -Dnative
```

Quarkus Dev UI (dev mode only): <http://localhost:8080/q/dev/>

## Scaffold status: implemented vs planned

Implemented:
- Quarkus project skeleton with REST/Jackson + H2 datasource
- Event service with table bootstrap and seed data
- Upcoming-event query, create-event flow, lookup-by-id flow
- Domain scaffold packages and placeholder endpoints for masterdata/scheduling/assignment
- REST tests for event, greeting, and domain scaffold endpoint coverage

Planned / not yet implemented:
- Persistent storage and application services behind new scaffold endpoints
- Full business-rule engine (eligibility/conflict/workload)
- History and notification features
- External integrations and import pipeline




TODO:
- scheduling based on staff availability and event requirements
-- admin endpoints to perform auto schedule and manual assignment
- rudimentary login with scopes (admin and user)
-- change to allow a user and admin functionality
- employee overview (hours worked, upcoming event schedule, qualifications, etc)
