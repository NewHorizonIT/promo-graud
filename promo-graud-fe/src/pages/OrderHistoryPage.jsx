import { useState, useEffect } from 'react';
import { ShoppingBag, ChevronLeft, ChevronRight, X, Receipt } from 'lucide-react';
import { useMyOrders } from '../features/user/hooks/useOrders';
import { formatCurrency } from '../utils/format';

const STATUS_STYLE = {
  SUCCESS: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
  CANCELLED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
};

const STATUS_LABEL = {
  SUCCESS: 'Thành công',
  PENDING: 'Đang xử lý',
  CANCELLED: 'Đã hủy',
};

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function StatusBadge({ status }) {
  return (
    <span
      className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
        STATUS_STYLE[status] ?? 'bg-surface-muted text-text-secondary'
      }`}
    >
      {STATUS_LABEL[status] ?? status}
    </span>
  );
}

function OrderModal({ order, onClose }) {
  useEffect(() => {
    const handleKey = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', handleKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = '';
    };
  }, [onClose]);

  const discount = Number(order.totalPrice ?? 0) - Number(order.finalPrice ?? 0);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-surface rounded-2xl w-full max-w-lg max-h-[85vh] overflow-y-auto"
        style={{ boxShadow: 'var(--shadow-level-2)' }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border sticky top-0 bg-surface rounded-t-2xl">
          <div>
            <h2 className="text-base font-bold text-text-primary">Đơn hàng #{order.id}</h2>
            <p className="text-xs text-text-secondary mt-0.5">{formatDate(order.createdAt)}</p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-surface-muted text-text-muted hover:text-text-primary transition-colors cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        <div className="px-6 py-5 space-y-5">
          {/* Items */}
          {order.items?.length > 0 ? (
            <div>
              <h3 className="text-sm font-semibold text-text-primary mb-3">Sản phẩm đã đặt</h3>
              <div className="divide-y divide-border">
                {order.items.map((item) => (
                  <div key={item.id} className="flex items-center justify-between gap-4 py-3">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-text-primary truncate">
                        {item.productName}
                      </p>
                      <p className="text-xs text-text-secondary mt-0.5">
                        {formatCurrency(item.price)} × {item.quantity}
                      </p>
                    </div>
                    <span className="text-sm font-semibold text-text-primary shrink-0">
                      {formatCurrency(Number(item.price) * item.quantity)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="text-sm text-text-secondary text-center py-4">
              Không có thông tin sản phẩm.
            </p>
          )}

          {/* Voucher */}
          {order.voucher && (
            <div className="flex items-center justify-between text-sm">
              <span className="text-text-secondary">Voucher</span>
              <span className="font-mono font-semibold text-primary bg-primary/10 px-2.5 py-0.5 rounded-lg">
                {order.voucher.code}
              </span>
            </div>
          )}

          {/* Totals */}
          <div className="bg-surface-muted rounded-xl px-4 py-4 space-y-2.5">
            <div className="flex justify-between text-sm">
              <span className="text-text-secondary">Giá gốc</span>
              <span className="text-text-primary">{formatCurrency(order.totalPrice)}</span>
            </div>
            {discount > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-text-secondary">Giảm giá</span>
                <span className="text-green-600 dark:text-green-400">
                  -{formatCurrency(discount)}
                </span>
              </div>
            )}
            <div className="flex justify-between text-sm font-bold border-t border-border pt-2.5">
              <span className="text-text-primary">Thành tiền</span>
              <span className="text-primary text-base">{formatCurrency(order.finalPrice)}</span>
            </div>
          </div>

          {/* Status */}
          <div className="flex items-center justify-between text-sm">
            <span className="text-text-secondary">Trạng thái</span>
            <StatusBadge status={order.status} />
          </div>
        </div>
      </div>
    </div>
  );
}

function TableSkeleton() {
  return (
    <div className="bg-surface rounded-2xl border border-border overflow-hidden animate-pulse">
      <div className="h-11 bg-surface-muted border-b border-border" />
      {[...Array(5)].map((_, i) => (
        <div key={i} className="flex gap-4 px-4 py-3.5 border-b border-border last:border-0">
          <div className="h-4 w-16 rounded bg-surface-muted" />
          <div className="h-4 w-32 rounded bg-surface-muted" />
          <div className="h-4 w-24 rounded bg-surface-muted ml-auto" />
          <div className="h-4 w-24 rounded bg-surface-muted" />
          <div className="h-5 w-20 rounded-full bg-surface-muted" />
        </div>
      ))}
    </div>
  );
}

export default function OrderHistoryPage() {
  const [page, setPage] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const { data: ordersData, isLoading, isError } = useMyOrders({ page, limit: 10 });

  const orders = ordersData?.data ?? [];
  const totalPages = ordersData?.totalPages ?? 0;
  const totalItems = ordersData?.totalItems ?? 0;

  return (
    <>
      <div className="max-w-5xl mx-auto px-4 py-10">
        {/* Page header */}
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary shrink-0">
            <Receipt size={20} />
          </div>
          <div>
            <h1 className="text-xl font-bold text-text-primary leading-none">Lịch sử mua hàng</h1>
            {!isLoading && totalItems > 0 && (
              <p className="text-xs text-text-secondary mt-1">{totalItems} đơn hàng</p>
            )}
          </div>
        </div>

        {/* Content */}
        {isLoading ? (
          <TableSkeleton />
        ) : isError ? (
          <div className="bg-surface rounded-2xl border border-border px-6 py-16 text-center">
            <ShoppingBag size={40} className="mx-auto text-text-muted mb-3" />
            <p className="text-sm text-text-secondary">Không thể tải lịch sử đơn hàng.</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="bg-surface rounded-2xl border border-border px-6 py-16 text-center">
            <ShoppingBag size={40} className="mx-auto text-text-muted mb-3" />
            <p className="text-sm font-medium text-text-primary">Bạn chưa có đơn hàng nào</p>
            <p className="text-xs text-text-secondary mt-1">
              Các đơn hàng của bạn sẽ xuất hiện ở đây sau khi thanh toán.
            </p>
          </div>
        ) : (
          <div className="bg-surface rounded-2xl border border-border overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border bg-surface-muted">
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Mã đơn
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Ngày đặt
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Voucher
                    </th>
                    <th className="text-right px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Giá gốc
                    </th>
                    <th className="text-right px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Thành tiền
                    </th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-text-secondary whitespace-nowrap">
                      Trạng thái
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {orders.map((order) => (
                    <tr
                      key={order.id}
                      onClick={() => setSelectedOrder(order)}
                      className="hover:bg-surface-muted transition-colors cursor-pointer"
                    >
                      <td className="px-4 py-3.5 font-mono font-semibold text-text-primary">
                        #{order.id}
                      </td>
                      <td className="px-4 py-3.5 text-text-secondary whitespace-nowrap">
                        {formatDate(order.createdAt)}
                      </td>
                      <td className="px-4 py-3.5">
                        {order.voucher ? (
                          <span className="font-mono text-xs font-semibold text-primary bg-primary/10 px-2 py-0.5 rounded-lg">
                            {order.voucher.code}
                          </span>
                        ) : (
                          <span className="text-text-muted">—</span>
                        )}
                      </td>
                      <td className="px-4 py-3.5 text-right text-text-secondary whitespace-nowrap">
                        {formatCurrency(order.totalPrice)}
                      </td>
                      <td className="px-4 py-3.5 text-right font-semibold text-text-primary whitespace-nowrap">
                        {formatCurrency(order.finalPrice)}
                      </td>
                      <td className="px-4 py-3.5 text-center">
                        <StatusBadge status={order.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-4 py-3 border-t border-border">
                <span className="text-xs text-text-secondary">
                  Trang {page + 1} / {totalPages}
                </span>
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="p-1.5 rounded-lg hover:bg-surface-muted text-text-secondary disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
                  >
                    <ChevronLeft size={16} />
                  </button>
                  <button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="p-1.5 rounded-lg hover:bg-surface-muted text-text-secondary disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
                  >
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {selectedOrder && <OrderModal order={selectedOrder} onClose={() => setSelectedOrder(null)} />}
    </>
  );
}
