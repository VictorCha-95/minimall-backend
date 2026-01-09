# 0) Working Agreement (Agent Rules)

- 작업은 작은 단위로 진행한다: “원인 분석 → 최소 수정 → 테스트/검증 → diff 요약” 순서.
- 파일 수정 후에는 반드시 변경 요약과 함께 **수정 근거(왜 이렇게 했는지)**를 남긴다.
- 테스트/빌드는 가능한 범위에서 실행하고, 실행하지 못한 경우 **왜 못 했는지 + 대체 검증 방법**을 적는다.
- 보안/인증/데이터 정합성 관련 변경은 더 보수적으로 진행하고, 관련 테스트를 우선 추가한다.
- 변경 범위를 최소화하고, 파일/패키지 이동은 금지(필요하면 사유를 주석으로 남긴다).
- PR 단위로 쪼개서 커밋 메시지까지 제안해라.
- 새로 추가하는 테스트는 “재현 가능”해야 한다(플레이키 금지).
- README/문서 변경은 반드시 “근거(테스트/로그/지표)” 중심으로 쓸 것.

# 1) Project Structure

## Backend
- Root: src/main/java/com/minimall
  - api/ : Controller, Request/Response DTO, API 레이어
  - service/ : Use-case / Application service
  - domain/ : Entity/VO/Domain Service, 도메인 규칙
  - auth/ : 인증/인가(JWT 등)
  - config/ : Spring 설정(Security 포함)
  - bootstrap/ : 초기화/시드 등(있는 경우)

## Resources
- src/main/resources
  - application-*.yml: 프로필별 설정(local/test/prod 등)
  - validation messages / static / templates

## Tests
- src/test/java/com/minimall
  - 단위 테스트: *Test
  - 통합 테스트: *IntegrationTest
  - (Testcontainers 사용 시) Docker 실행 필요

## Frontend
- frontend/ : Vite + React(+TS)
  - frontend/src/ : pages/services/components 등
  - frontend/vite.config.ts : dev proxy(/api → http://localhost:8080 같은 구성 가능)

## Docker
- docker/ : 개발용 compose/도커 관련 리소스
- docker-compose.prod.yml : 운영/배포용 compose(있는 경우)
- docs/, db/ : 문서/DB 자산(있는 경우)

# 2) Commands (Windows 기준, Repo Root에서 실행 권장)

Repo root 예: C:\MiniMall\Application\minimall

## 2.1 Backend (Gradle Wrapper)

### Build
- Command
  - gradlew.bat build
- Full syntax
  - gradlew.bat <task> [--no-daemon] [--stacktrace] [--info|--debug]
- Args/Options
  - <task>: 실행할 Gradle task (여기서는 build)
  - --no-daemon: 데몬 미사용(환경 이슈 디버깅에 유리)
  - --stacktrace: 예외 스택트레이스 출력
  - --info|--debug: 로그 상세도 증가

### Run (Local)
- Command
  - gradlew.bat bootRun
- Full syntax
  - gradlew.bat bootRun [-Dspring.profiles.active=<profile>] [--args="<app args>"]
- Args/Options
  - -Dspring.profiles.active=<profile>: 활성 프로필 지정(예: local)
  - --args="...": 애플리케이션 인자 전달(필요 시)

### Tests
- Command
  - gradlew.bat test
- Full syntax
  - gradlew.bat test [--tests "<pattern>"] [--stacktrace]
- Args/Options
  - --tests "<pattern>": 특정 테스트만 실행(예: "com.minimall.*IntegrationTest")
  - --stacktrace: 실패 원인 파악용

### Coverage (JaCoCo)
- Command
  - gradlew.bat jacocoTestReport
- Output
  - build/reports/jacoco/test/html/index.html

## 2.2 Frontend (Vite)

### Install
- Command
  - npm --prefix frontend install
- Full syntax
  - npm [--prefix <dir>] <command> [args...]
- Args/Options
  - --prefix <dir>: 해당 디렉터리를 작업 기준으로 npm 실행(여기서는 frontend)
  - <command>: install은 의존성 설치

### Dev server
- Command
  - npm --prefix frontend run dev
- Full syntax
  - npm [--prefix <dir>] run <script> [-- <script args>]
- Args/Options
  - run <script>: package.json의 scripts 실행
  - -- <script args>: 스크립트에 인자 전달(필요 시)

### Build
- Command
  - npm --prefix frontend run build

### Lint
- Command
  - npm --prefix frontend run lint

## 2.3 Docker Compose (Dev / Test / Prod)

아래 3개 명령만 기억하면 됩니다.
- dev(개발 기본): docker/docker-compose.dev.yml
- test(테스트 전용): docker/docker-compose.test.yml
- prod(운영/배포): docker-compose.prod.yml

명령어 전체 문법
- docker compose -f <compose-file> up -d
  - -f <compose-file>: 사용할 compose 파일 지정
  - up: 컨테이너 생성/시작
  - -d: 백그라운드(detached) 실행

### 2.3.1 dev 실행 (로컬 개발: App + DB)
- 실행 예시
  - docker compose -f docker/docker-compose.dev.yml up -d
- 로그 확인
  - docker compose -f docker/docker-compose.dev.yml logs -f app
- 정상 기준
  - Active profile(s): docker (또는 docker로 설정된 프로필)
  - DB Healthy
  - spring.jpa.open-in-view 경고 없음 (open-in-view=false)

### 2.3.2 test 실행 (테스트 격리 환경)
- 실행 예시
  - docker compose -f docker/docker-compose.test.yml up -d
- 테스트는 개발 DB와 완전히 분리해서 실행하는 것을 권장합니다.
  - (포트/볼륨/DB명/계정 분리)

# 3) Coding Style

## Backend (Java/Spring)
- Java 21 / Spring Boot 3.5.x 기준
- 레이어 규칙: api → service → domain 흐름 유지(역방향 참조 지양)
- 예외/에러코드 규약이 있다면 반드시 유지(응답 포맷 포함)
- DTO/Mapper(MapStruct 등) 사용 시 기존 패턴 준수(무리한 “한 방 리팩토링” 금지)

## Frontend (TS/React)
- ESLint/기존 폴더 구조 유지
- API 호출은 frontend/src/services 계열로 분리(있는 경우)

# 4) Testing Guidelines

- JUnit 5 / Spring Boot Test 사용
- Testcontainers 기반 통합테스트는 Docker가 실행 중이어야 함
- 테스트 네이밍:
  - 단위: *Test
  - 통합: *IntegrationTest
- 인증/인가 변경 시:
  - 최소한 “허용 엔드포인트”와 “차단 엔드포인트” 케이스를 테스트로 고정

# 5) Commit & PR Guidelines

## Current state (observed)
- 기존 커밋 메시지는 국문/영문 혼용이며 접두 규칙이 일관되지 않을 수 있음.

## Recommended convention (apply going forward)
- Format: type(scope): 짧은 명령형 요약 (권장 72자 이내)
- type: feat, fix, refactor, chore, test, docs, build, ci, perf, revert
- scope (optional): api, service, domain, auth, config, frontend, infra, db, docs
- Body (optional): 변경 이유/영향/마이그레이션 포인트를 간단히 기술(줄바꿈 72자 권장)
- Breaking change: type!: + footer에 BREAKING CHANGE: 명시

Examples
- feat(api): 주문 취소 엔드포인트 추가
- fix(service): 토큰 null 가드 추가
- ci: 워크플로 단순화

PR checklist
- 변경 요약(무엇/왜)
- 실행한 테스트 커맨드(또는 미실행 사유)
- 프론트 UI 변경 시 스크린샷
- 보안 관련 변경 시 영향 범위(permitAll 변경 등) 명시

# 6) Security & Configuration

- .env는 커밋 금지. 필요 시 .env.example로 키만 제공
- secret은 환경변수로 주입(예: JWT secret, DB password 등)
- 프로필 설정은 application-*.yml에서 관리하고, 운영 값은 환경변수 우선
- 인증/인가 설정 변경은 “기본 차단(default deny) + 필요한 것만 허용” 원칙 유지(프로젝트 정책에 따라 조정)

# 7) When updating AGENTS.md

- 이 문서는 “작업 방식과 규칙”을 고정하기 위한 문서다.
- 프로젝트 구조/빌드 커맨드가 바뀌면 반드시 동기화한다.
