# Employee Payroll — Microservices

Three independent Spring Boot services split from the original monolith.
Each service owns its **own MySQL schema** and runs on its own port.

| Service               | Port | Database          | Key domain        |
|-----------------------|------|-------------------|-------------------|
| `employee-service`    | 8081 | `employee_service`     | Employee, Dept    |
| `salary-service`      | 8082 | `salary_service`       | SalaryStructure   |
| `notification-service`| 8083 | `notification_service` | Notification      |

---

## Prerequisites

- Java 21
- Gradle 9.4+
- MySQL 8.x running on `localhost:3306`

### MySQL setup

```sql
-- Run once (or let createDatabaseIfNotExist=true do it on first boot)
CREATE DATABASE IF NOT EXISTS employee_service     CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS salary_service       CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS notification_service CHARACTER SET utf8mb4;
```

> **Credentials** — all three services default to `root / root`.
> Change `spring.datasource.username` / `password` in each `application.properties`
> if your MySQL uses different credentials.

---

## Running each service

Open three terminals, one per service:

```bash
# Terminal 1 — employee-service
cd employee-service
./gradlew bootRun

# Terminal 2 — salary-service
cd salary-service
./gradlew bootRun

# Terminal 3 — notification-service
cd notification-service
./gradlew bootRun
```

Flyway will create the schema on first boot automatically.

---

## Health checks

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

Expected response from each:

```json
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

---

## Quick smoke-test

```bash
# Create an employee
curl -s -X POST http://localhost:8081/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","employeeType":"FULL_TIME"}' | jq .

# Assign a salary (use the employee id returned above, e.g. 1)
curl -s -X POST http://localhost:8082/api/v1/salaries \
  -H "Content-Type: application/json" \
  -d '{"employeeId":1,"basicSalary":50000,"hra":20000,"pfEmployee":1800,"professionalTax":200}' | jq .

# Send a notification
curl -s -X POST http://localhost:8083/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"recipientId":1,"recipientEmail":"jane@example.com","subject":"Welcome","message":"Welcome to the team!"}' | jq .
```

---

## IntelliJ multi-module setup

1. **File → Open** → select the root `microservices/` folder.
2. IntelliJ detects three separate Gradle projects. Accept "Add as Gradle project" for each.
3. Create three Run Configurations:
   - `EmployeeServiceApplication`  — working dir `employee-service`
   - `SalaryServiceApplication`    — working dir `salary-service`
   - `NotificationServiceApplication` — working dir `notification-service`
4. Run all three simultaneously using **Run → Compound**.

---

## Architecture notes

### Why no physical foreign keys across services?

`salary_structure.employee_id` and `notification.recipient_id` are
**logical references** only — there are no cross-database `FOREIGN KEY`
constraints.  Cross-service lookups are resolved via REST calls to
`employee-service` when needed. This is standard microservice practice:
each service is the sole owner of its schema.

### Security removed intentionally

The original monolith used JWT + Spring Security. Those are stripped here
so each service starts without any auth dependency. Add them back per-service
when you wire up a gateway or shared auth mechanism.

### Inter-service communication stub

`salary-service/application.properties` contains:

```properties
services.employee-service.base-url=http://localhost:8081
```

Inject this via `@Value` into a `RestClient` or `WebClient` bean when you
need to validate that an employee exists before saving a salary record.
