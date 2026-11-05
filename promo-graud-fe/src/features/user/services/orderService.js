import axiosInstance from '../../../lib/axiosInstance';

export async function getMyOrders({ page = 0, limit = 10 } = {}) {
  const { data } = await axiosInstance.get('/orders/me', { params: { page, limit } });
  return data.result;
}
