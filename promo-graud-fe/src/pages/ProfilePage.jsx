import { User, Mail, Shield, Crown, Calendar, Hash, TrendingUp } from 'lucide-react';
import { useMe } from '../features/user/hooks/useUser';
import { useTypeOfUsers } from '../features/user/hooks/useTypeOfUsers';
import { formatCurrency } from '../utils/format';

const TIER_STYLE = {
  GOLD: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
  SILVER: 'bg-slate-100  text-slate-600  dark:bg-slate-700/40  dark:text-slate-300',
  BRONZE: 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400',
  NORMAL: 'bg-surface-muted text-text-secondary',
};

const TIER_LABEL = {
  GOLD: 'Gold',
  SILVER: 'Silver',
  BRONZE: 'Bronze',
  NORMAL: 'Thành viên',
};

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function InfoRow({ icon: Icon, label, children }) {
  return (
    <div className="flex items-center gap-4 px-6 py-4">
      <span className="shrink-0 w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
        <Icon size={17} />
      </span>
      <span className="w-36 shrink-0 text-sm text-text-secondary">{label}</span>
      <span className="text-sm font-medium text-text-primary">{children}</span>
    </div>
  );
}

function Skeleton() {
  return (
    <div className="max-w-2xl mx-auto px-4 py-10 animate-pulse">
      <div className="flex items-center gap-5 mb-8">
        <div className="w-20 h-20 rounded-full bg-surface-muted" />
        <div className="space-y-2">
          <div className="h-6 w-40 rounded-lg bg-surface-muted" />
          <div className="h-4 w-20 rounded-full bg-surface-muted" />
        </div>
      </div>
      <div className="h-20 rounded-2xl bg-surface-muted mb-4" />
      <div className="bg-surface rounded-2xl border border-border divide-y divide-border">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="flex items-center gap-4 px-6 py-4">
            <div className="w-9 h-9 rounded-xl bg-surface-muted shrink-0" />
            <div className="h-4 w-28 rounded bg-surface-muted" />
            <div className="h-4 w-36 rounded bg-surface-muted" />
          </div>
        ))}
      </div>
    </div>
  );
}

export default function ProfilePage() {
  const { data: user, isLoading, isError } = useMe();
  const { data: typeOfUsers } = useTypeOfUsers();

  if (isLoading) return <Skeleton />;

  if (isError || !user) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-24 text-center">
        <User size={48} className="mx-auto text-text-muted mb-4" />
        <p className="text-text-secondary">Không thể tải thông tin tài khoản.</p>
      </div>
    );
  }

  const initials = user.username?.[0]?.toUpperCase() ?? 'U';
  const tierStyle = TIER_STYLE[user.typeOfUserType] ?? TIER_STYLE.NORMAL;
  const tierLabel = TIER_LABEL[user.typeOfUserType] ?? TIER_LABEL.NORMAL;

  // Progress bar
  const tiers = [...(typeOfUsers ?? [])].sort((a, b) => Number(a.threshold) - Number(b.threshold));
  const currentTier = tiers.find((t) => t.type === user.typeOfUserType);
  const nextTier = tiers.find((t) => Number(t.threshold) > Number(currentTier?.threshold ?? -1));
  const totalSpending = Number(user.totalSpending ?? 0);
  const progressPercent = nextTier
    ? Math.min(100, (totalSpending / Number(nextTier.threshold)) * 100)
    : 100;
  const remaining = nextTier ? Math.max(0, Number(nextTier.threshold) - totalSpending) : 0;

  return (
    <div className="max-w-2xl mx-auto px-4 py-10 space-y-6">
      {/* ── Header card ── */}
      <div className="flex items-center gap-5">
        <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center text-primary text-3xl font-bold select-none shrink-0">
          {initials}
        </div>
        <div className="space-y-1.5">
          <h1 className="text-2xl font-bold text-text-primary leading-none">{user.username}</h1>
          <div className="flex items-center gap-2">
            <span
              className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${
                user.role === 'ADMIN'
                  ? 'bg-primary/10 text-primary'
                  : 'bg-surface-muted text-text-secondary'
              }`}
            >
              {user.role === 'ADMIN' ? 'Admin' : 'User'}
            </span>
            <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${tierStyle}`}>
              {tierLabel}
            </span>
          </div>
        </div>
      </div>

      {/* ── Progress bar ── */}
      {typeOfUsers && currentTier && (
        <div className="bg-surface rounded-2xl border border-border px-6 py-5">
          <div className="flex items-center gap-2 mb-3">
            <TrendingUp size={16} className="text-primary" />
            <span className="text-sm font-semibold text-text-primary">Hạng thành viên</span>
          </div>
          <div className="flex items-center justify-between text-xs text-text-secondary mb-2">
            <span>{formatCurrency(totalSpending)}</span>
            {nextTier ? (
              <span>
                Còn <span className="font-semibold text-primary">{formatCurrency(remaining)}</span>{' '}
                để lên hạng {TIER_LABEL[nextTier.type] ?? nextTier.type}
              </span>
            ) : (
              <span className="font-semibold text-primary">Hạng cao nhất</span>
            )}
          </div>
          <div className="h-2 rounded-full bg-surface-muted overflow-hidden">
            <div
              className="h-full rounded-full bg-primary transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      )}

      {/* ── Info rows ── */}
      <div
        className="bg-surface rounded-2xl border border-border divide-y divide-border"
        style={{ boxShadow: 'var(--shadow-level-2)' }}
      >
        <InfoRow icon={Hash} label="ID tài khoản">
          #{user.id}
        </InfoRow>
        <InfoRow icon={User} label="Tên đăng nhập">
          {user.username}
        </InfoRow>
        <InfoRow icon={Mail} label="Email">
          {user.email ?? '—'}
        </InfoRow>
        <InfoRow icon={Shield} label="Vai trò">
          {user.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng'}
        </InfoRow>
        <InfoRow icon={Crown} label="Hạng thành viên">
          <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${tierStyle}`}>
            {tierLabel}
          </span>
        </InfoRow>
        <InfoRow icon={Calendar} label="Ngày tham gia">
          {formatDate(user.createdAt)}
        </InfoRow>
      </div>
    </div>
  );
}
