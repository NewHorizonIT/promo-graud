import { useState, useEffect, useRef } from 'react';
import {
  ShoppingCart,
  Bell,
  Search,
  LogOut,
  LogIn,
  X,
  Sun,
  Moon,
  User,
  History,
} from 'lucide-react';
import { Link, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { useLogout } from '../../features/auth/hooks/useAuth';
import { useAuthStore } from '../../store/authStore';
import { useThemeStore } from '../../store/themeStore';
import { useCart } from '../../hooks/useCart';

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { cartCount, handleLogoutCart } = useCart();

  const token = useAuthStore((s) => s.token);
  const userName = useAuthStore((s) => s.userName);
  const { theme, toggle: toggleTheme } = useThemeStore();
  const { mutate: doLogout, isPending: logoutLoading } = useLogout();
  const [headerKeyword, setHeaderKeyword] = useState('');
  const [showMobileSearch, setShowMobileSearch] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (location.pathname === '/products') {
      const urlQuery = searchParams.get('name') || searchParams.get('search') || '';
      setHeaderKeyword(urlQuery);
    }
  }, [location.pathname, searchParams]);

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const query = headerKeyword.trim();
    if (query) {
      navigate(`/products?name=${encodeURIComponent(query)}`);
    } else {
      navigate('/products');
    }
    setShowMobileSearch(false);
  };

  const handleLogout = () => {
    doLogout(undefined, {
      onSettled: () => {
        handleLogoutCart();
        navigate('/login');
      },
    });
  };

  const initials = userName?.[0]?.toUpperCase() ?? 'U';

  return (
    <header className="sticky top-0 z-50 w-full bg-surface border-b border-border shadow-sm">
      <div className="max-w-[1280px] mx-auto px-4 md:px-6 h-16 flex items-center justify-between gap-4">
        {/* Logo & Navigation */}
        <div className="flex items-center gap-6 lg:gap-8 shrink-0">
          <Link to="/" className="text-2xl font-bold tracking-tight">
            Promo<span className="text-primary">Wear</span>
          </Link>
          <nav className="hidden md:flex items-center gap-6">
            <Link
              to="/"
              className={`text-sm font-semibold transition-colors ${
                location.pathname === '/'
                  ? 'text-primary'
                  : 'text-text-secondary hover:text-primary'
              }`}
            >
              Trang chủ
            </Link>
            <Link
              to="/products"
              className={`text-sm font-semibold transition-colors ${
                location.pathname === '/products'
                  ? 'text-primary'
                  : 'text-text-secondary hover:text-primary'
              }`}
            >
              Sản phẩm
            </Link>
            <Link
              to="/vouchers"
              className="text-sm font-semibold text-text-secondary hover:text-primary transition-colors"
            >
              Voucher
            </Link>
          </nav>
        </div>

        {/* Search Bar (Desktop) */}
        <form
          onSubmit={handleSearchSubmit}
          className="hidden md:flex flex-1 max-w-md relative items-center"
        >
          <input
            type="text"
            value={headerKeyword}
            onChange={(e) => setHeaderKeyword(e.target.value)}
            placeholder="Tìm kiếm sản phẩm theo tên..."
            className="
              w-full h-10 pl-4 pr-10 rounded-full border border-border bg-surface-muted
              text-sm text-text-primary placeholder:text-text-muted
              focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20
              transition-all
            "
          />
          <button
            type="submit"
            aria-label="Tìm kiếm"
            className="absolute right-3 text-text-muted hover:text-primary transition-colors cursor-pointer"
          >
            <Search size={18} />
          </button>
        </form>

        {/* Actions & Mobile Search Toggle */}
        <div className="flex items-center gap-2 lg:gap-3 shrink-0">
          {/* Mobile search button */}
          <button
            type="button"
            onClick={() => setShowMobileSearch(!showMobileSearch)}
            aria-label="Mở tìm kiếm"
            className="md:hidden p-2 rounded-full hover:bg-surface-muted text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
          >
            {showMobileSearch ? <X size={20} /> : <Search size={20} />}
          </button>

          {/* Theme toggle */}
          <button
            type="button"
            onClick={toggleTheme}
            aria-label={theme === 'dark' ? 'Chuyển sang sáng' : 'Chuyển sang tối'}
            className="p-2 rounded-full hover:bg-surface-muted text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
          >
            {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
          </button>

          {/* Cart */}
          <Link
            to="/cart"
            className="relative p-2 rounded-full hover:bg-surface-muted text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
            aria-label="Giỏ hàng"
          >
            <ShoppingCart size={21} />
            {cartCount > 0 && (
              <span className="absolute top-0.5 right-0.5 min-w-[18px] h-[18px] px-1 bg-primary text-white text-[10px] font-bold rounded-full flex items-center justify-center shadow-sm">
                {cartCount > 99 ? '99+' : cartCount}
              </span>
            )}
          </Link>

          {token ? (
            <>
              <button
                type="button"
                className="p-2 rounded-full hover:bg-surface-muted text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
                aria-label="Thông báo"
              >
                <Bell size={20} />
              </button>

              {/* Avatar dropdown */}
              <div ref={dropdownRef} className="relative">
                <button
                  type="button"
                  onClick={() => setDropdownOpen((o) => !o)}
                  aria-label="Tài khoản"
                  className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-bold hover:bg-primary/20 transition-colors cursor-pointer select-none"
                >
                  {initials}
                </button>

                {dropdownOpen && (
                  <div className="absolute right-0 mt-2 w-52 bg-surface border border-border rounded-xl shadow-level-2 z-50 overflow-hidden">
                    <div className="px-4 py-3 border-b border-border">
                      <p className="text-sm font-semibold text-text-primary truncate">{userName}</p>
                    </div>
                    <Link
                      to="/profile"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-3 px-4 py-2.5 text-sm text-text-secondary hover:bg-surface-muted hover:text-text-primary transition-colors"
                    >
                      <User size={16} />
                      Thông tin cá nhân
                    </Link>
                    <Link
                      to="/orders"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-3 px-4 py-2.5 text-sm text-text-secondary hover:bg-surface-muted hover:text-text-primary transition-colors"
                    >
                      <History size={16} />
                      Lịch sử mua hàng
                    </Link>
                    <div className="border-t border-border">
                      <button
                        type="button"
                        onClick={() => {
                          setDropdownOpen(false);
                          handleLogout();
                        }}
                        disabled={logoutLoading}
                        className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-text-secondary hover:bg-error-bg hover:text-error-text transition-colors disabled:opacity-60 cursor-pointer"
                      >
                        <LogOut size={16} />
                        {logoutLoading ? 'Đang đăng xuất...' : 'Đăng xuất'}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <Link
              to="/login"
              className="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-semibold text-white bg-primary hover:bg-primary-hover transition-all duration-200 hover:shadow-glow"
            >
              <LogIn size={16} />
              <span>Đăng nhập</span>
            </Link>
          )}
        </div>
      </div>

      {/* Mobile Search Expandable Bar */}
      {showMobileSearch && (
        <div className="md:hidden px-4 pb-3 border-t border-border pt-2 bg-surface">
          <form onSubmit={handleSearchSubmit} className="relative flex items-center">
            <input
              type="text"
              value={headerKeyword}
              onChange={(e) => setHeaderKeyword(e.target.value)}
              placeholder="Tìm kiếm sản phẩm theo tên..."
              autoFocus
              className="
                w-full h-10 pl-4 pr-10 rounded-xl border border-border bg-surface-muted
                text-sm text-text-primary placeholder:text-text-muted
                focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary
              "
            />
            <button
              type="submit"
              className="absolute right-3 text-text-muted hover:text-primary transition-colors"
            >
              <Search size={18} />
            </button>
          </form>
        </div>
      )}
    </header>
  );
}
