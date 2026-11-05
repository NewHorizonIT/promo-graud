import { Calendar, Tag } from 'lucide-react';

function MiniVoucherCard({ voucher }) {
  return (
    <div className="relative bg-surface rounded-lg border border-border flex h-16 overflow-hidden">
      <div className="absolute -left-1.5 top-1/2 -translate-y-1/2 w-3 h-3 bg-background rounded-full border border-border z-10" />

      <div className="w-[38%] border-r border-dashed border-border flex items-center justify-center bg-success-bg/30 shrink-0">
        <span className="text-success-text text-[10px] font-bold text-center leading-tight px-1">
          GIẢM GIÁ
        </span>
      </div>

      <div className="flex-1 px-2 flex flex-col justify-center min-w-0">
        <p className="text-[10px] font-semibold text-text-primary truncate">{voucher.code}</p>
        <p className="text-[9px] text-text-muted mt-0.5">
          Còn {voucher.quantityRemain}/{voucher.quantity}
        </p>
      </div>

      <div className="absolute -right-1.5 top-1/2 -translate-y-1/2 w-3 h-3 bg-background rounded-full border border-border z-10" />
    </div>
  );
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export default function CampaignCard({ campaign, vouchers = [] }) {
  return (
    <div className="flex-shrink-0 w-72 bg-surface border border-border rounded-2xl overflow-hidden hover:shadow-level-2 transition-all">
      {/* Header */}
      <div className="p-4 bg-primary/5 border-b border-border">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="text-sm font-bold text-text-primary line-clamp-2 leading-snug flex-1">
            {campaign.name}
          </h3>
          <span className="shrink-0 px-2 py-0.5 bg-success-bg text-success-text text-[10px] font-bold rounded-full uppercase">
            {campaign.status}
          </span>
        </div>

        <div className="flex items-center gap-1 text-[11px] text-text-muted">
          <Calendar size={11} />
          <span>
            {formatDate(campaign.startTime)} – {formatDate(campaign.endTime)}
          </span>
        </div>
      </div>

      {/* Voucher previews */}
      <div className="p-3">
        <div className="flex items-center gap-1 mb-2">
          <Tag size={11} className="text-primary" />
          <span className="text-[11px] font-semibold text-text-secondary">Mã giảm giá</span>
        </div>

        {vouchers.length > 0 ? (
          <div className="grid grid-cols-2 gap-2">
            {vouchers.slice(0, 4).map((v) => (
              <MiniVoucherCard key={v.id ?? v.code} voucher={v} />
            ))}
          </div>
        ) : (
          <div className="text-center py-4 text-[11px] text-text-muted">Chưa có mã nào.</div>
        )}
      </div>
    </div>
  );
}
