import { useQuery } from '@tanstack/react-query';

import { useAuthStore } from '../../../store/authStore';
import { getTypeOfUsers } from '../services/typeOfUserService';

export function useTypeOfUsers() {
  const token = useAuthStore((s) => s.token);
  return useQuery({
    queryKey: ['type-of-users'],
    queryFn: getTypeOfUsers,
    enabled: !!token,
    staleTime: 1000 * 60 * 30,
  });
}
