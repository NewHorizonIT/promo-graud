import { useState } from 'react';
import { Eye, EyeOff, LogIn, AlertCircle } from 'lucide-react';
import { useLogin } from '../hooks/useAuth';

function validate(username, password) {
  const errors = {};
  if (!username.trim()) {
    errors.username = 'Tên đăng nhập không được để trống.';
  } else if (username.trim().length < 3) {
    errors.username = 'Tên đăng nhập phải có ít nhất 3 ký tự.';
  }
  if (!password) {
    errors.password = 'Mật khẩu không được để trống.';
  } else if (password.length < 6) {
    errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.';
  }
  return errors;
}

export default function LoginForm({ onSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const { mutate: doLogin, isPending, error: apiError } = useLogin();

  const handleSubmit = (e) => {
    e.preventDefault();
    const errors = validate(username, password);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    doLogin(
      { username: username.trim(), password },
      { onSuccess: (result) => onSuccess?.(result) },
    );
  };

  const handleUsernameChange = (e) => {
    setUsername(e.target.value);
    if (fieldErrors.username) setFieldErrors((p) => ({ ...p, username: undefined }));
  };

  const handlePasswordChange = (e) => {
    setPassword(e.target.value);
    if (fieldErrors.password) setFieldErrors((p) => ({ ...p, password: undefined }));
  };

  return (
    <div className="bg-surface rounded-2xl p-8" style={{ boxShadow: 'var(--shadow-level-2)' }}>
      <h1 className="text-2xl font-bold text-text-primary mb-6">Đăng nhập</h1>

      {/* API error */}
      {apiError && (
        <div className="flex items-start gap-3 mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20">
          <AlertCircle size={18} className="text-error-text mt-0.5 shrink-0" />
          <p className="text-sm text-error-text">{apiError.message}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="space-y-5">
        {/* Username */}
        <div>
          <label
            htmlFor="username"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Tên đăng nhập
          </label>
          <input
            id="username"
            type="text"
            autoComplete="username"
            value={username}
            onChange={handleUsernameChange}
            placeholder="Nhập tên đăng nhập"
            aria-invalid={!!fieldErrors.username}
            aria-describedby={fieldErrors.username ? 'username-error' : undefined}
            className={`
              w-full h-12 px-4 rounded-xl border bg-surface-muted
              text-sm text-text-primary placeholder:text-text-muted
              focus:outline-none focus:ring-2 transition-all duration-200
              ${
                fieldErrors.username
                  ? 'border-error-text focus:border-error-text focus:ring-error-text/20'
                  : 'border-border focus:border-primary focus:ring-primary/20'
              }
            `}
          />
          {fieldErrors.username && (
            <p
              id="username-error"
              className="mt-1.5 text-xs text-error-text flex items-center gap-1"
            >
              <AlertCircle size={12} />
              {fieldErrors.username}
            </p>
          )}
        </div>

        {/* Password */}
        <div>
          <label
            htmlFor="password"
            className="block text-sm font-medium text-text-secondary mb-1.5"
          >
            Mật khẩu
          </label>
          <div className="relative">
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              value={password}
              onChange={handlePasswordChange}
              placeholder="Nhập mật khẩu"
              aria-invalid={!!fieldErrors.password}
              aria-describedby={fieldErrors.password ? 'password-error' : undefined}
              className={`
                w-full h-12 pl-4 pr-12 rounded-xl border bg-surface-muted
                text-sm text-text-primary placeholder:text-text-muted
                focus:outline-none focus:ring-2 transition-all duration-200
                ${
                  fieldErrors.password
                    ? 'border-error-text focus:border-error-text focus:ring-error-text/20'
                    : 'border-border focus:border-primary focus:ring-primary/20'
                }
              `}
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
            <p
              id="password-error"
              className="mt-1.5 text-xs text-error-text flex items-center gap-1"
            >
              <AlertCircle size={12} />
              {fieldErrors.password}
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
              Đang đăng nhập...
            </span>
          ) : (
            <>
              <LogIn size={18} />
              Đăng nhập
            </>
          )}
        </button>
      </form>
    </div>
  );
}
