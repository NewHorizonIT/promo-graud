import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Megaphone, Ticket, ShieldAlert, TrendingUp, ArrowRight } from 'lucide-react';
import { getCampaigns } from '../../features/admin/campaigns/services/campaignService';
import { getVouchers } from '../../features/admin/vouchers/services/voucherService';
import { getFraudStats } from '../../features/admin/fraud/services/fraudService';

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
  const [campaigns, setCampaigns] = useState([]);
  const [vouchers, setVouchers] = useState({ total: 0 });
  const [fraudStats, setFraudStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getCampaigns({ page_size: 100 }), getVouchers({ page_size: 1 }), getFraudStats()])
      .then(([cRes, vRes, fRes]) => {
        if (cRes?.data?.items) setCampaigns(cRes.data.items);
        if (vRes?.data?.total != null) setVouchers({ total: vRes.data.total });
        if (fRes?.data) setFraudStats(fRes.data);
      })
      .finally(() => setLoading(false));
  }, []);

  const activeCampaigns = campaigns.filter((c) => c.status === 'dang_dien_ra').length;

  return (
    <div className="space-y-6">
      {/* Stat Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
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
          to="/admin/campaigns?status=dang_dien_ra"
        />
        <StatCard
          label="Tổng Vouchers"
          value={loading ? '...' : vouchers.total}
          icon={Ticket}
          iconClass="bg-surface-muted text-text-secondary"
          to="/admin/vouchers"
        />
        <StatCard
          label="Fraud Blocks"
          value={loading ? '...' : (fraudStats?.total_blocked ?? 0)}
          icon={ShieldAlert}
          iconClass="bg-error-bg text-error-text"
          to="/admin/fraud"
        />
      </div>

      {/* Recent campaigns */}
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
