import { useMutation, useQueryClient } from '@tanstack/react-query';
import { login, logout } from '../services/authService';
import { getMe } from '../../user/services/userService';
import { useAuthStore } from '../../../store/authStore';

export function useLogin() {
  const setToken = useAuthStore((s) => s.setToken);
  const setAuth = useAuthStore((s) => s.setAuth);

  return useMutation({
    mutationFn: async ({ username, password }) => {
      const data = await login(username, password);

      if (!data?.result?.token) {
        throw new Error(data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
      }

      const token = data.result.token;
      setToken(token); // set trước để axiosInstance dùng được ngay cho getMe()

      try {
        const user = await getMe();
        setAuth({ token, userName: user.username, role: user.role });
        return { ...data.result, role: user.role };
      } catch {
        // getMe thất bại: vẫn login được, role = null
        setAuth({ token, userName: null, role: null });
        return data.result;
      }
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: logout,
    onSettled: () => {
      // Xóa toàn bộ cache khi logout để không lộ data của user cũ
      queryClient.clear();
    },
  });
}
