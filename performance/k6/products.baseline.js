import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const PATH = __ENV.PATH || "/api/products?page=0&size=20";

// 필요하면 여기만 바꿔서 재사용
const VUS = parseInt(__ENV.VUS || "20", 10);
const DURATION = __ENV.DURATION || "60s";

export const options = {
  scenarios: {
    baseline: { executor: "constant-vus", vus: VUS, duration: DURATION },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500"],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}${PATH}`);
  check(res, { "status is 200": (r) => r.status === 200 });
  sleep(0.1);
}
