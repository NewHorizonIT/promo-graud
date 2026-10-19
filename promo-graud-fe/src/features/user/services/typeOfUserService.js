import axiosInstance from '../../../lib/axiosInstance';

export async function getTypeOfUsers() {
  const { data } = await axiosInstance.get('/type-of-users');
  return data.result;
}
