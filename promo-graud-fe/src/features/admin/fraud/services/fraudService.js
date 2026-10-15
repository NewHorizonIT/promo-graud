import { adminFetch } from "../../services/adminApi.js";

export const getFraudLogs = (params = {}) =>
  adminFetch(`/fraud-logs?${new URLSearchParams(params)}`);

export const getFraudStats = (params = {}) =>
  adminFetch(`/analytics/fraud-stats?${new URLSearchParams(params)}`);
