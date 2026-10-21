import axiosInstance from '../../../../lib/axiosInstance';

export const getVouchers = async (params = {}) => {
  const { data } = await axiosInstance.get('/vouchers', { params });
  return data; // PaginatedResponse: { data: [], page, size, totalItems, totalPages }
};

export const getVoucher = async (id) => {
  const { data } = await axiosInstance.get(`/vouchers/${id}`);
  return data.result;
};

export const createVoucher = async (body) => {
  const { data } = await axiosInstance.post('/vouchers', body);
  return data.result;
};

// BE chưa implement DELETE /vouchers/{id}
export const deleteVoucher = async (id) => {
  const { data } = await axiosInstance.delete(`/vouchers/${id}`);
  return data.result;
};

// BE chưa implement POST /vouchers/{id}/distribute
export const distributeVoucher = async (id, body) => {
  const { data } = await axiosInstance.post(`/vouchers/${id}/distribute`, body);
  return data.result;
};
