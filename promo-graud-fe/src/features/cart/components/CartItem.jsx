import { Minus, Plus, Trash2 } from 'lucide-react';
import { formatCurrency } from '../../../utils/format';

export default function CartItem({ item, onUpdateQuantity, onRemove }) {
  const lineTotal = (item.price || 0) * (item.quantity || 1);

  return (
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-4 rounded-2xl bg-surface border border-border hover:shadow-level-1 transition-all">
      {/* Product info: image & title */}
      <div className="flex items-center gap-4 flex-1 min-w-0">
        <div className="w-20 h-20 rounded-xl bg-surface-muted overflow-hidden shrink-0 border border-border">
          {item.images ? (
            <img src={item.images} alt={item.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-3xl">👕</div>
          )}
        </div>

        <div className="flex-1 min-w-0">
          {item.typeName && (
            <span className="inline-block px-2 py-0.5 rounded-full text-[11px] font-semibold bg-primary-light text-primary mb-1">
              {item.typeName}
            </span>
          )}
          <h4 className="text-sm font-semibold text-text-primary line-clamp-1 leading-snug">
            {item.name}
          </h4>
          <p className="text-xs text-text-muted mt-1">
            Đơn giá:{' '}
            <span className="font-medium text-text-primary">{formatCurrency(item.price)}</span>
          </p>
        </div>
      </div>

      {/* Quantity & Line Total & Delete */}
      <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto pt-2 sm:pt-0 border-t sm:border-t-0 border-border/60">
        {/* Quantity selector */}
        <div className="flex items-center rounded-xl border border-border bg-surface-muted p-1">
          <button
            type="button"
            onClick={() => onUpdateQuantity(item.id, Math.max(1, (item.quantity || 1) - 1))}
            disabled={(item.quantity || 1) <= 1}
            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-surface text-text-secondary disabled:opacity-30 disabled:pointer-events-none transition-colors cursor-pointer"
            aria-label="Giảm số lượng"
          >
            <Minus size={14} />
          </button>
          <span className="w-9 text-center text-xs font-bold text-text-primary select-none">
            {item.quantity || 1}
          </span>
          <button
            type="button"
            onClick={() => onUpdateQuantity(item.id, (item.quantity || 1) + 1)}
            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-surface text-text-secondary transition-colors cursor-pointer"
            aria-label="Tăng số lượng"
          >
            <Plus size={14} />
          </button>
        </div>

        {/* Subtotal */}
        <div className="text-right min-w-[90px]">
          <span className="text-sm font-bold text-primary">{formatCurrency(lineTotal)}</span>
        </div>

        {/* Remove button */}
        <button
          type="button"
          onClick={() => onRemove(item.id)}
          className="p-2 text-text-muted hover:text-error-text hover:bg-error-bg rounded-xl transition-all cursor-pointer"
          title="Xóa khỏi giỏ hàng"
          aria-label={`Xóa ${item.name}`}
        >
          <Trash2 size={18} />
        </button>
      </div>
    </div>
  );
}
