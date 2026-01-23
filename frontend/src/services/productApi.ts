import axios from "axios";

export interface ProductListItemResponse {
  productId: number;
  productName: string;
  productPrice: number;
  stockQuantity: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProductSliceResponse {
  items: ProductListItemResponse[];
  page: number;
  size: number;
  hasNext: boolean;
}

export interface ProductRegisterRequest {
  name: string;
  price: number;
  stockQuantity: number;
}

export async function listProducts(
  page: number,
  size: number,
  name?: string
): Promise<ProductSliceResponse> {
  const { data } = await axios.get<ProductSliceResponse>("/api/products", {
    params: { page, size, name: name || undefined },
  });
  return data;
}

export async function registerProduct(
  payload: ProductRegisterRequest
): Promise<void> {
  await axios.post("/api/products", payload);
}

export async function addProductStock(
  productId: number,
  requestedQuantity: number
): Promise<void> {
  await axios.post(`/api/products/${productId}/stock/add`, null, {
    params: { requestedQuantity },
  });
}

export async function reduceProductStock(
  productId: number,
  requestedQuantity: number
): Promise<void> {
  await axios.post(`/api/products/${productId}/stock/reduce`, null, {
    params: { requestedQuantity },
  });
}

export async function clearProductStock(productId: number): Promise<void> {
  await axios.post(`/api/products/${productId}/stock/clear`);
}

export async function changeProductName(
  productId: number,
  name: string
): Promise<void> {
  await axios.patch(`/api/products/${productId}/name`, null, {
    params: { name },
  });
}

export async function changeProductPrice(
  productId: number,
  price: number
): Promise<void> {
  await axios.patch(`/api/products/${productId}/price`, null, {
    params: { price },
  });
}

export async function deleteProduct(productId: number): Promise<void> {
  await axios.delete(`/api/products/${productId}`);
}
