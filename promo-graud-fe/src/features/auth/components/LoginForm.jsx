import { useState } from "react";
import { Eye, EyeOff, LogIn, AlertCircle } from "lucide-react";
import { login, setStoredToken } from "../services/authService";

export default function LoginForm({ onSuccess }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await login(username, password);

      if (data?.result?.token) {
        setStoredToken(data.result.token);
        if (onSuccess) {
          onSuccess(data.result);
        }
      } else {
        setError(data?.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
      }
    } catch {
      setError("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng hoặc thử lại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="bg-surface rounded-2xl p-8"
      style={{ boxShadow: "var(--shadow-level-2)" }}
    >
      <h1 className="text-2xl font-bold text-text-primary mb-6">Đăng nhập</h1>

      {/* Error banner */}
      {error && (
        <div className="flex items-start gap-3 mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20">
          <AlertCircle size={18} className="text-error-text mt-0.5 shrink-0" />
          <p className="text-sm text-error-text">{error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
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
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Nhập tên đăng nhập"
            className="
              w-full h-12 px-4 rounded-xl border border-border bg-surface-muted
              text-sm text-text-primary placeholder:text-text-muted
              focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20
              transition-all duration-200
            "
          />
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
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Nhập mật khẩu"
              className="
                w-full h-12 pl-4 pr-12 rounded-xl border border-border bg-surface-muted
                text-sm text-text-primary placeholder:text-text-muted
                focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20
                transition-all duration-200
              "
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary transition-colors"
              aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={loading}
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
          {loading ? (
            <span className="flex items-center gap-2">
              <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
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
