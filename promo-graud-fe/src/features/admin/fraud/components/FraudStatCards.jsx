import { ShieldAlert, Users, Wifi, Bot } from 'lucide-react';

const REASON_CONFIG = [
  {
    key: 'multiple_accounts',
    label: 'Multi-Account',
    icon: Users,
    className: 'text-amber-600 bg-amber-50',
  },
  { key: 'vpn_detected', label: 'VPN Detected', icon: Wifi, className: 'text-blue-600 bg-blue-50' },
  { key: 'bot_script', label: 'Bot Script', icon: Bot, className: 'text-purple-600 bg-purple-50' },
];

export default function FraudStatCards({ stats }) {
  if (!stats) return null;

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total blocked */}
      <div className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-9 h-9 rounded-xl bg-error-bg flex items-center justify-center shrink-0">
            <ShieldAlert size={18} className="text-error-text" />
          </div>
          <span className="text-xs font-semibold text-text-muted uppercase tracking-wide">
            Tổng bị block
          </span>
        </div>
        <p className="text-3xl font-bold text-text-primary">{stats.total_blocked ?? 0}</p>
      </div>

      {/* Per-reason cards */}
      {REASON_CONFIG.map(({ key, label, icon: Icon, className }) => (
        <div key={key} className="bg-surface border border-border rounded-xl p-5 shadow-level-1">
          <div className="flex items-center gap-3 mb-3">
            <div
              className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${className}`}
            >
              <Icon size={18} />
            </div>
            <span className="text-xs font-semibold text-text-muted uppercase tracking-wide">
              {label}
            </span>
          </div>
          <p className="text-3xl font-bold text-text-primary">{stats.by_reason?.[key] ?? 0}</p>
        </div>
      ))}
    </div>
  );
}
