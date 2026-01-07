import React from "react";
import { Link } from "react-router-dom";

type AuthErrorPageProps = {
  title?: string;
  message: string;
};

const AuthErrorPage: React.FC<AuthErrorPageProps> = ({
  title = "인증 오류",
  message,
}) => {
  return (
    <section>
      <h1>{title}</h1>
      <div className="auth-error">
        <p>{message}</p>
        <div className="auth-error-actions">
          <Link to="/login" className="auth-link">
            로그인으로 이동
          </Link>
        </div>
      </div>
    </section>
  );
};

export default AuthErrorPage;
