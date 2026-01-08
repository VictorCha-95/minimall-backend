import React from "react";

type AuthLoadingProps = {
  message?: string;
};

const AuthLoading: React.FC<AuthLoadingProps> = ({
  message = "인증 확인 중...",
}) => {
  return (
    <div className="auth-loading">
      <span className="auth-spinner" />
      <span>{message}</span>
    </div>
  );
};

export default AuthLoading;
