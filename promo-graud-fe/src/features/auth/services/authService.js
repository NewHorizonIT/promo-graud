import axiosInstance from '../../../lib/axiosInstance';
import { useAuthStore } from '../../../store/authStore';

export async function login(username, password) {
  const { data } = await axiosInstance.post('/auth/login', { username, password });
  return data; // { code, message, result: { token, valid } }
}

export async function logout() {
  const token = useAuthStore.getState().token;
  try {
    await axiosInstance.post('/auth/logout', { token });
  } catch {
    // logout vẫn clear local dù request fail
  } finally {
    useAuthStore.getState().clearAuth();
  }
}
