import React, { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AuthMeResponse } from "../services/authApi";
import {
  ProductPageResponse,
  addProductStock,
  changeProductName,
  changeProductPrice,
  clearProductStock,
  deleteProduct,
  listProducts,
  registerProduct,
  reduceProductStock,
} from "../services/productApi";
import {
  AddressDto,
  DeliverySummaryResponse,
  OrderCreateResponse,
  OrderDetailResponse,
  PayMethod,
  PayResponse,
  cancelOrder,
  completeDelivery,
  createOrder,
  getOrder,
  payOrder,
  prepareDelivery,
  startDelivery,
} from "../services/orderApi";
import {
  MemberDetailResponse,
  MemberSummaryResponse,
  OrderSummaryResponse,
  getMemberDetail,
  getMemberOrders,
  getMemberSummaryByEmail,
  getMemberSummaryByLoginId,
  getMembers,
} from "../services/memberApi";

type WorkspacePageProps = {
  me: AuthMeResponse | null;
};

const WorkspacePage: React.FC<WorkspacePageProps> = ({ me }) => {
  const [productPage, setProductPage] = useState<ProductPageResponse | null>(
    null
  );
  const [productPageError, setProductPageError] = useState<string | null>(null);
  const [productPageLoading, setProductPageLoading] = useState(false);
  const [page, setPage] = useState("0");
  const [size, setSize] = useState("6");
  const [newProductName, setNewProductName] = useState("");
  const [newProductPrice, setNewProductPrice] = useState("");
  const [newProductStock, setNewProductStock] = useState("");
  const [productActionId, setProductActionId] = useState("");
  const [productStockQty, setProductStockQty] = useState("");
  const [productNameValue, setProductNameValue] = useState("");
  const [productPriceValue, setProductPriceValue] = useState("");
  const [productActionMessage, setProductActionMessage] = useState<string | null>(
    null
  );

  const [orderMemberId, setOrderMemberId] = useState(
    me?.id ? String(me.id) : ""
  );
  const [orderItems, setOrderItems] = useState<
    { productId: string; quantity: string }[]
  >([{ productId: "", quantity: "" }]);
  const [orderCreateResult, setOrderCreateResult] =
    useState<OrderCreateResponse | null>(null);
  const [orderCreateError, setOrderCreateError] = useState<string | null>(null);
  const [orderLookupId, setOrderLookupId] = useState("");
  const [orderDetail, setOrderDetail] = useState<OrderDetailResponse | null>(
    null
  );
  const [orderLookupError, setOrderLookupError] = useState<string | null>(null);
  const [orderCancelId, setOrderCancelId] = useState("");
  const [orderActionMessage, setOrderActionMessage] = useState<string | null>(
    null
  );
  const [paymentOrderId, setPaymentOrderId] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PayMethod>("CARD");
  const [paymentAmount, setPaymentAmount] = useState("");
  const [paymentResult, setPaymentResult] = useState<PayResponse | null>(null);
  const [deliveryOrderId, setDeliveryOrderId] = useState("");
  const [deliveryTracking, setDeliveryTracking] = useState("");
  const [deliveryShippedAt, setDeliveryShippedAt] = useState("");
  const [deliveryArrivedAt, setDeliveryArrivedAt] = useState("");
  const [deliveryAddress, setDeliveryAddress] = useState<AddressDto>({
    postcode: "",
    state: "",
    city: "",
    street: "",
    detail: "",
  });
  const [deliverySummary, setDeliverySummary] =
    useState<DeliverySummaryResponse | null>(null);

  const [members, setMembers] = useState<MemberSummaryResponse[] | null>(null);
  const [membersLoading, setMembersLoading] = useState(false);
  const [membersError, setMembersError] = useState<string | null>(null);
  const [memberLookupId, setMemberLookupId] = useState("");
  const [memberLookupEmail, setMemberLookupEmail] = useState("");
  const [memberLookupLoginId, setMemberLookupLoginId] = useState("");
  const [memberDetail, setMemberDetail] = useState<MemberDetailResponse | null>(
    null
  );
  const [memberLookupError, setMemberLookupError] = useState<string | null>(
    null
  );
  const [memberOrdersId, setMemberOrdersId] = useState("");
  const [memberOrders, setMemberOrders] = useState<OrderSummaryResponse[] | null>(
    null
  );

  const isSeller = me?.role === "SELLER";

  const loadProducts = async () => {
    setProductPageError(null);
    setProductPageLoading(true);
    setProductPage(null);
    try {
      const data = await listProducts(
        Number.parseInt(page, 10) || 0,
        Number.parseInt(size, 10) || 6
      );
      setProductPage(data);
    } catch {
      setProductPageError("상품 목록을 불러오지 못했습니다.");
    } finally {
      setProductPageLoading(false);
    }
  };

  const handleRegisterProduct = async (event: React.FormEvent) => {
    event.preventDefault();
    setProductActionMessage(null);
    try {
      await registerProduct({
        name: newProductName,
        price: Number.parseInt(newProductPrice, 10),
        stockQuantity: Number.parseInt(newProductStock, 10),
      });
      setProductActionMessage("상품이 등록되었습니다.");
      setNewProductName("");
      setNewProductPrice("");
      setNewProductStock("");
      await loadProducts();
    } catch {
      setProductActionMessage("상품 등록에 실패했습니다.");
    }
  };

  const handleStockAction = async (mode: "add" | "reduce" | "clear") => {
    setProductActionMessage(null);
    const id = Number.parseInt(productActionId, 10);
    if (!id) {
      setProductActionMessage("상품 ID를 입력해주세요.");
      return;
    }
    try {
      if (mode === "clear") {
        await clearProductStock(id);
      } else {
        const qty = Number.parseInt(productStockQty, 10);
        if (!qty) {
          setProductActionMessage("수량을 입력해주세요.");
          return;
        }
        if (mode === "add") {
          await addProductStock(id, qty);
        } else {
          await reduceProductStock(id, qty);
        }
      }
      setProductActionMessage("재고 요청이 반영되었습니다.");
      await loadProducts();
    } catch {
      setProductActionMessage("재고 변경에 실패했습니다.");
    }
  };

  const handleProductMeta = async (mode: "name" | "price") => {
    setProductActionMessage(null);
    const id = Number.parseInt(productActionId, 10);
    if (!id) {
      setProductActionMessage("상품 ID를 입력해주세요.");
      return;
    }
    try {
      if (mode === "name") {
        await changeProductName(id, productNameValue);
      } else {
        const price = Number.parseInt(productPriceValue, 10);
        if (!price) {
          setProductActionMessage("가격을 입력해주세요.");
          return;
        }
        await changeProductPrice(id, price);
      }
      setProductActionMessage("상품 정보가 변경되었습니다.");
      await loadProducts();
    } catch {
      setProductActionMessage("상품 정보 변경에 실패했습니다.");
    }
  };

  const handleProductDelete = async () => {
    setProductActionMessage(null);
    const id = Number.parseInt(productActionId, 10);
    if (!id) {
      setProductActionMessage("상품 ID를 입력해주세요.");
      return;
    }
    try {
      await deleteProduct(id);
      setProductActionMessage("상품이 삭제되었습니다.");
      await loadProducts();
    } catch {
      setProductActionMessage("상품 삭제에 실패했습니다.");
    }
  };

  const handleAddOrderItem = () => {
    setOrderItems((prev) => [...prev, { productId: "", quantity: "" }]);
  };

  const handleRemoveOrderItem = (index: number) => {
    setOrderItems((prev) => prev.filter((_, idx) => idx !== index));
  };

  const handleOrderItemChange = (
    index: number,
    field: "productId" | "quantity",
    value: string
  ) => {
    setOrderItems((prev) =>
      prev.map((item, idx) =>
        idx === index ? { ...item, [field]: value } : item
      )
    );
  };

  const handleCreateOrder = async (event: React.FormEvent) => {
    event.preventDefault();
    setOrderCreateError(null);
    setOrderCreateResult(null);
    const memberId = Number.parseInt(orderMemberId, 10);
    if (!memberId) {
      setOrderCreateError("회원 ID를 입력해주세요.");
      return;
    }
    const items = orderItems
      .filter((item) => item.productId && item.quantity)
      .map((item) => ({
        productId: Number.parseInt(item.productId, 10),
        quantity: Number.parseInt(item.quantity, 10),
      }))
      .filter((item) => item.productId && item.quantity);
    if (!items.length) {
      setOrderCreateError("상품 ID/수량을 1개 이상 입력해주세요.");
      return;
    }
    try {
      const result = await createOrder({ memberId, items });
      setOrderCreateResult(result);
      setOrderItems([{ productId: "", quantity: "" }]);
    } catch {
      setOrderCreateError("주문 생성에 실패했습니다.");
    }
  };

  const handleLookupOrder = async () => {
    setOrderLookupError(null);
    setOrderDetail(null);
    const orderId = Number.parseInt(orderLookupId, 10);
    if (!orderId) {
      setOrderLookupError("주문 ID를 입력해주세요.");
      return;
    }
    try {
      const result = await getOrder(orderId);
      setOrderDetail(result);
    } catch {
      setOrderLookupError("주문 정보를 불러오지 못했습니다.");
    }
  };

  const handleCancelOrder = async () => {
    setOrderActionMessage(null);
    const orderId = Number.parseInt(orderCancelId, 10);
    if (!orderId) {
      setOrderActionMessage("취소할 주문 ID를 입력해주세요.");
      return;
    }
    try {
      await cancelOrder(orderId);
      setOrderActionMessage("주문이 취소되었습니다.");
    } catch {
      setOrderActionMessage("주문 취소에 실패했습니다.");
    }
  };

  const handlePayOrder = async () => {
    setOrderActionMessage(null);
    setPaymentResult(null);
    const orderId = Number.parseInt(paymentOrderId, 10);
    const amount = Number.parseInt(paymentAmount, 10);
    if (!orderId || !amount) {
      setOrderActionMessage("주문 ID와 결제 금액을 입력해주세요.");
      return;
    }
    try {
      const result = await payOrder(orderId, {
        payMethod: paymentMethod,
        payAmount: amount,
      });
      setPaymentResult(result);
      setOrderActionMessage("결제 요청이 처리되었습니다.");
    } catch {
      setOrderActionMessage("결제 처리에 실패했습니다.");
    }
  };

  const handlePrepareDelivery = async () => {
    setOrderActionMessage(null);
    const orderId = Number.parseInt(deliveryOrderId, 10);
    if (!orderId) {
      setOrderActionMessage("주문 ID를 입력해주세요.");
      return;
    }
    const hasAnyAddress =
      deliveryAddress.postcode ||
      deliveryAddress.state ||
      deliveryAddress.city ||
      deliveryAddress.street ||
      deliveryAddress.detail;
    const hasRequiredAddress =
      deliveryAddress.postcode &&
      deliveryAddress.state &&
      deliveryAddress.city &&
      deliveryAddress.street;
    if (hasAnyAddress && !hasRequiredAddress) {
      setOrderActionMessage("배송 주소를 입력하려면 필수 주소를 모두 채워주세요.");
      return;
    }
    try {
      const result = await prepareDelivery(
        orderId,
        hasRequiredAddress ? deliveryAddress : null
      );
      setDeliverySummary(result);
      setOrderActionMessage("배송 준비가 완료되었습니다.");
    } catch {
      setOrderActionMessage("배송 준비에 실패했습니다.");
    }
  };

  const handleStartDelivery = async () => {
    setOrderActionMessage(null);
    const orderId = Number.parseInt(deliveryOrderId, 10);
    if (!orderId || !deliveryTracking) {
      setOrderActionMessage("주문 ID와 송장번호를 입력해주세요.");
      return;
    }
    try {
      await startDelivery(orderId, {
        trackingNo: deliveryTracking,
        shippedAt: deliveryShippedAt || null,
      });
      setOrderActionMessage("배송이 시작되었습니다.");
    } catch {
      setOrderActionMessage("배송 시작에 실패했습니다.");
    }
  };

  const handleCompleteDelivery = async () => {
    setOrderActionMessage(null);
    const orderId = Number.parseInt(deliveryOrderId, 10);
    if (!orderId) {
      setOrderActionMessage("주문 ID를 입력해주세요.");
      return;
    }
    try {
      await completeDelivery(orderId, {
        arrivedAt: deliveryArrivedAt || null,
      });
      setOrderActionMessage("배송 완료 처리가 완료되었습니다.");
    } catch {
      setOrderActionMessage("배송 완료 처리에 실패했습니다.");
    }
  };

  const handleLoadMembers = async () => {
    setMembersLoading(true);
    setMembersError(null);
    try {
      const data = await getMembers();
      setMembers(data);
    } catch {
      setMembersError("회원 목록을 불러오지 못했습니다.");
    } finally {
      setMembersLoading(false);
    }
  };

  const handleLookupMember = async (mode: "id" | "email" | "loginId") => {
    setMemberLookupError(null);
    setMemberDetail(null);
    try {
      if (mode === "id") {
        const id = Number.parseInt(memberLookupId, 10);
        if (!id) {
          setMemberLookupError("회원 ID를 입력해주세요.");
          return;
        }
        setMemberDetail(await getMemberDetail(id));
      } else if (mode === "email") {
        if (!memberLookupEmail) {
          setMemberLookupError("이메일을 입력해주세요.");
          return;
        }
        const summary = await getMemberSummaryByEmail(memberLookupEmail);
        setMemberDetail({
          id: summary.id,
          loginId: summary.loginId,
          name: summary.name,
          email: memberLookupEmail,
        });
      } else {
        if (!memberLookupLoginId) {
          setMemberLookupError("로그인 ID를 입력해주세요.");
          return;
        }
        const summary = await getMemberSummaryByLoginId(memberLookupLoginId);
        setMemberDetail({
          id: summary.id,
          loginId: summary.loginId,
          name: summary.name,
          email: "",
        });
      }
    } catch {
      setMemberLookupError("회원 정보를 찾지 못했습니다.");
    }
  };

  const handleLoadMemberOrders = async () => {
    setMemberOrders(null);
    const memberId = Number.parseInt(memberOrdersId, 10);
    if (!memberId) {
      setMemberLookupError("주문을 조회할 회원 ID를 입력해주세요.");
      return;
    }
    try {
      const data = await getMemberOrders(memberId);
      setMemberOrders(data);
    } catch {
      setMemberLookupError("회원 주문 목록을 불러오지 못했습니다.");
    }
  };

  const orderStatusTone = useMemo(() => {
    if (!orderDetail) return "neutral";
    if (orderDetail.orderStatus === "CANCELED") return "danger";
    if (orderDetail.orderStatus === "COMPLETED") return "success";
    if (orderDetail.orderStatus === "CONFIRMED") return "accent";
    return "warning";
  }, [orderDetail]);

  const formatMoney = (value: number) =>
    new Intl.NumberFormat("ko-KR").format(value);

  return (
    <section className="workspace">
      <header className="hero-panel">
        <p className="eyebrow">Operations</p>
        <h1>MiniMall 운영 콘솔</h1>
        <p className="hero-sub">
          상품, 주문, 회원 흐름을 한 화면에서 확인하고 테스트하세요.
        </p>
        <div className="hero-actions">
          <button type="button" className="btn-ghost" onClick={loadProducts}>
            상품 목록 불러오기
          </button>
          <Link to="/" className="btn-outline">
            홈으로 돌아가기
          </Link>
        </div>
      </header>

      <div className="panel-grid">
        <article className="panel">
          <div className="panel-header">
            <div>
              <h2>상품 카탈로그</h2>
              <p>공개 상품 목록과 판매자용 상품 관리</p>
            </div>
            <span className={`role-chip ${isSeller ? "seller" : "viewer"}`}>
              {isSeller ? "SELLER" : "VIEW ONLY"}
            </span>
          </div>

          <div className="form-row">
            <label>
              페이지
              <input value={page} onChange={(e) => setPage(e.target.value)} />
            </label>
            <label>
              사이즈
              <input value={size} onChange={(e) => setSize(e.target.value)} />
            </label>
            <button type="button" onClick={loadProducts}>
              상품 불러오기
            </button>
          </div>

          {productPageLoading && <p className="note">상품 불러오는 중...</p>}
          {productPageError && <p className="error-text">{productPageError}</p>}

          {productPage && (
            <div className="tile-grid">
              {productPage.items.map((item) => (
                <div className="tile" key={item.productId}>
                  <div className="tile-title">{item.productName}</div>
                  <div className="tile-meta">상품 ID #{item.productId}</div>
                  <div className="tile-stats">
                    <span>가격 {formatMoney(item.productPrice)}원</span>
                    <span>재고 {item.stockQuantity}개</span>
                  </div>
                  <div className="tile-sub">
                    생성 {item.createdAt.replace("T", " ")}
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="divider" />

          <form className="panel-form" onSubmit={handleRegisterProduct}>
            <h3>상품 등록</h3>
            <div className="form-grid">
              <label>
                상품명
                <input
                  value={newProductName}
                  onChange={(e) => setNewProductName(e.target.value)}
                  disabled={!isSeller}
                  required
                />
              </label>
              <label>
                가격
                <input
                  value={newProductPrice}
                  onChange={(e) => setNewProductPrice(e.target.value)}
                  disabled={!isSeller}
                  required
                />
              </label>
              <label>
                초기 재고
                <input
                  value={newProductStock}
                  onChange={(e) => setNewProductStock(e.target.value)}
                  disabled={!isSeller}
                  required
                />
              </label>
              <button type="submit" disabled={!isSeller}>
                등록하기
              </button>
            </div>
          </form>

          <div className="panel-form">
            <h3>상품 관리</h3>
            <div className="form-grid">
              <label>
                상품 ID
                <input
                  value={productActionId}
                  onChange={(e) => setProductActionId(e.target.value)}
                  disabled={!isSeller}
                />
              </label>
              <label>
                수량
                <input
                  value={productStockQty}
                  onChange={(e) => setProductStockQty(e.target.value)}
                  disabled={!isSeller}
                />
              </label>
              <div className="button-row">
                <button
                  type="button"
                  onClick={() => handleStockAction("add")}
                  disabled={!isSeller}
                >
                  재고 추가
                </button>
                <button
                  type="button"
                  onClick={() => handleStockAction("reduce")}
                  disabled={!isSeller}
                  className="btn-warn"
                >
                  재고 감소
                </button>
                <button
                  type="button"
                  onClick={() => handleStockAction("clear")}
                  disabled={!isSeller}
                  className="btn-ghost"
                >
                  재고 초기화
                </button>
              </div>
            </div>
            <div className="form-grid">
              <label>
                상품명 변경
                <input
                  value={productNameValue}
                  onChange={(e) => setProductNameValue(e.target.value)}
                  disabled={!isSeller}
                />
              </label>
              <button
                type="button"
                onClick={() => handleProductMeta("name")}
                disabled={!isSeller}
              >
                이름 변경
              </button>
              <label>
                가격 변경
                <input
                  value={productPriceValue}
                  onChange={(e) => setProductPriceValue(e.target.value)}
                  disabled={!isSeller}
                />
              </label>
              <button
                type="button"
                onClick={() => handleProductMeta("price")}
                disabled={!isSeller}
              >
                가격 변경
              </button>
              <button
                type="button"
                onClick={handleProductDelete}
                disabled={!isSeller}
                className="btn-danger"
              >
                상품 삭제
              </button>
            </div>
            {productActionMessage && (
              <p className="note">{productActionMessage}</p>
            )}
          </div>
        </article>

        <article className="panel">
          <div className="panel-header">
            <div>
              <h2>주문/배송 흐름</h2>
              <p>주문 생성부터 배송 완료까지 시나리오를 점검합니다.</p>
            </div>
            <span className="role-chip accent">AUTH REQUIRED</span>
          </div>

          <form className="panel-form" onSubmit={handleCreateOrder}>
            <h3>주문 생성</h3>
            <label>
              회원 ID
              <input
                value={orderMemberId}
                onChange={(e) => setOrderMemberId(e.target.value)}
                required
              />
            </label>
            <div className="order-items">
              {orderItems.map((item, index) => (
                <div className="order-item-row" key={`${index}`}>
                  <input
                    placeholder="상품 ID"
                    value={item.productId}
                    onChange={(e) =>
                      handleOrderItemChange(index, "productId", e.target.value)
                    }
                  />
                  <input
                    placeholder="수량"
                    value={item.quantity}
                    onChange={(e) =>
                      handleOrderItemChange(index, "quantity", e.target.value)
                    }
                  />
                  {orderItems.length > 1 && (
                    <button
                      type="button"
                      className="btn-ghost"
                      onClick={() => handleRemoveOrderItem(index)}
                    >
                      제거
                    </button>
                  )}
                </div>
              ))}
            </div>
            <div className="button-row">
              <button type="button" onClick={handleAddOrderItem}>
                상품 추가
              </button>
              <button type="submit">주문 생성</button>
            </div>
            {orderCreateError && <p className="error-text">{orderCreateError}</p>}
            {orderCreateResult && (
              <div className="callout">
                주문 #{orderCreateResult.id} 생성 완료 · 총액{" "}
                {formatMoney(orderCreateResult.finalAmount)}원
              </div>
            )}
          </form>

          <div className="panel-form">
            <h3>주문 조회</h3>
            <div className="form-row">
              <input
                placeholder="주문 ID"
                value={orderLookupId}
                onChange={(e) => setOrderLookupId(e.target.value)}
              />
              <button type="button" onClick={handleLookupOrder}>
                조회
              </button>
            </div>
            {orderLookupError && <p className="error-text">{orderLookupError}</p>}
            {orderDetail && (
              <div className={`order-card tone-${orderStatusTone}`}>
                <div className="order-card-header">
                  <span>주문 #{orderDetail.id}</span>
                  <span className="badge">{orderDetail.orderStatus}</span>
                </div>
                <div className="order-card-body">
                  <div>
                    최종 금액 {formatMoney(orderDetail.finalAmount)}원
                  </div>
                  <div className="order-items-list">
                    {orderDetail.orderItems.map((item) => (
                      <div className="order-item" key={item.productId}>
                        {item.productName} · {item.orderQuantity}개 ·{" "}
                        {formatMoney(item.totalAmount)}원
                      </div>
                    ))}
                  </div>
                  {orderDetail.pay && (
                    <div className="note">
                      결제 {orderDetail.pay.payMethod} ·{" "}
                      {orderDetail.pay.payStatus}
                    </div>
                  )}
                  {orderDetail.delivery && (
                    <div className="note">
                      배송 {orderDetail.delivery.deliveryStatus}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="panel-form">
            <h3>주문 상태 변경</h3>
            <div className="form-grid">
              <label>
                주문 ID
                <input
                  value={orderCancelId}
                  onChange={(e) => setOrderCancelId(e.target.value)}
                />
              </label>
              <button type="button" onClick={handleCancelOrder}>
                주문 취소
              </button>
              <label>
                결제 주문 ID
                <input
                  value={paymentOrderId}
                  onChange={(e) => setPaymentOrderId(e.target.value)}
                />
              </label>
              <label>
                결제 방식
                <select
                  value={paymentMethod}
                  onChange={(e) =>
                    setPaymentMethod(e.target.value as PayMethod)
                  }
                >
                  <option value="CARD">CARD</option>
                  <option value="BANK_TRANSFER">BANK_TRANSFER</option>
                  <option value="CASH">CASH</option>
                  <option value="PAYPAL">PAYPAL</option>
                  <option value="MOBILE_PAY">MOBILE_PAY</option>
                </select>
              </label>
              <label>
                결제 금액
                <input
                  value={paymentAmount}
                  onChange={(e) => setPaymentAmount(e.target.value)}
                />
              </label>
              <button type="button" onClick={handlePayOrder}>
                결제 처리
              </button>
            </div>
            {paymentResult && (
              <p className="note">
                결제 상태 {paymentResult.payStatus} ·{" "}
                {formatMoney(paymentResult.payAmount)}원
              </p>
            )}
          </div>

          <div className="panel-form">
            <h3>배송 단계</h3>
            <div className="form-grid">
              <label>
                주문 ID
                <input
                  value={deliveryOrderId}
                  onChange={(e) => setDeliveryOrderId(e.target.value)}
                />
              </label>
              <label>
                송장번호
                <input
                  value={deliveryTracking}
                  onChange={(e) => setDeliveryTracking(e.target.value)}
                />
              </label>
              <label>
                출고 시간(선택)
                <input
                  placeholder="2026-01-01T12:00:00"
                  value={deliveryShippedAt}
                  onChange={(e) => setDeliveryShippedAt(e.target.value)}
                />
              </label>
              <label>
                도착 시간(선택)
                <input
                  placeholder="2026-01-02T10:00:00"
                  value={deliveryArrivedAt}
                  onChange={(e) => setDeliveryArrivedAt(e.target.value)}
                />
              </label>
            </div>
            <div className="form-grid">
              <label>
                우편번호
                <input
                  value={deliveryAddress.postcode}
                  onChange={(e) =>
                    setDeliveryAddress((prev) => ({
                      ...prev,
                      postcode: e.target.value,
                    }))
                  }
                />
              </label>
              <label>
                시/도
                <input
                  value={deliveryAddress.state}
                  onChange={(e) =>
                    setDeliveryAddress((prev) => ({
                      ...prev,
                      state: e.target.value,
                    }))
                  }
                />
              </label>
              <label>
                시/군/구
                <input
                  value={deliveryAddress.city}
                  onChange={(e) =>
                    setDeliveryAddress((prev) => ({
                      ...prev,
                      city: e.target.value,
                    }))
                  }
                />
              </label>
              <label>
                도로명 주소
                <input
                  value={deliveryAddress.street}
                  onChange={(e) =>
                    setDeliveryAddress((prev) => ({
                      ...prev,
                      street: e.target.value,
                    }))
                  }
                />
              </label>
              <label>
                상세 주소
                <input
                  value={deliveryAddress.detail ?? ""}
                  onChange={(e) =>
                    setDeliveryAddress((prev) => ({
                      ...prev,
                      detail: e.target.value,
                    }))
                  }
                />
              </label>
              <div className="button-row">
                <button type="button" onClick={handlePrepareDelivery}>
                  배송 준비
                </button>
                <button type="button" onClick={handleStartDelivery}>
                  배송 시작
                </button>
                <button type="button" onClick={handleCompleteDelivery}>
                  배송 완료
                </button>
              </div>
            </div>
            {deliverySummary && (
              <p className="note">
                배송 상태 {deliverySummary.deliveryStatus}
              </p>
            )}
            {orderActionMessage && <p className="note">{orderActionMessage}</p>}
          </div>
        </article>

        <article className="panel">
          <div className="panel-header">
            <div>
              <h2>회원 허브</h2>
              <p>회원 목록과 주문 요약을 빠르게 확인합니다.</p>
            </div>
            <span className="role-chip">MEMBERS</span>
          </div>

          <div className="button-row">
            <button type="button" onClick={handleLoadMembers}>
              전체 회원 불러오기
            </button>
            {membersLoading && <span className="note">불러오는 중...</span>}
          </div>
          {membersError && <p className="error-text">{membersError}</p>}
          {members && (
            <div className="pill-grid">
              {members.map((member) => (
                <div className="pill" key={member.id}>
                  #{member.id} {member.name} ({member.loginId})
                </div>
              ))}
            </div>
          )}

          <div className="divider" />

          <div className="panel-form">
            <h3>회원 조회</h3>
            <div className="form-grid">
              <label>
                회원 ID
                <input
                  value={memberLookupId}
                  onChange={(e) => setMemberLookupId(e.target.value)}
                />
              </label>
              <button type="button" onClick={() => handleLookupMember("id")}>
                ID 조회
              </button>
              <label>
                이메일
                <input
                  value={memberLookupEmail}
                  onChange={(e) => setMemberLookupEmail(e.target.value)}
                />
              </label>
              <button type="button" onClick={() => handleLookupMember("email")}>
                이메일 조회
              </button>
              <label>
                로그인 ID
                <input
                  value={memberLookupLoginId}
                  onChange={(e) => setMemberLookupLoginId(e.target.value)}
                />
              </label>
              <button
                type="button"
                onClick={() => handleLookupMember("loginId")}
              >
                로그인 ID 조회
              </button>
            </div>
            {memberLookupError && (
              <p className="error-text">{memberLookupError}</p>
            )}
            {memberDetail && (
              <div className="callout">
                <div>회원 #{memberDetail.id}</div>
                <div>
                  {memberDetail.name} ({memberDetail.loginId})
                </div>
                <div>{memberDetail.email || "이메일 미상"}</div>
              </div>
            )}
          </div>

          <div className="panel-form">
            <h3>회원 주문 요약</h3>
            <div className="form-row">
              <input
                placeholder="회원 ID"
                value={memberOrdersId}
                onChange={(e) => setMemberOrdersId(e.target.value)}
              />
              <button type="button" onClick={handleLoadMemberOrders}>
                주문 조회
              </button>
            </div>
            {memberOrders && (
              <div className="order-summary-grid">
                {memberOrders.map((order) => (
                  <div className="order-summary" key={order.id}>
                    <span>#{order.id}</span>
                    <span>{order.orderStatus}</span>
                    <span>{formatMoney(order.finalAmount)}원</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </article>
      </div>
    </section>
  );
};

export default WorkspacePage;
