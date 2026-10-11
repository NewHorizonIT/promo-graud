import { useState } from "react";
import { ArrowRight, ShieldCheck, Tag } from "lucide-react";
import { formatCurrency } from "../../../utils/format";

export default function CartSummary({
  subtotal,
  onCheckout,
  checkoutLoading = false,
}) {
  const [voucherCode, setVoucherCode] = useState("");
  const [appliedVoucher, setAppliedVoucher] = useState(null);
  const [voucherError, setVoucherError] = useState("");

  // Miễn phí vận chuyển với đơn từ 500k, ngược lại phí 30k
  const shippingFee = subtotal >= 500000 || subtotal === 0 ? 0 : 30000;

  const handleApplyVoucher = (e) => {
    e.preventDefault();
    setVoucherError("");
    const code = voucherCode.trim().toUpperCase();
    if (!code) return;

    if (code === "PROMO10" || code === "PROMOWEAR") {
      setAppliedVoucher({
        code,
        discountPercent: 10,
        amount: Math.round(subtotal * 0.1),
      });
    } else if (code === "FREESHIP") {
      setAppliedVoucher({
        code,
        amount: shippingFee,
      });
    } else {
      setVoucherError("Mã giảm giá không hợp lệ hoặc đã hết hạn.");
    }
  };

  const discountAmount = appliedVoucher ? (appliedVoucher.amount || 0) : 0;
  const grandTotal = Math.max(0, subtotal + shippingFee - discountAmount);

  return (
    <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1 sticky top-24 space-y-6">
      <h3 className="font-bold text-lg text-text-primary pb-3 border-b border-border">
        Tóm tắt đơn hàng
      </h3>

      {/* Line items */}
      <div className="space-y-3 text-sm">
        <div className="flex items-center justify-between text-text-secondary">
          <span>Tạm tính</span>
          <span className="font-semibold text-text-primary">{formatCurrency(subtotal)}</span>
        </div>

        <div className="flex items-center justify-between text-text-secondary">
          <span>Phí vận chuyển</span>
          <span>
            {shippingFee === 0 ? (
              <span className="text-emerald-600 font-semibold">Miễn phí</span>
            ) : (
              <span className="font-semibold text-text-primary">{formatCurrency(shippingFee)}</span>
            )}
          </span>
        </div>

        {appliedVoucher && (
          <div className="flex items-center justify-between text-emerald-600 font-medium">
            <span className="flex items-center gap-1">
              <Tag size={14} /> Giảm giá ({appliedVoucher.code})
            </span>
            <span>-{formatCurrency(discountAmount)}</span>
          </div>
        )}
      </div>

      {/* Voucher Input */}
      <form onSubmit={handleApplyVoucher} className="space-y-2 pt-2 border-t border-border">
        <label className="text-xs font-semibold text-text-muted uppercase tracking-wider block">
          Mã ưu đãi / Voucher
        </label>
        <div className="flex gap-2">
          <input
            type="text"
            value={voucherCode}
            onChange={(e) => setVoucherCode(e.target.value)}
            placeholder="Nhập PROMO10..."
            className="flex-1 h-10 px-3 rounded-xl border border-border bg-surface-muted text-xs text-text-primary uppercase focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
          />
          <button
            type="submit"
            className="px-4 h-10 rounded-xl bg-surface-muted border border-border hover:bg-border text-xs font-semibold text-text-primary transition-colors cursor-pointer"
          >
            Áp dụng
          </button>
        </div>
        {voucherError && (
          <p className="text-xs text-error-text mt-1">{voucherError}</p>
        )}
        {subtotal < 500000 && subtotal > 0 && (
          <p className="text-[11px] text-text-muted">
            💡 Mua thêm <b>{formatCurrency(500000 - subtotal)}</b> để được miễn phí vận chuyển!
          </p>
        )}
      </form>

      {/* Grand Total */}
      <div className="pt-4 border-t border-border">
        <div className="flex items-baseline justify-between mb-1">
          <span className="font-bold text-base text-text-primary">Tổng thanh toán</span>
          <span className="font-bold text-2xl text-primary">{formatCurrency(grandTotal)}</span>
        </div>
        <p className="text-[11px] text-text-muted text-right">(Đã bao gồm VAT nếu có)</p>
      </div>

      {/* Checkout Button */}
      <button
        type="button"
        onClick={() => onCheckout?.(grandTotal)}
        disabled={subtotal === 0 || checkoutLoading}
        className="
          w-full py-3.5 px-4 bg-primary hover:bg-primary-hover
          text-white rounded-xl text-sm font-semibold
          flex items-center justify-center gap-2
          transition-all duration-200 hover:shadow-glow
          disabled:opacity-40 disabled:cursor-not-allowed
          active:scale-[0.98] cursor-pointer
        "
      >
        {checkoutLoading ? (
          <span>Đang xử lý đơn hàng...</span>
        ) : (
          <>
            <span>Tiến hành đặt hàng</span>
            <ArrowRight size={16} />
          </>
        )}
      </button>

      {/* Assurance */}
      <div className="flex items-center justify-center gap-2 text-xs text-text-muted pt-2">
        <ShieldCheck size={16} className="text-emerald-500 shrink-0" />
        <span>Bảo mật thanh toán & Đổi trả trong 7 ngày</span>
      </div>
    </div>
  );
}
