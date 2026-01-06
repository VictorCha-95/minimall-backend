import React from "react";
import { Routes, Route, Link } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import CustomerRegisterPage from "./pages/CustomerRegisterPage";

const App: React.FC = () => {
  return (
    <div className="app">
      <header className="header">
        <div className="logo">MiniMall</div>
        <nav className="nav">
          <Link to="/">홈</Link>
          <Link to="/login">로그인</Link>
          <Link to="/register/customer">회원가입</Link>
        </nav>
      </header>
      <main className="main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register/customer" element={<CustomerRegisterPage />} />
        </Routes>
      </main>
    </div>
  );
};

export default App;

