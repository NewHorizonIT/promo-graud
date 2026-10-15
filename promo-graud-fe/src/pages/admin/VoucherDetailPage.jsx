import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { ChevronLeft, Send, Trash2 } from "lucide-react";
import VoucherStatusBadge from "../../features/admin/vouchers/components/VoucherStatusBadge";
import DistributeModal from "../../features/admin/distribution/components/DistributeModal";
import { getVoucher, deleteVoucher, distributeVoucher } from "../../features/admin/vouchers/services/voucherService";
import { formatCurrency } from "../../utils/format";
import { useNavigate } from "react-router-dom";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("vi-VN");
}

export default function VoucherDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [voucher, setVoucher] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showDistribute, setShowDistribute] = useState(false);
  const [distributeLoading, setDistributeLoading] = useState(false);

  useEffect(() => {
    getVoucher(id)
      .then((res) => { if (res?.data) setVoucher(res.data); })
      .finally(() => setLoading(false));
  }, [id]);

  const handleDistribute = async (body) => {
    setDistributeLoading(true);
    try {
      await distributeVoucher(id, body);
      setShowDistribute(false);
    } finally {
      setDistributeLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm(`Xóa voucher "${voucher?.code}"?`)) return;
    await deleteVoucher(id);
    navigate("/admin/vouchers");
  };

  if (loading) {
    return (
      <div className="space-y-4">
        {[1, 2].map((i) => <div key={i} className="h-20 bg-surface-muted rounded-xl animate-pulse" />)}
      </div>
    );
  }

  if (!voucher) return <p className="text-sm text-text-muted">Không tìm thấy voucher.</p>;

  const remainPercent = voucher.quantity > 0 ? (voucher.quantity_remain / voucher.quantity) * 100 : 0;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/admin/vouchers" className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors">
          <ChevronLeft size={16} /> Danh sách Vouchers
        </Link>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setShowDistribute(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow cursor-pointer"
          >
            <Send size={14} /> Phân phối
          </button>
          <button
            type="button"
            onClick={handleDelete}
            className="flex items-center gap-1.5 px-3 py-1.5 text-sm font-semibold text-text-secondary hover:text-error-text hover:bg-error-bg rounded-xl transition-all cursor-pointer"
          >
            <Trash2 size={14} /> Xóa
          </button>
        </div>
      </div>

      {/* Main info */}
      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4 mb-5">
          <div>
            <p className="text-xs text-text-muted mb-1">Mã voucher</p>
            <p className="text-2xl font-bold font-mono text-text-primary">{voucher.code}</p>
          </div>
          <VoucherStatusBadge status={voucher.status} />
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {[
            { label: "Loại", value: voucher.type === "generic" ? "Generic" : "Unique" },
            { label: "Rule ID", value: `#${voucher.rule_id}` },
            { label: "Kênh", value: voucher.distribution_channel },
            { label: "Hết hạn", value: formatDate(voucher.expired_at) },
            { label: "Giới hạn / user", value: voucher.limit_client ?? "Không giới hạn" },
          ].map(({ label, value }) => (
            <div key={label} className="bg-surface-muted rounded-xl p-3">
              <p className="text-xs text-text-muted mb-0.5">{label}</p>
              <p className="text-sm font-semibold text-text-primary capitalize">{value}</p>
            </div>
          ))}
        </div>

        {/* Usage bar */}
        <div className="mt-5">
          <div className="flex justify-between text-xs text-text-muted mb-1.5">
            <span>Lượt sử dụng</span>
            <span>{voucher.quantity_remain} / {voucher.quantity} còn lại</span>
          </div>
          <div className="h-2 bg-border rounded-full overflow-hidden">
            <div
              className="h-full bg-primary rounded-full transition-all"
              style={{ width: `${remainPercent}%` }}
            />
          </div>
        </div>
      </div>

      {showDistribute && (
        <DistributeModal
          voucher={voucher}
          onSubmit={handleDistribute}
          onClose={() => setShowDistribute(false)}
          loading={distributeLoading}
        />
      )}
    </div>
  );
}
