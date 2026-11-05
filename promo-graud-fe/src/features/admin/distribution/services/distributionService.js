import axiosInstance from '../../../../lib/axiosInstance';

// BE chưa implement GET /distribution-logs
export const getDistributionLogs = async (params = {}) => {
  const { data } = await axiosInstance.get('/distributes', { params });
  return data; // PaginatedResponse
};
export const getDistributionLogsById = async (id) => {
  const { data } = await axiosInstance.get(`/distributes/${id}`);
  return data;
};
