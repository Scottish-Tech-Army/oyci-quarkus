# Bruno collection for OYCI Quarkus API

This folder contains a local Bruno collection that covers all currently implemented API endpoints.

## Prerequisites

- Start the API locally (default: `http://localhost:8080`):
  - `./mvnw quarkus:dev`
- Bruno desktop app or Bruno CLI (`bru`)

## Collection layout

- `bruno.json` - collection metadata
- `environments/local.bru` - local variables (`baseUrl`, `eventId`, `assignmentId`)
- `requests/*.bru` - one request per endpoint

## Suggested run order

1. `01-get-hello`
2. `02-get-events`
3. `03-post-events`
4. Update `eventId` in `environments/local.bru` with the created event id from step 3.
5. `04-get-event-by-id`
6. `05-get-staff`
7. `06-get-qualifications`
8. `07-get-event-types`
9. `08-get-locations`
10. `09-get-schedule-periods`
11. `10-get-event-eligibility`
12. `11-get-event-assignments`
13. `12-post-event-assignment`
14. `13-get-assignments`

## Run with Bruno CLI

From repository root:

```bash
bru run --env local bruno
```

You can also run a single request:

```bash
bru run --env local bruno/requests/03-post-events.bru
```

Each request includes a basic status code assertion.
