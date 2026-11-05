import axiosInstance from '../../../../lib/axiosInstance';

export const getTypeOfUser = async () => {
  const { data } = await axiosInstance.get('/type-of-users');
  return data.result;
};

export const createTypeOfUser = async (body) => {
  const { data } = await axiosInstance.post('/type-of-users', body);
  return data.result;
};

export const updateTypeOfUser = async (id, body) => {
  const { data } = await axiosInstance.put(`/type-of-users/${id}`, body);
  return data.result;
};
