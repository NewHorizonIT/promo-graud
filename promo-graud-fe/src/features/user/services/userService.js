import axiosInstance from '../../../lib/axiosInstance';

export async function getMe() {
  const { data } = await axiosInstance.get('/users/me');
  return data.result;
}

export async function getUserById(id) {
  const { data } = await axiosInstance.get(`/users/${id}`);
  return data.result;
}

export async function updateUser(id, payload) {
  const { data } = await axiosInstance.put(`/users/${id}`, payload);
  return data.result;
}
