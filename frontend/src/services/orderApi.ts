import axios from "axios";

export type OrderStatus = "ORDERED" | "CONFIRMED" | "COMPLETED" | "CANCELED";
export type PayMethod =
  | "CARD"
  | "BANK_TRANSFER"
  | "CASH"
  | "PAYPAL"
  | "MOBILE_PAY";
export type PayStatus = "READY" | "PAID" | "FAILED" | "CANCELED";
export type DeliveryStatus =
  | "READY"
  | "SHIPPING"
  | "COMPLETED"
  | "FAILED"
  | "CANCELED";

export interface OrderItemCreateRequest {
  productId: number;
  quantity: number;
}

export interface OrderCreateRequest {
  memberId: number;
  items: OrderItemCreateRequest[];
}

export interface OrderCreateResponse {
  id: number;
  orderedAt: string;
  orderStatus: OrderStatus;
  originalAmount: number;
  discountAmount: number;
  finalAmount: number;
  itemCount: number;
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  orderPrice: number;
  orderQuantity: number;
  totalAmount: number;
}

export interface PayResponse {
  payMethod: PayMethod;
  payAmount: number;
  payStatus: PayStatus;
  paidAt: string | null;
}

export interface AddressDto {
  postcode: string;
  state: string;
  city: string;
  street: string;
  detail?: string | null;
}

export interface DeliverySummaryResponse {
  deliveryStatus: DeliveryStatus;
  trackingNo: string | null;
  shipAddr: AddressDto | null;
  shippedAt: string | null;
  arrivedAt: string | null;
}

export interface OrderDetailResponse {
  id: number;
  orderedAt: string;
  orderStatus: OrderStatus;
  finalAmount: number;
  orderItems: OrderItemResponse[];
  pay: PayResponse | null;
  delivery: DeliverySummaryResponse | null;
}

export interface OrderSummaryResponse {
  id: number;
  orderedAt: string;
  orderStatus: OrderStatus;
  itemCount: number;
  finalAmount: number;
}

export interface PayRequest {
  payMethod: PayMethod;
  payAmount: number;
}

export interface StartDeliveryRequest {
  trackingNo: string;
  shippedAt?: string | null;
}

export interface CompleteDeliveryRequest {
  arrivedAt?: string | null;
}

export async function createOrder(
  payload: OrderCreateRequest
): Promise<OrderCreateResponse> {
  const { data } = await axios.post<OrderCreateResponse>(
    "/api/orders",
    payload
  );
  return data;
}

export async function getOrder(orderId: number): Promise<OrderDetailResponse> {
  const { data } = await axios.get<OrderDetailResponse>(`/api/orders/${orderId}`);
  return data;
}

export async function cancelOrder(orderId: number): Promise<void> {
  await axios.patch(`/api/orders/${orderId}/cancel`);
}

export async function payOrder(
  orderId: number,
  payload: PayRequest
): Promise<PayResponse> {
  const { data } = await axios.post<PayResponse>(
    `/api/orders/${orderId}/payment`,
    payload
  );
  return data;
}

export async function prepareDelivery(
  orderId: number,
  shipAddr?: AddressDto | null
): Promise<DeliverySummaryResponse> {
  const { data } = await axios.post<DeliverySummaryResponse>(
    `/api/orders/${orderId}/delivery`,
    shipAddr ?? null
  );
  return data;
}

export async function startDelivery(
  orderId: number,
  payload: StartDeliveryRequest
): Promise<void> {
  await axios.patch(`/api/orders/${orderId}/delivery`, payload);
}

export async function completeDelivery(
  orderId: number,
  payload: CompleteDeliveryRequest
): Promise<void> {
  await axios.patch(`/api/orders/${orderId}/delivery/complete`, payload);
}
