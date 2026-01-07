import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 200,
  duration: "10s",
  summaryTrendStats: ["avg", "p(95)", "p(99)", "min", "max"],
};

const BASE_URL = "http://host.docker.internal:8080";

export default function () {
  const url = `${BASE_URL}/api/orders`; // TODO: set order endpoint
  const payload = JSON.stringify({
    // TODO: set valid order payload
  });
  const params = {
    headers: { "Content-Type": "application/json" },
  };

  const res = http.post(url, payload, params);
  check(res, {
    "status is 2xx": (r) => r.status >= 200 && r.status < 300,
  });
  sleep(1);
}
