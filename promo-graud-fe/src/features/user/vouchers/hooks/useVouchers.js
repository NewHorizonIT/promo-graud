import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getCampaigns,
  getVouchersByCampaign,
  getVouchers,
  claimVoucher,
  getMyVouchers,
  getRules,
} from '../services/voucherCollectionService';
import { useAuthStore } from '../../../../store/authStore';

export function useCampaigns() {
  return useQuery({
    queryKey: ['campaigns', 'active'],
    queryFn: getCampaigns,
  });
}

// fetch vouchers cho tất cả campaigns trong 1 query → chỉ 1 re-render thay vì N
export function useAllCampaignVouchers(campaignIds) {
  return useQuery({
    queryKey: ['vouchers', 'by-campaigns', campaignIds],
    queryFn: () => Promise.all(campaignIds.map((id) => getVouchersByCampaign(id))),
    enabled: campaignIds.length > 0,
    staleTime: 1000 * 60 * 5,
  });
}

export function useVouchers({ page = 1, pageSize = 16, ruleId } = {}) {
  return useQuery({
    queryKey: ['vouchers', 'list', { page, pageSize, ruleId }],
    queryFn: () => getVouchers({ page, pageSize, ruleId }),
  });
}

export function useRules() {
  return useQuery({
    queryKey: ['rules', 'active'],
    queryFn: getRules,
    staleTime: 1000 * 60 * 10,
  });
}

export function useMyVouchers() {
  const token = useAuthStore((s) => s.token);
  return useQuery({
    queryKey: ['user-vouchers', 'me'],
    queryFn: () => getMyVouchers({ page: 0, limit: 100 }),
    enabled: !!token,
  });
}

export function useClaimVoucher() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: claimVoucher,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-vouchers', 'me'] });
    },
    onError: (error) => {
      if (error?.response?.status === 409) {
        queryClient.invalidateQueries({ queryKey: ['user-vouchers', 'me'] });
      }
    },
  });
}
