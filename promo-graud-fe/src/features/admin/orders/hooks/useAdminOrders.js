import { useQuery } from '@tanstack/react-query';
import { getAdminOrders, getAdminOrder } from '../services/orderService';

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
