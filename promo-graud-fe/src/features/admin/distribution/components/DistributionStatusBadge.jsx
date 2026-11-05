const STATUS_MAP = {
  PENDING: { label: 'Đang gửi', className: 'bg-amber-50 text-amber-600 border-amber-200' },
  SENT: {
    label: 'Thành công',
    className: 'bg-success-bg text-success-text border-success-text/20',
  },
  FAILED: { label: 'Thất bại', className: 'bg-error-bg text-error-text border-error-text/20' },
  DLQ: { label: 'DLQ', className: 'bg-surface-muted text-text-secondary border-border' },
};

export const CHANNEL_LABELS = {
  EMAIL: 'Email',
  WEBHOOK: 'Webhook',
};

export default function DistributionStatusBadge({ status }) {
  const cfg = STATUS_MAP[status] ?? STATUS_MAP.PENDING;
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cfg.className}`}
    >
      {cfg.label}
    </span>
  );
}
