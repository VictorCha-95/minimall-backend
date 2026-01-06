import React, { useState } from "react";
import axios from "axios";
import { registerCustomer } from "../services/memberApi";

const CustomerRegisterPage: React.FC = () => {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [postcode, setPostcode] = useState("");
  const [state, setState] = useState("");
  const [city, setCity] = useState("");
  const [street, setStreet] = useState("");
  const [detail, setDetail] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // 주소는 모두 입력하거나 모두 비워야 함 (일부만 입력 시 에러 방지)
      const hasAnyAddress = postcode || state || city || street || detail;
      const hasAllRequiredAddress = postcode && state && city && street;
      
      if (hasAnyAddress && !hasAllRequiredAddress) {
        setError("주소를 입력할 경우, 우편번호/시도/시군구/도로명 주소는 모두 입력해주세요.");
        setLoading(false);
        return;
      }

      const addrFilled = hasAllRequiredAddress
        ? {
            postcode,
            state,
            city,
            street,
            detail: detail || null
          }
        : null;

      const res = await registerCustomer({
        loginId,
        password,
        name,
        email,
        addr: addrFilled
      });

      setSuccess(`회원가입 완료! (ID: ${res.id}, 이름: ${res.name})`);
      console.log("registerCustomer result", res);
    } catch (err: unknown) {
      console.error("회원가입 에러:", err);
      if (axios.isAxiosError(err)) {
        const errorMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "회원가입에 실패했습니다. 입력 값을 확인해주세요.";
        setError(errorMessage);
      } else {
        setError("회원가입에 실패했습니다. 입력 값을 확인해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-card">
      <h1>고객 회원가입</h1>
      <form onSubmit={handleSubmit} className="form">
        <label>
          아이디
          <input
            type="text"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            required
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        <label>
          이름
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </label>
        <label>
          이메일
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>

        <hr />
        <p style={{ fontSize: "0.8rem", opacity: 0.9 }}>주소 (선택)</p>

        <label>
          우편번호
          <input
            type="text"
            value={postcode}
            onChange={(e) => setPostcode(e.target.value)}
          />
        </label>
        <label>
          시/도
          <input
            type="text"
            value={state}
            onChange={(e) => setState(e.target.value)}
          />
        </label>
        <label>
          시/군/구
          <input
            type="text"
            value={city}
            onChange={(e) => setCity(e.target.value)}
          />
        </label>
        <label>
          도로명 주소
          <input
            type="text"
            value={street}
            onChange={(e) => setStreet(e.target.value)}
          />
        </label>
        <label>
          상세 주소
          <input
            type="text"
            value={detail}
            onChange={(e) => setDetail(e.target.value)}
          />
        </label>

        <button type="submit" disabled={loading}>
          {loading ? "회원가입 중..." : "회원가입"}
        </button>
      </form>
      {error && <p className="error-text">{error}</p>}
      {success && <p className="success-text">{success}</p>}
    </section>
  );
};

export default CustomerRegisterPage;

