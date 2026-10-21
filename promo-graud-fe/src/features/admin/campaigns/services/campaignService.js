import axiosInstance from '../../../../lib/axiosInstance';

export const getCampaigns = async (params = {}) => {
  const { data } = await axiosInstance.get('/campaigns', { params });
  return data; // PaginatedResponse: { data: [], page, size, totalItems, totalPages }
};

export const getCampaign = async (id) => {
  const { data } = await axiosInstance.get(`/campaigns/${id}`);
  return data.result;
};

export const createCampaign = async (body) => {
  const { data } = await axiosInstance.post('/campaigns', body);
  return data.result;
};

export const updateCampaign = async (id, body) => {
  const { data } = await axiosInstance.put(`/campaigns/${id}`, body);
  return data.result;
};

export const deleteCampaign = async (id) => {
  const { data } = await axiosInstance.delete(`/campaigns/${id}`);
  return data.result;
};

export const updateCampaignStatus = async (id, status) => {
  const { data } = await axiosInstance.patch(`/campaigns/${id}`, { status });
  return data.result;
};

export const getRules = async (campaignId) => {
  const { data } = await axiosInstance.get('/rules', { params: { campaignId, pageSize: 100 } });
  return data; // PaginatedResponse: { data: [], page, size, totalItems, totalPages }
};

export const createRule = async (campaignId, body) => {
  const { data } = await axiosInstance.post('/rules', { ...body, campaignId });
  return data.result;
};

// TODO: BE chưa có PUT /rules/{id} — sẽ throw 404 cho đến khi BE thêm endpoint
export const updateRule = async (campaignId, ruleId, body) => {
  const { data } = await axiosInstance.put(`/rules/${ruleId}`, { ...body, campaignId });
  return data.result;
};

export const deleteRule = async (campaignId, ruleId) => {
  const { data } = await axiosInstance.delete(`/rules/${ruleId}`);
  return data.result;
};
