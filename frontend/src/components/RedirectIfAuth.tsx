import React from "react";
import { Navigate } from "react-router-dom";
import AuthLoading from "./AuthLoading";
import { getAccessToken } from "../services/authApi";

type RedirectIfAuthProps = {
  ready: boolean;
  loading?: boolean;
  children: React.ReactElement;
};

const RedirectIfAuth: React.FC<RedirectIfAuthProps> = ({
  ready,
  loading = false,
  children,
}) => {
  if (!ready || loading) {
    return <AuthLoading />;
  }

  const token = getAccessToken();
  if (token) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default RedirectIfAuth;
