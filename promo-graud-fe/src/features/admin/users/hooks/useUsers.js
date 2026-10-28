import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createUser,
  deleteUser,
  getUserById,
  getUsers,
  updateUser,
} from '../services/usersService';

export function useUsers(params = {}) {
  return useQuery({
    queryKey: ['admin', 'users', params],
    queryFn: () => getUsers(params),
  });
}

export function useUser(id) {
  return useQuery({
    queryKey: ['admin', 'users', id],
    queryFn: () => getUserById(id),
    enabled: !!id, // Chỉ cho phép gọi API nếu id có giá trị
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  // Dùng cho các tác vụ thay đổi dữ liệu (POST, PUT, DELETE, PATCH)
  return useMutation({
    mutationFn: (body) => createUser(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateUser(id, body),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'users', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'users'] });
    },
  });
}

export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteUser(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
  });
}
