import React, { useEffect, useState } from "react";
import { Routes, Route, Link, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import CustomerRegisterPage from "./pages/CustomerRegisterPage";
import ProfilePage from "./pages/ProfilePage";
import RequireAuth from "./components/RequireAuth";
import RedirectIfAuth from "./components/RedirectIfAuth";
import {
  AuthMeResponse,
  bootstrapAuth,
  getAccessToken,
  logout,
  me,
  setupAuthInterceptor,
} from "./services/authApi";

const App: React.FC = () => {
  const navigate = useNavigate();
  const [meInfo, setMeInfo] = useState<AuthMeResponse | null>(null);
  const [authNotice, setAuthNotice] = useState<string | null>(null);
  const [authReady, setAuthReady] = useState(false);
  const [meLoading, setMeLoading] = useState(false);

  useEffect(() => {
    setupAuthInterceptor();

    let active = true;

    const loadMe = async () => {
      setMeLoading(true);
      try {
        const data = await me();
        if (active) {
          setMeInfo(data);
        }
      } catch {
        if (active) {
          setMeInfo(null);
        }
      } finally {
        if (active) {
          setMeLoading(false);
        }
      }
    };

    bootstrapAuth()
      .then(() => {
        if (getAccessToken()) {
          return loadMe();
        }
        return undefined;
      })
      .catch(() => {
        // ignore bootstrap failures (e.g. no refresh cookie)
      })
      .finally(() => {
        if (active) {
          setAuthReady(true);
        }
      });

    const handleAuthChanged = () => {
      const token = getAccessToken();
      if (!token) {
        setMeInfo(null);
        return;
      }
      setAuthNotice(null);
      loadMe();
    };

    const handleAuthExpired = () => {
      setAuthNotice("세션이 만료되었습니다. 다시 로그인해주세요.");
      setMeInfo(null);
      navigate("/login");
    };

    window.addEventListener("auth:changed", handleAuthChanged);
    window.addEventListener("auth:expired", handleAuthExpired);

    return () => {
      active = false;
      window.removeEventListener("auth:changed", handleAuthChanged);
      window.removeEventListener("auth:expired", handleAuthExpired);
    };
  }, [navigate]);

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      setMeInfo(null);
      navigate("/login");
    }
  };

  return (
    <div className="app">
      <header className="header">
        <div className="logo">MiniMall</div>
        <nav className="nav">
          <Link to="/">홈</Link>
          {meInfo ? (
            <>
              <Link to="/me">내 정보</Link>
              <span className="nav-user">
                {meInfo.name} ({meInfo.role})
              </span>
              <button type="button" onClick={handleLogout} className="nav-btn">
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login">로그인</Link>
              <Link to="/register/customer">회원가입</Link>
            </>
          )}
        </nav>
      </header>
      {authNotice && (
        <div className="auth-notice">
          <span>{authNotice}</span>
          <button
            type="button"
            className="notice-close"
            onClick={() => setAuthNotice(null)}
          >
            닫기
          </button>
        </div>
      )}
      <main className="main">
        <Routes>
          <Route path="/" element={<HomePage me={meInfo} />} />
          <Route
            path="/login"
            element={
              <RedirectIfAuth ready={authReady} loading={meLoading}>
                <LoginPage />
              </RedirectIfAuth>
            }
          />
          <Route
            path="/register/customer"
            element={
              <RedirectIfAuth ready={authReady} loading={meLoading}>
                <CustomerRegisterPage />
              </RedirectIfAuth>
            }
          />
          <Route
            path="/me"
            element={
              <RequireAuth ready={authReady} loading={meLoading}>
                <ProfilePage me={meInfo} />
              </RequireAuth>
            }
          />
        </Routes>
      </main>
    </div>
  );
};

export default App;

