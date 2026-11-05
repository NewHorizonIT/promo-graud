import { useEffect, useState, useMemo } from 'react';
import { X, Tag, Ticket, Loader2, PackageOpen, Info } from 'lucide-react';
import { useMyVouchers, useRules } from '../../user/vouchers/hooks/useVouchers';
import VoucherDetailModal from '../../../components/VoucherDetailModal';

const TYPE_LABEL = { UNIQUE: 'Riêng', GENERIC: 'Dùng chung' };
const TYPE_STYLE = {
  UNIQUE: 'bg-violet-50 text-violet-700 border-violet-200',
  GENERIC: 'bg-sky-50 text-sky-700 border-sky-200',
};

export default function VoucherPickerModal({ onSelect, onClose }) {
  const [detailTarget, setDetailTarget] = useState(null);

  const { data: myVouchers, isLoading } = useMyVouchers();
  const { data: rulesData } = useRules();

  const ruleMap = useMemo(
    () => Object.fromEntries((rulesData?.data ?? []).map((r) => [r.id, r])),
    [rulesData],
  );

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
            unusedVouchers.map((uv) => {
              const rule = ruleMap[uv.voucher?.ruleId];
              const discountLabel = rule
                ? rule.typeOfRule === 'PERCENTAGE'
                  ? `Giảm ${rule.value}%`
                  : rule.typeOfRule === 'FIXED_AMOUNT'
                    ? `Giảm ${Number(rule.value).toLocaleString('vi-VN')}₫`
                    : rule.typeOfRule === 'BUY_X_GET_Y'
                      ? 'Mua X tặng Y'
                      : rule.typeOfRule === 'FLASH_SALE'
                        ? 'Flash Sale'
                        : 'Giảm theo bậc'
                : null;

              return (
                <div
                  key={uv.id}
                  className="flex items-center gap-3 p-4 rounded-xl border border-border hover:border-primary hover:bg-primary-light transition-all group"
                >
                  {/* Select button (most of the row) */}
                  <button
                    type="button"
                    onClick={() => onSelect({ id: uv.voucher.id, code: uv.voucher.code })}
                    className="flex items-center gap-3 flex-1 min-w-0 text-left cursor-pointer"
                  >
                    <div className="w-10 h-10 rounded-xl bg-primary-light flex items-center justify-center shrink-0 group-hover:bg-primary/20 transition-colors">
                      <Tag size={18} className="text-primary" />
                    </div>

                    <div className="flex-1 min-w-0">
                      <p className="font-bold text-sm text-text-primary font-mono tracking-wider truncate">
                        {uv.voucher.code}
                      </p>
                      <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                        <span
                          className={`inline-block px-2 py-0.5 rounded-full text-[11px] font-semibold border ${
                            TYPE_STYLE[uv.voucher.type] ??
                            'bg-surface-muted text-text-muted border-border'
                          }`}
                        >
                          {TYPE_LABEL[uv.voucher.type] ?? uv.voucher.type}
                        </span>
                        {discountLabel && (
                          <span className="text-[11px] text-emerald-600 font-semibold">
                            {discountLabel}
                          </span>
                        )}
                      </div>
                    </div>

                    <span className="text-xs font-semibold text-primary shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                      Chọn
                    </span>
                  </button>

                  {/* Info button */}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setDetailTarget({ voucher: uv.voucher, rule });
                    }}
                    className="p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-surface-muted transition-colors cursor-pointer shrink-0"
                    title="Xem chi tiết voucher"
                  >
                    <Info size={15} />
                  </button>
                </div>
              );
            })
          )}
        </div>
      </div>

      {detailTarget && (
        <VoucherDetailModal
          voucher={detailTarget.voucher}
          rule={detailTarget.rule}
          onClose={() => setDetailTarget(null)}
        />
      )}
    </div>
  );
}
