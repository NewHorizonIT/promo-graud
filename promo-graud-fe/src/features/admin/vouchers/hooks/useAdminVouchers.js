import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getVouchers,
  getVoucher,
  createVoucher,
  deleteVoucher,
  updateVoucher,
  getJobStatus,
} from '../services/voucherService';

export function useAdminVouchers(params = {}) {
  return useQuery({
    queryKey: ['admin', 'vouchers', params],
    queryFn: () => getVouchers(params),
  });
}

export function useAdminVoucher(id) {
  return useQuery({
    queryKey: ['admin', 'vouchers', id],
    queryFn: () => getVoucher(id),
    enabled: !!id,
  });
}

export function useCreateVoucher() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body) => createVoucher(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'vouchers'] }),
  });
}

export function useDeleteVoucher() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteVoucher(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'vouchers'] }),
  });
}

export function useUpdateVoucher() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }) => updateVoucher(id, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'vouchers'] }),
  });
}

export function useJobStatus(jobId) {
  return useQuery({
    queryKey: ['admin', 'vouchers', 'job', jobId],
    queryFn: () => getJobStatus(jobId),
    enabled: !!jobId,
    refetchInterval: (query) => {
      const s = query.state.data?.status;
      if (s === 'COMPLETED' || s === 'FAILED' || s === 'STOPPED') return false;
      return 2000;
    },
  });
}
