import { Link } from 'react-router-dom';
import { Megaphone, Ticket, TrendingUp, ArrowRight } from 'lucide-react';
import { useCampaigns } from '../../features/admin/campaigns/hooks/useCampaigns';
import { useAdminVouchers } from '../../features/admin/vouchers/hooks/useAdminVouchers';

function StatCard({ label, value, icon: Icon, iconClass, to }) {
  const inner = (
    <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1 hover:shadow-level-2 transition-shadow">
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
      {to && (
        <div className="flex items-center gap-1 mt-3 text-xs text-primary font-semibold">
          Xem chi tiết <ArrowRight size={12} />
        </div>
      )}
    </div>
  );
  return to ? <Link to={to}>{inner}</Link> : inner;
}

export default function DashboardPage() {
  const { data: campaignsPage, isLoading: cLoading } = useCampaigns({ pageSize: 100 });
  const { data: vouchersPage, isLoading: vLoading } = useAdminVouchers({ pageSize: 1 });

  const loading = cLoading || vLoading;
  const campaigns = campaignsPage?.data ?? [];
  const totalVouchers = vouchersPage?.totalItems ?? 0;
  const activeCampaigns = campaigns.filter((c) => c.status === 'ACTIVE').length;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard
          label="Tổng Campaigns"
          value={loading ? '...' : campaigns.length}
          icon={Megaphone}
          iconClass="bg-primary-light text-primary"
          to="/admin/campaigns"
        />
        <StatCard
          label="Đang hoạt động"
          value={loading ? '...' : activeCampaigns}
          icon={TrendingUp}
          iconClass="bg-success-bg text-success-text"
          to="/admin/campaigns?status=ACTIVE"
        />
        <StatCard
          label="Tổng Vouchers"
          value={loading ? '...' : totalVouchers}
          icon={Ticket}
          iconClass="bg-surface-muted text-text-secondary"
          to="/admin/vouchers"
        />
      </div>

      <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-base text-text-primary">Campaigns gần đây</h2>
          <Link
            to="/admin/campaigns"
            className="text-xs text-primary font-semibold hover:underline flex items-center gap-1"
          >
            Xem tất cả <ArrowRight size={12} />
          </Link>
        </div>

        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-12 bg-surface-muted rounded-xl animate-pulse" />
            ))}
          </div>
        ) : campaigns.length === 0 ? (
          <p className="text-sm text-text-muted py-4 text-center">Chưa có campaign nào.</p>
        ) : (
          <div className="space-y-2">
            {campaigns.slice(0, 5).map((c) => (
              <Link
                key={c.id}
                to={`/admin/campaigns/${c.id}`}
                className="flex items-center justify-between px-4 py-3 rounded-xl hover:bg-surface-muted transition-colors"
              >
                <span className="text-sm font-medium text-text-primary">{c.name}</span>
                <span className="text-xs text-text-muted">{c.status}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
