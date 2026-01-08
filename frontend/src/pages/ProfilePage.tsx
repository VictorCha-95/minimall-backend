import React, { useState } from "react";
import AuthErrorPage from "../components/AuthErrorPage";
import { AuthMeResponse } from "../services/authApi";
import { updateMember } from "../services/memberApi";

type ProfilePageProps = {
  me: AuthMeResponse | null;
};

const ProfilePage: React.FC<ProfilePageProps> = ({ me }) => {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(me?.name ?? "");
  const [email, setEmail] = useState(me?.email ?? "");
  const [postcode, setPostcode] = useState(me?.addr?.postcode ?? "");
  const [stateValue, setStateValue] = useState(me?.addr?.state ?? "");
  const [city, setCity] = useState(me?.addr?.city ?? "");
  const [street, setStreet] = useState(me?.addr?.street ?? "");
  const [detail, setDetail] = useState(me?.addr?.detail ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  if (!me) {
    return (
      <AuthErrorPage
        title="내 정보"
        message="사용자 정보를 불러오지 못했습니다. 다시 로그인해주세요."
      />
    );
  }

  const handleToggleEdit = () => {
    if (!editing) {
      setName(me.name);
      setEmail(me.email);
      setPostcode(me.addr?.postcode ?? "");
      setStateValue(me.addr?.state ?? "");
      setCity(me.addr?.city ?? "");
      setStreet(me.addr?.street ?? "");
      setDetail(me.addr?.detail ?? "");
      setError(null);
      setSuccess(null);
    }
    setEditing((prev) => !prev);
  };

  const handleSave = async (event: React.FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);

    const hasAnyAddress = postcode || stateValue || city || street || detail;
    const hasAllRequiredAddress = postcode && stateValue && city && street;
    if (hasAnyAddress && !hasAllRequiredAddress) {
      setError("주소를 입력할 경우, 우편번호/시도/시군구/도로명 주소는 모두 입력해주세요.");
      setSaving(false);
      return;
    }

    const addr = hasAllRequiredAddress
      ? {
          postcode,
          state: stateValue,
          city,
          street,
          detail: detail || null,
        }
      : null;

    try {
      await updateMember(me.id, {
        name,
        email,
        addr,
      });
      window.dispatchEvent(new CustomEvent("auth:changed"));
      setSuccess("회원 정보가 수정되었습니다.");
      setEditing(false);
    } catch (err: unknown) {
      setError("회원 정보 수정에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <section>
      <h1>내 정보</h1>
      <div className="profile-actions">
        <button type="button" onClick={handleToggleEdit}>
          {editing ? "수정 취소" : "정보 수정"}
        </button>
      </div>
      <div className="me-card">
        <dl>
          <dt>아이디</dt>
          <dd>{me.loginId}</dd>
          <dt>이름</dt>
          <dd>{me.name}</dd>
          <dt>이메일</dt>
          <dd>{me.email}</dd>
          <dt>권한</dt>
          <dd>{me.role}</dd>
          <dt>상태</dt>
          <dd>{me.status}</dd>
          {me.grade && (
            <>
              <dt>등급</dt>
              <dd>{me.grade}</dd>
            </>
          )}
          {me.addr && (
            <>
              <dt>주소</dt>
              <dd>
                {me.addr.postcode} {me.addr.state} {me.addr.city}{" "}
                {me.addr.street} {me.addr.detail ?? ""}
              </dd>
            </>
          )}
          {me.storeName && (
            <>
              <dt>상호명</dt>
              <dd>{me.storeName}</dd>
            </>
          )}
          {me.businessNumber && (
            <>
              <dt>사업자번호</dt>
              <dd>{me.businessNumber}</dd>
            </>
          )}
        </dl>
      </div>
      {editing && (
        <form className="profile-form" onSubmit={handleSave}>
          <h2>회원 정보 수정</h2>
          <label>
            이름
            <input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
            />
          </label>
          <label>
            이메일
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <div className="profile-form-divider">주소 (선택)</div>
          <label>
            우편번호
            <input
              type="text"
              value={postcode}
              onChange={(event) => setPostcode(event.target.value)}
            />
          </label>
          <label>
            시/도
            <input
              type="text"
              value={stateValue}
              onChange={(event) => setStateValue(event.target.value)}
            />
          </label>
          <label>
            시/군/구
            <input
              type="text"
              value={city}
              onChange={(event) => setCity(event.target.value)}
            />
          </label>
          <label>
            도로명 주소
            <input
              type="text"
              value={street}
              onChange={(event) => setStreet(event.target.value)}
            />
          </label>
          <label>
            상세 주소
            <input
              type="text"
              value={detail}
              onChange={(event) => setDetail(event.target.value)}
            />
          </label>
          <button type="submit" disabled={saving}>
            {saving ? "저장 중..." : "저장"}
          </button>
          {error && <p className="error-text">{error}</p>}
          {success && <p className="success-text">{success}</p>}
        </form>
      )}
    </section>
  );
};

export default ProfilePage;
