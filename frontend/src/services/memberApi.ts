import axios from "axios";

interface MemberAddress {
  postcode: string;
  state: string;
  city: string;
  street: string;
  detail?: string | null;
}

export interface CustomerRegisterRequest {
  loginId: string;
  password: string;
  name: string;
  email: string;
  addr?: MemberAddress | null;
}

interface MemberSummaryResponse {
  id: number;
  loginId: string;
  name: string;
  email: string;
}

export interface MemberUpdateRequest {
  password?: string | null;
  name?: string | null;
  email?: string | null;
  addr?: MemberAddress | null;
}

// 고객 회원가입: POST /members/customers
export async function registerCustomer(
  payload: CustomerRegisterRequest
): Promise<MemberSummaryResponse> {
  const { data } = await axios.post<MemberSummaryResponse>(
    "/api/members/customers",
    payload
  );
  return data;
}

// 회원 정보 수정: PATCH /members/{id}
export async function updateMember(
  memberId: number,
  payload: MemberUpdateRequest
): Promise<void> {
  await axios.patch(`/api/members/${memberId}`, payload);
}

