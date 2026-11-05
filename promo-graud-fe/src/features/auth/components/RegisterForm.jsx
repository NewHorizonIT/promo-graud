import { useState, useEffect } from 'react';
import { Eye, EyeOff, AlertCircle } from 'lucide-react';
import { useRegister } from '../hooks/useAuth';
import Toast from '../../../components/Toast';

function validate(username, email, password, confirmPassword) {
  const errors = {};
  if (!username.trim()) {
    errors.username = 'Tên đăng nhập không được để trống.';
  } else if (username.trim().length < 3) {
    errors.username = 'Tên đăng nhập phải có ít nhất 3 ký tự.';
  }
  if (!email.trim()) {
    errors.email = 'Email không được để trống.';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
    errors.email = 'Email không đúng định dạng.';
  }
  if (!password) {
    errors.password = 'Mật khẩu không được để trống.';
  } else if (password.length < 6) {
    errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.';
  }
  if (!confirmPassword) {
    errors.confirmPassword = 'Vui lòng xác nhận mật khẩu.';
  } else if (password !== confirmPassword) {
    errors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
  }
  return errors;
}

export default function RegisterForm({ onSuccess }) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const [toast, setToast] = useState(null);
  const { mutate: doRegister, isPending, error: apiError } = useRegister();

  useEffect(() => {
    if (apiError) {
      const msg = apiError.response?.data?.message || apiError.message;
      setToast({ type: 'error', message: msg });
    }
  }, [apiError]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const errors = validate(username, email, password, confirmPassword);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    doRegister(
      { username: username.trim(), email: email.trim(), password },
      { onSuccess: (data) => onSuccess?.(data) },
    );
  };

  const clearError = (field) => setFieldErrors((prev) => ({ ...prev, [field]: undefined }));

  const inputClass = (field) => `
    w-full h-12 px-4 rounded-xl border bg-surface-muted
    text-sm text-text-primary placeholder:text-text-muted
    focus:outline-none focus:ring-2 transition-all duration-200
    ${
      fieldErrors[field]
        ? 'border-error-text focus:border-error-text focus:ring-error-text/20'
        : 'border-border focus:border-primary focus:ring-primary/20'
    }
  `;

  return (
    <div className="bg-surface rounded-2xl p-8" style={{ boxShadow: 'var(--shadow-level-2)' }}>
      <Toast toast={toast} onClose={() => setToast(null)} />
      <h1 className="text-2xl font-bold text-text-primary mb-6">Đăng ký tài khoản</h1>

      <form onSubmit={handleSubmit} noValidate className="space-y-5">
        {/* Username */}
        <div>
          <label
            htmlFor="reg-username"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Tên đăng nhập
          </label>
          <input
            id="reg-username"
            type="text"
            autoComplete="username"
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              clearError('username');
            }}
            placeholder="Nhập tên đăng nhập"
            aria-invalid={!!fieldErrors.username}
            className={inputClass('username')}
          />
          {fieldErrors.username && (
            <p className="mt-1.5 text-xs text-error-text flex items-center gap-1">
              <AlertCircle size={12} /> {fieldErrors.username}
            </p>
          )}
        </div>

        {/* Email */}
        <div>
          <label
            htmlFor="reg-email"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Email
          </label>
          <input
            id="reg-email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              clearError('email');
            }}
            placeholder="Nhập địa chỉ email"
            aria-invalid={!!fieldErrors.email}
            className={inputClass('email')}
          />
          {fieldErrors.email && (
            <p className="mt-1.5 text-xs text-error-text flex items-center gap-1">
              <AlertCircle size={12} /> {fieldErrors.email}
            </p>
          )}
        </div>

        {/* Password */}
        <div>
          <label
            htmlFor="reg-password"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Mật khẩu
          </label>
          <div className="relative">
            <input
              id="reg-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                clearError('password');
                clearError('confirmPassword');
              }}
              placeholder="Tối thiểu 6 ký tự"
              aria-invalid={!!fieldErrors.password}
              className={`${inputClass('password')} pr-12`}
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary transition-colors"
              aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {fieldErrors.password && (
            <p className="mt-1.5 text-xs text-error-text flex items-center gap-1">
              <AlertCircle size={12} /> {fieldErrors.password}
            </p>
          )}
        </div>

        {/* Confirm password */}
        <div>
          <label
            htmlFor="reg-confirm"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Xác nhận mật khẩu
          </label>
          <div className="relative">
            <input
              id="reg-confirm"
              type={showConfirm ? 'text' : 'password'}
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                clearError('confirmPassword');
              }}
              placeholder="Nhập lại mật khẩu"
              aria-invalid={!!fieldErrors.confirmPassword}
              className={`${inputClass('confirmPassword')} pr-12`}
            />
            <button
              type="button"
              onClick={() => setShowConfirm((v) => !v)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary transition-colors"
              aria-label={showConfirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
            >
              {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {fieldErrors.confirmPassword && (
            <p className="mt-1.5 text-xs text-error-text flex items-center gap-1">
              <AlertCircle size={12} /> {fieldErrors.confirmPassword}
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={isPending}
          className="
            w-full h-12 rounded-xl font-semibold text-sm text-white
            bg-primary hover:bg-primary-hover
            flex items-center justify-center gap-2
            transition-all duration-200
            disabled:opacity-60 disabled:cursor-not-allowed
            hover:shadow-[var(--shadow-glow)]
            active:scale-[0.98]
            mt-2
          "
        >
          {isPending ? (
            <span className="flex items-center gap-2">
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
              Đang đăng ký...
            </span>
          ) : (
            <>Đăng ký</>
          )}
        </button>
      </form>
    </div>
  );
}
