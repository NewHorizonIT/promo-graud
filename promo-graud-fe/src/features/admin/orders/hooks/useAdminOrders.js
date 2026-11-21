import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getAdminOrders,
  getAdminOrder,
  cancelAdminOrder,
  updateStatusOrder,
  deleteOrder,
} from '../services/orderService';

export function useAdminOrders(params) {
  return useQuery({
    queryKey: ['admin', 'orders', 'list', params],
    queryFn: () => getAdminOrders(params),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  });
}

export function useAdminOrder(id) {
  return useQuery({
    queryKey: ['admin', 'orders', 'detail', id],
    queryFn: () => getAdminOrder(id),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
    enabled: !!id,
  });
}

export function useCancelOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: cancelAdminOrder,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders', 'detail', String(id)] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders', 'list'] });
    },
  });
}

export function useUpdateStatusOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateStatusOrder(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'orders'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'vouchers'] });
    },
  });
}

export function useDeleteOrder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteOrder(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'orders'] }),
  });
}
