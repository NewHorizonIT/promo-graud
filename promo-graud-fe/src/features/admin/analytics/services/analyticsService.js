import { adminFetch } from '../../services/adminApi.js';

export const getCampaignStats = (campaignId) =>
  adminFetch(`/analytics/campaigns/${campaignId}/stats`);

export const getCampaignTimeline = (campaignId, params = {}) =>
  adminFetch(`/analytics/campaigns/${campaignId}/timeline?${new URLSearchParams(params)}`);
