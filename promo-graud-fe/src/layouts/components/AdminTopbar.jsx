import { useNavigate, useLocation } from "react-router-dom";
import { LogOut, User } from "lucide-react";
import { logout, getStoredToken } from "../../features/auth/services/authService";

const PAGE_TITLES = {
  "/admin":              "Dashboard",
  "/admin/campaigns":    "Campaigns",
  "/admin/vouchers":     "Vouchers",
  "/admin/distribution": "Distribution Logs",
  "/admin/fraud":        "Fraud Logs",
};

export default function AdminTopbar() {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const title =
    PAGE_TITLES[pathname] ??
    (pathname.includes("/campaigns/") ? "Campaign Detail" : "Admin");

  const handleLogout = async () => {
    await logout(getStoredToken());
    navigate("/admin/login");
  };

  return (
    <header className="h-16 bg-surface border-b border-border flex items-center justify-between px-6 shrink-0 sticky top-0 z-30">
      <h1 className="text-lg font-bold text-text-primary">{title}</h1>
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-sm text-text-secondary">
          <div className="w-7 h-7 rounded-full bg-surface-muted border border-border flex items-center justify-center">
            <User size={14} />
          </div>
          <span className="font-medium">Admin</span>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-semibold text-text-secondary hover:text-error-text hover:bg-error-bg transition-all cursor-pointer"
        >
          <LogOut size={15} />
          Đăng xuất
        </button>
      </div>
    </header>
  );
}
