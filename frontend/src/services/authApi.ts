import axios from "axios";

const ACCESS_TOKEN_KEY = "minimall.accessToken";
const AUTH_CHANGED_EVENT = "auth:changed";
const AUTH_EXPIRED_EVENT = "auth:expired";
let interceptorInstalled = false;
let refreshPromise: Promise<string | null> | null = null;

interface LoginRequest {
  loginId: string;
  password: string;
}

interface LoginResponse {
  tokenType: string;
  accessToken: string;
  accessExpiresIn: number;
  refreshExpiresIn: number;
}

export interface AuthMeResponse {
  id: number;
  loginId: string;
  name: string;
  email: string;
  role: string;
  status: string;
  grade?: string | null;
  addr?: {
    postcode: string;
    state: string;
    city: string;
    street: string;
    detail?: string | null;
  } | null;
  storeName?: string | null;
  businessNumber?: string | null;
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string | null): void {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
    axios.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    delete axios.defaults.headers.common.Authorization;
  }
  window.dispatchEvent(new CustomEvent(AUTH_CHANGED_EVENT));
}

export async function bootstrapAuth(): Promise<void> {
  const stored = getAccessToken();
  if (stored) {
    setAccessToken(stored);
  }

  if (!stored) {
    try {
      const result = await refresh();
      setAccessToken(result.accessToken);
    } catch {
      setAccessToken(null);
    }
  }
}

export function setupAuthInterceptor(): void {
  if (interceptorInstalled) return;
  interceptorInstalled = true;

  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config as
        | (typeof error.config & { _retry?: boolean; url?: string })
        | undefined;
      const status = error.response?.status;
      const url = original?.url || "";

      if (status !== 401 || original?._retry) {
        return Promise.reject(error);
      }
      if (url.startsWith("/api/auth/")) {
        return Promise.reject(error);
      }

      if (original) {
        original._retry = true;
      }
      if (!refreshPromise) {
        refreshPromise = refresh()
          .then((result) => result.accessToken)
          .catch(() => null)
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newToken = await refreshPromise;
      if (!newToken) {
        window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT));
        setAccessToken(null);
        return Promise.reject(error);
      }

      if (original) {
        original.headers = {
          ...(original.headers || {}),
          Authorization: `Bearer ${newToken}`,
        };
        return axios(original);
      }
      return Promise.reject(error);
    }
  );
}

// 백엔드 AuthController 스펙(/auth/login)에 맞춘 로그인 API
export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await axios.post<LoginResponse>("/api/auth/login", payload, {
    withCredentials: true,
  });
  setAccessToken(data.accessToken);
  return data;
}

export async function refresh(): Promise<LoginResponse> {
  const { data } = await axios.post<LoginResponse>("/api/auth/refresh", null, {
    withCredentials: true,
  });
  setAccessToken(data.accessToken);
  return data;
}

export async function logout(): Promise<void> {
  await axios.post("/api/auth/logout", null, { withCredentials: true });
  setAccessToken(null);
}

export async function me(accessToken?: string): Promise<AuthMeResponse> {
  const token = accessToken ?? getAccessToken();
  if (!token) {
    throw new Error("access token is missing");
  }

  const { data } = await axios.get<AuthMeResponse>("/api/auth/me", {
    headers: { Authorization: `Bearer ${token}` },
    withCredentials: true,
  });
  return data;
}

