import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, ShieldCheck, Tag, Ticket, X, Loader2 } from 'lucide-react';
import { formatCurrency } from '../../../utils/format';
import { previewOrder } from '../../checkout/services/checkoutService';
import VoucherPickerModal from './VoucherPickerModal';

export default function CartSummary({ items = [], onCheckout, checkoutLoading = false }) {
  const [selectedVoucher, setSelectedVoucher] = useState(null); // { id, code }
  const [voucherPickerOpen, setVoucherPickerOpen] = useState(false);

  // Stable key so useQuery re-fetches when quantities change
  const itemsKey = items.map((i) => `${i.id}:${i.quantity}`).join(',');

  const {
    data: preview,
    isFetching: previewLoading,
    error: previewError,
  } = useQuery({
    queryKey: ['order-preview', selectedVoucher?.id, itemsKey],
    queryFn: () => previewOrder({ voucherId: selectedVoucher.id, items }),
    enabled: !!selectedVoucher && items.length > 0,
    staleTime: 30_000,
    retry: false,
  });

  const subtotal = items.reduce((sum, i) => sum + (i.price || 0) * (i.quantity || 1), 0);
  const discount = preview?.discount ?? 0;
  const finalAmount = preview?.finalAmount ?? subtotal;
  const errorMessage = previewError?.response?.data?.message;

  const handleRemoveVoucher = () => setSelectedVoucher(null);

  const handleVoucherSelect = (voucher) => {
    setSelectedVoucher(voucher);
    setVoucherPickerOpen(false);
  };

  return (
    <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1 sticky top-24 space-y-6">
      <h3 className="font-bold text-lg text-text-primary pb-3 border-b border-border">
        Tóm tắt đơn hàng
      </h3>

      {/* Voucher section */}
      <div className="space-y-2">
        <p className="text-xs font-semibold text-text-muted uppercase tracking-wider">Voucher</p>

        {selectedVoucher ? (
          <div className="flex items-center justify-between px-3 py-2.5 rounded-xl bg-emerald-50 border border-emerald-200">
            <div className="flex items-center gap-2">
              <Tag size={14} className="text-emerald-600 shrink-0" />
              <span className="text-sm font-semibold text-emerald-700">{selectedVoucher.code}</span>
              {previewLoading && (
                <Loader2 size={13} className="animate-spin text-emerald-500 ml-1" />
              )}
            </div>
            <button
              type="button"
              onClick={handleRemoveVoucher}
              className="p-1 rounded-lg hover:bg-emerald-100 text-emerald-600 transition-colors cursor-pointer"
              aria-label="Bỏ voucher"
            >
              <X size={14} />
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => setVoucherPickerOpen(true)}
            className="w-full flex items-center justify-center gap-2 h-10 rounded-xl border border-dashed border-border hover:border-primary hover:text-primary text-sm font-medium text-text-muted transition-colors cursor-pointer"
          >
            <Ticket size={15} />
            Lấy từ kho voucher
          </button>
        )}

        {errorMessage && <p className="text-xs text-error-text mt-1">{errorMessage}</p>}
      </div>

      {/* Line items */}
      <div className="space-y-3 text-sm border-t border-border pt-4">
        <div className="flex items-center justify-between text-text-secondary">
          <span>Tạm tính</span>
          <span className="font-semibold text-text-primary">{formatCurrency(subtotal)}</span>
        </div>

        {selectedVoucher && !previewLoading && preview && (
          <div className="flex items-center justify-between text-emerald-600 font-medium">
            <span className="flex items-center gap-1.5">
              <Tag size={13} />
              Giảm giá
            </span>
            <span>-{formatCurrency(discount)}</span>
          </div>
        )}
      </div>

      {/* Grand Total */}
      <div className="pt-4 border-t border-border">
        <div className="flex items-baseline justify-between mb-1">
          <span className="font-bold text-base text-text-primary">Tổng thanh toán</span>
          <span className="font-bold text-2xl text-primary">{formatCurrency(finalAmount)}</span>
        </div>
        <p className="text-[11px] text-text-muted text-right">(Đã bao gồm VAT nếu có)</p>
      </div>

      {/* Checkout Button */}
      <button
        type="button"
        onClick={() => onCheckout?.(selectedVoucher?.id ?? null)}
        disabled={items.length === 0 || checkoutLoading || previewLoading}
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

      {voucherPickerOpen && (
        <VoucherPickerModal
          onSelect={handleVoucherSelect}
          onClose={() => setVoucherPickerOpen(false)}
        />
      )}
    </div>
  );
}
