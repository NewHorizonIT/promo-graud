import axiosInstance from '../../../../lib/axiosInstance';

export const getUsers = async (params = {}) => {
  const { data } = await axiosInstance.get('/users/page', { params });
  return data.result;
};

export const getUserById = async (id) => {
  const { data } = await axiosInstance.get(`/users/${id}`);
  return data.result;
};

export const createUser = async (body) => {
  const { data } = await axiosInstance.post('/users', body);
  return data.result;
};

export const updateUser = async (id, body) => {
  const { data } = await axiosInstance.put(`/users/${id}`, body);
  return data.result;
};

export const deleteUser = async (id) => {
  const { data } = await axiosInstance.delete(`/users/${id}`);
  return data.result;
};
