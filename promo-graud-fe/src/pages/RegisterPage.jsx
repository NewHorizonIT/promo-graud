import { useNavigate, Link } from 'react-router-dom';
import RegisterForm from '../features/auth/components/RegisterForm';

export default function RegisterPage() {
  const navigate = useNavigate();

  const handleRegisterSuccess = () => {
    navigate('/login', { state: { registered: true } });
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-block text-3xl font-bold tracking-tight">
            Promo<span className="text-primary">Wear</span>
          </Link>
          <p className="mt-2 text-sm text-text-secondary">Tạo tài khoản để bắt đầu mua sắm</p>
        </div>

        <RegisterForm onSuccess={handleRegisterSuccess} />

        <p className="text-center text-sm text-text-secondary mt-6">
          Đã có tài khoản?{' '}
          <Link to="/login" className="text-primary font-medium hover:underline">
            Đăng nhập
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
