import React from "react";
import { Link } from "react-router-dom";

const RegisterSelectPage: React.FC = () => {
  return (
    <section className="auth-card">
      <h1>회원가입 선택</h1>
      <p className="muted-text">
        가입 유형을 선택한 다음 권한에 맞는 회원가입을 진행하세요.
      </p>
      <div className="button-row" style={{ marginTop: "1.5rem" }}>
        <Link to="/register/customer" className="btn-primary">
          고객 회원가입
        </Link>
        <Link to="/register/seller" className="btn-outline">
          판매자 회원가입
        </Link>
      </div>
    </section>
  );
};

export default RegisterSelectPage;
