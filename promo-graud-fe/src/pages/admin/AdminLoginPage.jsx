import { useNavigate, Link } from 'react-router-dom';
import LoginForm from '../../features/auth/components/LoginForm';

export default function AdminLoginPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-block text-3xl font-bold tracking-tight">
            Promo<span className="text-primary">Guard</span>
          </Link>
          <span className="ml-2 text-xs font-bold text-primary bg-primary-light px-2 py-0.5 rounded-sm uppercase tracking-wide align-middle">
            Admin
          </span>
          <p className="mt-3 text-sm text-text-secondary">Đăng nhập vào Admin Panel</p>
        </div>

        <LoginForm onSuccess={() => navigate('/admin')} />

        <p className="text-center text-xs text-text-muted mt-6">
          © {new Date().getFullYear()} PromoGuard. All rights reserved.
        </p>
      </div>
    </div>
  );
}
