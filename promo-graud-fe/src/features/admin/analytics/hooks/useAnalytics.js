import { useQuery } from '@tanstack/react-query';
import { getCampaignStats, getCampaignTimeline } from '../services/analyticsService';

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
