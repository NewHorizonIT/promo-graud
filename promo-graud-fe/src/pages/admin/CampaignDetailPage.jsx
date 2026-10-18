import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, Pencil } from 'lucide-react';
import CampaignStatusBadge from '../../features/admin/campaigns/components/CampaignStatusBadge';
import RuleList from '../../features/admin/campaigns/components/RuleList';
import {
  getCampaign,
  getRules,
  createRule,
  updateRule,
  deleteRule,
} from '../../features/admin/campaigns/services/campaignService';
import { getCampaignStats } from '../../features/admin/analytics/services/analyticsService';
import { formatCurrency } from '../../utils/format';

const TABS = ['Thông tin', 'Rules', 'Analytics'];

function StatItem({ label, value }) {
  return (
    <div className="bg-surface-muted rounded-xl p-4">
      <p className="text-xs font-semibold text-text-muted uppercase tracking-wide mb-1">{label}</p>
      <p className="text-2xl font-bold text-text-primary">{value ?? '—'}</p>
    </div>
  );
}

export default function CampaignDetailPage() {
  const { id } = useParams();
  const [tab, setTab] = useState(0);
  const [campaign, setCampaign] = useState(null);
  const [rules, setRules] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadCampaign = () =>
    getCampaign(id).then((r) => {
      if (r?.data) setCampaign(r.data);
    });

  const loadRules = () =>
    getRules(id).then((r) => {
      if (r?.data) setRules(r.data);
    });

  const loadStats = () =>
    getCampaignStats(id).then((r) => {
      if (r?.data) setStats(r.data);
    });

  useEffect(() => {
    Promise.all([loadCampaign(), loadRules(), loadStats()]).finally(() => setLoading(false));
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-20 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );
  }

  if (!campaign) {
    return <p className="text-sm text-text-muted">Không tìm thấy campaign.</p>;
  }

  return (
    <div className="space-y-5">
      {/* Breadcrumb + actions */}
      <div className="flex items-center justify-between">
        <Link
          to="/admin/campaigns"
          className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
        >
          <ChevronLeft size={16} /> Danh sách Campaigns
        </Link>
        <Link
          to={`/admin/campaigns/${id}/edit`}
          className="flex items-center gap-1.5 px-3 py-1.5 border border-border bg-surface hover:bg-surface-muted text-sm font-semibold text-text-primary rounded-xl transition-colors"
        >
          <Pencil size={14} /> Chỉnh sửa
        </Link>
      </div>

      {/* Campaign header */}
      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary">{campaign.name}</h2>
            <p className="text-sm text-text-muted mt-1">ID: #{campaign.id}</p>
          </div>
          <CampaignStatusBadge status={campaign.status} />
        </div>
        <div className="grid grid-cols-3 gap-4 mt-5">
          <div>
            <p className="text-xs text-text-muted mb-0.5">Bắt đầu</p>
            <p className="text-sm font-medium text-text-primary">
              {campaign.start_time
                ? new Date(campaign.start_time).toLocaleDateString('vi-VN')
                : '—'}
            </p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Kết thúc</p>
            <p className="text-sm font-medium text-text-primary">
              {campaign.end_time ? new Date(campaign.end_time).toLocaleDateString('vi-VN') : '—'}
            </p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Ngân sách</p>
            <p className="text-sm font-medium text-text-primary">
              {formatCurrency(campaign.promotion_budget)}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1 bg-surface-muted p-1 rounded-xl w-fit">
        {TABS.map((t, i) => (
          <button
            key={t}
            type="button"
            onClick={() => setTab(i)}
            className={`px-4 py-1.5 rounded-lg text-sm font-semibold transition-all cursor-pointer ${
              tab === i
                ? 'bg-surface text-text-primary shadow-level-1'
                : 'text-text-muted hover:text-text-primary'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {tab === 0 && (
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          <p className="text-sm text-text-secondary">
            Thông tin chi tiết campaign đã hiển thị ở trên.
          </p>
        </div>
      )}

      {tab === 1 && (
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          <RuleList
            campaignId={id}
            rules={rules}
            onAdd={async (body) => {
              await createRule(id, body);
              loadRules();
            }}
            onEdit={async (ruleId, body) => {
              await updateRule(id, ruleId, body);
              loadRules();
            }}
            onDelete={async (ruleId) => {
              if (confirm('Xóa rule này?')) {
                await deleteRule(id, ruleId);
                loadRules();
              }
            }}
          />
        </div>
      )}

      {tab === 2 && (
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          {stats ? (
            <div className="space-y-4">
              <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
                <StatItem label="Mã đã phát" value={stats.total_issued} />
                <StatItem label="Mã đã dùng" value={stats.total_redeemed} />
                <StatItem label="Tỷ lệ chuyển đổi" value={`${stats.redemption_rate ?? 0}%`} />
                <StatItem label="Tổng tiền giảm" value={formatCurrency(stats.total_discount)} />
                <StatItem label="Doanh thu" value={formatCurrency(stats.total_revenue)} />
              </div>
              {/* TODO: RedemptionChart — dùng /analytics/campaigns/{id}/timeline */}
            </div>
          ) : (
            <p className="text-sm text-text-muted">Chưa có dữ liệu thống kê.</p>
          )}
        </div>
      )}
    </div>
  );
}
