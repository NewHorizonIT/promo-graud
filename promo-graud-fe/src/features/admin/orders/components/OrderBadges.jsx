const SIZE = { sm: 'text-xs px-2 py-0.5', md: 'text-sm px-3 py-1' };
const FALLBACK = 'bg-surface-muted text-text-secondary';

const STATUS = {
  SUCCESS: ['Thành công', 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'],
  PENDING: [
    'Đang xử lý',
    'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
  ],
  CANCELLED: ['Đã hủy', 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'],
  FAILED: ['Thất bại', 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'],
};
const PAY_STATUS = {
  PAID: ['Đã thanh toán', 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'],
  UNPAID: [
    'Chưa thanh toán',
    'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
  ],
  FAILED: ['Thanh toán lỗi', 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'],
};
const METHOD = {
  COD: ['COD', 'bg-slate-100 text-slate-700 dark:bg-slate-800/50 dark:text-slate-300'],
  VNP: ['VNPay', 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'],
};

function Badge({ map, value, size }) {
  const [label, style] = map[value] ?? [value, FALLBACK];
  return (
    <span className={`inline-flex items-center font-semibold rounded-full ${SIZE[size]} ${style}`}>
      {label}
    </span>
  );
}

export const StatusBadge = ({ status, size = 'sm' }) => (
  <Badge map={STATUS} value={status} size={size} />
);
export const PaymentStatusBadge = ({ status, size = 'sm' }) => (
  <Badge map={PAY_STATUS} value={status} size={size} />
);
export const PaymentMethodBadge = ({ method, size = 'sm' }) => (
  <Badge map={METHOD} value={method} size={size} />
);
