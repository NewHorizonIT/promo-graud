import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ChevronLeft, CheckCircle2, XCircle, Loader2 } from 'lucide-react';
import VoucherForm from '../../features/admin/vouchers/components/VoucherForm';
import {
  useCreateVoucher,
  useJobStatus,
} from '../../features/admin/vouchers/hooks/useAdminVouchers';
import { getCampaigns, getRules } from '../../features/admin/campaigns/services/campaignService';

function JobStatusPanel({ jobStatus, onNavigate }) {
  const isRunning = !jobStatus || jobStatus.status === 'STARTING' || jobStatus.status === 'STARTED';
  const isCompleted = jobStatus?.status === 'COMPLETED';
  const isFailed = jobStatus?.status === 'FAILED' || jobStatus?.status === 'STOPPED';

  const processed = jobStatus?.processed ?? 0;
  const requested = jobStatus?.requested ?? 0;
  const pct = requested > 0 ? Math.round((processed / requested) * 100) : 0;

  return (
    <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1 space-y-4">
      <div className="flex items-center gap-3">
        {isRunning && <Loader2 size={20} className="text-primary animate-spin shrink-0" />}
        {isCompleted && <CheckCircle2 size={20} className="text-success-text shrink-0" />}
        {isFailed && <XCircle size={20} className="text-error-text shrink-0" />}
        <div>
          <p className="text-base font-bold text-text-primary">
            {isRunning && 'Đang sinh mã voucher...'}
            {isCompleted && 'Sinh mã hoàn thành!'}
            {isFailed && 'Sinh mã thất bại'}
          </p>
          <p className="text-xs text-text-muted mt-0.5">Job #{jobStatus?.jobId ?? '—'}</p>
        </div>
      </div>

      <div>
        <div className="flex justify-between text-xs text-text-muted mb-1.5">
          <span>Tiến trình</span>
          <span>
            {processed} / {requested} mã ({pct}%)
          </span>
        </div>
        <div className="h-2.5 bg-border rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              isFailed ? 'bg-error-text' : 'bg-primary'
            }`}
            style={{ width: `${pct}%` }}
          />
        </div>
      </div>

      {isFailed && jobStatus?.errorMessage && (
        <p className="text-sm text-error-text bg-error-bg border border-error-text/20 px-4 py-2.5 rounded-xl">
          {jobStatus.errorMessage}
        </p>
      )}

      {(isCompleted || isFailed) && (
        <button
          type="button"
          onClick={onNavigate}
          className="w-full py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow cursor-pointer"
        >
          Về danh sách Vouchers
        </button>
      )}
    </div>
  );
}

export default function VoucherFormPage() {
  const navigate = useNavigate();
  const [campaigns, setCampaigns] = useState([]);
  const [rules, setRules] = useState([]);
  const [loadingRules, setLoadingRules] = useState(false);
  const [error, setError] = useState('');
  const [pendingJobId, setPendingJobId] = useState(null);

  const createMutation = useCreateVoucher();
  const { data: jobStatus } = useJobStatus(pendingJobId);

  useEffect(() => {
    getCampaigns({ pageSize: 100 }).then((res) => setCampaigns(res?.data ?? []));
  }, []);

  const handleCampaignChange = (campaignId) => {
    setRules([]);
    if (!campaignId) return;
    setLoadingRules(true);
    getRules(campaignId)
      .then((res) => setRules(res?.data ?? []))
      .finally(() => setLoadingRules(false));
  };

  // Auto-navigate 2s sau khi job COMPLETED
  useEffect(() => {
    if (jobStatus?.status !== 'COMPLETED') return;
    const timer = setTimeout(() => navigate('/admin/vouchers'), 2000);
    return () => clearTimeout(timer);
  }, [jobStatus?.status, navigate]);

  const handleSubmit = (body) => {
    setError('');
    createMutation.mutate(body, {
      onSuccess: (result) => {
        if (result?.status === 'processing' && result?.jobId) {
          setPendingJobId(result.jobId);
        } else {
          navigate('/admin/vouchers');
        }
      },
      onError: (err) => setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.'),
    });
  };

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <Link
        to="/admin/vouchers"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Quay lại danh sách
      </Link>

      {pendingJobId ? (
        <JobStatusPanel jobStatus={jobStatus} onNavigate={() => navigate('/admin/vouchers')} />
      ) : (
        <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
          <h2 className="text-xl font-bold text-text-primary mb-6">Tạo Voucher mới</h2>

          {error && (
            <div className="mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20 text-sm text-error-text">
              {error}
            </div>
          )}

          <VoucherForm
            campaigns={campaigns}
            rules={rules}
            loadingRules={loadingRules}
            onCampaignChange={handleCampaignChange}
            onSubmit={handleSubmit}
            loading={createMutation.isPending}
          />
        </div>
      )}
    </div>
  );
}
