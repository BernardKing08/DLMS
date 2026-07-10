# Digital Library Management System (DLMS)

A microservices-based library management system. Four independent Spring Boot services, each with its own database, coordinating over HTTP through a shared Config Server.

## Architecture

```
                        ┌───────────────┐
                        │  Config Server │  :8071
                        │ (Spring Cloud) │
                        └───────┬───────┘
                                │ serves config to all services
        ┌───────────────┬──────┴──────┬───────────────┐
        │               │             │               │
   ┌────▼────┐     ┌────▼────┐   ┌────▼────┐     ┌────▼─────┐
   │  Auth   │◄───►│ Account │   │ Catalog │◄───►│ Inventory │
   │  :8090  │     │  :8080  │   │  :9000  │     │  :9010    │
   └────┬────┘     └────┬────┘   └────┬────┘     └────┬─────┘
        │               │             │               │
   ┌────▼────┐     ┌────▼────┐   ┌────▼────┐     ┌────▼─────┐
   │ authdb  │     │accountsdb│   │catalogdb│     │inventorydb│
   │  MySQL  │     │  MySQL  │   │  MySQL  │     │   MySQL   │
   └─────────┘     └─────────┘   └─────────┘     └───────────┘
```

**Cross-service relationships:**
- **Auth ↔ Account** — registering a user in Auth automatically provisions a matching profile in Account (fire-and-forget, after the registration transaction commits). Account validates any `userId` it's given against Auth before creating/trusting a profile.
- **Catalog ↔ Inventory** — cataloguing a book automatically provisions a stock record in Inventory. Inventory validates any `bookId` it's given against Catalog. Catalog enriches book reads with live stock numbers pulled from Inventory.

Each service talks to the others only over HTTP (via a `WebClient`-based `*Client` class per dependency, in a `client/` package) — never by touching another service's database directly.

## Services

| Service | Module path | Port | Database | Purpose |
|---|---|---|---|---|
| Config Server | `configServer/` | 8071 | — | Serves shared config to all services from this Git repo |
| Authentication | `authentication/auth/` | 8090 | `authdb` | User registration, login, role management |
| Account | `account/acc/` | 8080 | `accountsdb` | User profile data (name, contact info) |
| Catalog | `catalog/catalogs/` | 9000 | `catalogdb` | Book records |
| Inventory | `inventory/invtry/` | 9010 | `inventorydb` | Stock levels per book |

Stack: Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.3, MySQL, Maven (Jib for image builds).

## Running locally

```
cd docker-compose/default
docker compose up -d
```

This starts the Config Server, all four MySQL containers, and all four app services on the `bernardking08` bridge network. `depends_on` + healthchecks ensure the Config Server and each service's own database are ready before that service starts.

**MySQL ports** (published to host, offset from the default 3306 to avoid clashing with a local MySQL install):

| DB | Host port | Container-internal port |
|---|---|---|
| accountsdb | 3307 | 3306 |
| authdb | 3308 | 3306 |
| catalogdb | 3309 | 3306 |
| inventorydb | 3310 | 3306 |

Inside the Docker network, services must always address each other's databases on the internal port `3306` (e.g. `jdbc:mysql://authdb:3306/authdb`) — the host ports above only matter when connecting from outside Docker (e.g. a MySQL client on your machine, or an app run directly from the IDE, would use `localhost:3308` for `authdb`).

Rebuilding a single service's image after a code change (Jib, pushes to Docker Hub):
```
cd authentication/auth
./mvnw compile jib:build
docker pull bernardking08/auth-service:cc1
docker compose up -d
```

## API Reference

All requests use `Content-Type: application/json`. Base URLs: `http://localhost:<port>` from your host machine.

### Authentication — `:8090`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | `{ "name", "email", "password", "confirmPassword" }` | Creates the user, auto-provisions an Account profile after commit |
| POST | `/api/auth/login` | `{ "email", "password" }` | |
| GET | `/api/auth/users/{id}` | — | Used by Account to validate a `userId` |

### Account — `:8080`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/accounts` | `{ "userId", "firstName", "lastName", "phoneNumber", "address" }` | `userId` and `firstName` required; validates `userId` against Auth first |
| GET | `/api/accounts/{userId}` | — | |
| PUT | `/api/accounts/{userId}` | `{ "firstName", "lastName", "phoneNumber", "address" }` | `firstName`/`lastName` required |

### Catalog — `:9000`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/catalog.ctlog` | `{ "title", "author", "isbn", "category", "description", "totalCopies" }` | `title`/`author`/`isbn` required; auto-provisions an Inventory record after commit |
| GET | `/api/catalog.ctlog/{id}` | — | Response enriched with live `totalCopies`/`availableCopies` from Inventory |
| GET | `/api/catalog.ctlog` | — | List, same enrichment per item |

### Inventory — `:9010`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/inventory` | `{ "bookId", "totalCopies" }` | Validates `bookId` against Catalog first |
| GET | `/api/inventory/{bookId}` | — | |

## Example request

```
POST http://localhost:8090/api/auth/register
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "secret123",
  "confirmPassword": "secret123"
}
```
→ `201`, and a matching Account profile appears shortly after at `GET /api/accounts/{userId}`.

## Notes for local development

- Config Server (`configServer/src/main/resources/application.yml`) points at this repo's `CreatingInterConnection` branch (`default-label`) — update this if working from a different branch, or it'll silently serve whatever's on that branch regardless of your local checkout.
- Each service's `SPRING_DATASOURCE_URL` is set as a Docker Compose environment variable (`docker-compose/default/docker-compose.yml`) and always overrides the `application.yml` value when running via Compose — the `application.yml` datasource URL only takes effect if you run a service directly (outside Docker), and uses `localhost` for that case.
- `spring.sql.init.mode: always` is set in every service, backed by a `schema.sql` in each service's `resources/` — table names there must match each JPA entity's `@Table(name = ...)` exactly (a `Role`/`roles` mismatch was a real bug caught during setup).
