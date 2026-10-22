import axiosInstance from '../../../../lib/axiosInstance';

export const getProducts = async (params = {}) => {
  const { data } = await axiosInstance.get('/products', { params });
  return data.result;
};

export const getProduct = async (id) => {
  const { data } = await axiosInstance.get(`/products/${id}`);
  return data.result;
};

export const createProduct = async (body) => {
  const { data } = await axiosInstance.post('/products', body);
  return data.result;
};

export const updateProduct = async (id, body) => {
  const { data } = await axiosInstance.put(`/products/${id}`, body);
  return data.result;
};

export const deleteProduct = async (id) => {
  const { data } = await axiosInstance.delete(`/products/${id}`);
  return data.result;
};

export const getTypeProducts = async (params = {}) => {
  const { data } = await axiosInstance.get('/products/types', { params });
  return data.result;
};

export const getTypeProduct = async () => {
  const { data } = await axiosInstance.get('/products/type');
  return data.result;
};

export const createTypeOfProduct = async (body) => {
  const { data } = await axiosInstance.post('/products/type', body);
  return data.result;
};

export const updateTypeOfProduct = async (id, body) => {
  const { data } = await axiosInstance.put(`/products/type/${id}`, body);
  return data.result;
};
