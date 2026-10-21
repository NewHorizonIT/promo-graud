import axiosInstance from '../../../../lib/axiosInstance';

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
