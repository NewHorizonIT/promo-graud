import { useCart } from '../../../../store/CartContext';
import { useUpdateStatusOrder } from './useAdminOrders';

export function getAllowedTransitions(order) {
  if (!order || order.status !== 'PENDING') return [];
  const isCod = order.paymentMethod === 'COD';
  const next = [];
  if (isCod || order.paymentStatus === 'PAID') next.push('SUCCESS');
  if (isCod || order.paymentStatus === 'UNPAID') next.push('CANCELLED');
  return next;
}

export function useOrderStatusChange(order) {
  const { showToast } = useCart();
  const { mutate, isPending } = useUpdateStatusOrder();

  const change = (target, onDone) => {
    if (target === 'CANCELLED' && !window.confirm(`Hủy đơn #${order.id}?`)) return;
    mutate(
      { id: order.id, body: { status: target } },
      {
        onSuccess: () => {
          showToast('Đã cập nhật trạng thái đơn', 'success', 3000);
          onDone?.();
        },
        onError: (err) =>
          showToast(err.response?.data?.message || 'Cập nhật thất bại', 'error', 4000),
      },
    );
  };

  return { options: getAllowedTransitions(order), change, isPending };
}
