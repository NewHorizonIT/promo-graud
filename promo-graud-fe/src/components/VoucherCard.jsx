export default function VoucherCard({ voucher }) {
  // Determine text based on voucher type
  const discountText = voucher.type === 'GENERIC' ? 'GIẢM GIÁ' : 'CÁ NHÂN';

  return (
    <div className="relative bg-surface rounded-xl border border-border flex h-28 overflow-hidden hover:shadow-level-1 transition-shadow">
      {/* Left Notches */}
      <div className="absolute -left-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border"></div>

      {/* Left Zone (Discount Value) */}
      <div className="w-1/3 min-w-[100px] border-r border-dashed border-border flex flex-col items-center justify-center p-4 bg-success-bg/30">
        <span className="text-success-text text-lg font-bold">
          {voucher.code.includes('50K') ? '50K' : '20K'}
        </span>
        <span className="text-[10px] text-text-secondary text-center mt-1">Đơn từ 299k</span>
      </div>

      {/* Right Zone (Details & Action) */}
      <div className="flex-1 p-4 flex flex-col justify-between">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2 py-0.5 bg-primary-light text-primary text-[10px] font-bold rounded-sm">
              {discountText}
            </span>
          </div>
          <h3 className="text-sm font-semibold text-text-primary uppercase tracking-wide">
            Mã: {voucher.code}
          </h3>
          <p className="text-xs text-text-muted mt-1">HSD: 31/12/2026</p>
        </div>

        <div className="flex items-end justify-between mt-2">
          <div className="w-1/2 bg-surface-muted h-1.5 rounded-full overflow-hidden">
            <div
              className="bg-primary h-full"
              style={{ width: `${(voucher.quantityRemain / voucher.quantity) * 100}%` }}
            ></div>
          </div>
          <button className="px-3 py-1 bg-primary text-white text-xs font-bold rounded-full hover:bg-primary-hover shadow-glow transition-all">
            Lưu mã
          </button>
        </div>
      </div>

      {/* Right Notches */}
      <div className="absolute -right-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border"></div>
    </div>
  );
}
