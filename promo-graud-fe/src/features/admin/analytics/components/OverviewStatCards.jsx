import {
  Megaphone,
  Ticket,
  RotateCcw,
  BadgePercent,
  TrendingUp,
  DollarSign,
  ChartNoAxesColumn,
} from 'lucide-react';
import { formatCurrency } from '../../../../utils/format';

function StatCard({ label, value, icon: Icon, iconClass }) {
  return (
    <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
      <div className="flex items-center gap-3 mb-3">
        <div
          className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${iconClass}`}
        >
          <Icon size={18} />
        </div>
        <span className="text-xs font-semibold text-text-muted uppercase tracking-wide">
          {label}
        </span>
      </div>
      <p className="text-3xl font-bold text-text-primary">{value ?? '—'}</p>
    </div>
  );
}

function computeRoi(revenue, discount) {
  if (!discount || Number(discount) === 0) return null;
  return (Number(revenue) / Number(discount)).toFixed(2) + 'x';
}

export default function OverviewStatCards({ stats }) {
  if (!stats) return null;

  const roi = computeRoi(stats.totalRevenue, stats.totalDiscountGiven);

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
        label="Tổng Campaigns"
        value={stats.totalCampaigns}
        icon={Megaphone}
        iconClass="bg-primary-light text-primary"
      />
      <StatCard
        label="Đang hoạt động"
        value={stats.activeCampaigns}
        icon={TrendingUp}
        iconClass="bg-success-bg text-success-text"
      />
      <StatCard
        label="Voucher đã phát"
        value={stats.totalVouchersIssued}
        icon={Ticket}
        iconClass="bg-surface-muted text-text-secondary"
      />
      <StatCard
        label="Lượt redeem"
        value={stats.totalRedeemed}
        icon={RotateCcw}
        iconClass="bg-surface-muted text-text-secondary"
      />
      <StatCard
        label="Tiền giảm giá"
        value={formatCurrency(stats.totalDiscountGiven)}
        icon={BadgePercent}
        iconClass="bg-warning-bg text-warning-text"
      />
      <StatCard
        label="Doanh thu"
        value={formatCurrency(stats.totalRevenue)}
        icon={DollarSign}
        iconClass="bg-success-bg text-success-text"
      />
      <StatCard
        label="ROI"
        value={roi}
        icon={ChartNoAxesColumn}
        iconClass="bg-primary-light text-primary"
      />
    </div>
  );
}
