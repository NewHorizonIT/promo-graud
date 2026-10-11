import { useNavigate, Link } from "react-router-dom";
import LoginForm from "../features/auth/components/LoginForm";

export default function LoginPage() {
  const navigate = useNavigate();

  const handleLoginSuccess = () => {
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-block text-3xl font-bold tracking-tight">
            Promo<span className="text-primary">Wear</span>
          </Link>
          <p className="mt-2 text-sm text-text-secondary">
            Đăng nhập để khám phá ưu đãi độc quyền
          </p>
        </div>

        {/* Feature Login Form */}
        <LoginForm onSuccess={handleLoginSuccess} />

        <p className="text-center text-xs text-text-muted mt-6">
          © {new Date().getFullYear()} PromoWear. All rights reserved.
        </p>
      </div>
    </div>
  );
}
