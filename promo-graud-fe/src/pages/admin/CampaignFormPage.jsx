import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import CampaignForm from '../../features/admin/campaigns/components/CampaignForm';
import {
  useCampaign,
  useCreateCampaign,
  useUpdateCampaign,
} from '../../features/admin/campaigns/hooks/useCampaigns';

export default function CampaignFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [initial, setInitial] = useState(null);
  const [error, setError] = useState('');
  const initializedIdRef = useRef(null);

  const { data: campaign, isLoading: fetchLoading } = useCampaign(id);
  const createMutation = useCreateCampaign();
  const updateMutation = useUpdateCampaign();

  useEffect(() => {
    setError('');
  }, [id]);

  useEffect(() => {
    if (!campaign || initializedIdRef.current === id) return;
    initializedIdRef.current = id;
    setInitial({
      name: campaign.name,
      start_time: campaign.startTime?.slice(0, 16) ?? '',
      end_time: campaign.endTime?.slice(0, 16) ?? '',
      promotion_budget: String(campaign.promotionBudget ?? ''),
    });
  }, [campaign, id]);

  const loading = createMutation.isPending || updateMutation.isPending;

  const handleSubmit = (body) => {
    setError('');
    if (isEdit) {
      updateMutation.mutate(
        { id, body },
        {
          onSuccess: () => navigate(`/admin/campaigns/${id}`),
          onError: (err) => setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.'),
        },
      );
    } else {
      createMutation.mutate(body, {
        onSuccess: () => navigate('/admin/campaigns'),
        onError: (err) => setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.'),
      });
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
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

        {isEdit && fetchLoading ? (
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
