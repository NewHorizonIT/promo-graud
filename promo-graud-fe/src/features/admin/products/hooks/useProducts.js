import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createProduct,
  createTypeOfProduct,
  deleteProduct,
  getProduct,
  getProducts,
  getTypeProduct,
  getTypeProducts,
  updateProduct,
  updateTypeOfProduct,
} from '../services/productService';

export function useProducts(params = {}) {
  return useQuery({
    queryKey: ['admin', 'products', params],
    queryFn: () => getProducts(params),
  });
}

export function useProduct(id) {
  return useQuery({
    queryKey: ['admin', 'products', id],
    queryFn: () => getProduct(id),
    enabled: !!id, // Chỉ cho phép gọi API nếu id có giá trị
  });
}

export function useCreateProduct() {
  const qc = useQueryClient();
  // Dùng cho các tác vụ thay đổi dữ liệu (POST, PUT, DELETE, PATCH)
  return useMutation({
    mutationFn: (body) => createProduct(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'products'] }),
  });
}

export function useUpdateProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateProduct(id, body),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'products', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'products'] });
    },
  });
}

export function useDeleteProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteProduct(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'products'] }),
  });
}

export function useTypeProducts(params = {}) {
  return useQuery({
    queryKey: ['admin', 'product-types', params],
    queryFn: () => getTypeProducts(params),
  });
}

export function useTypeProduct() {
  return useQuery({
    queryKey: ['admin', 'product-types'],
    queryFn: getTypeProduct,
    staleTime: 5 * 60 * 1000,
    select: (res) => (Array.isArray(res) ? res : (res?.data ?? [])),
  });
}

export function useCreateTypeProduct() {
  const qc = useQueryClient();
  // Dùng cho các tác vụ thay đổi dữ liệu (POST, PUT, DELETE, PATCH)
  return useMutation({
    mutationFn: (body) => createTypeOfProduct(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'product-types'] }),
  });
}

export function useUpdateTypeProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateTypeOfProduct(id, body),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'product-types', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'product-types'] });
    },
  });
}
