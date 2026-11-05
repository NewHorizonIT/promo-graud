import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import DistributionStatusBadge, {
  CHANNEL_LABELS,
} from '../../features/admin/distribution/components/DistributionStatusBadge';
import { getDistributionLogsById } from '../../features/admin/distribution/services/distributionService';

function formatDateTime(iso) {
  return iso ? new Date(iso).toLocaleString('vi-VN') : '—';
}

export default function DistributionLogDetailPage() {
  const { id } = useParams();
  const [log, setLog] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    getDistributionLogsById(id)
      .then((res) => {
        if (!ignore) setLog(res?.result ?? null);
      })
      .catch(() => {
        if (!ignore) setLog(null);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-20 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  if (!log) {
    return <p className="text-sm text-text-muted">Không tìm thấy log phân phối.</p>;
  }

  return (
    <div className="space-y-5">
      <Link
        to="/admin/distributions"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Lịch sử phân phối
      </Link>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <h2 className="text-2xl font-bold text-text-primary">Log #{log.id}</h2>
            <p className="text-sm text-text-muted mt-1 break-all">{log.recipient}</p>
          </div>
          <DistributionStatusBadge status={log.status} />
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-5">
          <div>
            <p className="text-xs text-text-muted mb-0.5">Voucher ID</p>
            <p className="text-sm font-medium text-text-primary">#{log.voucherId ?? '—'}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Kênh</p>
            <p className="text-sm font-medium text-text-primary">
              {CHANNEL_LABELS[log.channel] ?? log.channel}
            </p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Số lần thử lại</p>
            <p className="text-sm font-medium text-text-primary">{log.retryCount ?? 0}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Lần thử cuối</p>
            <p className="text-sm font-medium text-text-primary">
              {formatDateTime(log.lastAttemptAt)}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
        <p className="text-xs font-semibold text-text-muted uppercase tracking-wide mb-2">
          Thông báo lỗi
        </p>
        {log.errorMessage ? (
          <pre className="text-xs text-error-text bg-error-bg rounded-lg p-3 whitespace-pre-wrap break-words font-mono">
            {log.errorMessage}
          </pre>
        ) : (
          <p className="text-sm text-text-muted">Không có lỗi.</p>
        )}
      </div>
    </div>
  );
}
