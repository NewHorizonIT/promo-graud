import { adminFetch } from '../../services/adminApi.js';

export const getVouchers = (params = {}) => adminFetch(`/vouchers?${new URLSearchParams(params)}`);

export const getVoucher = (id) => adminFetch(`/vouchers/${id}`);

export const createVoucher = (body) =>
  adminFetch('/vouchers', { method: 'POST', body: JSON.stringify(body) });

export const deleteVoucher = (id) => adminFetch(`/vouchers/${id}`, { method: 'DELETE' });

export const distributeVoucher = (id, body) =>
  adminFetch(`/vouchers/${id}/distribute`, { method: 'POST', body: JSON.stringify(body) });
