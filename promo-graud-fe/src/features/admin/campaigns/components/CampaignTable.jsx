import { Link } from 'react-router-dom';
import { Pencil, Trash2, Eye } from 'lucide-react';
import CampaignStatusBadge from './CampaignStatusBadge';
import { formatCurrency } from '../../../../utils/format';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN');
}

export default function CampaignTable({ campaigns = [], onDelete }) {
  if (!campaigns.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          📋
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có campaign nào</p>
        <p className="text-xs text-text-muted mt-1">Tạo campaign đầu tiên để bắt đầu</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Tên Campaign
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thời gian
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Ngân sách
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {campaigns.map((c) => (
              <tr key={c.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4">
                  <span className="font-medium text-text-primary">{c.name}</span>
                </td>
                <td className="py-3 px-4">
                  <CampaignStatusBadge status={c.status} />
                </td>
                <td className="py-3 px-4 text-text-secondary text-xs">
                  {formatDate(c.startTime)} → {formatDate(c.endTime)}
                </td>
                <td className="py-3 px-4 font-medium text-text-primary">
                  {formatCurrency(c.promotionBudget)}
                </td>
                <td className="py-3 px-4">
                  <div className="flex items-center justify-end gap-1">
                    <Link
                      to={`/admin/campaigns/${c.id}`}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                      title="Xem chi tiết"
                    >
                      <Eye size={15} />
                    </Link>
                    <Link
                      to={`/admin/campaigns/${c.id}/edit`}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                      title="Chỉnh sửa"
                    >
                      <Pencil size={15} />
                    </Link>
                    <button
                      type="button"
                      onClick={() => onDelete?.(c)}
                      className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer"
                      title="Xóa"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
