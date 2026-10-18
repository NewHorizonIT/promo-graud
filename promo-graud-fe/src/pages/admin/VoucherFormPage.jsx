import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import VoucherForm from '../../features/admin/vouchers/components/VoucherForm';
import { createVoucher } from '../../features/admin/vouchers/services/voucherService';
import { getCampaigns, getRules } from '../../features/admin/campaigns/services/campaignService';

export default function VoucherFormPage() {
  const navigate = useNavigate();
  const [allRules, setAllRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getCampaigns({ page_size: 100 }).then(async (res) => {
      if (!res?.data?.items) return;
      const rulePromises = res.data.items.map((c) =>
        getRules(c.id).then((r) =>
          (r?.data ?? []).map((rule) => ({ ...rule, campaignName: c.name })),
        ),
      );
      const results = await Promise.all(rulePromises);
      setAllRules(results.flat());
    });
  }, []);

  const handleSubmit = async (body) => {
    setLoading(true);
    setError('');
    try {
      const res = await createVoucher(body);
      if (res?.success === false) {
        setError(res.message ?? 'Có lỗi xảy ra.');
        return;
      }
      navigate('/admin/vouchers');
    } catch {
      setError('Không thể kết nối server.');
    } finally {
      setLoading(false);
    }
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

        <VoucherForm rules={allRules} onSubmit={handleSubmit} loading={loading} />
      </div>
    </div>
  );
}
