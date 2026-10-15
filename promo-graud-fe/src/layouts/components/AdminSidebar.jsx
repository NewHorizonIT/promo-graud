import { NavLink } from "react-router-dom";
import { LayoutDashboard, Megaphone, Ticket, Send, ShieldAlert } from "lucide-react";

const NAV_ITEMS = [
  { to: "/admin",              label: "Dashboard",         icon: LayoutDashboard, end: true },
  { to: "/admin/campaigns",    label: "Campaigns",         icon: Megaphone },
  { to: "/admin/vouchers",     label: "Vouchers",          icon: Ticket },
  { to: "/admin/distribution", label: "Distribution",      icon: Send },
  { to: "/admin/fraud",        label: "Fraud Logs",        icon: ShieldAlert },
];

export default function AdminSidebar() {
  return (
    <aside className="fixed top-0 left-0 h-full w-60 bg-surface border-r border-border flex flex-col z-40">
      {/* Logo */}
      <div className="h-16 flex items-center gap-2 px-5 border-b border-border shrink-0">
        <span className="text-xl font-bold tracking-tight">
          Promo<span className="text-primary">Guard</span>
        </span>
        <span className="text-[10px] font-bold text-primary bg-primary-light px-1.5 py-0.5 rounded-sm uppercase tracking-wide">
          Admin
        </span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-4 px-3 space-y-0.5 overflow-y-auto">
        {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                isActive
                  ? "bg-primary-light text-primary font-semibold"
                  : "text-text-secondary hover:bg-surface-muted hover:text-text-primary"
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="px-5 py-3 border-t border-border">
        <p className="text-[11px] text-text-muted">PromoGuard Admin v1.0</p>
      </div>
    </aside>
  );
}
