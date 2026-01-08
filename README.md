# MiniMall (Backend + Frontend)

Java 21 / Spring Boot 3.5.6 기반의 이커머스 API 프로젝트입니다.  
JWT 인증/인가를 적용했고, 주문–결제–배송 상태 전이와 재고 변경을 중심으로 도메인 규칙을 구현했습니다.  
Testcontainers 기반 통합 테스트, E2E(백엔드/프론트) 테스트, GitHub Actions CI를 통해 품질을 검증합니다.

---

## 주요 테스트

1) **주문 라이프사이클(주문/결제/배송) 상태 전이 구현 + E2E 검증**
- 백엔드 E2E: `src/test/java/com/minimall/e2e/OrderApiE2ETest.java`

2) **재고 동시성/정합성 재현 테스트**
- 동시 요청에서 오버셀(중복 차감) 위험을 재현/검증하는 테스트를 구성했습니다.
- 테스트: `src/test/java/com/minimall/service/order/StockConcurrencyIntegrationTest.java`

3) **쿼리 병목(N+1 문제) 감지용 쿼리 카운트 테스트**
- 주문 목록 조회에서 N+1 문제를 감지하기 위한 테스트를 구성했습니다.
- 테스트: `src/test/java/com/minimall/service/order/OrderQueryCountIntegrationTest.java`


---

## 핵심 기능 (구현 범위 기준)

### 인증/회원
- 회원 가입(고객/판매자 분리):  
  `POST /api/members/customers`, `POST /api/members/sellers`
- 로그인/토큰 발급 및 재발급:  
  `POST /api/auth/login`, `POST /api/auth/refresh`
- 내 정보 조회:  
  `GET /api/auth/me`
- 로그아웃:  
  `POST /api/auth/logout`

### 상품
- 상품 등록/삭제:  
  `POST /api/products`, `DELETE /api/products/{id}`
- 재고 증감/초기화:  
  `POST /api/products/{id}/stock/add|reduce|clear`
- 상품명/가격 변경:  
  `PATCH /api/products/{id}/name|price`

### 주문/결제/배송
- 주문 생성/취소/조회:  
  `POST /api/orders`, `PATCH /api/orders/{id}/cancel`, `GET /api/orders/{id}`
- 결제 처리:  
  `POST /api/orders/{id}/payment`
- 배송 준비/시작/완료:  
  `POST /api/orders/{id}/delivery`, `PATCH /api/orders/{id}/delivery`, `PATCH /api/orders/{id}/delivery/complete`

---

## Architecture

### 패키지/레이어 구조
- API 레이어: `src/main/java/com/minimall/api`
- Service 레이어: `src/main/java/com/minimall/service`
- Domain 레이어: `src/main/java/com/minimall/domain`
- Auth 모듈: `src/main/java/com/minimall/auth`
- Config/Bootstrap: `src/main/java/com/minimall/config`, `src/main/java/com/minimall/bootstrap`
- Frontend: `frontend/`

### ERD
- `docs/erd/MiniMall ERD V2.png`
- `docs/erd/MiniMall ERD V1.png`

---

## Tech Stack (버전 포함)

| 영역 | 기술/버전 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Gradle Wrapper 8.14.3 |
| DB | MySQL 8.4.4 |
| Test DB | H2 |
| Test | JUnit 5 |
| Integration Test | Testcontainers (MySQL) |
| Security | Spring Security + JWT |
| Observability | Actuator + Prometheus Registry |
| Frontend | Vite + React |
| Front E2E | Playwright |

---

## Quick Start

### 1) 로컬 직접 실행 (bootRun)

**실행 명령(전체 문법)**
- Windows:
```bash
gradlew.bat bootRun [-Dspring.profiles.active=<profile>] [--args="<app args>"] [--no-daemon] [--stacktrace] [--info|--debug]
````

- 옵션/인자
  - `-Dspring.profiles.active=<profile>`: 활성 프로필 지정
  - `--args="<app args>"`: 애플리케이션 인자 전달
  - `--no-daemon`: Gradle 데몬 미사용
  - `--stacktrace`: 예외 스택트레이스 출력
  - `--info|--debug`: 로그 상세도 증가
- 성공 기준: TBD (리포 내 로컬 실행 성공 기준/로그 기준 명시 없음)
- 관련 설정: `src/main/resources/application.yml`, `src/main/resources/application-local.yml`

### 2) Docker Compose (local)
실행 명령(전체 문법)
```bash
 docker compose -f <compose-file> up -d
```
- 옵션/인자
  - `-f <compose-file>`: 사용할 compose 파일 지정
  - `up`: 컨테이너 생성/시작
  - `-d`: 백그라운드 실행
- 사용 파일: `docker-compose.local.v2.yml`
- 성공 기준
  - `db` 포트 매핑: `13306:3306` (`docker-compose.local.v2.yml`)
  - `app` 포트 매핑: `8080:8080` (`docker-compose.local.v2.yml`)
  - `db` healthcheck 통과 (`docker-compose.local.v2.yml`)

### 3) Docker Compose (prod)
- 실행 명령(전체 문법)
```bash
docker compose -f <compose-file> up -d
````
- 옵션/인자
  - `-f <compose-file>`: 사용할 compose 파일 지정
  - `up`: 컨테이너 생성/시작
  - `-d`: 백그라운드 실행
- 사용 파일: `docker-compose.prod.yml`
- 성공 기준
  - `app` 포트 매핑: `8080:8080` (`docker-compose.prod.yml`)
  - 프로필: `SPRING_PROFILES_ACTIVE=prod` (`docker-compose.prod.yml`)

### 4) (옵션) Monitoring Compose
- 실행 명령(전체 문법)
```bash
docker compose -f <compose-file> up -d
```
- 옵션/인자
  - `-f <compose-file>`: 사용할 compose 파일 지정
  - `up`: 컨테이너 생성/시작
  - `-d`: 백그라운드 실행
- 사용 파일: `docker-compose.monitoring.v2.yml` 또는 `monitoring/docker-compose.yml`
- 성공 기준
  - Prometheus 포트: `9090:9090`
  - Grafana 포트: `3000:3000`

## Docker Compose 인벤토리 (목적/사용 시점)

| 파일명 | 목적 | 포함 서비스 | 포트 | 프로필 | 데이터 볼륨 | 사용 시점 |
|---|---|---|---|---|---|---|
| `docker-compose.local.v2.yml` | 로컬 앱 + DB 실행 | app, db | 8080 / 13306 | `.env` | `db_data_v2` | 로컬 개발/검증 |
| `docker/docker-compose.dev.yml` | 개발 컨테이너에서 `bootRun` | app, db | 8080 / 13306 | `docker` | `db_data` | 도커 기반 개발 |
| `docker/docker-compose.test.yml` | 백엔드 E2E | app-test, db-test | 8080 / 3307 | `e2e` | `db_data_test` | E2E 테스트 |
| `docker-compose.prod.yml` | 운영 실행 | app | 8080 | `prod` | 없음 | 배포/운영 |
| `docker-compose.monitoring.v2.yml` | 모니터링 | prometheus, grafana | 9090 / 3000 | 없음 | 없음 | 모니터링 |
| `monitoring/docker-compose.yml` | 모니터링(동일 구성) | prometheus, grafana | 9090 / 3000 | 없음 | 없음 | 모니터링 |

---

## Configuration

환경변수 기준 파일: `.env.example`  
시크릿(JWT/DB 비밀번호 등)은 운영 환경에서 **환경변수로만 주입**합니다.

| 변수 | 의미 | 예시 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring 활성 프로필 | `docker` |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:mysql://db:3306/minimall?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | `minimall` |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | `minimall` |
| `SPRING_JPA_SHOW_SQL` | SQL 로그 출력 | `true` |
| `JWT_SECRET_BASE64` | JWT 서명 키(Base64) | `MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=` |
| `MYSQL_DATABASE` | MySQL DB명 | `minimall` |
| `MYSQL_USER` | MySQL 사용자 | `minimall` |
| `MYSQL_PASSWORD` | MySQL 비밀번호 | `minimall` |
| `MYSQL_ROOT_PASSWORD` | MySQL 루트 비밀번호 | `change-me` |
| `TZ` | 타임존 | `Asia/Seoul` |

---

## Auth (JWT)

흐름: 로그인 → Access/Refresh 발급 → Access로 API 호출 → Refresh로 재발급 → 로그아웃

- Refresh 토큰 쿠키 이름: `refreshToken`
- Authorization 헤더: `Authorization: Bearer <accessToken>`

### 예시 요청: 로그인

```http
POST /api/auth/login
Content-Type: application/json

{
  "loginId": "customer",
  "password": "pass1234!"
}

```
- 요청/응답 DTO 근거: `src/main/java/com/minimall/api/auth/dto/LoginRequest.java`, `src/main/java/com/minimall/api/auth/dto/LoginResponse.java`

### 예시 요청: 내 정보 조회
```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```
- 응답 DTO 근거: `src/main/java/com/minimall/api/auth/dto/AuthMeResponse.java`

## Testing Strategy
- 단위 테스트: 도메인/서비스 단위 검증 (`src/test/java/com/minimall/domain`, `src/test/java/com/minimall/service`)
- 통합 테스트: Testcontainers 기반 MySQL (`src/test/java/com/minimall/AbstractIntegrationTest.java`)
- E2E 테스트(백엔드): 실제 HTTP 흐름 검증 (`src/test/java/com/minimall/e2e/OrderApiE2ETest.java`)
- E2E 테스트(프론트): Playwright (`frontend/e2e/auth.spec.ts`)

실행 명령
1) 단위/통합 기본 테스트
   - 전체 문법: `gradlew.bat test [--tests "<pattern>"] [--stacktrace] [--no-daemon] [--info|--debug]`
   - 옵션/인자: `--tests` 특정 테스트 선택, `--stacktrace` 실패 원인 출력, `--no-daemon` 데몬 비활성, `--info|--debug` 상세 로그
2) 인증/인가 전용 통합 테스트
   - 전체 문법: `gradlew.bat authIntegrationTest [--stacktrace] [--no-daemon] [--info|--debug]`
   - 옵션/인자: `--stacktrace` 실패 원인 출력, `--no-daemon` 데몬 비활성, `--info|--debug` 상세 로그
3) E2E 테스트(백엔드)
   - 전체 문법: `gradlew.bat e2eTest [--stacktrace] [--no-daemon] [--info|--debug]`
   - 옵션/인자: `--stacktrace` 실패 원인 출력, `--no-daemon` 데몬 비활성, `--info|--debug` 상세 로그
4) E2E 테스트(프론트)
   - 전체 문법: `npm --prefix <dir> run <script> [-- <script args>]`
   - 옵션/인자: `--prefix <dir>` 실행 디렉터리 지정, `run <script>` package.json script 실행, `-- <script args>` 스크립트 인자 전달
   - 실행 예: `npm --prefix frontend run e2e`

대표 테스트 (리포 내 근거)
- `StockConcurrencyIntegrationTest`: 재고 동시성 오버셀 재현 (`src/test/java/com/minimall/service/order/StockConcurrencyIntegrationTest.java`)
- `OrderQueryCountIntegrationTest`: 주문 목록 N+1 감지 (`src/test/java/com/minimall/service/order/OrderQueryCountIntegrationTest.java`)
- `OrderApiE2ETest`: 주문/결제/배송 E2E 흐름 (`src/test/java/com/minimall/e2e/OrderApiE2ETest.java`)

## CI/CD
### CI (워크플로 기준, 보장 범위)
1) PR 대상 브랜치: `main` (`.github/workflows/ci.yml`)
2) JDK 21 세팅 후 Gradle 캐시 사용 (`.github/workflows/ci.yml`)
3) `./gradlew clean test jacocoTestReport --no-daemon` 실행 (`.github/workflows/ci.yml`)
4) 테스트 리포트 아티팩트 업로드 단계 존재 (`.github/workflows/ci.yml`)
5) Jacoco 리포트 아티팩트 업로드 단계 존재 (`.github/workflows/ci.yml`)

### CD (미구현, TODO)
1) Dockerfile 기반 이미지 빌드 자동화 
2) 레지스트리 업로드 및 버전 태깅 
3) `docker-compose.prod.yml` 활용 배포 파이프라인 설계
4) 운영 환경변수 안전 주입 전략 수립 
5) 배포 후 헬스체크/롤백 절차 정의 

## Observability / Load Test
- Prometheus/Grafana 구성 파일
  - Prometheus scrape 설정: `monitoring/prometheus.yml`
  - Compose: `docker-compose.monitoring.v2.yml`, `monitoring/docker-compose.yml`
- 애플리케이션 메트릭 노출 설정: `src/main/resources/application.yml`

부하 재현 스크립트 (k6)
- 스크립트: `loadtest/order_concurrency.js`
- 실행 명령(전체 문법): `k6 run <script> [-e <VAR>=<value>]...`
- 옵션/인자
  - `-e <VAR>=<value>`: 환경변수 전달 (`VUS`, `DURATION`, `BASE_URL`, `ORDER_PATH`, `MEMBER_ID`, `PRODUCT_ID`, `QTY`, `TOKEN`)
