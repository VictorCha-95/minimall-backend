
import React from "react";
import { AuthMeResponse } from "../services/authApi";

type HomePageProps = {
  me: AuthMeResponse | null;
};

const HomePage: React.FC<HomePageProps> = ({ me }) => {
  return (
    <section>
      <h1>MiniMall 홈</h1>
      <p>백엔드와 연동될 프론트엔드 기본 화면입니다.</p>
      {me ? (
        <div className="me-card">
          <h2>내 정보</h2>
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

