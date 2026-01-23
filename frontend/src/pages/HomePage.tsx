
import React from "react";
import { Link } from "react-router-dom";
import { AuthMeResponse } from "../services/authApi";

type HomePageProps = {
  me: AuthMeResponse | null;
};

const HomePage: React.FC<HomePageProps> = ({ me }) => {
  return (
    <section className="home">
      <div className="hero">
        <div>
          <p className="eyebrow">MiniMall Platform</p>
          <h1>Minimall 이커머스 </h1>
          <p className="hero-sub">
            인증, 주문, 배송, 재고까지 백엔드 플로우를 한 화면에서 확인하세요.
          </p>
          <div className="hero-actions">
            {me ? (
              <Link to="/workspace" className="btn-primary">
                운영 콘솔 열기
              </Link>
            ) : (
              <Link to="/login" className="btn-primary">
                로그인하고 시작하기
              </Link>
            )}
            <Link to="/register/customer" className="btn-outline">
              고객 회원가입
            </Link>
          </div>
        </div>
        <div className="hero-card">
          <h2>오늘의 상태</h2>
          <ul>
            <li>상품 API: /api/products</li>
            <li>주문 API: /api/orders</li>
            <li>회원 API: /api/members</li>
          </ul>
          <p className="muted-text">
            로그인 후 운영 콘솔에서 상세 시나리오를 실행할 수 있습니다.
          </p>
        </div>
      </div>

      <div className="feature-grid">
        <div className="feature-card">
          <h3>상품 카탈로그</h3>
          <p>상품 목록 조회와 재고 변화 흐름을 빠르게 검증합니다.</p>
        </div>
        <div className="feature-card">
          <h3>주문/결제/배송</h3>
          <p>주문 생성부터 배송 완료까지 엔드-투-엔드를 시뮬레이션합니다.</p>
        </div>
        <div className="feature-card">
          <h3>회원 허브</h3>
          <p>회원 조회와 주문 요약을 한 화면에서 확인합니다.</p>
        </div>
      </div>

      {me ? (
        <div className="me-card">
          <h2>내 정보 스냅샷</h2>
          <dl>
            <dt>아이디</dt>
            <dd>{me.loginId}</dd>
            <dt>이름</dt>
            <dd>{me.name}</dd>
            <dt>이메일</dt>
            <dd>{me.email}</dd>
            <dt>권한</dt>
            <dd>{me.role}</dd>
            <dt>상태</dt>
            <dd>{me.status}</dd>
            {me.grade && (
              <>
                <dt>등급</dt>
                <dd>{me.grade}</dd>
              </>
            )}
            {me.addr && (
              <>
                <dt>주소</dt>
                <dd>
                  {me.addr.postcode} {me.addr.state} {me.addr.city}{" "}
                  {me.addr.street} {me.addr.detail ?? ""}
                </dd>
              </>
            )}
            {me.storeName && (
              <>
                <dt>상호명</dt>
                <dd>{me.storeName}</dd>
              </>
            )}
            {me.businessNumber && (
              <>
                <dt>사업자번호</dt>
                <dd>{me.businessNumber}</dd>
              </>
            )}
          </dl>
        </div>
      ) : (
        <p className="muted-text">로그인하면 내 정보를 확인할 수 있습니다.</p>
      )}
    </section>
  );
};

export default HomePage;

