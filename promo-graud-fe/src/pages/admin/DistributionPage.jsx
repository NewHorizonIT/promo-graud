import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import DistributionLogTable from '../../features/admin/distribution/components/DistributionLogTable';
import { getDistributionLogs } from '../../features/admin/distribution/services/distributionService';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'pending', label: 'Đang gửi' },
  { value: 'success', label: 'Thành công' },
  { value: 'failed', label: 'Thất bại' },
];

export default function DistributionPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const [logs, setLogs] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  const PAGE_SIZE = 20;

  useEffect(() => {
    setLoading(true);
    const params = { page, page_size: PAGE_SIZE };
    if (status) params.status = status;
    getDistributionLogs(params)
      .then((res) => {
        if (res?.data) {
          setLogs(res.data.items ?? []);
          setTotal(res.data.total ?? 0);
        }
      })
      .finally(() => setLoading(false));
  }, [status, page]);

  const totalPages = Math.ceil(total / PAGE_SIZE);

  return (
    <div className="space-y-5">
      <p className="text-sm text-text-muted">{total} bản ghi</p>

      {/* Status filter */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setSearchParams(f.value ? { status: f.value } : {})}
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

      {loading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <DistributionLogTable logs={logs} />
      )}

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
