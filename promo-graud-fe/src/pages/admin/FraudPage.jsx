import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import FraudStatCards from '../../features/admin/fraud/components/FraudStatCards';
import FraudLogTable from '../../features/admin/fraud/components/FraudLogTable';
import { getFraudLogs, getFraudStats } from '../../features/admin/fraud/services/fraudService';

const REASON_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'multiple_accounts', label: 'Multi-Account' },
  { value: 'vpn_detected', label: 'VPN Detected' },
  { value: 'bot_script', label: 'Bot Script' },
];

export default function FraudPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const reason = searchParams.get('reason') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState(null);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  const PAGE_SIZE = 20;

  useEffect(() => {
    const params = { page, page_size: PAGE_SIZE };
    if (reason) params.reason = reason;
    Promise.all([getFraudLogs(params), getFraudStats()])
      .then(([logsRes, statsRes]) => {
        if (logsRes?.data) {
          setLogs(logsRes.data.items ?? []);
          setTotal(logsRes.data.total ?? 0);
        }
        if (statsRes?.data) setStats(statsRes.data);
      })
      .finally(() => setLoading(false));
  }, [reason, page]);

  const totalPages = Math.ceil(total / PAGE_SIZE);

  return (
    <div className="space-y-6">
      {/* Stat cards */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-24 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <FraudStatCards stats={stats} />
      )}

      {/* Reason filter */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {REASON_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setSearchParams(f.value ? { reason: f.value } : {})}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              reason === f.value
                ? 'bg-surface text-text-primary shadow-level-1'
                : 'text-text-muted hover:text-text-primary'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      <p className="text-sm text-text-muted -mt-2">{total} bản ghi</p>

      {loading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <FraudLogTable logs={logs} />
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() => setSearchParams({ ...(reason ? { reason } : {}), page: String(pg) })}
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
