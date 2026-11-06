import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      userName: null,
      role: null,

      // gọi sau login — set đầy đủ từ JWT
      setAuth: ({ token, userName, role }) => set({ token, userName, role }),

      // gọi sau refresh — chỉ cập nhật token, userName/role không đổi
      setToken: (token) => set({ token }),

      // gọi khi logout
      clearAuth: () => set({ token: null, userName: null, role: null }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        userName: state.userName,
        role: state.role,
      }),
    },
  ),
);
