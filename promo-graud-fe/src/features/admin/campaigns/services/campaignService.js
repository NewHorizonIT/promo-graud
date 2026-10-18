import { adminFetch } from '../../services/adminApi.js';

export const getCampaigns = (params = {}) =>
  adminFetch(`/campaigns?${new URLSearchParams(params)}`);

export const getCampaign = (id) => adminFetch(`/campaigns/${id}`);

export const createCampaign = (body) =>
  adminFetch('/campaigns', { method: 'POST', body: JSON.stringify(body) });

export const updateCampaign = (id, body) =>
  adminFetch(`/campaigns/${id}`, { method: 'PUT', body: JSON.stringify(body) });

export const deleteCampaign = (id) => adminFetch(`/campaigns/${id}`, { method: 'DELETE' });

export const updateCampaignStatus = (id, status) =>
  adminFetch(`/campaigns/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });

// ── Rules (nested under campaign) ──────────────────────────────────────────

export const getRules = (campaignId) => adminFetch(`/campaigns/${campaignId}/rules`);

export const getRule = (campaignId, ruleId) =>
  adminFetch(`/campaigns/${campaignId}/rules/${ruleId}`);

export const createRule = (campaignId, body) =>
  adminFetch(`/campaigns/${campaignId}/rules`, { method: 'POST', body: JSON.stringify(body) });

export const updateRule = (campaignId, ruleId, body) =>
  adminFetch(`/campaigns/${campaignId}/rules/${ruleId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });

export const deleteRule = (campaignId, ruleId) =>
  adminFetch(`/campaigns/${campaignId}/rules/${ruleId}`, { method: 'DELETE' });
