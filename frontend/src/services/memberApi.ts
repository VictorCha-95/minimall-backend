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

export interface MemberSummaryResponse {
  id: number;
  loginId: string;
  name: string;
}

export interface MemberUpdateRequest {
  password?: string | null;
  name?: string | null;
  email?: string | null;
  addr?: MemberAddress | null;
}

export interface MemberDetailResponse {
  id: number;
  loginId: string;
  name: string;
  email: string;
  grade?: string | null;
  addr?: MemberAddress | null;
}

export interface OrderSummaryResponse {
  id: number;
  orderedAt: string;
  orderStatus: string;
  itemCount: number;
  finalAmount: number;
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

export async function getMembers(): Promise<MemberSummaryResponse[]> {
  const { data } = await axios.get<MemberSummaryResponse[]>("/api/members");
  return data;
}

export async function getMemberDetail(
  memberId: number
): Promise<MemberDetailResponse> {
  const { data } = await axios.get<MemberDetailResponse>(
    `/api/members/${memberId}`
  );
  return data;
}

export async function getMemberOrders(
  memberId: number
): Promise<OrderSummaryResponse[]> {
  const { data } = await axios.get<OrderSummaryResponse[]>(
    `/api/members/${memberId}/orders`
  );
  return data;
}

export async function getMemberSummaryByEmail(
  email: string
): Promise<MemberSummaryResponse> {
  const { data } = await axios.get<MemberSummaryResponse>(
    "/api/members/by-email/summary",
    { params: { email } }
  );
  return data;
}

export async function getMemberSummaryByLoginId(
  loginId: string
): Promise<MemberSummaryResponse> {
  const { data } = await axios.get<MemberSummaryResponse>(
    "/api/members/by-loginId/summary",
    { params: { loginId } }
  );
  return data;
}

