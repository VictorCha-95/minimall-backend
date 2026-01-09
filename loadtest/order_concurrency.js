import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

export const app_failed = new Rate("app_failed"); // 우리 기준 실패율

export const options = {
  vus: __ENV.VUS ? Number(__ENV.VUS) : 200,
  duration: __ENV.DURATION || "10s",
  thresholds: {
    http_req_duration: ["p(95)<800", "p(99)<1500"],
    app_failed: ["rate<0.02"], // 우리 기준 실패율 2% 미만
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const ORDER_PATH = __ENV.ORDER_PATH || "/api/orders";
const MEMBER_ID = __ENV.MEMBER_ID ? Number(__ENV.MEMBER_ID) : 1;
const PRODUCT_ID = __ENV.PRODUCT_ID ? Number(__ENV.PRODUCT_ID) : 1;
const QTY = __ENV.QTY ? Number(__ENV.QTY) : 1;
const TOKEN = __ENV.TOKEN || "";

export default function () {
  const url = `${BASE_URL}${ORDER_PATH}`;
  const payload = JSON.stringify({
    memberId: MEMBER_ID,
    items: [{ productId: PRODUCT_ID, quantity: QTY }],
  });

  const headers = { "Content-Type": "application/json" };
  if (TOKEN) headers["Authorization"] = `Bearer ${TOKEN}`;

  const res = http.post(url, payload, { headers });

  const ok = (res.status === 201) || (res.status === 409); // 409를 정상 흐름으로 인정(현재 단계)
  app_failed.add(!ok);

  check(res, {
    "status is 201 or 409": () => ok,
  });

  sleep(0.05);
}