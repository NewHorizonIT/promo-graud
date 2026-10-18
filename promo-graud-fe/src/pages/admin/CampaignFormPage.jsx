import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import CampaignForm from '../../features/admin/campaigns/components/CampaignForm';
import {
  getCampaign,
  createCampaign,
  updateCampaign,
} from '../../features/admin/campaigns/services/campaignService';

export default function CampaignFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [initial, setInitial] = useState(null);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEdit);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    getCampaign(id)
      .then((res) => {
        if (res?.data) {
          const c = res.data;
          setInitial({
            name: c.name,
            start_time: c.start_time?.slice(0, 16) ?? '',
            end_time: c.end_time?.slice(0, 16) ?? '',
            promotion_budget: String(c.promotion_budget ?? ''),
          });
        }
      })
      .finally(() => setFetchLoading(false));
  }, [id, isEdit]);

  const handleSubmit = async (body) => {
    setLoading(true);
    setError('');
    try {
      const res = isEdit ? await updateCampaign(id, body) : await createCampaign(body);
      if (res?.success === false) {
        setError(res.message ?? 'Có lỗi xảy ra.');
        return;
      }
      navigate(isEdit ? `/admin/campaigns/${id}` : '/admin/campaigns');
    } catch {
      setError('Không thể kết nối server.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      {/* Breadcrumb */}
      <Link
        to={isEdit ? `/admin/campaigns/${id}` : '/admin/campaigns'}
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} />
        {isEdit ? 'Quay lại chi tiết' : 'Quay lại danh sách'}
      </Link>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <h2 className="text-xl font-bold text-text-primary mb-6">
          {isEdit ? 'Chỉnh sửa Campaign' : 'Tạo Campaign mới'}
        </h2>

        {error && (
          <div className="mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20 text-sm text-error-text">
            {error}
          </div>
        )}

        {fetchLoading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-11 bg-surface-muted rounded-xl animate-pulse" />
            ))}
          </div>
        ) : (
          <CampaignForm initial={initial ?? undefined} onSubmit={handleSubmit} loading={loading} />
        )}
      </div>
    </div>
  );
}
