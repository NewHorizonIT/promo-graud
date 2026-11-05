import { useEffect } from 'react';
import { X, Tag, ShoppingCart, Users, Zap, TrendingUp, Gift } from 'lucide-react';
import { formatCurrency, formatDiscountFull } from '../utils/format';

const RULE_TYPE_META = {
  PERCENTAGE: {
    label: 'Giảm phần trăm',
    Icon: TrendingUp,
    color: 'text-blue-600',
    bg: 'bg-blue-50',
  },
  FIXED_AMOUNT: {
    label: 'Giảm trực tiếp',
    Icon: Tag,
    color: 'text-emerald-600',
    bg: 'bg-emerald-50',
  },
  BUY_X_GET_Y: { label: 'Mua X tặng Y', Icon: Gift, color: 'text-violet-600', bg: 'bg-violet-50' },
  TIERED: {
    label: 'Giảm theo bậc',
    Icon: TrendingUp,
    color: 'text-orange-600',
    bg: 'bg-orange-50',
  },
  FLASH_SALE: { label: 'Flash Sale', Icon: Zap, color: 'text-red-600', bg: 'bg-red-50' },
};

const AUDIENCE_LABEL = {
  ALL: 'Tất cả người dùng',
  VIP: 'Khách VIP',
  NEW_USER: 'Người dùng mới',
};

function formatDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function InfoRow({ label, value, highlight = false }) {
  if (value === null || value === undefined) return null;
  return (
    <div className="flex items-center justify-between py-2.5 border-b border-border last:border-0">
      <span className="text-sm text-text-muted">{label}</span>
      <span className={`text-sm font-semibold ${highlight ? 'text-primary' : 'text-text-primary'}`}>
        {value}
      </span>
    </div>
  );
}

export default function VoucherDetailModal({ voucher, rule, onClose }) {
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

  const meta = RULE_TYPE_META[rule?.typeOfRule] ?? {
    label: 'Khuyến mãi',
    Icon: Tag,
    color: 'text-primary',
    bg: 'bg-primary-light',
  };
  const { Icon } = meta;

  const quantityPct =
    voucher?.quantity > 0 ? Math.max(0, (voucher.quantityRemain / voucher.quantity) * 100) : 0;

  const discountText = formatDiscountFull(rule);

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />

      <div className="relative z-10 bg-surface rounded-2xl shadow-level-2 w-full max-w-sm flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div
              className={`w-9 h-9 rounded-xl ${meta.bg} flex items-center justify-center shrink-0`}
            >
              <Icon size={18} className={meta.color} />
            </div>
            <div>
              <p className="font-bold text-sm text-text-primary font-mono tracking-widest uppercase">
                {voucher?.code}
              </p>
              <p className={`text-xs font-medium ${meta.color}`}>{meta.label}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Discount highlight */}
        {discountText && (
          <div className={`mx-5 mt-4 px-4 py-3 rounded-xl ${meta.bg} flex items-center gap-2`}>
            <Icon size={16} className={meta.color} />
            <span className={`text-base font-bold ${meta.color}`}>{discountText}</span>
          </div>
        )}

        {/* Info rows */}
        <div className="px-5 py-2">
          <InfoRow
            label="Đơn tối thiểu"
            value={rule?.minOrderValue > 0 ? formatCurrency(rule.minOrderValue) : 'Không yêu cầu'}
          />
          <InfoRow
            label="Giảm tối đa"
            value={rule?.maxDiscountValue > 0 ? formatCurrency(rule.maxDiscountValue) : null}
          />
          <InfoRow
            label="Đối tượng"
            value={AUDIENCE_LABEL[rule?.typeOfUser] ?? rule?.typeOfUser ?? null}
          />
          <InfoRow label="Hạn dùng" value={formatDate(rule?.endTime)} highlight={!!rule?.endTime} />
          <InfoRow
            label="Loại mã"
            value={voucher?.type === 'UNIQUE' ? 'Mã riêng (1 người)' : 'Mã dùng chung'}
          />
        </div>

        {/* Quantity bar */}
        {voucher?.type === 'GENERIC' && voucher?.quantity > 0 && (
          <div className="px-5 pb-2">
            <div className="flex justify-between text-xs text-text-muted mb-1.5">
              <span className="flex items-center gap-1">
                <ShoppingCart size={12} /> Số lượng còn
              </span>
              <span className="font-semibold text-text-primary">
                {voucher.quantityRemain}/{voucher.quantity}
              </span>
            </div>
            <div className="w-full bg-surface-muted h-2 rounded-full overflow-hidden">
              <div
                className="bg-primary h-full rounded-full transition-all"
                style={{ width: `${quantityPct}%` }}
              />
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="px-5 pb-5 pt-3 flex items-center gap-1.5 text-xs text-text-muted border-t border-border mt-2">
          <Users size={12} />
          <span>Mỗi tài khoản dùng tối đa {voucher?.limitClient ?? 1} lần</span>
        </div>
      </div>
    </div>
  );
}
