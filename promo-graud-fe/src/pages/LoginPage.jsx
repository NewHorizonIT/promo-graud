import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import LoginForm from '../features/auth/components/LoginForm';
import Toast from '../components/Toast';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const registered = location.state?.registered;
  const [toast, setToast] = useState(null);

  useEffect(() => {
    if (registered)
      setToast({ type: 'success', message: 'Đăng ký thành công! Vui lòng đăng nhập.' });
  }, [registered]);

  const from = location.state?.from?.pathname || '/';
  const handleLoginSuccess = () => navigate(from, { replace: true });

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <Toast toast={toast} onClose={() => setToast(null)} />
      <div className="relative w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-block text-3xl font-bold tracking-tight">
            Promo<span className="text-primary">Wear</span>
          </Link>
          <p className="mt-2 text-sm text-text-secondary">Đăng nhập để khám phá ưu đãi độc quyền</p>
        </div>

        <LoginForm onSuccess={handleLoginSuccess} />

        <p className="text-center text-sm text-text-secondary mt-6">
          Chưa có tài khoản?{' '}
          <Link to="/register" className="text-primary font-medium hover:underline">
            Đăng ký ngay
          </Link>
        </p>

        <p className="text-center text-xs text-text-muted mt-3">
          <Link to="/" className="hover:text-text-secondary transition-colors">
            ← Quay lại trang chủ
          </Link>
        </p>
      </div>
    </div>
  );
}
