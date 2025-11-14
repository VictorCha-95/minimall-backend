# MiniMall – 전자상거래 백엔드 서비스

Spring Boot 3.5 / JPA 기반 쇼핑몰 백엔드 핵심 기능 전체 구현  
주문 · 결제 · 배송 · 재고관리 · 회원관리까지 포함한 실전형 전자상거래 도메인 모델링 프로젝트

---

## 프로젝트 개요

MiniMall은 전자상거래 시스템의 핵심 기능을 도메인 주도 방식으로 구현한 백엔드 서비스입니다.

- 회원가입 / 조회 / 수정
- 상품 등록 / 가격 변경 / 재고 증가·감소 / 재고 초기화
- 주문 생성 / 결제 처리 / 배송 시작 / 주문 취소
- JPA 기반의 트랜잭션 처리와 상태 전이
- Testcontainers 기반 통합 테스트로 품질 보장

---

## 기술 스택

Language : Java 21  
Framework : Spring Boot 3.5, Spring MVC  
ORM : JPA / Hibernate  
DB : MySQL 8, Testcontainers  
API 문서 : Swagger(OpenAPI 3)  
빌드 : Gradle  
Validation : Jakarta Validation  
기타 : Lombok, MapStruct 일부 적용

---

## 아키텍처 개요

Presentation Layer
- Controller, DTO
- API 요청/응답 처리
- Request → Command 변환

Service Layer
- 트랜잭션 경계 설정
- 도메인 로직 실행
- Repository 접근
- 도메인 예외 변환

Domain Layer
- Member, Product, Order
- Delivery, OrderItem, Pay
- 비즈니스 규칙과 상태 전이
- Guards 및 CustomException

Infrastructure Layer
- Spring Data JPA
- MySQL

---

## 도메인 구조

Member
- id
- name
- email
- address

Product
- id
- name
- price
- stockQuantity

Order
- id
- member_id
- orderStatus
- finalAmount
- createdAt

OrderItem
- id
- order_id
- product_id
- quantity
- unitPrice

Delivery
- id
- order_id
- deliveryStatus
- trackingNo
- shippedAt

Pay
- id
- order_id
- payMethod
- payAmount
- payStatus

---

## REST API 요약

### Product API
POST /product  
→ 상품 등록 (201)

POST /product/{id}/stock/add  
→ 재고 증가 (200)

POST /product/{id}/stock/reduce  
→ 재고 감소 (200)

POST /product/{id}/stock/clear  
→ 재고 초기화 (200)

PATCH /product/{id}/name  
→ 상품명 변경 (200)

PATCH /product/{id}/price  
→ 상품 가격 변경 (200)

DELETE /product/{id}  
→ 상품 삭제 (204)

---

### Member API
POST /members  
→ 회원 등록 (201)

GET /members/{id}  
→ 회원 조회 (200)

PATCH /members/{id}  
→ 회원 수정 (200)

---

### Order API
POST /orders  
→ 주문 생성 (201)

POST /orders/{id}/payment  
→ 결제 처리 (200)

PATCH /orders/{id}/delivery  
→ 배송 시작 (200)

PATCH /orders/{id}/cancel  
→ 주문 취소 (200)

GET /orders/{id}  
→ 주문 상세 조회 (200)
