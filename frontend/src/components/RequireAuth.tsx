import React from "react";
import { Navigate } from "react-router-dom";
import AuthLoading from "./AuthLoading";
import { getAccessToken } from "../services/authApi";

type RequireAuthProps = {
  ready: boolean;
  loading?: boolean;
  children: React.ReactElement;
};

const RequireAuth: React.FC<RequireAuthProps> = ({
  ready,
  loading = false,
  children,
}) => {
  if (!ready || loading) {
    return <AuthLoading />;
  }

  const token = getAccessToken();
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default RequireAuth;
