import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import VoucherForm from '../../features/admin/vouchers/components/VoucherForm';
import { useCreateVoucher } from '../../features/admin/vouchers/hooks/useAdminVouchers';
import { getCampaigns, getRules } from '../../features/admin/campaigns/services/campaignService';

export default function VoucherFormPage() {
  const navigate = useNavigate();
  const [allRules, setAllRules] = useState([]);
  const [error, setError] = useState('');

  const createMutation = useCreateVoucher();

  useEffect(() => {
    getCampaigns({ pageSize: 100 }).then(async (res) => {
      const campaigns = res?.data ?? [];
      if (!campaigns.length) return;
      const rulePromises = campaigns.map((c) =>
        getRules(c.id).then((r) =>
          (r?.data ?? []).map((rule) => ({ ...rule, campaignName: c.name })),
        ),
      );
      const results = await Promise.all(rulePromises);
      setAllRules(results.flat());
    });
  }, []);

  const handleSubmit = (body) => {
    setError('');
    createMutation.mutate(body, {
      onSuccess: () => navigate('/admin/vouchers'),
      onError: (err) => setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.'),
    });
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <Link
        to="/admin/vouchers"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Quay lại danh sách
      </Link>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <h2 className="text-xl font-bold text-text-primary mb-6">Tạo Voucher mới</h2>

        {error && (
          <div className="mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20 text-sm text-error-text">
            {error}
          </div>
        )}

        <VoucherForm rules={allRules} onSubmit={handleSubmit} loading={createMutation.isPending} />
      </div>
    </div>
  );
}
