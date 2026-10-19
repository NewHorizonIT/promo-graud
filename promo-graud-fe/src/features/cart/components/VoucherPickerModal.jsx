import { useEffect } from 'react';
import { X, Tag, Ticket, Loader2, PackageOpen } from 'lucide-react';
import { useMyVouchers } from '../../user/vouchers/hooks/useVouchers';

const TYPE_LABEL = { UNIQUE: 'Riêng', GENERIC: 'Dùng chung' };
const TYPE_STYLE = {
  UNIQUE: 'bg-violet-50 text-violet-700 border-violet-200',
  GENERIC: 'bg-sky-50 text-sky-700 border-sky-200',
};

export default function VoucherPickerModal({ onSelect, onClose }) {
  const { data: myVouchers, isLoading } = useMyVouchers();

  const unusedVouchers = myVouchers?.data?.filter((uv) => uv.status === 'UNUSED') ?? [];

  useEffect(() => {
    const handler = (e) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    document.body.style.overflow = 'hidden';
    return () => {
      window.removeEventListener('keydown', handler);
      document.body.style.overflow = '';
    };
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />

      <div className="relative z-10 bg-surface rounded-2xl shadow-level-2 w-full max-w-md flex flex-col max-h-[80vh]">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-border shrink-0">
          <div className="flex items-center gap-2">
            <Ticket size={18} className="text-primary" />
            <h2 className="font-bold text-base text-text-primary">Kho voucher của bạn</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer"
            aria-label="Đóng"
          >
            <X size={18} />
          </button>
        </div>

        {/* Body */}
        <div className="overflow-y-auto flex-1 p-4 space-y-2">
          {isLoading ? (
            <div className="flex items-center justify-center py-16 text-text-muted gap-2">
              <Loader2 size={20} className="animate-spin" />
              <span className="text-sm">Đang tải voucher...</span>
            </div>
          ) : unusedVouchers.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center gap-3">
              <PackageOpen size={36} className="text-text-muted opacity-50" />
              <p className="text-sm font-medium text-text-muted">Bạn chưa có voucher nào</p>
              <p className="text-xs text-text-muted">Ghé trang Voucher để nhận mã giảm giá nhé!</p>
            </div>
          ) : (
            unusedVouchers.map((uv) => (
              <button
                key={uv.id}
                type="button"
                onClick={() => onSelect({ id: uv.voucher.id, code: uv.voucher.code })}
                className="w-full flex items-center gap-4 p-4 rounded-xl border border-border hover:border-primary hover:bg-primary-light text-left transition-all group cursor-pointer"
              >
                {/* Icon */}
                <div className="w-10 h-10 rounded-xl bg-primary-light flex items-center justify-center shrink-0 group-hover:bg-primary/20 transition-colors">
                  <Tag size={18} className="text-primary" />
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-sm text-text-primary font-mono tracking-wider">
                    {uv.voucher.code}
                  </p>
                  <div className="flex items-center gap-2 mt-1">
                    <span
                      className={`inline-block px-2 py-0.5 rounded-full text-[11px] font-semibold border ${
                        TYPE_STYLE[uv.voucher.type] ??
                        'bg-surface-muted text-text-muted border-border'
                      }`}
                    >
                      {TYPE_LABEL[uv.voucher.type] ?? uv.voucher.type}
                    </span>
                    <span className="text-[11px] text-emerald-600 font-medium">Chưa dùng</span>
                  </div>
                </div>

                {/* CTA */}
                <span className="text-xs font-semibold text-primary shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                  Chọn
                </span>
              </button>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
