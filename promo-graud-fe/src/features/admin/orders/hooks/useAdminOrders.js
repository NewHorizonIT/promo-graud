import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAdminOrders, getAdminOrder, cancelAdminOrder } from '../services/orderService';

export function useAdminOrders(params) {
  return useQuery({
    queryKey: ['admin', 'orders', 'list', params],
    queryFn: () => getAdminOrders(params),
  });
}

export function useAdminOrder(id) {
  return useQuery({
    queryKey: ['admin', 'orders', 'detail', id],
    queryFn: () => getAdminOrder(id),
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
