import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getCampaigns,
  getCampaign,
  createCampaign,
  updateCampaign,
  deleteCampaign,
  updateCampaignStatus,
} from '../services/campaignService';

export function useCampaigns(params = {}) {
  return useQuery({
    queryKey: ['admin', 'campaigns', params],
    queryFn: () => getCampaigns(params),
  });
}

export function useCampaign(id) {
  return useQuery({
    queryKey: ['admin', 'campaigns', id],
    queryFn: () => getCampaign(id),
    enabled: !!id,
  });
}

export function useCreateCampaign() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body) => createCampaign(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'campaigns'] }),
  });
}

export function useUpdateCampaign() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateCampaign(id, body),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'campaigns', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'campaigns'] });
    },
  });
}

export function useDeleteCampaign() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteCampaign(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'campaigns'] }),
  });
}

export function useUpdateCampaignStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }) => updateCampaignStatus(id, status),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['admin', 'campaigns', id] });
      qc.invalidateQueries({ queryKey: ['admin', 'campaigns'] });
    },
  });
}
