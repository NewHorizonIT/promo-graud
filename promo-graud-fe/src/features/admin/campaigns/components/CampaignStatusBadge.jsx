const STATUS_MAP = {
  chua_dien_ra: { label: 'Chưa diễn ra', className: 'bg-amber-50 text-amber-600 border-amber-200' },
  dang_dien_ra: {
    label: 'Đang diễn ra',
    className: 'bg-success-bg text-success-text border-success-text/20',
  },
  da_dong: { label: 'Đã đóng', className: 'bg-surface-muted text-text-muted border-border' },
};

export default function CampaignStatusBadge({ status }) {
  const cfg = STATUS_MAP[status] ?? STATUS_MAP.da_dong;
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cfg.className}`}
    >
      {cfg.label}
    </span>
  );
}
