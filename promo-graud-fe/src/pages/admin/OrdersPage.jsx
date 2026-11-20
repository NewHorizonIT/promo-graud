import { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Search, Eye, ChevronDown, X, Trash2 } from 'lucide-react';
import {
  useAdminOrders,
  useAdminOrder,
  useDeleteOrder,
} from '../../features/admin/orders/hooks/useAdminOrders';
import { formatCurrency } from '../../utils/format';
import { StatusBadge } from '../../features/admin/orders/components/OrderBadges';
import { useOrderStatusChange } from '../../features/admin/orders/hooks/useOrderStatusChange';
import ConfirmModal from '../../components/ConfirmModal';
import { useCart } from '../../store/CartContext';

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'SUCCESS', label: 'Thành công' },
  { value: 'PENDING', label: 'Đang xử lý' },
  { value: 'FAILED', label: 'Thất bại' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

function StatusCell({ order }) {
  const [editing, setEditing] = useState(false);
  const { options, change, isPending } = useOrderStatusChange(order);

  if (options.length === 0) return <StatusBadge status={order.status} />;

  if (!editing) {
    return (
      <button
        type="button"
        onClick={() => setEditing(true)}
        title="Bấm để đổi trạng thái"
        className="inline-flex items-center gap-1 cursor-pointer group"
      >
        <StatusBadge status={order.status} />
        <ChevronDown size={13} className="text-text-muted group-hover:text-text-primary" />
      </button>
    );
  }

  return (
    <div className="inline-flex items-center gap-1.5">
      {options.map((s) => (
        <button
          key={s}
          type="button"
          disabled={isPending}
          onClick={() => change(s, () => setEditing(false))}
          className="cursor-pointer disabled:opacity-50"
        >
          <StatusBadge status={s} />
        </button>
      ))}
      <button
        type="button"
        onClick={() => setEditing(false)}
        aria-label="Đóng"
        className="p-0.5 text-text-muted hover:text-text-primary cursor-pointer"
      >
        <X size={13} />
      </button>
    </div>
  );
}

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
  // SỬA: Đặt giá trị mặc định là 1 (trùng với 1-based index ở Backend)
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const { showToast } = useCart();
  // --- STATE QUẢN LÝ CONFIRM MODAL ---
  const [modalState, setModalState] = useState({
    isOpen: false,
    title: '',
    message: '',
    confirmLabel: '',
    onConfirm: null,
    isPending: false, // Dùng để khóa nút khi đang xử lý API
  });

  const deleteMutation = useDeleteOrder();
  useEffect(() => {
    const t = setTimeout(() => setSearch(searchInput.trim()), 400);
    return () => clearTimeout(t);
  }, [searchInput]);

  // SỬA: Xóa param page khi tìm kiếm từ khóa mới nhưng giữ nguyên trạng thái searchParams khác
  useEffect(() => {
    if (search !== '') {
      setSearchParams(
        (prev) => {
          prev.delete('page');
          return prev;
        },
        { replace: true },
      );
    }
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

  // SỬA: Giữ lại searchParams hiện tại khi chuyển status filter
  const setStatus = (val) => {
    setSearchParams((prev) => {
      if (val) prev.set('status', val);
      else prev.delete('status');
      prev.delete('page');
      return prev;
    });
  };

  // SỬA: Giữ lại searchParams hiện tại khi chuyển trang
  const setPage = (pg) => {
    setSearchParams((prev) => {
      prev.set('page', String(pg));
      return prev;
    });
  };

  // --- HÀM ĐÓNG MODAL ---
  const closeModal = () => setModalState((prev) => ({ ...prev, isOpen: false }));

  // --- HÀM MỞ MODAL XÓA ĐƠN HÀNG ---
  const handleRequestDelete = (order) => {
    setModalState({
      isOpen: true,
      title: 'Xóa đơn hàng',
      message: `Bạn có chắc chắn muốn xóa vĩnh viễn đơn hàng #${order.id}? Hành động này không thể hoàn tác.`,
      confirmLabel: 'Xóa',
      isPending: false,
      onConfirm: async () => {
        setModalState((prev) => ({ ...prev, isPending: true }));
        try {
          // Gọi API xóa
          await deleteMutation.mutateAsync(order.id);
          // NẾU THÀNH CÔNG: Hiện Toast thành công
          showToast('Đã xóa đơn hàng thành công!', 'success');
        } catch (error) {
          // NẾU BỊ BACKEND CHẶN LẠI: Lấy message từ AppException của Backend hiện lên Toast
          const errorMsg = error.response?.data?.message || 'Không thể xóa đơn hàng này';
          showToast(errorMsg, 'error');
        } finally {
          // Dù thành công hay thất bại cũng đóng Modal
          closeModal();
        }
      },
    });
  };

  // --- HÀM MỞ MODAL THAY ĐỔI TRẠNG THÁI ---
  const handleRequestStatusChange = (order, newStatus, executeChange) => {
    setModalState({
      isOpen: true,
      title: 'Xác nhận đổi trạng thái',
      message: `Bạn có chắc chắn muốn chuyển trạng thái đơn hàng #${order.id} thành "${newStatus}"?`,
      confirmLabel: 'Xác nhận',
      isPending: false,
      onConfirm: async () => {
        setModalState((prev) => ({ ...prev, isPending: true }));
        try {
          // Gọi hàm `change()` được truyền từ StatusCell
          await executeChange();
        } finally {
          closeModal();
        }
      },
    });
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
                        <StatusCell order={o} onStatusChangeRequest={handleRequestStatusChange} />
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
                        <div className="flex items-center justify-end gap-1">
                          <Link
                            to={`/admin/orders/${o.id}`}
                            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors inline-flex"
                            title="Xem chi tiết"
                          >
                            <Eye size={15} />
                          </Link>
                          {/* --- NÚT XÓA --- */}
                          <button
                            type="button"
                            onClick={() => handleRequestDelete(o)}
                            className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors inline-flex cursor-pointer"
                            title="Xóa đơn hàng"
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SỬA: Phân trang tính từ 1 thay vì 0 */}
      {totalPages > 0 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pageNumber = i + 1;
            return (
              <button
                key={pageNumber}
                type="button"
                onClick={() => setPage(pageNumber)}
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pageNumber === page
                    ? 'bg-primary text-white shadow-glow'
                    : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                }`}
              >
                {pageNumber}
              </button>
            );
          })}
        </div>
      )}
      {modalState.isOpen && (
        <ConfirmModal
          title={modalState.title}
          message={modalState.message}
          confirmLabel={modalState.confirmLabel}
          loading={modalState.isPending}
          onConfirm={modalState.onConfirm}
          onClose={closeModal}
        />
      )}
    </div>
  );
}
