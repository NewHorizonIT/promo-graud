import { useState } from 'react';
import {
  User,
  Mail,
  Shield,
  Crown,
  Calendar,
  Hash,
  TrendingUp,
  Pencil,
  X,
  Eye,
  EyeOff,
  AlertCircle,
  Check,
} from 'lucide-react';
import { useMe, useUpdateUser } from '../features/user/hooks/useUser';
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

function FieldError({ id, message }) {
  if (!message) return null;
  return (
    <p id={id} className="mt-1.5 text-xs text-error-text flex items-center gap-1">
      <AlertCircle size={12} />
      {message}
    </p>
  );
}

function InputField({
  id,
  label,
  type = 'text',
  value,
  onChange,
  error,
  placeholder,
  autoComplete,
  rightSlot,
}) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-text-secondary mb-1.5">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          aria-invalid={!!error}
          aria-describedby={error ? `${id}-error` : undefined}
          className={`w-full h-12 px-4 ${rightSlot ? 'pr-12' : ''} rounded-xl border bg-surface-muted
            text-sm text-text-primary placeholder:text-text-muted
            focus:outline-none focus:ring-2 transition-all duration-200
            ${
              error
                ? 'border-error-text focus:border-error-text focus:ring-error-text/20'
                : 'border-border focus:border-primary focus:ring-primary/20'
            }`}
        />
        {rightSlot && <div className="absolute right-4 top-1/2 -translate-y-1/2">{rightSlot}</div>}
      </div>
      <FieldError id={`${id}-error`} message={error} />
    </div>
  );
}

function validate(form) {
  const errors = {};
  if (!form.username.trim()) {
    errors.username = 'Tên đăng nhập không được để trống.';
  } else if (form.username.trim().length < 3) {
    errors.username = 'Tên đăng nhập phải có ít nhất 3 ký tự.';
  }
  if (!form.email.trim()) {
    errors.email = 'Email không được để trống.';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = 'Email không đúng định dạng.';
  }
  if (form.password) {
    if (form.password.length < 6) {
      errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.';
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    }
  }
  return errors;
}

function EditProfileModal({ user, onClose }) {
  const [form, setForm] = useState({
    username: user.username ?? '',
    email: user.email ?? '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [success, setSuccess] = useState(false);

  const { mutate: doUpdate, isPending, error: apiError } = useUpdateUser();

  const set = (field) => (e) => {
    setForm((p) => ({ ...p, [field]: e.target.value }));
    if (fieldErrors[field]) setFieldErrors((p) => ({ ...p, [field]: undefined }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errors = validate(form);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});

    const payload = {
      username: form.username.trim(),
      email: form.email.trim(),
      typeOfUser: user.typeOfUserId,
      role: user.role,
      ...(form.password ? { password: form.password } : {}),
    };

    doUpdate(
      { id: user.id, payload },
      {
        onSuccess: () => {
          setSuccess(true);
          setTimeout(onClose, 1200);
        },
      },
    );
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div
        className="w-full max-w-md bg-surface rounded-2xl p-6 space-y-5"
        style={{ boxShadow: 'var(--shadow-level-2)' }}
      >
        {/* Header */}
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-text-primary">Chỉnh sửa hồ sơ</h2>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Success */}
        {success && (
          <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-green-50 border border-green-200 text-green-700 text-sm dark:bg-green-900/20 dark:border-green-700/30 dark:text-green-400">
            <Check size={16} />
            Cập nhật thành công!
          </div>
        )}

        {/* API error */}
        {apiError && !success && (
          <div className="flex items-start gap-3 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20">
            <AlertCircle size={18} className="text-error-text mt-0.5 shrink-0" />
            <p className="text-sm text-error-text">{apiError.message}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="space-y-4">
          <InputField
            id="edit-username"
            label="Tên đăng nhập"
            value={form.username}
            onChange={set('username')}
            error={fieldErrors.username}
            placeholder="Nhập tên đăng nhập"
            autoComplete="username"
          />

          <InputField
            id="edit-email"
            label="Email"
            type="email"
            value={form.email}
            onChange={set('email')}
            error={fieldErrors.email}
            placeholder="Nhập email"
            autoComplete="email"
          />

          <InputField
            id="edit-password"
            label="Mật khẩu mới (để trống nếu không đổi)"
            type={showPassword ? 'text' : 'password'}
            value={form.password}
            onChange={set('password')}
            error={fieldErrors.password}
            placeholder="Nhập mật khẩu mới"
            autoComplete="new-password"
            rightSlot={
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="text-text-muted hover:text-text-primary transition-colors"
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            }
          />

          {form.password && (
            <InputField
              id="edit-confirm-password"
              label="Xác nhận mật khẩu mới"
              type={showConfirm ? 'text' : 'password'}
              value={form.confirmPassword}
              onChange={set('confirmPassword')}
              error={fieldErrors.confirmPassword}
              placeholder="Nhập lại mật khẩu mới"
              autoComplete="new-password"
              rightSlot={
                <button
                  type="button"
                  onClick={() => setShowConfirm((v) => !v)}
                  className="text-text-muted hover:text-text-primary transition-colors"
                  aria-label={showConfirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              }
            />
          )}

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 h-11 rounded-xl border border-border text-sm font-medium text-text-secondary hover:bg-surface-muted transition-colors"
            >
              Huỷ
            </button>
            <button
              type="submit"
              disabled={isPending || success}
              className="flex-1 h-11 rounded-xl bg-primary hover:bg-primary-hover text-white text-sm font-semibold
                flex items-center justify-center gap-2 transition-all duration-200
                disabled:opacity-60 disabled:cursor-not-allowed hover:shadow-[var(--shadow-glow)] active:scale-[0.98]"
            >
              {isPending ? (
                <>
                  <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                    />
                  </svg>
                  Đang lưu...
                </>
              ) : (
                'Lưu thay đổi'
              )}
            </button>
          </div>
        </form>
      </div>
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
  const [editOpen, setEditOpen] = useState(false);

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

  const tiers = [...(typeOfUsers ?? [])].sort((a, b) => Number(a.threshold) - Number(b.threshold));
  const currentTier = tiers.find((t) => t.type === user.typeOfUserType);
  const nextTier = tiers.find((t) => Number(t.threshold) > Number(currentTier?.threshold ?? -1));
  const totalSpending = Number(user.totalSpending ?? 0);
  const progressPercent = nextTier
    ? Math.min(100, (totalSpending / Number(nextTier.threshold)) * 100)
    : 100;
  const remaining = nextTier ? Math.max(0, Number(nextTier.threshold) - totalSpending) : 0;

  return (
    <>
      <div className="max-w-2xl mx-auto px-4 py-10 space-y-6">
        {/* ── Header card ── */}
        <div className="flex items-center justify-between gap-5">
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

          <button
            onClick={() => setEditOpen(true)}
            className="shrink-0 flex items-center gap-2 h-9 px-4 rounded-xl border border-border text-sm font-medium text-text-secondary hover:bg-surface-muted hover:text-text-primary transition-colors"
          >
            <Pencil size={15} />
            Chỉnh sửa
          </button>
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
                  Còn{' '}
                  <span className="font-semibold text-primary">{formatCurrency(remaining)}</span> để
                  lên hạng {TIER_LABEL[nextTier.type] ?? nextTier.type}
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

      {editOpen && <EditProfileModal user={user} onClose={() => setEditOpen(false)} />}
    </>
  );
}
