const REASON_MAP = {
  multiple_accounts: {
    label: 'Multi-Account',
    className: 'bg-amber-50 text-amber-600 border-amber-200',
  },
  vpn_detected: { label: 'VPN Detected', className: 'bg-blue-50 text-blue-600 border-blue-200' },
  bot_script: { label: 'Bot Script', className: 'bg-purple-50 text-purple-600 border-purple-200' },
};

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN');
}

export default function FraudLogTable({ logs = [] }) {
  if (!logs.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          🛡️
        </div>
        <p className="text-sm font-semibold text-text-primary">Không có log gian lận</p>
        <p className="text-xs text-text-muted mt-1">Hệ thống chưa phát hiện hành vi gian lận nào</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                User ID
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                IP Address
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Lý do
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Voucher Code
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thời gian
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {logs.map((log) => {
              const reasonCfg = REASON_MAP[log.reason] ?? {
                label: log.reason,
                className: 'bg-surface-muted text-text-muted border-border',
              };
              return (
                <tr key={log.id} className="hover:bg-surface-muted/40 transition-colors">
                  <td className="py-3 px-4 text-text-secondary">#{log.user_id}</td>
                  <td className="py-3 px-4 font-mono text-xs text-text-primary">
                    {log.ip_address ?? '—'}
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${reasonCfg.className}`}
                    >
                      {reasonCfg.label}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-mono text-xs text-text-secondary">
                    {log.voucher_code ?? '—'}
                  </td>
                  <td className="py-3 px-4 text-xs text-text-secondary">
                    {formatDate(log.created_at)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
