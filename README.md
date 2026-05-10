# Employee Payroll — Microservices

A Spring Boot 4.x microservices application split from an original monolith. Each service owns its
own MySQL schema, registers with Eureka, and is accessed exclusively through the API Gateway.

| Service                 | Port | Database               | Key domain                        |
|-------------------------|------|------------------------|-----------------------------------|
| `eureka-server`         | 8761 | —                      | Service registry                  |
| `auth-service`          | 8085 | `auth_service`         | Users, JWT issuance (RS256)       |
| `employee-service`      | 8081 | `employee_service`     | Employee, Department              |
| `salary-service`        | 8082 | `salary_service`       | SalaryStructure                   |
| `notification-service`  | 8083 | `notification_service` | Notification log                  |
| `spring-cloud-gateway`  | 8090 | —                      | JWT validation, routing           |

---

## Tech stack

- **Java 21**, **Spring Boot 4.0.6**, **Gradle 9.4+**
- **Spring Cloud Oakwood (2025.1.1)** — Eureka, OpenFeign, Gateway (WebFlux), LoadBalancer
- **Spring Security 7** (auth-service) — stateless, BCrypt password hashing
- **JJWT 0.12.6** — RS256 asymmetric signing (no shared secret)
- **Resilience4j 2.4.0** — circuit breaker + retry on salary→employee Feign call
- **Flyway** — schema migrations on every service startup
- **MySQL 8.x**, **Lombok**, **springdoc-openapi 3.0.2** (Swagger UI)

---

## Prerequisites

- Java 21
- Gradle 9.4+
- MySQL 8.x running locally

### MySQL setup

```sql
CREATE DATABASE IF NOT EXISTS auth_service         CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS employee_service     CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS salary_service       CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS notification_service CHARACTER SET utf8mb4;
```

Default credentials across all services are `root / root`. Change
`spring.datasource.username` / `password` in each `application-dev.properties` if your MySQL
uses different credentials.

---

## Startup order

The gateway fetches the RSA public key from auth-service at startup — auth-service **must be
fully up** before the gateway starts.

```
1. eureka-server         (others need to register before starting)
2. auth-service          (gateway depends on its JWKS endpoint at startup)
3. employee-service
   salary-service
   notification-service  (these three can start in parallel)
4. spring-cloud-gateway  (last — requires auth-service JWKS + Eureka registry)
```

---

## Running the services

Each service reads `application-dev.properties` when started with the `dev` profile:

```bash
# From each service directory
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Or in IntelliJ: add `-Dspring.profiles.active=dev` as a VM option in each Run Configuration,
then use **Run → Compound** to start all services simultaneously.

Flyway creates the schema on first boot automatically.

---

## Security — how JWT works

Authentication uses **RS256 asymmetric signing** — there is no shared secret between services.

```
1. Client  →  POST /api/auth/login         →  Gateway (public path, no JWT check)
              →  auth-service              ←  returns { "accessToken": "eyJ..." }

2. Client  →  GET  /api/employees          →  Gateway
              Authorization: Bearer eyJ...
              ↓
              JwtAuthenticationFilter validates signature with cached RSA public key
              Adds X-Auth-User and X-Auth-Role headers
              ↓
              →  employee-service          ←  returns employee data
```

**Key distribution:** auth-service generates an RSA-2048 key pair on startup and publishes the
public key as a JWKS document at `/api/v1/auth/.well-known/jwks.json`. The gateway fetches this
once at startup and caches it — no per-request call to auth-service is needed.

> **Note:** The current implementation generates a new key pair on every restart, which invalidates
> existing tokens. For production, persist the key pair in a PKCS12 keystore or secrets manager.

### Roles

| Role       | Description                          |
|------------|--------------------------------------|
| `ADMIN`    | Full access, can manage users/roles  |
| `HR`       | Employee and salary management       |
| `EMPLOYEE` | Read-only access to own data         |
| `GUEST`    | Minimal access                       |

A default admin user is seeded on auth-service startup using credentials from `application-dev.properties`:

```properties
app.admin.username=ranjan.kumar@xyz.xyz
app.admin.password=dummy123
```

---

## API Gateway routes

All requests go through the gateway on **port 8090**. The gateway rewrites paths before forwarding —
the `/v1` segment is added automatically so downstream services are not exposed directly.

| Gateway path              | Forwards to                                  | Auth required |
|---------------------------|----------------------------------------------|---------------|
| `POST /api/auth/register` | `auth-service /api/v1/auth/register`         | No            |
| `POST /api/auth/login`    | `auth-service /api/v1/auth/login`            | No            |
| `GET  /api/auth/.well-known/**` | `auth-service` JWKS endpoint           | No            |
| `ANY  /api/users/**`      | `auth-service /api/v1/users/**`              | Yes (ADMIN)   |
| `ANY  /api/employees/**`  | `employee-service /api/v1/employees/**`      | Yes           |
| `ANY  /api/salary/**`     | `salary-service /api/v1/salaries/**`         | Yes           |
| `ANY  /api/notifications/**` | `notification-service /api/v1/notifications/**` | Yes      |

---

## API reference

### Auth — `POST /api/auth/register`
```json
{
  "username": "jane.doe@example.com",
  "password": "SecurePass1!"
}
```
Returns `{ "accessToken": "eyJ..." }`. Use this token as `Authorization: Bearer <token>` on all
subsequent requests.

### Auth — `POST /api/auth/login`
```json
{
  "username": "jane.doe@example.com",
  "password": "SecurePass1!"
}
```

### Employees — `POST /api/employees`
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "phoneNumber": "9876543210",
  "designation": "Engineer",
  "employeeType": "FULL_TIME",
  "departmentId": 1
}
```

Employee endpoints support **pagination** on `GET /api/employees` via standard Spring Data
`Pageable` parameters: `?page=0&size=10&sort=firstName,asc`.

### Employees — `PUT /api/employees/{id}`
Full update of an existing employee.

### Employees — `DELETE /api/employees/{id}`
Soft-deactivates the employee (sets status to INACTIVE).

### Salaries — `POST /api/salary`
```json
{
  "employeeId": 1,
  "basicSalary": 50000,
  "hra": 20000,
  "specialAllowance": 5000,
  "pfEmployee": 1800,
  "pfEmployer": 1800,
  "professionalTax": 200,
  "tds": 3000,
  "effectiveFrom": "2026-05-01"
}
```
salary-service validates the employee exists by calling employee-service via Feign before
saving. Gross and net salary are computed automatically.

### Salaries — `POST /api/salary/revise/{employeeId}`
Revises an active salary — marks the current record inactive and creates a new one.

### Salaries — `GET /api/salary?employeeId={id}`
Returns all salary records for a specific employee. Omit the query param to get all records.

### Notifications — `POST /api/notifications/send`
```json
{
  "recipientId": 1,
  "recipientEmail": "jane@example.com",
  "subject": "Welcome",
  "message": "Welcome to the team!",
  "channel": "EMAIL"
}
```

### Notifications — `GET /api/notifications/recipient/{recipientId}`
Returns all notifications for a specific recipient.

---

## Resilience — circuit breaker on salary-service

salary-service calls employee-service via **OpenFeign** to validate employee existence before
creating or revising a salary record. This call is protected by a Resilience4j circuit breaker
and retry policy.

```
Normal:   salary-service → Feign → employee-service        ← 200 OK
Failure:  salary-service → Feign × (retry 3×) → CB records failure
After 5 failures at ≥50% failure rate → circuit OPENS
Open:     salary-service → fallback immediately → 503 SERVICE_UNAVAILABLE (no network call)
After 10s → HALF_OPEN → 2 probe requests → SUCCESS → CLOSED
```

Key configuration in `salary-service/application-dev.properties`:

```properties
spring.cloud.openfeign.circuitbreaker.enabled=false   # Resilience4j owns the CB, not Feign
resilience4j.circuitbreaker.instances.employee-service.sliding-window-size=5
resilience4j.circuitbreaker.instances.employee-service.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.employee-service.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.employee-service.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.employee-service.record-exceptions=java.lang.Exception
resilience4j.retry.instances.employee-service.max-attempts=3
```

Check circuit state at runtime:

```bash
GET http://localhost:8082/actuator/health/circuitBreakers
GET http://localhost:8082/actuator/circuitbreakerevents/employee-service
```

---

## Health checks

```bash
curl http://localhost:8761/actuator/health   # Eureka
curl http://localhost:8085/actuator/health   # auth-service
curl http://localhost:8081/actuator/health   # employee-service
curl http://localhost:8082/actuator/health   # salary-service
curl http://localhost:8083/actuator/health   # notification-service
curl http://localhost:8090/actuator/health   # gateway
```

Swagger UI is available on each business service (not the gateway):

```
http://localhost:8085/swagger-ui.html   # auth-service
http://localhost:8081/swagger-ui.html   # employee-service
http://localhost:8082/swagger-ui.html   # salary-service
http://localhost:8083/swagger-ui.html   # notification-service
```

---

## Quick smoke-test (via gateway)

```bash
# 1. Register and capture the token
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"Test@1234"}' | jq -r .accessToken)

# 2. Create an employee
curl -s -X POST http://localhost:8090/api/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","employeeType":"FULL_TIME"}' | jq .

# 3. Assign a salary (use the employee id returned above, e.g. 1)
curl -s -X POST http://localhost:8090/api/salary \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"employeeId":1,"basicSalary":50000,"hra":20000,"pfEmployee":1800,"professionalTax":200}' | jq .

# 4. Send a notification
curl -s -X POST http://localhost:8090/api/notifications/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"recipientId":1,"recipientEmail":"jane@example.com","subject":"Welcome","message":"Welcome to the team!","channel":"EMAIL"}' | jq .

# 5. Circuit breaker demo — stop employee-service, then:
curl -s -X POST http://localhost:8090/api/salary \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"employeeId":1,"basicSalary":60000}' | jq .
# After 5 failures → 503 with instant fallback response (no wait)
```

---

## Architecture notes

### No physical foreign keys across services

`salary_structure.employee_id` and `notification.recipient_id` are logical references only — no
cross-database `FOREIGN KEY` constraints exist. Cross-service lookups happen via REST (Feign) when
needed. Each service is the sole owner of its own schema.

### Why Eureka hostnames are set to `localhost`

```properties
eureka.instance.hostname=localhost
```

On Windows with WSL/Hyper-V, the machine hostname (e.g. `Ranjan.mshome.net`) is not resolvable by
Reactor Netty's async DNS resolver used in the WebFlux gateway. Setting `hostname=localhost`
ensures the gateway's load balancer can reach downstream services without DNS lookup failures.

### Why RS256 instead of HS256

HS256 requires the same secret to both sign and verify tokens, meaning the gateway must hold the
secret and could theoretically forge tokens. RS256 uses a private key (auth-service only) to sign
and a public key (fetched by the gateway at startup) to verify. Even if the gateway's config is
compromised, an attacker only gets the public key — useless for forging tokens.

### Why `fallbackMethod` is not set on `@CircuitBreaker` in EmployeeClientService

When a `fallbackMethod` is specified, Resilience4j records the call as a handled fallback event,
not a failure — so the sliding window never accumulates enough failures to open the circuit.
Removing `fallbackMethod` lets real failures propagate and be counted, and the exception is caught
at the `SalaryService` layer instead.
