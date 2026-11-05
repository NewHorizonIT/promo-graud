import axiosInstance from '../../../../lib/axiosInstance';

// BE chưa implement GET /distribution-logs
export const getDistributionLogs = async (params = {}) => {
  const { data } = await axiosInstance.get('/distribution-logs', { params });
  return data; // PaginatedResponse
};
