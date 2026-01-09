# README 사실 인벤토리 (리포 기반)

> 모든 항목은 리포에서 직접 확인한 사실만 기록한다. 추측은 금지하고, 모르는 것은 TBD로 남긴다.

## 1) 프로젝트 개요(리포명/모듈 구조/주요 디렉토리)
- 리포명: `minimall-api` (`settings.gradle`)
- 모듈 구조
  - Backend: `src/main/java/com/minimall` (API/Service/Domain/Auth/Config/Bootstrap) (`src/main/java/com/minimall`)
  - Backend 리소스: `src/main/resources` (profile별 `application-*.yml`, validation 메시지) (`src/main/resources`)
  - Backend 테스트: `src/test/java/com/minimall` (unit/integration/e2e) (`src/test/java/com/minimall`)
  - Frontend: `frontend/` (Vite + React) (`frontend/package.json`, `frontend/vite.config.ts`)
  - Docker/Compose: 루트 및 `docker/` 디렉터리 (`docker-compose.local.v2.yml`, `docker-compose.prod.yml`, `docker/docker-compose.dev.yml`, `docker/docker-compose.test.yml`)
  - 문서/자산: `docs/` (ERD/스키마 등) (`docs/erd`, `docs/schema`)

## 2) 기술 스택(버전 포함)
- Java 21 (`build.gradle`)
- Spring Boot 3.5.6 (`build.gradle`)
- Gradle 8.14.3 (Wrapper) (`gradle/wrapper/gradle-wrapper.properties`)
- DB
  - MySQL 8.4.4 (Docker Compose) (`docker-compose.local.v2.yml`, `docker/docker-compose.dev.yml`, `docker/docker-compose.test.yml`)
  - H2 (테스트 런타임) (`build.gradle`, `src/test/resources/application-test.yml`)
- Redis (Spring Data Redis 사용) (`build.gradle`, `src/main/resources/application.yml`)
- Test
  - JUnit 5 (`build.gradle` - `spring-boot-starter-test`)
  - Testcontainers (MySQL) (`build.gradle`, `src/test/java/com/minimall/AbstractIntegrationTest.java`)
  - Spring Boot Test (`build.gradle`, 테스트 클래스 전반)
  - Playwright (프론트 E2E) (`frontend/package.json`, `frontend/e2e/auth.spec.ts`)
- 기타
  - Spring Security (`build.gradle`, `src/main/java/com/minimall/config/SecurityConfig.java`)
  - JWT (`build.gradle`, `src/main/resources/application.yml`, `src/main/java/com/minimall/auth/jwt/JwtProperties.java`)
  - Actuator + Prometheus Registry (`build.gradle`, `monitoring/prometheus.yml`)
  - OpenAPI (springdoc) (`build.gradle`)

## 3) 실행 방법 3종
- 로컬 직접 실행(bootRun)
  - Command: `gradlew.bat bootRun` (`gradlew.bat`)
  - 관련 설정: `src/main/resources/application.yml`, `src/main/resources/application-local.yml`
- Docker Compose(local)
  - Command: `docker compose -f docker-compose.local.v2.yml up -d` (`docker-compose.local.v2.yml`)
- Docker Compose(prod)
  - Command: `docker compose -f docker-compose.prod.yml up -d` (`docker-compose.prod.yml`)

## 4) 환경변수(.env.example 기준) 목록과 의미
- `SPRING_PROFILES_ACTIVE`: Spring 활성 프로필 (`.env.example`)
- `SPRING_DATASOURCE_URL`: JDBC URL (`.env.example`)
- `SPRING_DATASOURCE_USERNAME`: DB 사용자 (`.env.example`)
- `SPRING_DATASOURCE_PASSWORD`: DB 비밀번호 (`.env.example`)
- `SPRING_JPA_SHOW_SQL`: SQL 로그 출력 (`.env.example`)
- `JWT_SECRET_BASE64`: JWT 서명 키 (Base64) (`.env.example`, `src/main/resources/application.yml`)
- `MYSQL_DATABASE`: MySQL DB명 (`.env.example`)
- `MYSQL_USER`: MySQL 사용자 (`.env.example`)
- `MYSQL_PASSWORD`: MySQL 비밀번호 (`.env.example`)
- `MYSQL_ROOT_PASSWORD`: MySQL 루트 비밀번호 (`.env.example`)
- `TZ`: 타임존 (`.env.example`)

## 5) 인증/인가(JWT) 흐름 및 실제 엔드포인트
- 로그인/토큰
  - `POST /api/auth/login` (로그인, Access/Refresh 발급) (`src/main/java/com/minimall/api/auth/AuthController.java`)
  - `POST /api/auth/refresh` (Refresh로 재발급) (`src/main/java/com/minimall/api/auth/AuthController.java`)
  - `POST /api/auth/logout` (Refresh 폐기) (`src/main/java/com/minimall/api/auth/AuthController.java`)
  - `GET /api/auth/me` (Access 토큰으로 내 정보) (`src/main/java/com/minimall/api/auth/AuthController.java`)
- 권한 정책(permitAll/role)
  - permitAll: `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`, `/api/members/customers`, `/api/members/sellers`, `GET /api/products/**`, `GET /actuator/prometheus`, swagger 경로 (`src/main/java/com/minimall/config/SecurityConfig.java`)
  - ROLE_ADMIN: `/api/admin/**` (`src/main/java/com/minimall/config/SecurityConfig.java`)
  - ROLE_SELLER: `POST|PATCH|DELETE /api/products/**` (`src/main/java/com/minimall/config/SecurityConfig.java`)
  - 그 외: 인증 필요 (`src/main/java/com/minimall/config/SecurityConfig.java`)
- 주요 도메인 엔드포인트(Controller 기준)
  - Members: `/api/members/**` (`src/main/java/com/minimall/api/member/MemberController.java`)
  - Products: `/api/products/**` (`src/main/java/com/minimall/api/product/ProductController.java`)
  - Orders: `/api/orders/**` (`src/main/java/com/minimall/api/order/OrderController.java`)
  - Admin: `/api/admin/ping` (`src/main/java/com/minimall/api/admin/AdminPingController.java`)

## 6) 테스트 체계 + 실행 명령
- 단위 테스트: 도메인/서비스 테스트 (*Test) (`src/test/java/com/minimall/domain`, `src/test/java/com/minimall/service`)
  - 실행: `gradlew.bat test` (`build.gradle`)
  - 기본 프로필: `test` (`build.gradle`, `src/test/resources/application-test.yml`)
- 통합 테스트: Testcontainers 기반 (*IntegrationTest) (`src/test/java/com/minimall/service/**IntegrationTest.java`, `src/test/java/com/minimall/AbstractIntegrationTest.java`)
  - Testcontainers MySQL 사용 (`src/test/java/com/minimall/AbstractIntegrationTest.java`)
- 인증/인가 통합 테스트 전용 task
  - 실행: `gradlew.bat authIntegrationTest` (`build.gradle`)
  - 포함 클래스: `AuthControllerIntegrationTest`, `SecurityIntegrationTest` (`build.gradle`, `src/test/java/com/minimall/auth`)
- E2E 테스트(백엔드)
  - 실행: `gradlew.bat e2eTest` (`build.gradle`)
  - Tag: `@Tag("e2e")`, Profile: `e2e` (`src/test/java/com/minimall/e2e/OrderApiE2ETest.java`, `src/test/resources/application-e2e.yml`)
- E2E 테스트(프론트)
  - 실행: `npm --prefix frontend run e2e` (`frontend/package.json`)

## 7) CI(워크플로 기준)
- Workflow: `.github/workflows/ci.yml`
  - job: `test`
  - steps: checkout → JDK 21 세팅 → gradlew 실행권한 부여 → `./gradlew clean test jacocoTestReport --no-daemon` → 테스트/Jacoco 리포트 업로드

## 8) Compose 인벤토리 표

| 파일명 | 목적 | 포함 서비스 | 포트 | 프로필 | 데이터 볼륨 | 사용 시점 |
|---|---|---|---|---|---|---|
| `docker-compose.local.v2.yml` | 로컬 앱 + DB 실행 | `app`, `db` | `8080:8080`, `13306:3306` | `.env`에서 설정 (`.env.example`) | `db_data_v2` | 로컬 개발/검증 |
| `docker/docker-compose.dev.yml` | 개발 컨테이너에서 `gradlew bootRun` | `app`, `db` | `8080:8080`, `13306:3306` | `SPRING_PROFILES_ACTIVE=docker` | `db_data` | 도커 기반 개발 |
| `docker/docker-compose.test.yml` | E2E 테스트용 앱 + 테스트 DB | `app-test`, `db-test` | `8080:8080`, `3307:3306` | `SPRING_PROFILES_ACTIVE=e2e` | `db_data_test` | E2E 테스트 |
| `docker-compose.prod.yml` | 프로덕션 실행 | `app` | `8080:8080` | `SPRING_PROFILES_ACTIVE=prod` | 없음 | 배포/운영 |
| `docker-compose.monitoring.v2.yml` | 모니터링 스택 | `prometheus`, `grafana` | `9090:9090`, `3000:3000` | 없음 | 없음 | 모니터링 구성 |
| `monitoring/docker-compose.yml` | 모니터링 스택(동일 구성) | `prometheus`, `grafana` | `9090:9090`, `3000:3000` | 없음 | 없음 | 모니터링 구성 |

## 9) 모니터링(Prometheus/Grafana) 구성 여부, 설정 경로
- Prometheus 설정: `monitoring/prometheus.yml`
- 모니터링 Compose: `docker-compose.monitoring.v2.yml`, `monitoring/docker-compose.yml`
- Actuator/Prometheus 노출: `src/main/resources/application.yml` (prometheus endpoint exposure), `build.gradle` (registry 의존성)
