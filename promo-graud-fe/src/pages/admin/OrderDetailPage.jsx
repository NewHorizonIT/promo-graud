import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, XCircle, CheckCircle2 } from 'lucide-react';
import { useAdminOrder } from '../../features/admin/orders/hooks/useAdminOrders';
import { formatCurrency } from '../../utils/format';
import {
  PaymentMethodBadge,
  PaymentStatusBadge,
  StatusBadge,
} from '../../features/admin/orders/components/OrderBadges';
import { useOrderStatusChange } from '../../features/admin/orders/hooks/useOrderStatusChange';

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
    <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-1 sm:gap-4 py-3 border-b border-border last:border-0">
      <span className="text-sm text-text-muted shrink-0">{label}</span>
      <span className="text-sm font-medium text-text-primary text-left sm:text-right break-all">
        {value ?? '—'}
      </span>
    </div>
  );
}

export default function OrderDetailPage() {
  const { id } = useParams();
  const { data: order, isLoading } = useAdminOrder(id);
  const { options, change, isPending } = useOrderStatusChange(order);

  if (isLoading) {
    return (
      <div className="w-full max-w-5xl mx-auto space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-24 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  if (!order) {
    return (
      <div className="w-full max-w-5xl mx-auto">
        <p className="text-sm text-text-muted">Không tìm thấy đơn hàng.</p>
      </div>
    );
  }

  const discount = (order.totalPrice ?? 0) - (order.finalPrice ?? 0);

  return (
    <div className="w-full max-w-5xl mx-auto space-y-5 min-w-0">
      {/* Back */}
      <Link
        to="/admin/orders"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} />
        Danh sách Orders
      </Link>

      {/* Order header */}
      <div className="bg-surface border border-border rounded-2xl p-4 sm:p-6 shadow-level-1">
        <div className="flex flex-col gap-5">
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
            <div className="min-w-0">
              <h2 className="text-xl sm:text-2xl font-bold text-text-primary">
                Đơn hàng #{order.id}
              </h2>
              <p className="text-sm text-text-muted mt-1">{formatDate(order.createdAt)}</p>
            </div>

            {/* Status badges */}
            <div className="flex flex-wrap items-center gap-2">
              <StatusBadge status={order.status} size="md" />
              <PaymentStatusBadge status={order.paymentStatus} size="md" />
              <PaymentMethodBadge method={order.paymentMethod} size="md" />
            </div>
          </div>

          {/* Status actions */}
          {options.length > 0 && (
            <div className="flex flex-col sm:flex-row sm:justify-end gap-2 pt-4 border-t border-border">
              {options.includes('SUCCESS') && (
                <button
                  type="button"
                  disabled={isPending}
                  onClick={() => change('SUCCESS')}
                  className="w-full sm:w-auto inline-flex justify-center items-center gap-1.5 px-4 py-2.5 rounded-xl bg-primary text-white text-sm font-semibold hover:opacity-90 disabled:opacity-50 transition-all cursor-pointer"
                >
                  <CheckCircle2 size={15} />
                  Xác nhận hoàn thành
                </button>
              )}

              {options.includes('CANCELLED') && (
                <button
                  type="button"
                  disabled={isPending}
                  onClick={() => change('CANCELLED')}
                  className="w-full sm:w-auto inline-flex justify-center items-center gap-1.5 px-4 py-2.5 rounded-xl border border-error-text/30 text-error-text text-sm font-semibold hover:bg-error-bg disabled:opacity-50 transition-colors cursor-pointer"
                >
                  <XCircle size={15} />
                  Hủy đơn
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Customer and voucher */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Customer */}
        <div className="min-w-0 bg-surface border border-border rounded-xl p-4 sm:p-5 shadow-level-1">
          <h3 className="text-sm font-bold text-text-primary mb-3">Khách hàng</h3>

          <InfoRow label="Username" value={order.user?.username} />
          <InfoRow label="Email" value={order.user?.email} />
        </div>

        {/* Voucher */}
        <div className="min-w-0 bg-surface border border-border rounded-xl p-4 sm:p-5 shadow-level-1">
          <h3 className="text-sm font-bold text-text-primary mb-3">Voucher</h3>

          {order.voucher ? (
            <InfoRow label="Mã voucher" value={order.voucher.code} />
          ) : (
            <p className="text-sm text-text-muted">Không dùng voucher</p>
          )}
        </div>
      </div>

      {/* Products */}
      <div className="min-w-0 bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
        <div className="px-4 sm:px-5 py-4 border-b border-border">
          <h3 className="text-sm font-bold text-text-primary">Sản phẩm</h3>
        </div>

        <div className="w-full overflow-x-auto">
          <table className="w-full min-w-[600px] text-sm">
            <thead className="bg-surface-muted">
              <tr>
                {['Sản phẩm', 'Đơn giá', 'Số lượng', 'Thành tiền'].map((h) => (
                  <th
                    key={h}
                    className="text-left py-3 px-4 sm:px-5 text-xs font-semibold text-text-muted uppercase tracking-wider whitespace-nowrap"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>

            <tbody className="divide-y divide-border">
              {(order.items ?? []).map((item) => (
                <tr key={item.id} className="hover:bg-surface-muted/40 transition-colors">
                  <td className="py-3 px-4 sm:px-5 font-medium text-text-primary">
                    {item.productName}
                  </td>

                  <td className="py-3 px-4 sm:px-5 text-text-secondary whitespace-nowrap">
                    {formatCurrency(item.price)}
                  </td>

                  <td className="py-3 px-4 sm:px-5 text-text-secondary">{item.quantity}</td>

                  <td className="py-3 px-4 sm:px-5 font-medium text-text-primary whitespace-nowrap">
                    {formatCurrency((item.price ?? 0) * (item.quantity ?? 0))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Price summary */}
        <div className="px-4 sm:px-5 py-4 border-t border-border space-y-3 bg-surface-muted/30">
          <div className="flex justify-between items-center gap-4 text-sm text-text-secondary">
            <span>Tạm tính</span>
            <span className="text-right whitespace-nowrap">{formatCurrency(order.totalPrice)}</span>
          </div>

          {discount > 0 && (
            <div className="flex justify-between items-center gap-4 text-sm text-success-text">
              <span>Giảm giá</span>
              <span className="text-right whitespace-nowrap">- {formatCurrency(discount)}</span>
            </div>
          )}

          <div className="flex justify-between items-center gap-4 text-base sm:text-lg font-bold text-text-primary pt-3 border-t border-border">
            <span>Tổng cộng</span>
            <span className="text-right whitespace-nowrap">{formatCurrency(order.finalPrice)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
