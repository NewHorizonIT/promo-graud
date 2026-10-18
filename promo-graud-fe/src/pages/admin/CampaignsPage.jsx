import { useEffect, useState, useCallback } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import CampaignTable from '../../features/admin/campaigns/components/CampaignTable';
import {
  getCampaigns,
  deleteCampaign,
} from '../../features/admin/campaigns/services/campaignService';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'chua_dien_ra', label: 'Chưa diễn ra' },
  { value: 'dang_dien_ra', label: 'Đang diễn ra' },
  { value: 'da_dong', label: 'Đã đóng' },
];

export default function CampaignsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const [campaigns, setCampaigns] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  const PAGE_SIZE = 20;

  const load = useCallback(() => {
    setLoading(true);
    const params = { page, page_size: PAGE_SIZE };
    if (status) params.status = status;
    getCampaigns(params)
      .then((res) => {
        if (res?.data) {
          setCampaigns(res.data.items ?? []);
          setTotal(res.data.total ?? 0);
        }
      })
      .finally(() => setLoading(false));
  }, [status, page]);

  useEffect(() => {
    load();
  }, [load]);

  const handleDelete = async (campaign) => {
    if (!confirm(`Xóa campaign "${campaign.name}"?`)) return;
    await deleteCampaign(campaign.id);
    load();
  };

  const setFilter = (val) => {
    const next = {};
    if (val) next.status = val;
    setSearchParams(next);
  };

  const totalPages = Math.ceil(total / PAGE_SIZE);

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <p className="text-sm text-text-muted">{total} campaigns</p>
        </div>
        <Link
          to="/admin/campaigns/new"
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
        >
          <Plus size={16} /> Tạo Campaign
        </Link>
      </div>

      {/* Status filter tabs */}
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

      {/* Table */}
      {loading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <CampaignTable campaigns={campaigns} onDelete={handleDelete} />
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() => setSearchParams({ ...(status ? { status } : {}), page: String(pg) })}
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
