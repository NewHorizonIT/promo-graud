import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, X } from 'lucide-react';
import DistributionLogTable from '../../features/admin/distribution/components/DistributionLogTable';
import { getDistributionLogs } from '../../features/admin/distribution/services/distributionService';
import Pagination from '../../components/pagination';

const PAGE_SIZE = 20;

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Đang gửi' },
  { value: 'SENT', label: 'Thành công' },
  { value: 'FAILED', label: 'Thất bại' },
  { value: 'DLQ', label: 'DLQ' },
];

const CHANNEL_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'EMAIL', label: 'Email' },
  { value: 'WEBHOOK', label: 'Webhook' },
];

function FilterTabs({ options, value, onChange }) {
  return (
    <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
      {options.map((o) => (
        <button
          key={o.value}
          type="button"
          onClick={() => onChange(o.value)}
          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
            value === o.value
              ? 'bg-surface text-text-primary shadow-level-1'
              : 'text-text-muted hover:text-text-primary'
          }`}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}

export default function DistributionPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const channel = searchParams.get('channel') ?? '';
  const recipient = searchParams.get('recipient') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const [searchInput, setSearchInput] = useState(recipient);
  const [logs, setLogs] = useState([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  // Cập nhật 1 hoặc nhiều param; đổi filter thì tự về trang 1
  const updateParams = (patch) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      Object.entries(patch).forEach(([k, v]) => (v ? next.set(k, v) : next.delete(k)));
      if (!('page' in patch)) next.delete('page');
      return next;
    });
  };

  // Debounce ô tìm kiếm
  useEffect(() => {
    const value = searchInput.trim();
    if (value === recipient) return;
    const t = setTimeout(() => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (value) next.set('recipient', value);
        else next.delete('recipient');
        next.delete('page');
        return next;
      });
    }, 400);
    return () => clearTimeout(t);
  }, [searchInput, recipient, setSearchParams]);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    const params = { page, size: PAGE_SIZE };
    if (status) params.status = status;
    if (channel) params.channel = channel;
    if (recipient) params.recipient = recipient;

    getDistributionLogs(params)
      .then((res) => {
        if (ignore) return;
        const result = res?.result;
        setLogs(result?.data ?? []);
        setTotal(result?.totalItems ?? 0);
        setTotalPages(result?.totalPages ?? 0);
      })
      .catch(() => {
        if (ignore) return;
        setLogs([]);
        setTotal(0);
        setTotalPages(0);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [status, channel, recipient, page]);

  return (
    <div className="space-y-5">
      <p className="text-sm text-text-muted">{total} bản ghi</p>

      {/* Search */}
      <div className="relative max-w-sm">
        <Search
          size={16}
          className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none"
        />
        <input
          type="text"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          placeholder="Tìm theo người nhận (email / URL)..."
          className="w-full h-10 pl-10 pr-9 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
        />
        {searchInput && (
          <button
            type="button"
            onClick={() => setSearchInput('')}
            className="absolute right-2.5 top-1/2 -translate-y-1/2 p-1 rounded-md text-text-muted hover:text-text-primary cursor-pointer"
          >
            <X size={14} />
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-x-6 gap-y-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-text-muted">Trạng thái</span>
          <FilterTabs
            options={STATUS_FILTERS}
            value={status}
            onChange={(v) => updateParams({ status: v })}
          />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-text-muted">Kênh</span>
          <FilterTabs
            options={CHANNEL_FILTERS}
            value={channel}
            onChange={(v) => updateParams({ channel: v })}
          />
        </div>
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

      <Pagination
        meta={{ current_page: page, last_page: totalPages }}
        onPageChange={(p) => updateParams({ page: String(p) })}
      />
    </div>
  );
}
