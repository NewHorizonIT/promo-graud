import { useQuery } from '@tanstack/react-query';
import {
  getOverviewStats,
  getCampaignStats,
  getCampaignTimeline,
  getRuleBreakdown,
  getTopUsers,
  getVoucherUtilization,
} from '../services/analyticsService';

export function useOverviewStats() {
  return useQuery({
    queryKey: ['admin', 'analytics', 'overview'],
    queryFn: getOverviewStats,
    staleTime: 1000 * 60 * 5,
  });
}

export function useCampaignStats(campaignId) {
  return useQuery({
    queryKey: ['admin', 'analytics', 'stats', campaignId],
    queryFn: () => getCampaignStats(campaignId),
    enabled: !!campaignId,
    staleTime: 1000 * 60 * 5,
  });
}

export function useCampaignTimeline(campaignId, params = {}) {
  return useQuery({
    queryKey: ['admin', 'analytics', 'timeline', campaignId, params],
    queryFn: () => getCampaignTimeline(campaignId, params),
    enabled: !!campaignId,
    staleTime: 1000 * 60 * 5,
  });
}

export function useRuleBreakdown(campaignId) {
  return useQuery({
    queryKey: ['admin', 'analytics', 'rule-breakdown', campaignId],
    queryFn: () => getRuleBreakdown(campaignId),
    enabled: !!campaignId,
    staleTime: 1000 * 60 * 5,
  });
}

export function useTopUsers(campaignId, limit = 10) {
  return useQuery({
    queryKey: ['admin', 'analytics', 'top-users', campaignId, limit],
    queryFn: () => getTopUsers(campaignId, limit),
    enabled: !!campaignId,
    staleTime: 1000 * 60 * 5,
  });
}

export function useVoucherUtilization(campaignId, sortBy = 'usage_rate', sortDir = 'desc') {
  return useQuery({
    queryKey: ['admin', 'analytics', 'voucher-utilization', campaignId, sortBy, sortDir],
    queryFn: () => getVoucherUtilization(campaignId, sortBy, sortDir),
    enabled: !!campaignId,
    staleTime: 1000 * 60 * 5,
  });
}
