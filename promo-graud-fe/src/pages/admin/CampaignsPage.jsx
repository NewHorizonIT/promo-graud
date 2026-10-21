import { useSearchParams, Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import CampaignTable from '../../features/admin/campaigns/components/CampaignTable';
import { useCampaigns, useDeleteCampaign } from '../../features/admin/campaigns/hooks/useCampaigns';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'UPCOMING', label: 'Chưa diễn ra' },
  { value: 'ACTIVE', label: 'Đang diễn ra' },
  { value: 'ENDED', label: 'Đã đóng' },
];

const PAGE_SIZE = 20;

export default function CampaignsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const params = { page, pageSize: PAGE_SIZE, ...(status ? { status } : {}) };
  const { data, isLoading } = useCampaigns(params);
  const deleteMutation = useDeleteCampaign();

  const campaigns = data?.data ?? [];
  const total = data?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? 1;

  const handleDelete = (campaign) => {
    if (!confirm(`Xóa campaign "${campaign.name}"?`)) return;
    deleteMutation.mutate(campaign.id);
  };

  const setFilter = (val) => {
    setSearchParams(val ? { status: val } : {});
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{total} campaigns</p>
        <Link
          to="/admin/campaigns/new"
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
        >
          <Plus size={16} /> Tạo Campaign
        </Link>
      </div>

      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setFilter(f.value)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              status === f.value
                ? 'bg-surface text-text-primary shadow-level-1'
                : 'text-text-muted hover:text-text-primary'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <CampaignTable campaigns={campaigns} onDelete={handleDelete} />
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() =>
                  setSearchParams({ ...(status ? { status } : {}), page: String(pg) })
                }
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pg === page
                    ? 'bg-primary text-white shadow-glow'
                    : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                }`}
              >
                {pg}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
