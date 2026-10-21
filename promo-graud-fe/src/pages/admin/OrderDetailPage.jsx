import { useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useAdminOrder } from '../../features/admin/orders/hooks/useAdminOrders';
import { formatCurrency } from '../../utils/format';

const STATUS_STYLES = {
  SUCCESS: 'bg-success-bg text-success-text',
  PENDING: 'bg-warning-bg text-warning-text',
  FAILED: 'bg-error-bg text-error-text',
  CANCELLED: 'bg-surface-muted text-text-muted',
};

const STATUS_LABELS = {
  SUCCESS: 'Thành công',
  PENDING: 'Đang xử lý',
  FAILED: 'Thất bại',
  CANCELLED: 'Đã hủy',
};

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

function InfoRow({ label, value }) {
  return (
    <div className="flex justify-between items-center py-2.5 border-b border-border last:border-0">
      <span className="text-sm text-text-muted">{label}</span>
      <span className="text-sm font-medium text-text-primary">{value ?? '—'}</span>
    </div>
  );
}

export default function OrderDetailPage() {
  const { id } = useParams();
  const { data: order, isLoading } = useAdminOrder(id);

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-24 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  if (!order) {
    return <p className="text-sm text-text-muted">Không tìm thấy đơn hàng.</p>;
  }

  const discount = (order.totalPrice ?? 0) - (order.finalPrice ?? 0);

  return (
    <div className="space-y-5 max-w-3xl">
      <Link
        to="/admin/orders"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Danh sách Orders
      </Link>

      {/* Header */}
      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary">Đơn hàng #{order.id}</h2>
            <p className="text-sm text-text-muted mt-1">{formatDate(order.createdAt)}</p>
          </div>
          <span
            className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold ${
              STATUS_STYLES[order.status] ?? 'bg-surface-muted text-text-muted'
            }`}
          >
            {STATUS_LABELS[order.status] ?? order.status}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Customer */}
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          <h3 className="text-sm font-bold text-text-primary mb-3">Khách hàng</h3>
          <InfoRow label="Username" value={order.user?.username} />
          <InfoRow label="Email" value={order.user?.email} />
        </div>

        {/* Voucher */}
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          <h3 className="text-sm font-bold text-text-primary mb-3">Voucher</h3>
          {order.voucher ? (
            <InfoRow label="Mã voucher" value={order.voucher.code} />
          ) : (
            <p className="text-sm text-text-muted">Không dùng voucher</p>
          )}
        </div>
      </div>

      {/* Items */}
      <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
        <div className="px-5 py-4 border-b border-border">
          <h3 className="text-sm font-bold text-text-primary">Sản phẩm</h3>
        </div>
        <table className="w-full text-sm">
          <thead className="bg-surface-muted">
            <tr>
              {['Sản phẩm', 'Đơn giá', 'Số lượng', 'Thành tiền'].map((h) => (
                <th
                  key={h}
                  className="text-left py-3 px-5 text-xs font-semibold text-text-muted uppercase tracking-wider"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {(order.items ?? []).map((item) => (
              <tr key={item.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-5 font-medium text-text-primary">{item.productName}</td>
                <td className="py-3 px-5 text-text-secondary">{formatCurrency(item.price)}</td>
                <td className="py-3 px-5 text-text-secondary">{item.quantity}</td>
                <td className="py-3 px-5 font-medium text-text-primary">
                  {formatCurrency((item.price ?? 0) * (item.quantity ?? 0))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Price summary */}
        <div className="px-5 py-4 border-t border-border space-y-2 bg-surface-muted/30">
          <div className="flex justify-between text-sm text-text-secondary">
            <span>Tạm tính</span>
            <span>{formatCurrency(order.totalPrice)}</span>
          </div>
          {discount > 0 && (
            <div className="flex justify-between text-sm text-success-text">
              <span>Giảm giá</span>
              <span>- {formatCurrency(discount)}</span>
            </div>
          )}
          <div className="flex justify-between text-base font-bold text-text-primary pt-2 border-t border-border">
            <span>Tổng cộng</span>
            <span>{formatCurrency(order.finalPrice)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
