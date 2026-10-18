import { API_BASE } from '../../../config/constants';

export async function fetchProducts({
  limit = 16,
  offset = 0,
  typeId,
  minPrice,
  maxPrice,
  name,
  search,
} = {}) {
  const params = new URLSearchParams({
    limit: String(limit),
    offset: String(offset),
  });

  if (typeId !== undefined && typeId !== null && typeId !== '') {
    params.set('typeId', typeId);
  }
  if (minPrice !== undefined && minPrice !== null && minPrice !== '') {
    params.set('minPrice', minPrice);
  }
  if (maxPrice !== undefined && maxPrice !== null && maxPrice !== '') {
    params.set('maxPrice', maxPrice);
  }

  // Sử dụng tham số `name` để tìm kiếm sản phẩm theo tên (hỗ trợ cả alias `search` nếu có)
  const searchKeyword = (name ?? search ?? '').trim();
  if (searchKeyword !== '') {
    params.set('name', searchKeyword);
  }

  const res = await fetch(`${API_BASE}/products?${params}`);
  if (!res.ok) {
    throw new Error(`Failed to fetch products: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchProductTypes() {
  const res = await fetch(`${API_BASE}/products/type`);
  if (!res.ok) {
    throw new Error(`Failed to fetch product types: ${res.statusText}`);
  }
  return res.json();
}
