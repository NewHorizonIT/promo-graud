import axiosInstance from '../../../../lib/axiosInstance';

// BE chưa implement GET /fraud-logs
export const getFraudLogs = async (params = {}) => {
  const { data } = await axiosInstance.get('/fraud-logs', { params });
  return data; // PaginatedResponse
};

// BE chưa implement GET /analytics/fraud-stats
export const getFraudStats = async (params = {}) => {
  const { data } = await axiosInstance.get('/analytics/fraud-stats', { params });
  return data.result;
};
