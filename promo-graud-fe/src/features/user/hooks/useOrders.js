import { useQuery } from '@tanstack/react-query';

import { useAuthStore } from '../../../store/authStore';
import { getMyOrders } from '../services/orderService';

export function useMyOrders({ page = 0, limit = 10 } = {}) {
  const token = useAuthStore((s) => s.token);
  return useQuery({
    queryKey: ['orders', 'me', page, limit],
    queryFn: () => getMyOrders({ page, limit }),
    enabled: !!token,
  });
}
