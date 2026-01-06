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

// 고객 회원가입: POST /members/customers
export async function registerCustomer(
  payload: CustomerRegisterRequest
): Promise<MemberSummaryResponse> {
  const { data } = await axios.post<MemberSummaryResponse>(
    "/members/customers",
    payload
  );
  return data;
}

