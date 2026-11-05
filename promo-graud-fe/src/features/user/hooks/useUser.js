import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMe, getUserById, updateUser } from '../services/userService';
import { useAuthStore } from '../../../store/authStore';

export function useMe() {
  const token = useAuthStore((s) => s.token);
  return useQuery({
    queryKey: ['user', 'me'],
    queryFn: getMe,
    enabled: !!token,
  });
}

export function useUser(id) {
  return useQuery({
    queryKey: ['user', id],
    queryFn: () => getUserById(id),
    enabled: !!id,
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => updateUser(id, payload),
    onSuccess: (updated) => {
      queryClient.setQueryData(['user', 'me'], updated);
      queryClient.invalidateQueries({ queryKey: ['user'] });
    },
  });
}
