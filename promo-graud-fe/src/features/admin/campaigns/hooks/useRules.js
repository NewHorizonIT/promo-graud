import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getRules, createRule, deleteRule } from '../services/campaignService';

export function useRules(campaignId) {
  return useQuery({
    queryKey: ['admin', 'rules', campaignId],
    queryFn: () => getRules(campaignId),
    enabled: !!campaignId,
  });
}

export function useCreateRule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ campaignId, body }) => createRule(campaignId, body),
    onSuccess: (_, { campaignId }) =>
      qc.invalidateQueries({ queryKey: ['admin', 'rules', campaignId] }),
  });
}

export function useDeleteRule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ campaignId, ruleId }) => deleteRule(campaignId, ruleId),
    onSuccess: (_, { campaignId }) =>
      qc.invalidateQueries({ queryKey: ['admin', 'rules', campaignId] }),
  });
}
