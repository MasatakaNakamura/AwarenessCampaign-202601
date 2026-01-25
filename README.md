# 1on1 Management System

Spring Boot based 1on1 management system skeleton. Includes REST API, H2 (dev) / PostgreSQL (docker), Docker build, and CI.

## Project Structure
- backend/: Spring Boot service (Java 17, Gradle)
- docs/: Requirements and design references (see [docs/1on1_management_requirements.md](docs/1on1_management_requirements.md))
- Dockerfile, docker-compose.yml: Container build and local stack (app + PostgreSQL)
- .github/workflows/ci.yml: Maven build/test on push/PR

## Prerequisites
- JDK 17+
- (同梱の Gradle Wrapper 利用推奨)
- Docker & Docker Compose (optional, for containerized run)

## Run Locally (H2)
```bash
cd backend
./gradlew bootRun
```
- App: http://localhost:8080
- Health: http://localhost:8080/api/health
- H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:oneonone, user: sa, no password)

## Run Tests
```bash
cd backend
./gradlew test
```

## Docker / Compose (PostgreSQL)
```bash
docker-compose up --build
```
- App: http://localhost:8080 (profile `postgres` enabled by environment variables in compose)
- DB: postgres on localhost:55432 (user/password: oneonone)

## Database Migration
- Flyway を利用しています。PostgreSQL プロファイルでは Hibernate の ddl-auto を無効化し、起動時に Flyway が `backend/src/main/resources/db/migration` のスクリプトを適用します。
- 初期化: `V1__init_one_on_one.sql` で `one_on_one` テーブルを作成します。
- サンプルデータ: `V2__seed_one_on_one.sql` でサンプル行を投入します。

## API (starter)
- GET /api/health — simple health check
- GET /api/oneonones — list meetings
- POST /api/oneonones — create meeting (see [docs/1on1_management_api_spec.md](docs/1on1_management_api_spec.md) for future expansion)

## CI
- GitHub Actions workflow builds and runs tests with Gradle on push to main / feat/* and on PRs targeting main.

## References
- Requirements: [docs/1on1_management_requirements.md](docs/1on1_management_requirements.md)
- Design: [docs/1on1_management_design.md](docs/1on1_management_design.md)
- DB design: [docs/1on1_management_db_design.md](docs/1on1_management_db_design.md)
- API spec: [docs/1on1_management_api_spec.md](docs/1on1_management_api_spec.md)
