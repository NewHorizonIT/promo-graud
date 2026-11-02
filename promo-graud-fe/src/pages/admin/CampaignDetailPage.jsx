import { useState, useRef, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, Pencil } from 'lucide-react';
import CampaignStatusBadge from '../../features/admin/campaigns/components/CampaignStatusBadge';
import RuleList from '../../features/admin/campaigns/components/RuleList';
import {
  useCampaign,
  useUpdateCampaignStatus,
} from '../../features/admin/campaigns/hooks/useCampaigns';
import {
  useRules,
  useCreateRule,
  useUpdateRule,
  useDeleteRule,
} from '../../features/admin/campaigns/hooks/useRules';
import { useCampaignStats } from '../../features/admin/analytics/hooks/useAnalytics';
import { formatCurrency } from '../../utils/format';
import ConfirmModal from '../../components/ConfirmModal';

const TABS = ['Thông tin', 'Rules', 'Analytics'];

function StatItem({ label, value }) {
  return (
    <div className="bg-surface-muted rounded-xl p-4">
      <p className="text-xs font-semibold text-text-muted uppercase tracking-wide mb-1">{label}</p>
      <p className="text-2xl font-bold text-text-primary">{value ?? '—'}</p>
    </div>
  );
}

const STATUS_TRANSITIONS = {
  ACTIVE: { next: 'ENDED', label: 'Kết thúc ngay', requireConfirm: true },
};

export default function CampaignDetailPage() {
  const { id } = useParams();
  const [tab, setTab] = useState(0);
  const [mutationError, setMutationError] = useState(null);
  const [deletingRuleId, setDeletingRuleId] = useState(null);
  const [showEndConfirm, setShowEndConfirm] = useState(false);
  const [pendingDeleteRuleId, setPendingDeleteRuleId] = useState(null);
  const errorTimerRef = useRef(null);

  const { data: campaign, isLoading } = useCampaign(id);
  const { data: rulesPage } = useRules(id);
  const { data: stats } = useCampaignStats(id);

  const updateStatus = useUpdateCampaignStatus();
  const createRule = useCreateRule();
  const updateRule = useUpdateRule();
  const deleteRule = useDeleteRule();

  useEffect(() => () => clearTimeout(errorTimerRef.current), []);

  const rules = rulesPage?.data ?? [];

  const showError = (err) => {
    const msg =
      err?.response?.data?.message ?? err?.message ?? 'Đã xảy ra lỗi, vui lòng thử lại.';
    setMutationError(msg);
    if (errorTimerRef.current) clearTimeout(errorTimerRef.current);
    errorTimerRef.current = setTimeout(() => setMutationError(null), 5000);
  };

  if (isLoading) {
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

  const transition = STATUS_TRANSITIONS[campaign.status];

  const handleStatusChange = () => {
    if (!transition) return;
    if (transition.requireConfirm) {
      setShowEndConfirm(true);
      return;
    }
    updateStatus.mutate({ id, status: transition.next }, { onError: showError });
  };

  const handleConfirmEnd = () => {
    updateStatus.mutate(
      { id, status: transition.next },
      {
        onSuccess: () => setShowEndConfirm(false),
        onError: (err) => {
          setShowEndConfirm(false);
          showError(err);
        },
      },
    );
  };

  const handleConfirmDeleteRule = async () => {
    const ruleId = pendingDeleteRuleId;
    setPendingDeleteRuleId(null);
    setDeletingRuleId(ruleId);
    try {
      await deleteRule.mutateAsync({ campaignId: id, ruleId });
    } catch (err) {
      showError(err);
    } finally {
      setDeletingRuleId(null);
    }
  };

  return (
    <div className="space-y-5">
      {showEndConfirm && (
        <ConfirmModal
          title="Kết thúc Campaign"
          message={`Xác nhận kết thúc campaign "${campaign.name}"? Hành động này KHÔNG THỂ hoàn tác.`}
          confirmLabel="Kết thúc ngay"
          loading={updateStatus.isPending}
          onConfirm={handleConfirmEnd}
          onClose={() => {
            if (!updateStatus.isPending) setShowEndConfirm(false);
          }}
        />
      )}

      {pendingDeleteRuleId && (
        <ConfirmModal
          title="Xóa Rule"
          message="Bạn có chắc muốn xóa rule này? Hành động này không thể hoàn tác."
          confirmLabel="Xóa"
          loading={deleteRule.isPending}
          onConfirm={handleConfirmDeleteRule}
          onClose={() => setPendingDeleteRuleId(null)}
        />
      )}

      <div className="flex items-center justify-between">
        <Link
          to="/admin/campaigns"
          className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
        >
          <ChevronLeft size={16} /> Danh sách Campaigns
        </Link>
        {campaign.status !== 'ENDED' && (
          <Link
            to={`/admin/campaigns/${id}/edit`}
            className="flex items-center gap-1.5 px-3 py-1.5 border border-border bg-surface hover:bg-surface-muted text-sm font-semibold text-text-primary rounded-xl transition-colors"
          >
            <Pencil size={14} /> Chỉnh sửa
          </Link>
        )}
      </div>

      {mutationError && (
        <div className="px-4 py-3 bg-error-bg border border-error-text/20 text-error-text text-sm rounded-xl">
          {mutationError}
        </div>
      )}

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-text-primary">{campaign.name}</h2>
            <p className="text-sm text-text-muted mt-1">ID: #{campaign.id}</p>
          </div>
          <div className="flex items-center gap-3">
            <CampaignStatusBadge status={campaign.status} />
            {transition && (
              <button
                type="button"
                disabled={updateStatus.isPending}
                onClick={handleStatusChange}
                className="px-3 py-1.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-semibold transition-all hover:shadow-glow disabled:opacity-60 cursor-pointer"
              >
                {updateStatus.isPending ? 'Đang xử lý...' : transition.label}
              </button>
            )}
          </div>
        </div>
        <div className="grid grid-cols-3 gap-4 mt-5">
          <div>
            <p className="text-xs text-text-muted mb-0.5">Bắt đầu</p>
            <p className="text-sm font-medium text-text-primary">
              {campaign.startTime ? new Date(campaign.startTime).toLocaleDateString('vi-VN') : '—'}
            </p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Kết thúc</p>
            <p className="text-sm font-medium text-text-primary">
              {campaign.endTime ? new Date(campaign.endTime).toLocaleDateString('vi-VN') : '—'}
            </p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-0.5">Ngân sách</p>
            <p className="text-sm font-medium text-text-primary">
              {formatCurrency(campaign.promotionBudget)}
            </p>
          </div>
        </div>
      </div>

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
            {i === 1 && rules.length > 0 && (
              <span className="ml-1.5 text-xs bg-primary/10 text-primary rounded-full px-1.5">
                {rules.length}
              </span>
            )}
          </button>
        ))}
      </div>

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
            campaignStatus={campaign.status}
            addLoading={createRule.isPending}
            editLoading={updateRule.isPending}
            deletingId={deletingRuleId}
            onAdd={async (body) => {
              try {
                await createRule.mutateAsync({ campaignId: id, body });
              } catch (err) {
                showError(err);
                throw err;
              }
            }}
            onEdit={async (ruleId, body) => {
              try {
                await updateRule.mutateAsync({ campaignId: id, ruleId, body });
              } catch (err) {
                showError(err);
                throw err;
              }
            }}
            onDelete={(ruleId) => setPendingDeleteRuleId(ruleId)}
          />
        </div>
      )}

      {tab === 2 && (
        <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          {stats ? (
            <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
              <StatItem label="Mã đã phát" value={stats.totalIssued} />
              <StatItem label="Mã đã dùng" value={stats.totalRedeemed} />
              <StatItem label="Tỷ lệ chuyển đổi" value={`${stats.redemptionRate ?? 0}%`} />
              <StatItem label="Tổng tiền giảm" value={formatCurrency(stats.totalDiscount)} />
              <StatItem label="Doanh thu" value={formatCurrency(stats.totalRevenue)} />
            </div>
          ) : (
            <p className="text-sm text-text-muted">Chưa có dữ liệu thống kê.</p>
          )}
        </div>
      )}
    </div>
  );
}
