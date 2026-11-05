import { useNavigate, useLocation } from 'react-router-dom';
import { LogOut, User } from 'lucide-react';
import { useLogout } from '../../features/auth/hooks/useAuth';
import { useMe } from '../../features/user/hooks/useUser';

const PAGE_TITLES = {
  '/admin': 'Dashboard',
  '/admin/campaigns': 'Campaigns',
  '/admin/campaigns/new': 'Campaign Create',
  '/admin/vouchers': 'Vouchers',
  '/admin/vouchers/new': 'Voucher Create',
  '/admin/orders': 'Orders',
  '/admin/products': 'Products',
  '/admin/products/new': 'Product Create',
  '/admin/product-types': 'Product Types',
  '/admin/users': 'Users',
  '/admin/users/new': 'User Create',
  '/admin/user-types': 'User Types',
};

const getPageTitle = (pathname) => {
  // 1. Kiểm tra xem có khớp chính xác trong từ điển không (ví dụ: /admin/users, /admin/users/new)
  if (PAGE_TITLES[pathname]) {
    return PAGE_TITLES[pathname];
  }

  // 2. Xử lý các đường dẫn động (có chứa ID)
  if (pathname.startsWith('/admin/users/')) {
    if (pathname.endsWith('/edit')) return 'User Edit';
    return 'User Detail';
  }

  if (pathname.startsWith('/admin/products/')) {
    if (pathname.endsWith('/edit')) return 'Product Edit';
    return 'Product Detail';
  }

  if (pathname.startsWith('/admin/campaigns/')) {
    if (pathname.endsWith('/edit')) return 'Campaign Edit';
    return 'Campaign Detail';
  }

  if (pathname.startsWith('/admin/vouchers/')) {
    if (pathname.endsWith('/edit')) return 'Voucher Edit';
    return 'Voucher Detail';
  }

  if (pathname.startsWith('/admin/orders/')) {
    return 'Order Detail';
  }

  return 'Admin';
};
export default function AdminTopbar() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { data: me } = useMe();
  const { mutate: doLogout, isPending } = useLogout();

  const title = getPageTitle(pathname);

  const handleLogout = () => {
    doLogout(undefined, {
      onSettled: () => navigate('/admin/login'),
    });
  };

  return (
    <header className="h-16 bg-surface border-b border-border flex items-center justify-between px-6 shrink-0 sticky top-0 z-30">
      <h1 className="text-lg font-bold text-text-primary">{title}</h1>
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-sm text-text-secondary">
          <div className="w-7 h-7 rounded-full bg-surface-muted border border-border flex items-center justify-center">
            <User size={14} />
          </div>
          <span className="font-medium">{me?.username ?? 'Admin'}</span>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          disabled={isPending}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-semibold text-text-secondary hover:text-error-text hover:bg-error-bg transition-all cursor-pointer disabled:opacity-60"
        >
          <LogOut size={15} />
          {isPending ? 'Đang xuất...' : 'Đăng xuất'}
        </button>
      </div>
    </header>
  );
}
