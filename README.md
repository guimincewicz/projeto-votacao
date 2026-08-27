# Cooperative Voting API

REST API for managing cooperative agendas, voting sessions and votes. It uses Java 17, Spring Boot 3, MongoDB and Maven.

## Architecture

The project follows a deliberately small layered structure: `controller` exposes HTTP, `service` contains business rules, `repository` accesses MongoDB, `model` holds persisted documents, and `dto` defines the public API. The CPF provider is isolated behind `VoterEligibilityService`, keeping the voting rule independent from HTTP details. This favors clarity and testability over unnecessary abstractions.

## Run

Requirements: Java 17+, Maven 3.9+ and MongoDB 7+.

```bash
mvn clean test
mvn spring-boot:run
```

Or start both services with Docker:

```bash
docker compose up --build
```

The API runs at `http://localhost:8080`; API documentation is available at `http://localhost:8080/swagger-ui.html`.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/agendas` | Creates an agenda |
| POST | `/api/v1/agendas/{agendaId}/sessions` | Opens its voting session |
| POST | `/api/v1/agendas/{agendaId}/votes` | Registers a vote |
| GET | `/api/v1/agendas/{agendaId}/result` | Gets the result |

```bash
curl -X POST http://localhost:8080/api/v1/agendas \
  -H 'Content-Type: application/json' \
  -d '{"title":"Annual budget","description":"Cooperative budget vote"}'

curl -X POST http://localhost:8080/api/v1/agendas/{agendaId}/sessions \
  -H 'Content-Type: application/json' -d '{"durationMinutes":5}'

curl -X POST http://localhost:8080/api/v1/agendas/{agendaId}/votes \
  -H 'Content-Type: application/json' \
  -d '{"associateId":"123456","cpf":"12345678909","vote":"YES"}'
```

## Business rules

- An agenda must exist before a session or vote is created.
- A session lasts one minute by default, or the positive `durationMinutes` informed in the request.
- There is one session per agenda and votes are accepted strictly before `closesAt`.
- Each associate has one vote per agenda; permitted values are `YES` and `NO`.
- Results are `APPROVED`, `REJECTED`, or `TIED` when the counts are equal.

## Integrity and performance

MongoDB creates a unique compound index on `agendaId` and `associateId`. This is the authoritative duplicate-vote protection: competing writes cause MongoDB to reject one with a duplicate-key error, which is returned as HTTP 409. A unique session index also prevents two concurrent session openings.

Results use indexed `countByAgendaIdAndVote` queries, not an in-memory scan of every vote. The `agendaId` index and the compound uniqueness index serve the expected voting/result access patterns.

## CPF integration

The default local implementation allows votes so that development and tests do not rely on an external service. To enable the remote provider, set:

```yaml
external:
  user-info:
    enabled: true
    base-url: https://user-info.herokuapp.com
```

or export `USER_INFO_ENABLED=true`. A remote 404 produces an invalid-CPF error; an unavailable or non-authorized response is rejected with HTTP 422.

## API conventions and errors

The URL prefix `/api/v1` is explicit, simple and easy to evolve when a breaking API version is needed. Errors have a consistent timestamp, status, error, message and path body. Invalid input uses 400, missing resources use 404, duplicate operations use 409 and closed sessions/CPF eligibility failures use 422.

## Tests and load test

Run `mvn clean test`. Unit tests focus on default/custom session duration, vote registration, duplicate protection, closing behavior and tied results. `performance/voting-test.js` is a K6 script; create and open an agenda first, then run:

```bash
k6 run -e AGENDA_ID=your-agenda-id performance/voting-test.js
```

No mobile-screen contract was modeled: the supplied prompt refers to an Annex 1 but does not provide the JSON contract needed to safely implement `FORMULARIO` or `SELECAO` representations. The domain REST endpoints remain independent and ready for an adapter DTO when that specification is available.
