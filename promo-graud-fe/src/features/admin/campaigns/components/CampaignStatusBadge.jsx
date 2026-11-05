const STATUS_MAP = {
  UPCOMING: { label: 'Chưa diễn ra', className: 'bg-amber-50 text-amber-600 border-amber-200' },
  ACTIVE: {
    label: 'Đang diễn ra',
    className: 'bg-success-bg text-success-text border-success-text/20',
  },
  ENDED: { label: 'Đã đóng', className: 'bg-surface-muted text-text-muted border-border' },
};

export default function CampaignStatusBadge({ status }) {
  const cfg = STATUS_MAP[status] ?? STATUS_MAP.ENDED;
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cfg.className}`}
    >
      {cfg.label}
    </span>
  );
}
