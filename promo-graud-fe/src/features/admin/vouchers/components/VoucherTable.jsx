import { Link } from 'react-router-dom';
import { Eye, Trash2, Send } from 'lucide-react';
import VoucherStatusBadge from './VoucherStatusBadge';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN');
}

export default function VoucherTable({ vouchers = [], onDelete, onDistribute }) {
  if (!vouchers.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          🎫
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có voucher nào</p>
        <p className="text-xs text-text-muted mt-1">
          Tạo voucher mới từ một campaign đang hoạt động
        </p>
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
                Mã
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Loại
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Còn lại / Tổng
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Hết hạn
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Kênh
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {vouchers.map((v) => (
              <tr key={v.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4 font-mono text-xs font-semibold text-text-primary">
                  {v.code}
                </td>
                <td className="py-3 px-4">
                  <span
                    className={`px-2 py-0.5 text-xs font-semibold rounded-sm ${
                      v.type === 'GENERIC'
                        ? 'bg-primary-light text-primary'
                        : 'bg-surface-muted text-text-secondary'
                    }`}
                  >
                    {v.type === 'GENERIC' ? 'Generic' : 'Unique'}
                  </span>
                </td>
                <td className="py-3 px-4">
                  <VoucherStatusBadge status={v.status} />
                </td>
                <td className="py-3 px-4">
                  <div className="flex items-center gap-2">
                    <div className="w-20 h-1.5 bg-border rounded-full overflow-hidden">
                      <div
                        className="bg-primary h-full rounded-full"
                        style={{
                          width: `${v.quantity > 0 ? (v.quantityRemain / v.quantity) * 100 : 0}%`,
                        }}
                      />
                    </div>
                    <span className="text-xs text-text-muted">
                      {v.quantityRemain}/{v.quantity}
                    </span>
                  </div>
                </td>
                <td className="py-3 px-4 text-xs text-text-secondary">
                  {formatDate(v.expiredAt)}
                </td>
                <td className="py-3 px-4 text-xs text-text-secondary capitalize">
                  {v.distributionChannel}
                </td>
                <td className="py-3 px-4">
                  <div className="flex items-center justify-end gap-1">
                    <Link
                      to={`/admin/vouchers/${v.id}`}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                      title="Xem chi tiết"
                    >
                      <Eye size={15} />
                    </Link>
                    <button
                      type="button"
                      onClick={() => onDistribute?.(v)}
                      className="p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-primary-light transition-colors cursor-pointer"
                      title="Phân phối"
                    >
                      <Send size={15} />
                    </button>
                    <button
                      type="button"
                      onClick={() => onDelete?.(v)}
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
