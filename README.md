# MiniMall – 전자상거래 백엔드 서비스

Spring Boot 3.5 / JPA 기반으로 **회원·상품·주문·결제·배송** 등  
전자상거래 핵심 도메인을 도메인 주도 설계(DDD) 방식으로 구현한 백엔드 프로젝트입니다.

---

## 1. 프로젝트 개요

MiniMall은 다음을 목표로 합니다.

- 실제 서비스 수준의 **주문·결제·배송 흐름**을 설계하고 구현
- **도메인 규칙과 상태 전이**를 코드로 명확하게 표현
- **테스트 코드와 Docker 환경**을 통한 안정적인 개발/실행 환경 제공

이 프로젝트는 단순 CRUD 예제가 아니라,  
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
| Infra / Dev   | Docker, Testcontainers                     |
| API 문서      | Swagger(OpenAPI)                           |
| Test          | JUnit 5, Mockito, Spring Boot Test         |

---

## 3. 아키텍처 개요

레이어드 아키텍처를 바탕으로, 도메인 중심 구조를 사용합니다.

- **API 레이어 (`api`)**
    - HTTP 요청/응답 처리
    - Request/Response DTO 정의
    - Swagger 기반 API 문서화
- **서비스 레이어 (`service`)**
    - 트랜잭션 경계 설정
    - 도메인 객체 조합 및 유스케이스 구현
- **도메인 레이어 (`domain`)**
    - 엔티티, 값 객체(Value Object), 도메인 서비스, 도메인 예외 정의
    - 비즈니스 규칙·상태 전이 책임
- **인프라 레이어 (`repository`, `config`)**
    - Spring Data JPA Repository
    - DB 설정, 애플리케이션 공통 설정

---

## 4. 주요 도메인 및 기능

### 4.1 회원(Member)

- 회원 가입
- 회원 정보 수정
- 단건 조회 / 목록 조회
- 이메일 중복 검증

주요 규칙

- 이메일은 **중복 불가**
- 필수 값(이메일, 이름 등) 누락 시 도메인/검증 예외 발생

---

### 4.2 상품(Product)

- 상품 등록
- 가격 변경
- 재고 증가 / 감소
- 재고 초기화

주요 규칙

- 재고는 **음수 불가**
- 가격은 **0 이하 불가**
- 판매 중지/품절 상태로의 전이 가능

---

### 4.3 주문(Order)

- 주문 생성
- 주문 상세 조회
- 주문 목록 조회(회원 기준)
- 주문 취소

주요 규칙

- 주문 상태: `ORDERED` → `PAID` → `SHIPPING` → `DELIVERED`
- 주문 취소는 **아직 배송이 시작되지 않은 상태**에서만 가능
- 주문 금액 = 각 주문 상품의 (단가 × 수량) 합계로 계산
- 주문 생성 시:
    - 상품 재고 차감
    - 주문 금액 계산 및 검증

---

### 4.4 결제(Pay)

- 결제 요청 처리
- 결제 성공/실패 상태 관리

주요 규칙

- 결제 금액은 **주문 최종 금액과 일치**해야 함
- 결제 성공 시 주문 상태를 `PAID`로 변경
- 결제 실패 시 적절한 예외 및 상태 처리

---

### 4.5 배송(Delivery)

- 배송 시작 (운송장 번호 등록)
- 배송 상태 조회

주요 규칙

- 배송 상태: `READY` → `SHIPPING` → `DELIVERED`
- 배송 시작 시:
    - 운송장 번호 필수
    - 이미 배송 중/완료 상태면 시작 불가

---

## 5. 도메인 설계 특징

- **도메인 중심 패키지 구성**
    - `member`, `product`, `order`, `delivery`, `pay` 등 도메인별로 분리
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

RESTful 스타일을 기반으로, 리소스 중심 URL과 HTTP 메서드를 사용합니다.

예시 엔드포인트

### 회원(Member)

- `POST /members` – 회원 가입
- `GET /members/{id}` – 회원 단건 조회
- `GET /members` – 회원 목록 조회
- `PATCH /members/{id}` – 회원 정보 수정

### 상품(Product)

- `POST /products` – 상품 등록
- `PATCH /products/{id}/price` – 가격 변경
- `PATCH /products/{id}/stock` – 재고 증가/감소
- `GET /products/{id}` – 상품 단건 조회
- `GET /products` – 상품 목록 조회

### 주문(Order)

- `POST /orders` – 주문 생성
- `GET /orders/{id}` – 주문 상세 조회
- `GET /members/{memberId}/orders` – 회원별 주문 목록 조회
- `DELETE /orders/{id}` – 주문 취소

### 결제(Pay)

- `POST /orders/{id}/payment` – 주문 결제 요청

### 배송(Delivery)

- `PATCH /orders/{id}/delivery` – 배송 시작(운송장 번호 포함)

---

## 7. 예외 처리 정책

- 공통 예외 핸들러를 통해 HTTP 상태 코드와 응답 포맷을 통일합니다.
- 검증 오류: `400 Bad Request`
- 리소스 없음: `404 Not Found`
- 도메인 규칙 위반(상태 전이 불가 등): `422 Unprocessable Entity` 등

응답 바디 예시

```json
{
  "status": 422,
  "code": "ORDER_STATUS_ERROR",
  "message": "배송이 이미 시작된 주문은 취소할 수 없습니다.",
  "path": "/orders/1"
}

## 8. 테스트 전략

### 단위 테스트

도메인 엔티티/값 객체의 **비즈니스 규칙**을 검증합니다.

- 주문 금액 계산
- 재고 차감
- 상태 전이
- 예외 발생 조건 등

### 서비스 테스트

서비스 레이어의 유스케이스 흐름을 검증합니다.

- Mockito 기반으로 Repository/외부 의존성을 Mock 처리
- 이메일 중복 검증
- 주문 생성 시 도메인 호출 플로우 등 검증

### 통합 테스트

실제 애플리케이션 구동과 유사한 환경에서 end-to-end 흐름을 검증합니다.

- `@SpringBootTest`, `@AutoConfigureMockMvc`
- Testcontainers MySQL을 사용해 실제 DB와 유사한 환경 구성
- REST API 요청/응답, 트랜잭션, JPA 동작을 포함한 end-to-end 테스트

---

## 9. 프로젝트 구조

```text
src
└── main
    ├── java
    │   └── com.minimall
    │       ├── api
    │       │   ├── member
    │       │   ├── product
    │       │   ├── order
    │       │   ├── delivery
    │       │   ├── pay
    │       │   └── exception
    │       ├── domain
    │       │   ├── member
    │       │   ├── product
    │       │   ├── order
    │       │   ├── delivery
    │       │   ├── pay
    │       │   └── common   // 값 객체, 공통 Enum, 도메인 예외 등
    │       ├── service
    │       │   ├── member
    │       │   ├── product
    │       │   ├── order
    │       │   ├── delivery
    │       │   └── pay
    │       └── config
    └── resources
        ├── application.yml

## 10. 로컬 실행 방법

### 10.1 사전 준비

- JDK 21  
- Docker (선택, MySQL 컨테이너 사용 시)  
- Gradle Wrapper 포함 프로젝트  

### 10.2 MySQL 실행 (Docker 사용 예시)

아래 명령어로 MySQL 8.0 컨테이너를 실행합니다.

    docker run --name minimall-mysql \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=minimall \
      -p 3307:3306 \
      -d mysql:8.0

### 10.3 `application.yml` 예시

로컬 개발 환경에서 사용할 수 있는 기본 설정 예시는 다음과 같습니다.

    spring:
      datasource:
        url: jdbc:mysql://localhost:3307/minimall?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
        username: root
        password: root
      jpa:
        hibernate:
          ddl-auto: update
        properties:
          hibernate:
            format_sql: true
            show_sql: false

    server:
      port: 8080

### 10.4 애플리케이션 실행

Gradle Wrapper를 사용해 애플리케이션을 실행합니다.

    ./gradlew bootRun

또는 IDE에서 `MiniMallApiApplication` 메인 클래스를 실행합니다.

### 10.5 API 문서(Swagger) 확인

애플리케이션 실행 후 브라우저에서 아래 주소로 접속하면 Swagger UI를 통해  
API 명세를 확인할 수 있습니다.

- `http://localhost:8080/swagger-ui/index.html`

---

## 11. 향후 계획

- AWS EC2 + RDS 환경에 배포  
- Nginx Reverse Proxy 및 HTTPS(SSL) 적용  
- 주문/결제/배송 흐름에 대한 모니터링 지표 추가  
- 간단한 관리자 페이지(상품/주문 관리) API 확장  