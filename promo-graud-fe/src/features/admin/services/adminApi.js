import { API_BASE } from '../../../config/constants.js';
import { useAuthStore } from '../../../store/authStore.js';

export async function adminFetch(path, { headers: extraHeaders = {}, ...rest } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${useAuthStore.getState().token}`,
      ...extraHeaders,
    },
    ...rest,
  });
  return res.json();
}
