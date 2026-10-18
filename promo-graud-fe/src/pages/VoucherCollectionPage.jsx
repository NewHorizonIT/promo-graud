import { useState, useMemo } from 'react';
import { ChevronLeft, ChevronRight, Ticket, SlidersHorizontal, X } from 'lucide-react';
import CampaignBanner from '../features/user/vouchers/components/CampaignBanner';
import {
  useVouchers,
  useMyVouchers,
  useClaimVoucher,
  useRules,
} from '../features/user/vouchers/hooks/useVouchers';
import { useAuthStore } from '../store/authStore';

const PAGE_SIZE = 16;

function VoucherSkeleton() {
  return (
    <div className="relative bg-surface rounded-xl border border-border flex h-28 overflow-hidden">
      <div className="w-1/3 border-r border-dashed border-border bg-surface-muted animate-pulse" />
      <div className="flex-1 p-4 space-y-2">
        <div className="h-3 bg-surface-muted animate-pulse rounded w-2/3" />
        <div className="h-3 bg-surface-muted animate-pulse rounded w-1/2" />
        <div className="h-3 bg-surface-muted animate-pulse rounded w-1/3 mt-2" />
      </div>
    </div>
  );
}

function VoucherItem({ voucher, claimed, claiming, onClaim }) {
  return (
    <div className="relative bg-surface rounded-xl border border-border flex h-28 overflow-hidden hover:shadow-level-1 transition-shadow">
      <div className="absolute -left-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border z-10" />

      <div className="w-1/3 min-w-[90px] border-r border-dashed border-border flex flex-col items-center justify-center p-3 bg-success-bg/30 shrink-0">
        <span className="text-success-text text-base font-bold">GIẢM</span>
        <span className="text-[10px] text-text-secondary text-center mt-1">GIÁ</span>
      </div>

      <div className="flex-1 p-3 flex flex-col justify-between min-w-0">
        <div>
          <span className="px-2 py-0.5 bg-primary/10 text-primary text-[10px] font-bold rounded-sm">
            GENERIC
          </span>
          <h3 className="text-sm font-semibold text-text-primary uppercase tracking-wide mt-1 truncate">
            {voucher.code}
          </h3>
          <p className="text-xs text-text-muted mt-0.5">
            Còn {voucher.quantityRemain}/{voucher.quantity}
          </p>
        </div>

        <div className="flex items-end justify-between">
          <div className="w-1/2 bg-surface-muted h-1.5 rounded-full overflow-hidden">
            <div
              className="bg-primary h-full transition-all"
              style={{
                width: `${Math.max(0, (voucher.quantityRemain / voucher.quantity) * 100)}%`,
              }}
            />
          </div>

          <button
            type="button"
            onClick={() => onClaim(voucher.id)}
            disabled={claimed || claiming}
            className={`px-3 py-1 text-xs font-bold rounded-full transition-all cursor-pointer
              ${
                claimed
                  ? 'bg-surface-muted text-text-muted cursor-default'
                  : claiming
                    ? 'bg-primary/50 text-white cursor-wait'
                    : 'bg-primary text-white hover:bg-primary-hover shadow-glow'
              }`}
          >
            {claimed ? 'Đã lưu' : claiming ? 'Đang lưu...' : 'Lưu mã'}
          </button>
        </div>
      </div>

      <div className="absolute -right-2 top-1/2 -translate-y-1/2 w-4 h-4 bg-background rounded-full border border-border z-10" />
    </div>
  );
}

const RULE_TYPE_LABELS = {
  PERCENTAGE: 'Giảm %',
  FIXED_AMOUNT: 'Giảm trực tiếp',
  TIERED: 'Giảm theo bậc',
  BUY_X_GET_Y: 'Mua X tặng Y',
};

export default function VoucherCollectionPage() {
  const token = useAuthStore((s) => s.token);
  const [page, setPage] = useState(1);
  const [ruleId, setRuleId] = useState(null);

  const { data: rulesData } = useRules();
  const rules = rulesData?.data ?? [];

  const { data: vouchersData, isLoading } = useVouchers({ page, pageSize: PAGE_SIZE, ruleId });
  const { data: myVouchersData } = useMyVouchers();
  const { mutate: claim, isPending, variables: claimingId } = useClaimVoucher();

  const vouchers = vouchersData?.data ?? [];
  const totalPages = vouchersData?.totalPages ?? 1;
  const totalItems = vouchersData?.totalItems ?? 0;

  const claimedIds = useMemo(
    () => new Set((myVouchersData?.data ?? []).map((uv) => uv.voucher?.id).filter(Boolean)),
    [myVouchersData],
  );

  const handleClaim = (voucherId) => {
    if (!token) {
      window.location.href = '/login';
      return;
    }
    claim(voucherId);
  };

  const startIdx = (page - 1) * PAGE_SIZE + 1;
  const endIdx = Math.min(page * PAGE_SIZE, totalItems);

  return (
    <div className="space-y-8">
      <CampaignBanner />

      <div className="border-t border-border" />

      <section>
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary flex items-center gap-2">
              <Ticket size={22} className="text-primary" />
              Danh sách mã giảm giá
            </h2>
            <p className="text-sm text-text-muted mt-0.5">
              {!isLoading && totalItems > 0
                ? `Hiển thị ${startIdx}–${endIdx} trong ${totalItems} mã`
                : 'Tất cả mã GENERIC đang hoạt động'}
            </p>
          </div>
        </div>

        {/* Filter bar */}
        <div className="flex items-center gap-3 mb-6 flex-wrap">
          <span className="flex items-center gap-1.5 text-sm font-medium text-text-secondary shrink-0">
            <SlidersHorizontal size={14} />
            Lọc theo rule:
          </span>

          <select
            value={ruleId ?? ''}
            onChange={(e) => {
              setRuleId(e.target.value ? Number(e.target.value) : null);
              setPage(1);
            }}
            className="h-9 px-3 rounded-xl border border-border bg-surface text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/30 transition-all cursor-pointer"
          >
            <option value="">Tất cả rule</option>
            {rules.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name}
                {r.typeOfRule ? ` — ${RULE_TYPE_LABELS[r.typeOfRule] ?? r.typeOfRule}` : ''}
              </option>
            ))}
          </select>

          {ruleId && (
            <button
              type="button"
              onClick={() => {
                setRuleId(null);
                setPage(1);
              }}
              className="flex items-center gap-1 h-9 px-3 rounded-xl border border-border bg-surface text-sm text-text-muted hover:bg-surface-muted transition-all cursor-pointer"
            >
              <X size={13} />
              Xóa lọc
            </button>
          )}
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {Array.from({ length: PAGE_SIZE }).map((_, i) => (
              <VoucherSkeleton key={i} />
            ))}
          </div>
        ) : vouchers.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {vouchers.map((v) => (
              <VoucherItem
                key={v.id ?? v.code}
                voucher={v}
                claimed={claimedIds.has(v.id)}
                claiming={isPending && claimingId === v.id}
                onClaim={handleClaim}
              />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20 bg-surface rounded-2xl border border-border text-center">
            <div className="w-16 h-16 rounded-full bg-surface-muted flex items-center justify-center mb-4 text-3xl">
              🎫
            </div>
            <h3 className="text-lg font-bold text-text-primary mb-1">Chưa có mã giảm giá nào</h3>
            <p className="text-sm text-text-muted">Quay lại sau để xem các mã mới nhất.</p>
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-8">
            <button
              type="button"
              onClick={() => {
                setPage((p) => Math.max(1, p - 1));
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
              disabled={page <= 1}
              className="p-2 rounded-xl border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all cursor-pointer"
            >
              <ChevronLeft size={18} />
            </button>

            {Array.from({ length: totalPages }, (_, i) => i + 1).map((pg) => {
              if (pg === 1 || pg === totalPages || (pg >= page - 2 && pg <= page + 2)) {
                return (
                  <button
                    key={pg}
                    type="button"
                    onClick={() => {
                      setPage(pg);
                      window.scrollTo({ top: 0, behavior: 'smooth' });
                    }}
                    className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all cursor-pointer
                      ${
                        pg === page
                          ? 'bg-primary text-white shadow-glow'
                          : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                      }`}
                  >
                    {pg}
                  </button>
                );
              }
              if (pg === page - 3 || pg === page + 3) {
                return (
                  <span key={pg} className="px-1 text-text-muted select-none">
                    …
                  </span>
                );
              }
              return null;
            })}

            <button
              type="button"
              onClick={() => {
                setPage((p) => Math.min(totalPages, p + 1));
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
              disabled={page >= totalPages}
              className="p-2 rounded-xl border border-border bg-surface hover:bg-surface-muted disabled:opacity-30 disabled:pointer-events-none transition-all cursor-pointer"
            >
              <ChevronRight size={18} />
            </button>
          </div>
        )}
      </section>
    </div>
  );
}
