[![CI](https://github.com/VictorCha-95/minimall-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/VictorCha-95/minimall-backend/actions/workflows/ci.yml)

# MiniMall – 전자상거래 백엔드 서비스

Spring Boot 3.5 / JPA 기반으로 **회원·상품·주문·결제·배송** 등  
전자상거래 핵심 도메인을 도메인 주도 설계(DDD) 방식으로 구현한 백엔드 프로젝트입니다.

---

## 1. 프로젝트 개요

MiniMall은 다음을 목표로 합니다.

- 실제 서비스 수준의 **주문·결제·배송 흐름**을 설계하고 구현
- **도메인 규칙과 상태 전이**를 코드로 명확하게 표현
- **테스트 코드와 Docker 환경**을 통한 안정적인 개발/실행 환경 제공

“**실제 쇼핑몰 백엔드의 핵심 흐름**”에 집중한 학습·포트폴리오용 서비스입니다.

---

## 2. 기술 스택

| 영역          | 사용 기술                                  |
|---------------|--------------------------------------------|
| Language      | Java 21                                    |
| Framework     | Spring Boot 3.5, Spring MVC                |
| ORM           | JPA / Hibernate                            |
| Database      | MySQL 8.x                                  |
| Build Tool    | Gradle                                     |
| CI            | GitHub Actions (빌드/테스트 자동화)         |
| Docs          | 용어사전, 테이블 정의서(변경 이력 관리)     |
| Infra / Dev   | Docker, Docker Compose, Testcontainers      |
| API 문서      | Swagger(OpenAPI)                           |
| Test          | JUnit 5, Mockito, Spring Boot Test         |

---

## 2.1 CI / 문서화

이 저장소는 “기능 구현”뿐 아니라 **운영/유지보수에 필요한 신뢰도와 인수인계 가능성**을 함께 확보하는 것을 목표로 합니다.

### CI (GitHub Actions)
- PR/Push 시 **빌드 및 테스트를 자동 실행**하여 변경으로 인한 회귀를 조기에 탐지합니다.
- 상단의 CI 배지로 최근 워크플로 상태를 확인할 수 있습니다.

### DB 문서(용어사전/테이블 정의서)
- 도메인 용어(예: Member, Order 등)를 **용어사전으로 통일**하여 코드/DB/문서 간 불일치를 줄입니다.
- 테이블 정의서로 컬럼 의미/제약조건을 명확히 하여, 스키마 변경 시 영향 범위를 빠르게 파악할 수 있도록 합니다.
- 링크: [용어 사전](https://gratis-closet-772.notion.site/29140a3906a580c797bcfc9c60cd9eab?v=29140a3906a58049b367000cb0086f90&pvs=74)
, [테이블 정의서](https://gratis-closet-772.notion.site/29140a3906a580fc831ac69dc09a9997?pvs=74)
---

## 3. 아키텍처 개요

레이어드 아키텍처를 바탕으로, 도메인 중심 구조를 사용합니다.

- **API 레이어 (`api`)**
  - HTTP 요청/응답 처리
  - Request/Response DTO 정의
  - GlobalExceptionHandler를 통해 일관된 오류 코드 및 메시지 전달
  - Swagger 기반 API 문서화
- **서비스 레이어 (`service`)**
  - 트랜잭션 경계 설정
  - Command/Result DTO 정의
  - 도메인 객체 조합 및 유스케이스 구현
- **도메인 레이어 (`domain`)**
  - 엔티티, 값 객체(Value Object), 도메인 서비스 정의
  - 도메인 별 예외 정의 및 Guards 클래스 활용하여 일관된 예외 처리
  - 비즈니스 규칙·상태 전이 책임
- **인프라 레이어 (`repository`, `config`)**
  - Spring Data JPA Repository
  - DB 설정, 애플리케이션 공통 설정

---

## 4. 주요 도메인 및 기능

### 4.1 회원(Member)

- 회원 가입
- 로그인
- 회원 정보 수정
- 단건 조회 / 목록 조회

주요 규칙

- 이메일, 아이디 **중복 불가**
- 필수 값(이메일, 이름 등) 누락 시 도메인/검증 예외 발생

---

### 4.2 상품(Product)

- 상품 등록
- 가격 변경
- 재고 증가 / 감소
- 재고 초기화

주요 규칙

- 재고, 가격 **음수 불가**

---

### 4.3 주문(Order)

- 주문 생성
- 주문 상세 조회
- 주문 목록 조회
- 주문 취소

주요 규칙

- `OrderStatus`: `ORDERED` → `CONFIRMED` → `COMPLETED` (기타: `CANCELED`)
- 주문 취소는 **아직 배송이 시작되지 않은 상태**에서만 가능
- 주문 금액 = 각 주문 상품의 (단가 × 수량) 합계로 계산
- 주문 생성 시
  - 상품 재고 차감
  - 주문 금액 계산 및 검증

---

### 4.4 결제(Pay)

- 결제 요청 처리
- 결제 성공/실패 상태 관리

주요 규칙

- `PayStatus`: `READY` → `PAID` (기타: `FAILED`, `CANCELED`)
- 결제 금액은 **주문 최종 금액과 일치**해야 함
- 결제 성공 시 주문 상태를 `CONFIRMED`로 변경
- 결제 실패 시 예외 처리 및 상태 `FAILED` 변경

---

### 4.5 배송(Delivery)

- 배송 시작 (운송장 번호 등록)
- 배송 상태 조회

주요 규칙

- `DeliveryStatus`: `READY` → `SHIPPING` → `COMPLETED` (기타: `FAILED, CANCELED`)
- 배송 시작 시
  - 운송장 번호 필수
  - 이미 배송 중/완료 상태면 시작 불가

---

## 5. 도메인 설계 특징

- **값 객체(Value Object) 활용**
  - `Address`, `OrderAmount` 등 의미 있는 값 타입으로 캡슐화
- **풍부한 Enum과 메시지 정의**
  - `OrderStatus`, `DeliveryStatus`, `PayStatus` 등 상태를 Enum으로 관리
  - 도메인·예외 메시지를 Enum으로 분리해 재사용
- **도메인 예외 계층**
  - `DuplicateException`, `NotFoundException`, `DomainRuleException` 등
  - 예외에 도메인 타입, 필드명, 사유 등을 담아 **원인 추적이 용이**하도록 설계

---

## 6. API 설계 개요

RESTful API 스타일을 기반으로, 리소스 중심 URL과 HTTP 메서드를 사용합니다.

### 회원(Member) – `/members`
- `POST /members` – 회원 가입
- `POST /members/login` – 로그인
- `GET /members` – 회원 목록 조회
- `GET /members/{id}` – 회원 단건 조회
- `GET /members/{id}/summary` – 회원 요약 조회
- `GET /members/{id}/with-orders` – 회원 + 주문 포함 조회
- `GET /members/{id}/orders` – 회원 주문 목록 조회
- `GET /members/by-email` – 이메일로 조회
- `GET /members/by-email/summary` – 이메일로 요약 조회
- `GET /members/by-loginId` – 로그인ID로 조회
- `GET /members/by-loginId/summary` – 로그인ID로 요약 조회
- `PATCH /members/{id}` – 회원 정보 수정
- `DELETE /members/{id}` – 회원 삭제

### 상품(Product) – `/products`
- `POST /products` – 상품 등록
- `PATCH /products/{id}/name` – 상품명 변경
- `PATCH /products/{id}/price` – 상품 가격 변경
- `POST /products/{id}/stock/add` – 재고 증가
- `POST /products/{id}/stock/reduce` – 재고 차감
- `POST /products/{id}/stock/clear` – 재고 초기화
- `DELETE /products/{id}` – 상품 삭제

### 주문(Order) – `/orders`
- `POST /orders` – 주문 생성
- `GET /orders/{id}` – 주문 상세 조회
- `PATCH /orders/{id}/cancel` – 주문 취소
- `POST /orders/{id}/payment` – 결제 처리
- `POST /orders/{id}/delivery` – 배송 등록(준비)
- `PATCH /orders/{id}/delivery` – 배송 시작(운송장 번호 등록 등)
- `PATCH /orders/{id}/delivery/complete` – 배송 완료

---

## 7. 예외 처리 정책
공통 예외 핸들러를 통해 HTTP 상태 코드와 응답 포맷을 통일합니다.

### 공통 처리 방식
- `GlobalExceptionHandler`에서 예외를 처리하여 일관된 응답 포맷으로 변환합니다.
- 오류 유형은 `ApiErrorCode`로 표준화하며, 응답의 `code` 필드로 전달합니다.

### 오류 코드(ApiErrorCode)
- VALIDATION_ERROR
- NOT_FOUND
- DOMAIN_RULE_VIOLATION
- DUPLICATE_VALUE
- CONFLICT
- INVALID_CREDENTIALS
- INTERNAL_ERROR

### HTTP 상태 코드 매핑

#### 400 Bad Request
- 요청 검증 오류(Body/Param Bean Validation 등): VALIDATION_ERROR

#### 404 Not Found
- 리소스 없음(회원/상품/주문 등): NOT_FOUND

#### 422 Unprocessable Entity
- 도메인 규칙 위반(상태 전이 불가, 금액 불일치 등): DOMAIN_RULE_VIOLATION

#### 409 Conflict
- 중복/무결성 위반 등 충돌: DUPLICATE_VALUE, CONFLICT

#### 401 Unauthorized
- 인증 실패(로그인 비밀번호 오류 등): INVALID_CREDENTIALS

#### 500 Internal Server Error
- 예상치 못한 서버 오류: INTERNAL_ERROR

응답 바디 예시

```json
{
  "status": 422,
  "code": "DOMAIN_RULE_VIOLATION",
  "message": "배송이 이미 시작된 주문은 취소할 수 없습니다.",
  "path": "/orders/1"
}
```

## 8. 테스트 전략

도메인 규칙은 **단위 테스트로 촘촘히 검증**하고, 서비스/컨트롤러는 **단위(모킹) + 통합(Testcontainers/E2E) 테스트**로 계층별 신뢰도를 확보합니다.

### 8.1 도메인 테스트 (단위)

도메인 엔티티/값 객체의 **순수 비즈니스 규칙**을 검증합니다.

- 주문 금액 계산(원금/할인/최종)
- 재고 차감/복구
- 상태 전이 규칙
- 예외 발생 조건(도메인 규칙 위반 등)

### 8.2 서비스 테스트

#### 서비스 단위 테스트 (Mock 기반)

서비스 레이어의 유스케이스 흐름을 **모킹 기반으로 빠르게 검증**합니다.

- Repository/외부 의존성 객체를 Mock 처리하여 서비스 로직에 집중
- 분기 로직(중복/존재 검증 등) 및 예외 발생 조건 검증
- 도메인 호출 플로우(유스케이스 오케스트레이션) 검증

#### 서비스 통합 테스트 (SpringBootTest + Testcontainers)

실제 애플리케이션 구동과 유사한 환경에서 **DB 반영까지 포함하여 end-to-end 흐름**을 검증합니다.

- `@SpringBootTest`
- `@ActiveProfiles("integration-test")`
- Testcontainers MySQL 기반으로 실제와 유사한 DB 환경 구성
- 트랜잭션, JPA 영속성 동작, `flush/clear` 이후 재조회 결과까지 포함해 검증

### 8.3 컨트롤러(API) 테스트

#### 컨트롤러 단위 테스트 (Web Layer)

웹 레이어만 분리하여 **요청/응답 매핑, 검증, 예외 응답 포맷**을 빠르게 검증합니다.

- `MockMvc` 기반 요청/응답 검증
- 서비스 레이어는 Mock 처리하여 컨트롤러 책임(입출력)만 검증
- 검증 오류(400), 리소스 없음(404), 도메인 규칙 위반(422) 등 응답을 공통 포맷으로 검증

#### 컨트롤러 통합 테스트 (E2E)

실제 스프링 컨텍스트에서 **HTTP → Controller → Service → DB** 흐름을 통합 검증합니다.

- “@SpringBootTest + Testcontainers(MySQL) 기반으로 HTTP→Service→DB까지 검증”
- “단위(MockMvc+Mock)와 역할이 겹치지 않도록, E2E는 핵심 시나리오(주문→결제→배송) 위주로 최소 개수만 유지”


### 8.4 트러블슈팅(통합 테스트 트랜잭션 경계)

통합 테스트에서 “설정 단계는 커밋, 실행 단계는 롤백” 같은 **트랜잭션 경계 분리**가 필요할 때가 있습니다.  
이 경우 `TestTransaction`을 활용해 테스트를 2개의 트랜잭션으로 분리하여, 데이터 오염 없이 재현/검증이 가능하도록 정리했습니다.

---

## 9. 실행 방법 (Docker Compose 기준)

이 프로젝트는 **환경별 설정을 분리**하여(dev/test/prod) 실행합니다.  
실제 비밀번호/접속정보는 **절대 저장소에 커밋하지 않으며**, `.env.example`만 예시로 제공합니다.

---

### 9.1 사전 준비

- Docker Desktop 설치 및 실행
- (권장) 로컬에서 실행 표준을 “dev/test/prod”로 고정

---

### 9.2 환경 변수 파일(.env) 정책

- ✅ 커밋 허용: `.env.example`
- ❌ 커밋 금지: `.env` (반드시 `.gitignore`)

#### 9.2.1 `.env.example` 생성(예시 값만)

프로젝트 루트에 `.env.example`를 생성합니다.

```env
# ---- Spring Profile ----
SPRING_PROFILES_ACTIVE=docker

# ---- Datasource (Docker 기준 기본값 예시) ----
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/minimall?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=minimall
SPRING_DATASOURCE_PASSWORD=minimall

# ---- MySQL Container ----
MYSQL_DATABASE=minimall
MYSQL_USER=minimall
MYSQL_PASSWORD=minimall
MYSQL_ROOT_PASSWORD=change-me
```

### 9.2.2 실제 `.env` 생성(실값은 로컬에만)

```bash
cp .env.example .env
```
- `.env`에는 **실제 비밀번호/운영 접속정보**를 넣습니다.
- `.env` 파일은 반드시 `.gitignore`에 포함되어야 합니다.

---

### 9.3 실행 표준 (dev / test / prod)

아래 3개 명령만 기억하면 됩니다.

- **dev(개발 기본)**: `docker/docker-compose.dev.yml`
- **test(테스트 전용)**: `docker/docker-compose.test.yml`
- **prod(운영/배포)**: `docker-compose.prod.yml`


**명령어 전체 문법**
```bash
docker compose -f <compose-file> up -d
```
- `-f <compose-file>`: 사용할 compose 파일 지정
- `up`: 컨테이너 생성/시작
- `-d`: 백그라운드(detached) 실행
---

#### 9.3.1 dev 실행 (로컬 개발: App + DB)

**실행 예시**
```bash
docker compose -f docker/docker-compose.dev.yml up -d
```
**로그 확인**
```bash
docker compose -f docker/docker-compose.dev.yml logs -f app
```

**정상 기준**
- `Active profile(s): docker` (또는 docker로 설정된 프로필)
- DB `Healthy`
- `spring.jpa.open-in-view` 경고 없음 (`open-in-view=false`)

---

#### 9.3.2 test 실행 (테스트 격리 환경)

**실행 예시**
```bash
docker compose -f docker/docker-compose.test.yml up -d
```
- 테스트는 개발 DB와 완전히 분리해서 실행하는 것을 권장합니다.
- (포트/볼륨/DB명/계정 분리)

---

#### 9.3.3 prod 실행 (운영/배포)

**실행 예시**
```bash
docker compose -f docker-compose.prod.yml up -d
```
- 운영 환경에서는 일반적으로 DB를 컨테이너가 아닌 RDS 등 외부 DB로 분리합니다.
- prod는 기본값 없이 환경변수/.env로만 주입되도록 구성하는 것을 권장합니다.

---

### 9.4 중지 / 정리

#### 9.4.1 dev 중지

**명령어 전체 문법**
```bash
docker compose -f <compose-file> down
```
- -f <compose-file>: 사용할 compose 파일 지정
- down: 컨테이너/네트워크 정리(중지 및 제거)

**실행 예시**
```bash
docker compose -f docker/docker-compose.dev.yml down
```
---

#### 9.4.2 볼륨까지 제거(데이터 초기화)

**명령어 전체 문법**
```bash
docker compose -f <compose-file> down -v
```
- -f <compose-file>: 사용할 compose 파일 지정
- down: 컨테이너/네트워크 정리(중지 및 제거)
- -v: named volume 제거(데이터 초기화)

**실행 예시**
```bash
docker compose -f docker/docker-compose.dev.yml down -v
```


## 10. Spring 프로필 구성 (local / docker / prod)

이 프로젝트는 설정 충돌 방지를 위해 **프로필별 설정 파일**을 분리합니다.

- `application.yml` : 공통 설정 (기본 프로필 지정, JPA 공통 옵션 등)
- `application-local.yml` : 로컬 PC에서 직접 DB에 연결할 때
- `application-docker.yml` : Docker Compose 네트워크(DB=서비스명 `db`)로 연결할 때
- `application-prod.yml` : 운영 환경(기본값 금지, 환경변수 필수)

---

### 10.1 공통: open-in-view 비활성화

`spring.jpa.open-in-view=false`로 설정하여

- View 렌더링 단계에서 DB 쿼리가 발생할 수 있는 위험을 줄이고
- 로그 경고를 제거합니다.

---

### 10.2 Hibernate Dialect 설정

`hibernate.dialect`(예: `MySQL8Dialect`)는 최신 Hibernate에서 자동 선택되므로 불필요한 경고를 피하기 위해 **명시 지정하지 않는 것을 권장**합니다.

---

## 11. 보안 정책 (필수)

- `.env`는 절대 커밋하지 않습니다.
- 실수로 커밋한 비밀번호/토큰은 “삭제”가 아니라 “노출”로 취급합니다.
  - 즉시 비밀번호 변경(필수)
  - Git 히스토리 정리(필수)
  - `.env.example`만 유지(필수)

---

## 12. API 문서(Swagger) 확인

애플리케이션 실행 후 브라우저에서 아래 주소로 접속하면 Swagger UI를 통해 API 명세를 확인할 수 있습니다.

- `http://localhost:8080/swagger-ui/index.html`

---

## 13. 향후 계획

- 운영 환경 배포 구성 고도화 (EC2 + 외부 DB(RDS 등) + 환경변수 기반 설정 정리)
- Reverse Proxy 적용 (Nginx) 및 HTTPS(SSL) 적용
- 주문/결제/배송 주요 흐름 모니터링 지표 추가 (로그/메트릭)
- 관리자 기능 확장 (상품/주문 관리 API 및 필요 시 Admin 화면)
- 트래픽 발생 후 성능 측정 및 성능 튜닝 (병목 지점 식별 → 개선 전/후 결과 비교)# docs-erd