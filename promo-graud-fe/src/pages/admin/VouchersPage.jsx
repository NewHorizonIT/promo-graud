import { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { Plus } from "lucide-react";
import VoucherTable from "../../features/admin/vouchers/components/VoucherTable";
import DistributeModal from "../../features/admin/distribution/components/DistributeModal";
import { getVouchers, deleteVoucher, distributeVoucher } from "../../features/admin/vouchers/services/voucherService";

const TYPE_FILTERS = [
  { value: "", label: "Tất cả" },
  { value: "generic", label: "Generic" },
  { value: "unique", label: "Unique" },
];

export default function VouchersPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const type = searchParams.get("type") ?? "";
  const page = Math.max(1, Number(searchParams.get("page") ?? 1));

  const [vouchers, setVouchers] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [distributeTarget, setDistributeTarget] = useState(null);
  const [distributeLoading, setDistributeLoading] = useState(false);

  const PAGE_SIZE = 20;

  const load = () => {
    setLoading(true);
    const params = { page, page_size: PAGE_SIZE };
    if (type) params.type = type;
    getVouchers(params)
      .then((res) => {
        if (res?.data) {
          setVouchers(res.data.items ?? []);
          setTotal(res.data.total ?? 0);
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [type, page]);

  const handleDelete = async (voucher) => {
    if (!confirm(`Xóa voucher "${voucher.code}"?`)) return;
    await deleteVoucher(voucher.id);
    load();
  };

  const handleDistribute = async (body) => {
    if (!distributeTarget) return;
    setDistributeLoading(true);
    try {
      await distributeVoucher(distributeTarget.id, body);
      setDistributeTarget(null);
    } finally {
      setDistributeLoading(false);
    }
  };

  const totalPages = Math.ceil(total / PAGE_SIZE);

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

      {/* Type filter */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {TYPE_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            onClick={() => setSearchParams(f.value ? { type: f.value } : {})}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              type === f.value
                ? "bg-surface text-text-primary shadow-level-1"
                : "text-text-muted hover:text-text-primary"
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <VoucherTable
          vouchers={vouchers}
          onDelete={handleDelete}
          onDistribute={setDistributeTarget}
        />
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
                    ? "bg-primary text-white shadow-glow"
                    : "border border-border bg-surface text-text-secondary hover:bg-surface-muted"
                }`}
              >
                {pg}
              </button>
            );
          })}
        </div>
      )}

      {distributeTarget && (
        <DistributeModal
          voucher={distributeTarget}
          onSubmit={handleDistribute}
          onClose={() => setDistributeTarget(null)}
          loading={distributeLoading}
        />
      )}
    </div>
  );
}
