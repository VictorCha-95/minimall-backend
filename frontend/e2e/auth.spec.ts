import { test, expect } from "@playwright/test";

test("로그인 -> 내 정보 확인 -> 로그아웃", async ({ page }) => {
  await page.route("**/api/auth/refresh", async (route) => {
    await route.fulfill({ status: 401, body: "Unauthorized" });
  });
  await page.route("**/api/auth/login", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      headers: {
        "Set-Cookie": "refreshToken=mock; Path=/api/auth; HttpOnly",
      },
      body: JSON.stringify({
        tokenType: "Bearer",
        accessToken: "mock-access",
        accessExpiresIn: 600,
        refreshExpiresIn: 1209600,
      }),
    });
  });
  await page.route("**/api/auth/me", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        id: 1,
        loginId: "customer",
        name: "고객",
        email: "customer@naver.com",
        role: "CUSTOMER",
        status: "ACTIVE",
        grade: "BRONZE",
        addr: {
          postcode: "12345",
          state: "Seoul",
          city: "Gangnam",
          street: "Teheran-ro",
          detail: "101",
        },
      }),
    });
  });
  await page.route("**/api/auth/logout", async (route) => {
    await route.fulfill({ status: 204, body: "" });
  });

  await page.goto("/");
  await page.getByRole("link", { name: "로그인" }).click();

  await page.getByLabel("아이디").fill("customer");
  await page.getByLabel("비밀번호").fill("pass1234!");
  await page.getByRole("button", { name: "로그인" }).click();

  await expect(page.getByRole("link", { name: "내 정보" })).toBeVisible();
  await page.getByRole("link", { name: "내 정보" }).click();
  await expect(page.getByRole("heading", { name: "내 정보" })).toBeVisible();
  await expect(page.getByText("customer")).toBeVisible();

  await page.getByRole("button", { name: "로그아웃" }).click();
  await expect(page.getByRole("link", { name: "로그인" })).toBeVisible();
});
