const STATUS_MAP = {
  pending: { label: "Đang gửi",   className: "bg-amber-50 text-amber-600 border-amber-200" },
  success: { label: "Thành công", className: "bg-success-bg text-success-text border-success-text/20" },
  failed:  { label: "Thất bại",   className: "bg-error-bg text-error-text border-error-text/20" },
};

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

export default function DistributionLogTable({ logs = [] }) {
  if (!logs.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          📨
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có lịch sử phân phối</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Voucher ID</th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Kênh</th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Người nhận</th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Trạng thái</th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Thời gian</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {logs.map((log) => {
              const statusCfg = STATUS_MAP[log.status] ?? STATUS_MAP.pending;
              return (
                <tr key={log.id} className="hover:bg-surface-muted/40 transition-colors">
                  <td className="py-3 px-4 text-text-secondary">#{log.voucher_id}</td>
                  <td className="py-3 px-4 text-text-secondary capitalize">{log.channel}</td>
                  <td className="py-3 px-4 text-text-primary font-medium">{log.recipient ?? "—"}</td>
                  <td className="py-3 px-4">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${statusCfg.className}`}>
                      {statusCfg.label}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-xs text-text-secondary">{formatDate(log.sent_at)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
