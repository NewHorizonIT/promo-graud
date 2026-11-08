const STATUS_MAP = {
  ACTIVE: {
    label: 'Hoạt động',
    className: 'bg-success-bg text-success-text border-success-text/20',
  },
  DISABLED: { label: 'Đã tắt', className: 'bg-error-bg text-error-text border-error-text/20' },
};

export default function VoucherStatusBadge({ status }) {
  const cfg = STATUS_MAP[status] ?? STATUS_MAP.DISABLED;
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cfg.className}`}
    >
      {cfg.label}
    </span>
  );
}
