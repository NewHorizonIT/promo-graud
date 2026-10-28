import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createTypeOfUser, getTypeOfUser, updateTypeOfUser } from '../services/userTypeService';

export function useTypeUser() {
  return useQuery({
    queryKey: ['admin', 'user-types'],
    queryFn: () => getTypeOfUser(),
  });
}

export function useCreateTypeUser() {
  const qc = useQueryClient();
  // Dùng cho các tác vụ thay đổi dữ liệu (POST, PUT, DELETE, PATCH)
  return useMutation({
    mutationFn: (body) => createTypeOfUser(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'user-types'] }),
  });
}

export function useUpdateTypeUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateTypeOfUser(id, body),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'user-types', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'user-types'] });
      qc.invalidateQueries({ queryKey: ['admin', 'users'] });
    },
  });
}
