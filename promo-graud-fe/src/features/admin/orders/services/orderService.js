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

export async function cancelAdminOrder(id) {
  const { data } = await axiosInstance.patch(`/orders/${id}/cancel`);
  return data.result;
}

export async function updateStatusOrder(id, body) {
  const { data } = await axiosInstance.put(`/orders/${id}`, body);
  return data.result;
}

export const deleteOrder = async (id) => {
  const { data } = await axiosInstance.delete(`/orders/${id}`);
  return data.result;
};
