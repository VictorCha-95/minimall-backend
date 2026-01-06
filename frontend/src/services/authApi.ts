import axios from "axios";

interface LoginRequest {
  loginId: string;
  password: string;
}

interface LoginResponse {
  tokenType: string;
  accessToken: string;
  expiresIn: number;
}

// 백엔드 AuthController 스펙(/auth/login)에 맞춘 로그인 API
export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await axios.post<LoginResponse>("/auth/login", payload);
  return data;
}

