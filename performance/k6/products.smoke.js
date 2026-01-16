import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const PATH = __ENV.PATH || "/api/products?page=0&size=20";

export const options = {
  scenarios: {
    smoke: { executor: "constant-vus", vus: 1, duration: "10s" },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<800"],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}${PATH}`);
  check(res, { "status is 200": (r) => r.status === 200 });
  sleep(1);
}