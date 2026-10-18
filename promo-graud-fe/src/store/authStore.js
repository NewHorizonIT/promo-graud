import { create } from 'zustand';

export const useAuthStore = create((set) => ({
  token: null,
  userName: null,
  role: null,

  // gọi sau login — set đầy đủ từ JWT
  setAuth: ({ token, userName, role }) => set({ token, userName, role }),

  // gọi sau refresh — chỉ cập nhật token, userName/role không đổi
  setToken: (token) => set({ token }),

  // gọi khi logout
  clearAuth: () => set({ token: null, userName: null, role: null }),
}));
