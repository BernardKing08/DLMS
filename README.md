# Digital Library Management System (DLMS)

A microservices-based library management system. Four independent Spring Boot services, each with its own database, sitting behind an API Gateway, coordinating over HTTP through a shared Config Server, with Eureka for service discovery — plus a server-rendered Thymeleaf frontend so the whole thing can be used from a browser, not just Postman.

## Architecture

```
                                    ┌────────────────┐
                    browser ──────► │  Gateway Server │ :8072
                                    │ (routes + traces)│
                                    └────────┬────────┘
                                             │ lb://SERVICE (via Eureka)
                    ┌────────────────────────┼────────────────────────┐
                    │                                                 │
             ┌──────▼──────┐                                   ┌──────▼──────┐
             │  Frontend   │  :8073                            │  API routes │
             │ (Thymeleaf) │  /, /books, /signin, /account...  │  /dlms/**   │
             └──────┬──────┘                                   └──────┬──────┘
                    │ calls the four services below, same as any client │
        ┌───────────┴────┬───────────────────┬───────────────┬─────────┴──────┐
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
                                    │ (Spring Cloud)  │   (all seven services fetch shared
                                    └────────────────┘    config from here, and all seven
                                                           register with Eureka)
```

**Cross-service relationships:**
- **Auth ↔ Account** — registering a user in Auth automatically provisions a matching profile in Account (fire-and-forget, after the registration transaction commits). Account validates any `userId` it's given against Auth before creating/trusting a profile.
- **Catalog ↔ Inventory** — cataloguing a book automatically provisions a stock record in Inventory. Inventory validates any `bookId` it's given against Catalog. Catalog enriches book reads with live stock numbers pulled from Inventory.
- **Gateway → everything** — the Gateway is the single entry point. `/dlms/<service>/**` routes to the matching API (`lb://ACCOUNT`, `lb://AUTHENTICATION`, `lb://CATALOG`, `lb://INVENTORY`), load-balanced via Eureka across however many instances are registered. Every other path (`/`, `/books`, `/signin`, static assets, ...) falls through to a catch-all route that forwards to the Frontend (`lb://FRONTEND`) — this route is registered **last**, since Spring Cloud Gateway matches routes top-to-bottom and a catch-all placed earlier would swallow the API routes.
- **Frontend → the four domain services** — same `@LoadBalanced WebClient` + Eureka-service-name pattern the domain services already use to call each other; the Frontend isn't special-cased, it's just another Eureka client.

Each service talks to the others only over HTTP (via a `WebClient`-based `*Client` class per dependency, in a `client/` package) — never by touching another service's database directly.

## Services

| Service | Module path | Port | Database | Purpose |
|---|---|---|---|---|
| Config Server | `configServer/` | 8071 | — | Serves shared config to all seven services from this Git repo |
| Eureka Server | `eurekaserver/` | 8761 | — | Service discovery/registry — all six other services register here |
| Gateway Server | `gatewayserver/` | 8072 | — | Single entry point; routes `/dlms/<service>/**` to the matching API, everything else to the Frontend, adds request tracing |
| Frontend | `frontend/` | 8073 | — | Server-rendered (Thymeleaf) browser UI — home, catalog browsing, sign in/register, account profile |
| Authentication | `authentication/auth/` | 8090 | `authdb` | User registration, login, role management |
| Account | `account/acc/` | 8080 | `accountsdb` | User profile data (name, contact info) |
| Catalog | `catalog/catalogs/` | 9000 | `catalogdb` | Book records |
| Inventory | `inventory/invtry/` | 9010 | `inventorydb` | Stock levels per book |

Stack: Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.3, MySQL (default profile) or H2 in-memory (`qa` profile), Maven (Jib for image builds). This applies to the four domain services **and** the Frontend — the Frontend was deliberately built on this same, older stack (not the Gateway's newer one) because its only real requirement is reusing the `@LoadBalanced WebClient` + `AppConfig` pattern the domain services already use, and that pattern is what's proven on 3.2.5.

`eurekaserver` and `gatewayserver` were both generated separately and run Spring Boot 4.1.0 / Spring Cloud 2025.1.2 — newer than the rest of the project. This hasn't caused problems since Eureka/Gateway talk to everything else over plain HTTP, but worth knowing if you touch either module — some config property names differ between the two generations (see Gateway Routing below for a real example).

## Frontend (Thymeleaf)

Server-rendered, not a JS single-page app — `@Controller` classes fetch data via `WebClient` and hand it to Thymeleaf templates. Because the browser only ever talks to the Gateway's single origin (`:8072`) for both pages and API calls, **no CORS configuration exists or is needed** anywhere in this project.

| Route | Backing data | Notes |
|---|---|---|
| `GET /` | `GET /api/catalog` (first 6 books) | Home page; degrades gracefully to an empty "featured" section if Catalog is unreachable |
| `GET /books` | `GET /api/catalog` | Full catalog grid |
| `GET /books/{id}` | `GET /api/catalog/{id}` | Book detail + live availability (Catalog's response is already enriched with Inventory stock, so no separate Inventory call is needed) |
| `GET /signin` | — | Login + registration forms on one page |
| `POST /signin/login` | `POST /api/auth/login` | On success, stores `userId`/name/email in the HTTP session |
| `POST /signin/register` | `POST /api/auth/register` | Redirects to `/signin?registered=true` on success |
| `GET /account` | `GET /api/accounts/{userId}` | Requires a session (redirects to `/signin` otherwise); shows a "still setting up" message instead of erroring if the profile hasn't finished auto-provisioning yet |
| `POST /account` | `PUT /api/accounts/{userId}` | Updates profile fields |
| `GET /logout` | — | Clears the session |
| `GET /contact`, `/news-events`, `/news-events/{slug}` | — | Static informational pages — no backend service exists for contact/news content in this system, kept for site completeness |

**Login/session note**: this backend has no JWT or token system — `/api/auth/login` validates credentials and returns who the user is (`id`/`name`/`email`), nothing more. The Frontend keeps "who's logged in" in a plain servlet `HttpSession`, scoped to the Frontend module only. That's enough for a server-rendered app but isn't a substitute for real authentication if this were ever public-facing.

**Template origin**: pages started from the LIBRARIA HTML template (a bookstore theme) and were adapted to Thymeleaf — asset paths converted to `th:href="@{/css/...}"` etc., the fake cart/checkout widget removed (a borrowing system has no cart), the book grid/detail sections rewired to `th:each`/`th:text` against real `BookResponseDto` data, and the login/register forms rewired to real `LoginRequestDto`/`RegisterRequestDto` fields. The `/account` page has no equivalent in the original template — it's newly built, styled to match the rest of the site.

## Running locally

```
cd docker-compose/default
docker compose up -d
```

This starts the Config Server, Eureka Server, all four MySQL containers, the Gateway, the Frontend, and all four app services on the `bernardking08` bridge network. `depends_on` + healthchecks ensure the Config Server and each service's own database are ready before that service starts, and the Gateway waits on the Frontend and all four app services being healthy before it starts.

Once it's up, **the whole system is reachable from a browser at `http://localhost:8072/`** — that's the Gateway, which forwards page requests to the Frontend and API requests to the right service.

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
(Same pattern for `frontend` → `bernardking08/frontend-service:cc1`, `gatewayserver` → `bernardking08/gatewayserver:cc1`, etc.)

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
This opens four separate PowerShell windows (`account`, `auth`, `catalog`, `inventory`), each already on the `qa` profile, so you can watch each service's logs independently. See the script's own header comment for the execution-policy workaround if PowerShell blocks it from running. It doesn't start `eurekaserver`, `gatewayserver`, or `frontend` — start those separately (`cd eurekaserver && ./mvnw spring-boot:run`, same pattern for the other two) if you want the full discovery/routing/browser picture while testing standalone. The Frontend has no `qa` profile of its own (it has no database), so it runs the same way regardless.

To run just one service standalone:
```
cd account/acc
$env:SPRING_PROFILES_ACTIVE = "qa"   # PowerShell
./mvnw spring-boot:run
```
(Setting the profile via `-Dspring-boot.run.profiles=qa` also works, but must be quoted in PowerShell — e.g. `mvn spring-boot:run "-Dspring-boot.run.profiles=qa"` — otherwise PowerShell can mis-split the argument and Maven fails with `Unknown lifecycle phase`.)

Each `application-qa.yml` uses `jdbc:h2:mem:<dbname>;MODE=MySQL` — the `MODE=MySQL` makes H2 parse MySQL-flavored SQL (e.g. `AUTO_INCREMENT`), so the existing `schema.sql` runs unmodified against it, no separate QA schema needed. Credentials are `root`/`root` in both the yml and the Compose env vars, kept consistent so either way of running it works the same.

## Gateway routing & request tracing

The Gateway doesn't use Spring Cloud Gateway's automatic "one route per registered service" discovery locator (that setting exists in `gatewayserver/application.yml` but is set to `enabled: false`) — routes are instead defined explicitly as a `RouteLocator` bean in `GatewayserverApplication.java`. Each `/dlms/*` route matches a path prefix, **strips that prefix**, and forwards the rest of the path to the target service by its Eureka service ID; the Frontend gets a final catch-all route with no rewriting, since its own paths (`/`, `/books`, ...) are already the real paths:

| Gateway path | Forwarded to | Example |
|---|---|---|
| `/dlms/account/**` | `lb://ACCOUNT` | `GET localhost:8072/dlms/account/api/accounts/1` → Account's `GET /api/accounts/1` |
| `/dlms/authentication/**` | `lb://AUTHENTICATION` | `POST localhost:8072/dlms/authentication/api/auth/register` → Auth's `POST /api/auth/register` |
| `/dlms/catalog/**` | `lb://CATALOG` | `GET localhost:8072/dlms/catalog/api/catalog` → Catalog's `GET /api/catalog` |
| `/dlms/inventory/**` | `lb://INVENTORY` | `GET localhost:8072/dlms/inventory/api/inventory/1` → Inventory's `GET /api/inventory/1` |
| `/**` (catch-all, last) | `lb://FRONTEND` | `GET localhost:8072/books` → Frontend's `GET /books`, unchanged |

The `lb://` prefix means "look this service ID up in Eureka and pick one of its registered, healthy instances" — not a fixed hostname. That's what makes the `accounts1`/`auth1` second instances meaningful: hit the same Gateway URL enough times and you should see requests land on both `account` and `account1` in turn.

**A real gotcha worth knowing**: the property that turns on the discovery locator is `spring.cloud.gateway.server.webflux.discovery.locator.enabled` in this Gateway's Spring Cloud version (5.0.2) — *not* the classic `spring.cloud.gateway.discovery.locator.enabled` you'll find in most tutorials and Stack Overflow answers. The artifact itself was renamed from `spring-cloud-starter-gateway` to `spring-cloud-starter-gateway-server-webflux` in this generation, and the property prefix moved with it. Using the old property name doesn't error — Spring Boot silently ignores unrecognized properties by default — it just quietly never turns anything on, which is a much harder bug to notice than a crash.

**Correlation ID tracing**: two global filters in `gatewayserver/src/main/java/com/dlms/gatewayserver/filters/` (`RequestTraceFilter`, `ResponseTraceFilter`) generate a UUID correlation ID for every request that doesn't already carry one (header name `eazybank-correlation-id`), and echo it back on the response. Every domain service **and the Frontend** carry a matching `CorrelationIdFilter` that reads that header into MDC, and every `application.yml` sets:
```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name},%X{correlationId}]"
```
so a single request that touches multiple services — e.g. browsing `/books/5` in the Frontend, which calls Catalog, which calls Inventory internally — produces log lines across all of them carrying the same ID, making it possible to trace one request across every service boundary just by grepping logs for that UUID.

## API Reference

All requests use `Content-Type: application/json`. Base URLs below are direct-to-service (`http://localhost:<port>`) — prefix any path with `/dlms/<service>` and call port `8072` instead to go through the Gateway (see table above), or just use the Frontend's browser pages at `http://localhost:8072/`.

### Authentication — `:8090`

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | `{ "name", "email", "password", "confirmPassword" }` | Creates the user, auto-provisions an Account profile after commit |
| POST | `/api/auth/login` | `{ "email", "password" }` | Returns `{ "id", "name", "email" }` on success — used by the Frontend to know who just logged in |
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

**Easiest way to test end to end**: open `http://localhost:8072/` in a browser, register an account via `/signin`, log in, browse `/books`, and view `/account`. Every click is a real call through the Gateway to a real service.

Or, API-style, direct to service:
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
- When adding a new Spring Initializr-generated module (as with `eurekaserver`, `gatewayserver`, and initially `frontend`), double check the generated `pom.xml` — both `eurekaserver` and `gatewayserver` had a malformed `<plugin>` block sitting outside `<plugins>` and a `${jib-maven-plugin.version}` property that was referenced but never defined. Neither is obvious from reading the file quickly; both cause "Malformed POM" or dependency-resolution errors that look unrelated to the actual cause.
- Java package folders must physically nest one directory per dot in the package name (`com/dlms/gatewayserver/filters/`, not a single folder literally named `com.dlms.gatewayserver`). A class with no `package` declaration at all sits in the default package and is invisible to `@SpringBootApplication`'s component scan — both mistakes showed up in `gatewayserver`'s filter classes during setup.
- **A `@LoadBalanced WebClient` call to a service Eureka can't currently find throws `WebClientResponseException.ServiceUnavailable` (503), not `WebClientRequestException`.** Every `*Client` class in this project catches both — if you add a new client class, catch both too, or a perfectly reachable-but-unregistered service will produce an unhandled 500 instead of the intended graceful degradation.
