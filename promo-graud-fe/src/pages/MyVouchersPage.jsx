import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Ticket, Tag, ArrowRight, PackageOpen, Info } from 'lucide-react';
import { useMyVouchers, useRules } from '../features/user/vouchers/hooks/useVouchers';
import VoucherDetailModal from '../components/VoucherDetailModal';

const TYPE_LABEL = { UNIQUE: 'Riêng', GENERIC: 'Dùng chung' };
const TYPE_STYLE = {
  UNIQUE: 'bg-violet-50 text-violet-700 border-violet-200',
  GENERIC: 'bg-sky-50 text-sky-700 border-sky-200',
};

function VoucherWalletCard({ uv, rule, onDetail }) {
  const v = uv.voucher;
  const isUsed = uv.status === 'USED';

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
    : (TYPE_LABEL[v?.type] ?? v?.type);

  return (
    <div
      className={`relative flex h-28 rounded-xl border overflow-hidden transition-all ${
        isUsed
          ? 'bg-surface-muted border-border opacity-60'
          : 'bg-surface border-border hover:shadow-level-1'
      }`}
    >
      <div className="absolute -left-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border z-10" />

      {/* Left zone */}
      <div className="w-1/3 min-w-[90px] border-r border-dashed border-border flex flex-col items-center justify-center p-3 bg-success-bg/30 shrink-0">
        <Tag size={16} className="text-success-text mb-1 shrink-0" />
        <span className="text-[10px] text-success-text font-bold text-center leading-tight">
          {discountLabel}
        </span>
      </div>

      {/* Right zone */}
      <div className="flex-1 p-3 flex flex-col justify-between min-w-0">
        <div>
          <div className="flex items-center justify-between gap-1 mb-1">
            <div className="flex items-center gap-1.5">
              <span
                className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-semibold border ${
                  TYPE_STYLE[v?.type] ?? 'bg-surface-muted text-text-muted border-border'
                }`}
              >
                {TYPE_LABEL[v?.type] ?? v?.type}
              </span>
              <span
                className={`text-[10px] font-semibold ${
                  isUsed ? 'text-text-muted' : 'text-success-text'
                }`}
              >
                {isUsed ? 'Đã dùng' : 'Chưa dùng'}
              </span>
            </div>
            {!isUsed && (
              <button
                type="button"
                onClick={() => onDetail(v, rule)}
                className="p-0.5 text-text-muted hover:text-primary transition-colors cursor-pointer shrink-0"
                title="Xem chi tiết"
              >
                <Info size={14} />
              </button>
            )}
          </div>
          <p className="text-sm font-bold text-text-primary font-mono uppercase tracking-wide truncate">
            {v?.code}
          </p>
        </div>

        {!isUsed && v?.quantity > 0 && (
          <div className="w-1/2 bg-surface-muted h-1.5 rounded-full overflow-hidden">
            <div
              className="bg-primary h-full transition-all"
              style={{ width: `${Math.max(0, (v.quantityRemain / v.quantity) * 100)}%` }}
            />
          </div>
        )}
      </div>

      <div className="absolute -right-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border z-10" />
    </div>
  );
}

function SkeletonCard() {
  return (
    <div className="relative flex h-28 rounded-xl border border-border overflow-hidden bg-surface">
      <div className="w-1/3 border-r border-dashed border-border bg-surface-muted animate-pulse" />
      <div className="flex-1 p-3 space-y-2">
        <div className="h-3 bg-surface-muted animate-pulse rounded w-1/2" />
        <div className="h-3 bg-surface-muted animate-pulse rounded w-2/3" />
        <div className="h-2 bg-surface-muted animate-pulse rounded w-1/3 mt-2" />
      </div>
    </div>
  );
}

const TABS = [
  { key: 'UNUSED', label: 'Chưa dùng' },
  { key: 'USED', label: 'Đã dùng' },
];

export default function MyVouchersPage() {
  const [tab, setTab] = useState('UNUSED');
  const [detailTarget, setDetailTarget] = useState(null);

  const { data, isLoading } = useMyVouchers();
  const { data: rulesData } = useRules();

  const ruleMap = useMemo(
    () => Object.fromEntries((rulesData?.data ?? []).map((r) => [r.id, r])),
    [rulesData],
  );

  const allVouchers = data?.data ?? [];
  const filtered = allVouchers.filter((uv) => uv.status === tab);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <Ticket size={22} className="text-primary" />
            Ví Voucher của tôi
          </h1>
          <p className="text-sm text-text-muted mt-0.5">
            {!isLoading && `${allVouchers.length} voucher đã lưu`}
          </p>
        </div>
        <Link
          to="/vouchers"
          className="flex items-center gap-1.5 text-sm font-semibold text-primary hover:underline"
        >
          Lấy thêm voucher <ArrowRight size={15} />
        </Link>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => setTab(t.key)}
            className={`px-4 py-1.5 rounded-lg text-sm font-semibold transition-all cursor-pointer ${
              tab === t.key
                ? 'bg-surface text-text-primary shadow-level-1'
                : 'text-text-muted hover:text-text-primary'
            }`}
          >
            {t.label}
            {!isLoading && (
              <span className="ml-1.5 text-xs font-normal text-text-muted">
                ({allVouchers.filter((uv) => uv.status === t.key).length})
              </span>
            )}
          </button>
        ))}
      </div>

      {/* List */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      ) : filtered.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((uv) => (
            <VoucherWalletCard
              key={uv.id}
              uv={uv}
              rule={ruleMap[uv.voucher?.ruleId]}
              onDetail={(voucher, rule) => setDetailTarget({ voucher, rule })}
            />
          ))}
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-20 bg-surface rounded-2xl border border-border text-center">
          <PackageOpen size={40} className="text-text-muted opacity-40 mb-4" />
          <h3 className="text-base font-bold text-text-primary mb-1">
            {tab === 'UNUSED' ? 'Chưa có voucher nào chưa dùng' : 'Chưa có voucher nào đã dùng'}
          </h3>
          <p className="text-sm text-text-muted mb-4">
            {tab === 'UNUSED' ? 'Ghé trang Voucher để nhận mã giảm giá!' : ''}
          </p>
          {tab === 'UNUSED' && (
            <Link
              to="/vouchers"
              className="px-5 py-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold rounded-xl transition-all hover:shadow-glow"
            >
              Lấy voucher ngay
            </Link>
          )}
        </div>
      )}

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
