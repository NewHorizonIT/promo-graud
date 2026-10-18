const STATUS_MAP = {
  active: {
    label: 'Hoạt động',
    className: 'bg-success-bg text-success-text border-success-text/20',
  },
  expired: { label: 'Hết hạn', className: 'bg-error-bg text-error-text border-error-text/20' },
  depleted: { label: 'Hết lượt', className: 'bg-surface-muted text-text-muted border-border' },
};

export default function VoucherStatusBadge({ status }) {
  const cfg = STATUS_MAP[status] ?? STATUS_MAP.active;
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cfg.className}`}
    >
      {cfg.label}
    </span>
  );
}
