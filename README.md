# BBL User API

A small RESTful API for managing users, built with **Java 25 (latest LTS)** and **Spring Boot 4.0**.
User data is stored **in memory** — no database required. The store is **seeded with a few sample Thai users** on startup.

## Tech stack

- Java 25 (latest LTS)
- Spring Boot 4.0 (`spring-boot-starter-web`, `spring-boot-starter-validation`)
- Lombok (getters/setters/constructors)
- Gradle (with wrapper)
- JUnit 5 + Spring MockMvc for tests

## Running

```bash
# Run the app (http://localhost:8080)
./gradlew bootRun

# Or build a jar and run it
./gradlew clean bootJar
java -jar build/libs/user-api-1.0.0.jar
```

### With Docker

```bash
docker build -t bbl-user-api .
docker run -p 8080:8080 bbl-user-api
```

## API

| Method | Path             | Description                | Success | Errors            |
|--------|------------------|----------------------------|---------|-------------------|
| GET    | `/users`         | List all users             | 200     | —                 |
| GET    | `/users/{id}`    | Get one user               | 200     | 404               |
| POST   | `/users`         | Create a user              | 201     | 400               |
| PUT    | `/users/{id}`    | Partial update (one or more fields) | 200 | 400, 404      |
| DELETE | `/users/{id}`    | Delete a user              | 204     | 404               |

`POST` returns a `Location` header pointing at the created resource.

### User model

```json
{
  "id": 1,
  "name": "สมชาย ใจดี",
  "username": "somchai",
  "email": "somchai@example.co.th",
  "phone": "081-234-5678",
  "website": "somchai.co.th"
}
```

### Validation

For `POST`, `name`, `username`, and `email` are required (non-blank).
`PUT` is a **partial update**: every field is optional and only the fields you send are
changed (the rest are left untouched), so you can update a single field on its own.
In both cases, if `email` is present it must be a valid email address.
Failures return `400 Bad Request` with a JSON body listing the offending fields:

```json
{
  "timestamp": "2026-06-02T10:00:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/users",
  "errors": ["name: name is required", "email: email is required"]
}
```

`404 Not Found` responses share the same envelope (without the `errors` array).

## Examples

```bash
# List
curl http://localhost:8080/users

# Create
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","username":"ada","email":"ada@example.com"}'

# Get one
curl http://localhost:8080/users/1

# Update just one field (partial update)
curl -X PUT http://localhost:8080/users/1 \
  -H 'Content-Type: application/json' \
  -d '{"phone":"099-999-9999"}'

# Delete
curl -i -X DELETE http://localhost:8080/users/1
```

See [`requests.http`](requests.http) for a ready-to-run request collection.

## Tests

```bash
./gradlew test
```

- `UserServiceTest` — service-layer logic (CRUD + not-found behaviour).
- `UserControllerTest` — controller/HTTP behaviour via MockMvc (status codes, validation, Location header).

## Project layout

```
src/main/java/com/bbl/userapi
├── UserApiApplication.java        # Spring Boot entry point
├── controller/UserController.java # REST endpoints
├── service/UserService.java       # In-memory store + business logic
├── model/User.java                # User entity
├── dto/UserRequest.java           # Validated create/update payload
└── exception/                     # UserNotFoundException + GlobalExceptionHandler
```

## CI/CD

`.github/workflows/ci.yml` builds, tests, and builds a Docker image on every push/PR to `main`
(JDK 25 + Gradle). A commented-out deploy step shows where a registry push / deployment would slot in.
