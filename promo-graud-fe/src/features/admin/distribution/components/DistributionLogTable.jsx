import { Link } from 'react-router-dom';
import { Eye } from 'lucide-react';
import DistributionStatusBadge, { CHANNEL_LABELS } from './DistributionStatusBadge';

const TH = 'text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider';

export default function DistributionLogTable({ logs = [] }) {
  if (!logs.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          📨
        </div>
        <p className="text-sm font-semibold text-text-primary">Không có lịch sử phân phối</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className={TH}>ID Log</th>
              <th className={TH}>Kênh</th>
              <th className={TH}>Người nhận</th>
              <th className={TH}>Trạng thái</th>
              <th className={`${TH} text-right`}>Chi tiết</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {logs.map((log) => (
              <tr key={log.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4 text-text-secondary font-mono">#{log.id}</td>
                <td className="py-3 px-4 text-text-secondary">
                  {CHANNEL_LABELS[log.channel] ?? log.channel}
                </td>
                <td
                  className="py-3 px-4 text-text-primary font-medium max-w-xs truncate"
                  title={log.recipient}
                >
                  {log.recipient ?? '—'}
                </td>
                <td className="py-3 px-4">
                  <DistributionStatusBadge status={log.status} />
                </td>
                <td className="py-3 px-4 text-right">
                  <Link
                    to={`/admin/distributions/${log.id}`}
                    className="inline-flex p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-surface-muted transition-colors"
                    title="Xem chi tiết"
                  >
                    <Eye size={16} />
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
