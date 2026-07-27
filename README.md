# Digital Library Management System (DLMS)

A microservices-based library management system. Four independent Spring Boot services, each with its own database, sitting behind an API Gateway, coordinating over HTTP through a shared Config Server, with Eureka for service discovery.

## Architecture

```
                                    ┌────────────────┐
                    clients ──────► │  Gateway Server │ :8072
                                    │ (routes + traces)│
                                    └────────┬────────┘
                                             │ lb://SERVICE (via Eureka)
        ┌────────────────┬───────────────────┼───────────────┬────────────────┐
        │                │                   │               │                │
   ┌────▼────┐      ┌────▼────┐        ┌─────▼─────┐   ┌─────▼─────┐   ┌──────▼──────┐
   │  Auth   │◄────►│ Account │        │  Catalog   │◄─►│ Inventory │   │Eureka Server│ :8761
   │  :8090  │      │  :8080  │        │   :9000    │   │  :9010    │   │ (discovery) │
   └────┬────┘      └────┬────┘        └─────┬──────┘   └─────┬─────┘   └──────┬──────┘
        │                │                   │                │                │
   ┌────▼────┐      ┌────▼────┐        ┌─────▼─────┐   ┌──────▼──────┐         │
   │ authdb  │      │accountsdb│       │ catalogdb │   │ inventorydb │         │
   │MySQL/H2 │      │ MySQL/H2 │       │ MySQL/H2  │   │  MySQL/H2   │         │
   └─────────┘      └─────────┘        └───────────┘   └─────────────┘         │
                                                                                 │
                                    ┌────────────────┐                          │
                                    │  Config Server  │ :8071 ◄──────────────────┘
                                    │ (Spring Cloud)  │   (all six services fetch shared
                                    └────────────────┘    config from here, and all six
                                                           register with Eureka)
```

**Cross-service relationships:**
- **Auth ↔ Account** — registering a user in Auth automatically provisions a matching profile in Account (fire-and-forget, after the registration transaction commits). Account validates any `userId` it's given against Auth before creating/trusting a profile.
- **Catalog ↔ Inventory** — cataloguing a book automatically provisions a stock record in Inventory. Inventory validates any `bookId` it's given against Catalog. Catalog enriches book reads with live stock numbers pulled from Inventory.
- **Gateway → all four** — the Gateway is the single entry point clients are meant to use. It doesn't call services directly by hostname; it asks Eureka for a healthy instance of the target service (`lb://ACCOUNT`, `lb://AUTHENTICATION`, `lb://CATALOG`, `lb://INVENTORY`) and load-balances across however many instances are currently registered.

Each service talks to the others only over HTTP (via a `WebClient`-based `*Client` class per dependency, in a `client/` package) — never by touching another service's database directly.

## Services

| Service | Module path | Port | Database | Purpose |
|---|---|---|---|---|
| Config Server | `configServer/` | 8071 | — | Serves shared config to all six services from this Git repo |
| Eureka Server | `eurekaserver/` | 8761 | — | Service discovery/registry — all four microservices and the Gateway register here |
| Gateway Server | `gatewayserver/` | 8072 | — | Single entry point; routes `/dlms/<service>/**` to the matching service via Eureka, adds request tracing |
| Authentication | `authentication/auth/` | 8090 | `authdb` | User registration, login, role management |
| Account | `account/acc/` | 8080 | `accountsdb` | User profile data (name, contact info) |
| Catalog | `catalog/catalogs/` | 9000 | `catalogdb` | Book records |
| Inventory | `inventory/invtry/` | 9010 | `inventorydb` | Stock levels per book |

Stack: Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.3, MySQL (default profile) or H2 in-memory (`qa` profile), Maven (Jib for image builds). Note: `eurekaserver` and `gatewayserver` were both generated separately and run Spring Boot 4.1.0 / Spring Cloud 2025.1.2 — newer than the rest of the project. This hasn't caused problems since Eureka/Gateway talk to everything else over plain HTTP, but worth knowing if you touch either module — some config property names differ between the two generations (see the Gateway Routing section below for a real example of this).

## Running locally

```
cd docker-compose/default
docker compose up -d
```

This starts the Config Server, Eureka Server, all four MySQL containers, the Gateway, and all four app services on the `bernardking08` bridge network. `depends_on` + healthchecks ensure the Config Server and each service's own database are ready before that service starts, and the Gateway waits on all four app services being healthy before it starts.

The compose file also brings up **`accounts1`** (port 8081) and **`auth1`** (port 8091) — second instances of Account and Auth, registered under the same Eureka service ID as their originals. These exist specifically so you can see the Gateway's load balancing actually pick between two instances rather than just always hitting one — not required for the system to work, just useful for testing that `lb://` is really load-balancing and not just routing to a single hardcoded address.

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

### QA profile (no MySQL needed)

```
cd docker-compose/qa
docker compose up -d
```
Same four services + Config Server, but each runs on the `qa` Spring profile (`SPRING_PROFILES_ACTIVE=qa` is set in `common-config.yml`), which activates `application-qa.yml` in each service — an in-memory H2 database instead of MySQL. No MySQL containers exist in this compose file at all.

To run all four services standalone against H2 (no Docker at all — useful for quickly testing something like Eureka or the Gateway without spinning up the full stack), use the helper script instead of starting each one by hand:
```
& "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\docs\run-qa.ps1"
```
This opens four separate PowerShell windows (`account`, `auth`, `catalog`, `inventory`), each already on the `qa` profile, so you can watch each service's logs independently. See the script's own header comment for the execution-policy workaround if PowerShell blocks it from running. It doesn't start `eurekaserver` or `gatewayserver` — start those separately (`cd eurekaserver && ./mvnw spring-boot:run`, same for `gatewayserver`) if you want the full discovery/routing picture while testing standalone.

To run just one service standalone:
```
cd account/acc
$env:SPRING_PROFILES_ACTIVE = "qa"   # PowerShell
./mvnw spring-boot:run
```
(Setting the profile via `-Dspring-boot.run.profiles=qa` also works, but must be quoted in PowerShell — e.g. `mvn spring-boot:run "-Dspring-boot.run.profiles=qa"` — otherwise PowerShell can mis-split the argument and Maven fails with `Unknown lifecycle phase`.)

Each `application-qa.yml` uses `jdbc:h2:mem:<dbname>;MODE=MySQL` — the `MODE=MySQL` makes H2 parse MySQL-flavored SQL (e.g. `AUTO_INCREMENT`), so the existing `schema.sql` runs unmodified against it, no separate QA schema needed. Credentials are `root`/`root` in both the yml and the Compose env vars, kept consistent so either way of running it works the same.

## Gateway routing & request tracing

The Gateway doesn't use Spring Cloud Gateway's automatic "one route per registered service" discovery locator (that setting exists in `gatewayserver/application.yml` but is set to `enabled: false`) — routes are instead defined explicitly as a `RouteLocator` bean in `GatewayserverApplication.java`. Each route matches a path prefix, **strips that prefix**, and forwards the rest of the path to the target service by its Eureka service ID:

| Gateway path | Forwarded to | Example |
|---|---|---|
| `/dlms/account/**` | `lb://ACCOUNT` | `GET localhost:8072/dlms/account/api/accounts/1` → Account's `GET /api/accounts/1` |
| `/dlms/authentication/**` | `lb://AUTHENTICATION` | `POST localhost:8072/dlms/authentication/api/auth/register` → Auth's `POST /api/auth/register` |
| `/dlms/catalog/**` | `lb://CATALOG` | `GET localhost:8072/dlms/catalog/api/catalog` → Catalog's `GET /api/catalog` |
| `/dlms/inventory/**` | `lb://INVENTORY` | `GET localhost:8072/dlms/inventory/api/inventory/1` → Inventory's `GET /api/inventory/1` |

The `lb://` prefix means "look this service ID up in Eureka and pick one of its registered, healthy instances" — not a fixed hostname. That's what makes the `accounts1`/`auth1` second instances meaningful: hit the same Gateway URL enough times and you should see requests land on both `account` and `account1` in turn.

**A real gotcha worth knowing**: the property that turns on the discovery locator is `spring.cloud.gateway.server.webflux.discovery.locator.enabled` in this Gateway's Spring Cloud version (5.0.2) — *not* the classic `spring.cloud.gateway.discovery.locator.enabled` you'll find in most tutorials and Stack Overflow answers. The artifact itself was renamed from `spring-cloud-starter-gateway` to `spring-cloud-starter-gateway-server-webflux` in this generation, and the property prefix moved with it. Using the old property name doesn't error — Spring Boot silently ignores unrecognized properties by default — it just quietly never turns anything on, which is a much harder bug to notice than a crash.

**Correlation ID tracing**: two global filters in `gatewayserver/src/main/java/com/dlms/gatewayserver/filters/` (`RequestTraceFilter`, `ResponseTraceFilter`) generate a UUID correlation ID for every request that doesn't already carry one (header name `eazybank-correlation-id`), and echo it back on the response. All four microservices' `application.yml` files set:
```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name},%X{correlationId}]"
```
so if you extend each service's own filter/interceptor to read that incoming header into MDC (`org.slf4j.MDC`) under the key `correlationId`, every log line from every service involved in handling one request — Gateway included — will carry the same ID, making it possible to trace a single request across service boundaries just by grepping logs for that UUID. `account`'s `CorrelationIdFilter` already does this; the same pattern would need replicating to `auth`/`catalog`/`inventory` if you want it everywhere.

## API Reference

All requests use `Content-Type: application/json`. Base URLs below are direct-to-service (`http://localhost:<port>`) — prefix any path with `/dlms/<service>` and call port `8072` instead to go through the Gateway (see table above).

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
| POST | `/api/catalog` | `{ "title", "author", "isbn", "category", "description", "totalCopies" }` | `title`/`author`/`isbn` required; auto-provisions an Inventory record after commit |
| GET | `/api/catalog/{id}` | — | Response enriched with live `totalCopies`/`availableCopies` from Inventory |
| GET | `/api/catalog` | — | List, same enrichment per item |

### Inventory — `:9010`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/inventory` | `{ "bookId", "totalCopies" }` | Validates `bookId` against Catalog first |
| GET | `/api/inventory/{bookId}` | — | |

## Example requests

Direct to service:
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

Same request, through the Gateway instead:
```
POST http://localhost:8072/dlms/authentication/api/auth/register
```
Identical body, identical result — the Gateway strips `/dlms/authentication`, forwards `/api/auth/register` to whichever `AUTHENTICATION` instance Eureka currently has registered, and both request and response pick up an `eazybank-correlation-id` header along the way.

## Notes for local development

- Config Server (`configServer/src/main/resources/application.yml`) points at this repo's `CreatingInterConnection` branch (`default-label`) — update this if working from a different branch, or it'll silently serve whatever's on that branch regardless of your local checkout.
- Each service's `SPRING_DATASOURCE_URL` is set as a Docker Compose environment variable (`docker-compose/default/docker-compose.yml`) and always overrides the `application.yml` value when running via Compose — the `application.yml` datasource URL only takes effect if you run a service directly (outside Docker), and uses `localhost` for that case.
- `spring.sql.init.mode: always` is set in every service, backed by a `schema.sql` in each service's `resources/` — table names there must match each JPA entity's `@Table(name = ...)` exactly (a `Role`/`roles` mismatch was a real bug caught during setup).
