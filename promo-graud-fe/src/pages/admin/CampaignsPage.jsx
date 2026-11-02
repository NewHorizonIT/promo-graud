import { Fragment, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Plus, Search } from 'lucide-react';
import CampaignTable from '../../features/admin/campaigns/components/CampaignTable';
import { useCampaigns, useDeleteCampaign } from '../../features/admin/campaigns/hooks/useCampaigns';
import ConfirmModal from '../../components/ConfirmModal';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'UPCOMING', label: 'Chưa diễn ra' },
  { value: 'ACTIVE', label: 'Đang diễn ra' },
  { value: 'ENDED', label: 'Đã đóng' },
];

const PAGE_SIZE = 20;

function getVisiblePages(current, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
  const s = new Set([1, total]);
  for (let d = -2; d <= 2; d++) {
    const p = current + d;
    if (p > 1 && p < total) s.add(p);
  }
  return [...s].sort((a, b) => a - b);
}

export default function CampaignsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const nameParam = searchParams.get('name') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  // local state để input không gây re-fetch mỗi keystroke
  const [nameInput, setNameInput] = useState(nameParam);
  const [pendingDelete, setPendingDelete] = useState(null);

  const params = {
    page,
    size: PAGE_SIZE,
    ...(status ? { status } : {}),
    ...(nameParam ? { name: nameParam } : {}),
  };
  const { data, isLoading } = useCampaigns(params);
  const deleteMutation = useDeleteCampaign();

  const campaigns = data?.data ?? [];
  const total = data?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? 1;

  const handleDelete = (campaign) => {
    setPendingDelete(campaign);
  };

  const setFilter = (val) => {
    setSearchParams(val ? { status: val } : {});
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const newParams = {};
    if (status) newParams.status = status;
    if (nameInput.trim()) newParams.name = nameInput.trim();
    setSearchParams(newParams);
  };

  const handleClearSearch = () => {
    setNameInput('');
    const newParams = {};
    if (status) newParams.status = status;
    setSearchParams(newParams);
  };

  return (
    <div className="space-y-5">
      {pendingDelete && (
        <ConfirmModal
          title="Xóa Campaign"
          message={`Xác nhận xóa campaign "${pendingDelete.name}"? Hành động này không thể hoàn tác.`}
          confirmLabel="Xóa"
          loading={deleteMutation.isPending}
          onConfirm={() => {
            deleteMutation.mutate(pendingDelete.id, {
              onSuccess: () => setPendingDelete(null),
              onError: () => setPendingDelete(null),
            });
          }}
          onClose={() => {
            if (!deleteMutation.isPending) setPendingDelete(null);
          }}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{total} campaigns</p>
        <Link
          to="/admin/campaigns/new"
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
        >
          <Plus size={16} /> Tạo Campaign
        </Link>
      </div>

      {/* Search + filter row */}
      <div className="flex flex-col sm:flex-row gap-3">
        <form onSubmit={handleSearch} className="flex gap-2 flex-1 max-w-sm">
          <div className="relative flex-1">
            <Search
              size={15}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none"
            />
            <input
              type="text"
              value={nameInput}
              onChange={(e) => setNameInput(e.target.value)}
              placeholder="Tìm theo tên..."
              className="w-full h-9 pl-9 pr-3 rounded-xl border border-border bg-surface text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all"
            />
          </div>
          <button
            type="submit"
            className="h-9 px-3 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow cursor-pointer"
          >
            Tìm
          </button>
          {nameParam && (
            <button
              type="button"
              onClick={handleClearSearch}
              className="h-9 px-3 border border-border bg-surface hover:bg-surface-muted text-sm font-semibold text-text-secondary rounded-xl transition-colors cursor-pointer"
            >
              Xóa
            </button>
          )}
        </form>

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
          {getVisiblePages(page, totalPages).map((pg, idx, arr) => (
            <Fragment key={pg}>
              {idx > 0 && arr[idx - 1] !== pg - 1 && (
                <span className="flex items-center px-1 text-text-muted text-sm select-none">…</span>
              )}
              <button
                type="button"
                onClick={() =>
                  setSearchParams({
                    ...(status ? { status } : {}),
                    ...(nameParam ? { name: nameParam } : {}),
                    page: String(pg),
                  })
                }
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pg === page
                    ? 'bg-primary text-white shadow-glow'
                    : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                }`}
              >
                {pg}
              </button>
            </Fragment>
          ))}
        </div>
      )}
    </div>
  );
}
