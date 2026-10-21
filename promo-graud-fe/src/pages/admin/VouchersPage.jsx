import { useSearchParams, Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import VoucherTable from '../../features/admin/vouchers/components/VoucherTable';
import {
  useAdminVouchers,
  useDeleteVoucher,
} from '../../features/admin/vouchers/hooks/useAdminVouchers';

const TYPE_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'GENERIC', label: 'Generic' },
  { value: 'UNIQUE', label: 'Unique' },
];

const PAGE_SIZE = 20;

export default function VouchersPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const type = searchParams.get('type') ?? '';
  const page = Math.max(1, Number(searchParams.get('page') ?? 1));

  const params = { page, pageSize: PAGE_SIZE, ...(type ? { type } : {}) };
  const { data, isLoading } = useAdminVouchers(params);
  const deleteMutation = useDeleteVoucher();

  const vouchers = data?.data ?? [];
  const total = data?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? 1;

  const handleDelete = (voucher) => {
    if (!confirm(`Xóa voucher "${voucher.code}"?`)) return;
    deleteMutation.mutate(voucher.id);
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{total} vouchers</p>
        <Link
          to="/admin/vouchers/new"
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
        >
          <Plus size={16} /> Tạo Voucher
        </Link>
      </div>

      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {TYPE_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setSearchParams(f.value ? { type: f.value } : {})}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              type === f.value
                ? 'bg-surface text-text-primary shadow-level-1'
                : 'text-text-muted hover:text-text-primary'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <VoucherTable vouchers={vouchers} onDelete={handleDelete} />
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() => setSearchParams({ ...(type ? { type } : {}), page: String(pg) })}
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${
                  pg === page
                    ? 'bg-primary text-white shadow-glow'
                    : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'
                }`}
              >
                {pg}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
