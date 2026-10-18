import { adminFetch } from '../../services/adminApi.js';

export const getDistributionLogs = (params = {}) =>
  adminFetch(`/distribution-logs?${new URLSearchParams(params)}`);
