import axiosInstance from '../../../../lib/axiosInstance';

export const getOverviewStats = async () => {
  const { data } = await axiosInstance.get('/analytics/overview');
  return data.result;
};

export const getCampaignStats = async (campaignId) => {
  const { data } = await axiosInstance.get(`/analytics/campaigns/${campaignId}/stats`);
  return data.result;
};

export const getCampaignTimeline = async (campaignId, params = {}) => {
  const { data } = await axiosInstance.get(`/analytics/campaigns/${campaignId}/timeline`, {
    params,
  });
  return data.result;
};

export const getRuleBreakdown = async (campaignId) => {
  const { data } = await axiosInstance.get(`/analytics/campaigns/${campaignId}/rule-breakdown`);
  return data.result;
};

export const getTopUsers = async (campaignId, limit = 10) => {
  const { data } = await axiosInstance.get(`/analytics/campaigns/${campaignId}/top-users`, {
    params: { limit },
  });
  return data.result;
};

export const getVoucherUtilization = async (
  campaignId,
  sortBy = 'usage_rate',
  sortDir = 'desc',
) => {
  const { data } = await axiosInstance.get(
    `/analytics/campaigns/${campaignId}/voucher-utilization`,
    { params: { sortBy, sortDir } },
  );
  return data.result;
};
