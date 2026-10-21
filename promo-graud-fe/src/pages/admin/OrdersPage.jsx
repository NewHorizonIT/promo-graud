import { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Search, Eye } from 'lucide-react';
import { useAdminOrders, useAdminOrder } from '../../features/admin/orders/hooks/useAdminOrders';
import { formatCurrency } from '../../utils/format';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'SUCCESS', label: 'Thành công' },
  { value: 'PENDING', label: 'Đang xử lý' },
  { value: 'FAILED', label: 'Thất bại' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

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

const PAGE_SIZE = 10;

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function OrdersPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const status = searchParams.get('status') ?? '';
  const page = Math.max(0, Number(searchParams.get('page') ?? 0));

  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const t = setTimeout(() => setSearch(searchInput.trim()), 400);
    return () => clearTimeout(t);
  }, [searchInput]);

  // Reset to page 0 when search changes
  useEffect(() => {
    setSearchParams(
      (prev) => {
        prev.delete('page');
        return prev;
      },
      { replace: true },
    );
  }, [search, setSearchParams]);

  const isIdSearch = search !== '' && /^\d+$/.test(search);

  const { data: listData, isLoading: listLoading } = useAdminOrders({
    page,
    size: PAGE_SIZE,
    status: status || undefined,
    username: !isIdSearch && search ? search : undefined,
  });

  const { data: orderById, isLoading: idLoading } = useAdminOrder(
    isIdSearch ? Number(search) : undefined,
  );

  const isLoading = isIdSearch ? idLoading : listLoading;
  const orders = isIdSearch ? (orderById ? [orderById] : []) : (listData?.data ?? []);
  const totalItems = isIdSearch ? (orderById ? 1 : 0) : (listData?.totalItems ?? 0);
  const totalPages = isIdSearch ? 1 : (listData?.totalPages ?? 1);

  const setStatus = (val) => {
    setSearchParams(val ? { status: val } : {});
  };

  const setPage = (pg) => {
    setSearchParams({ ...(status ? { status } : {}), page: String(pg) });
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center gap-3">
        {/* Search input */}
        <div className="relative flex-1 max-w-sm">
          <Search
            size={15}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none"
          />
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Order ID hoặc username..."
            className="w-full h-10 pl-9 pr-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
          />
        </div>
        <p className="text-sm text-text-muted shrink-0">{totalItems} đơn hàng</p>
      </div>

      {/* Status filter */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setStatus(f.value)}
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

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-muted border-b border-border">
                <tr>
                  {[
                    '#ID',
                    'Khách hàng',
                    'Trạng thái',
                    'Tổng tiền',
                    'Sau giảm',
                    'Voucher',
                    'Thời gian',
                    '',
                  ].map((h) => (
                    <th
                      key={h}
                      className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-14 text-center text-sm text-text-muted">
                      {search ? 'Không tìm thấy kết quả' : 'Không có đơn hàng nào'}
                    </td>
                  </tr>
                ) : (
                  orders.map((o) => (
                    <tr key={o.id} className="hover:bg-surface-muted/40 transition-colors">
                      <td className="py-3 px-4 font-medium text-text-primary">#{o.id}</td>
                      <td className="py-3 px-4 text-text-secondary">{o.user?.username ?? '—'}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                            STATUS_STYLES[o.status] ?? 'bg-surface-muted text-text-muted'
                          }`}
                        >
                          {STATUS_LABELS[o.status] ?? o.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-text-secondary">
                        {formatCurrency(o.totalPrice)}
                      </td>
                      <td className="py-3 px-4 font-medium text-text-primary">
                        {formatCurrency(o.finalPrice)}
                      </td>
                      <td className="py-3 px-4 text-text-muted text-xs">
                        {o.voucher?.code ?? '—'}
                      </td>
                      <td className="py-3 px-4 text-text-muted text-xs">
                        {formatDate(o.createdAt)}
                      </td>
                      <td className="py-3 px-4">
                        <Link
                          to={`/admin/orders/${o.id}`}
                          className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors inline-flex"
                          title="Xem chi tiết"
                        >
                          <Eye size={15} />
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => (
            <button
              key={i}
              type="button"
              onClick={() => setPage(i)}
              className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                i === page
                  ? 'bg-primary text-white shadow-glow'
                  : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
              }`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
