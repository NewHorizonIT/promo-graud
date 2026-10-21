import axiosInstance from '../../../../lib/axiosInstance';

export async function getAdminOrders({ page = 0, size = 10, status, username } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (username) params.username = username;
  const { data } = await axiosInstance.get('/orders', { params });
  return data.result;
}

export async function getAdminOrder(id) {
  const { data } = await axiosInstance.get(`/orders/${id}`);
  return data.result;
}
