import { formatCurrency } from '../../../../utils/format';

export default function TopUsersTable({ data = [] }) {
  if (!data.length) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center bg-surface-muted rounded-xl">
        <p className="text-sm font-semibold text-text-primary">Chưa có dữ liệu</p>
        <p className="text-xs text-text-muted mt-1">Chưa có lượt redeem nào cho campaign này</p>
      </div>
    );
  }

  const RANK_CLASS = ['text-amber-500', 'text-slate-400', 'text-orange-400'];

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider w-12">
                #
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                User
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Lượt redeem
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Tổng chi tiêu
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {data.map((u, i) => (
              <tr key={u.userId} className="hover:bg-surface-muted/40 transition-colors">
                <td
                  className={`py-3 px-4 font-bold text-base ${RANK_CLASS[i] ?? 'text-text-muted'}`}
                >
                  {i + 1}
                </td>
                <td className="py-3 px-4">
                  <p className="font-medium text-text-primary">{u.username}</p>
                  <p className="text-xs text-text-muted">ID #{u.userId}</p>
                </td>
                <td className="py-3 px-4 text-right font-semibold text-text-primary">
                  {u.redemptionCount}
                </td>
                <td className="py-3 px-4 text-right text-text-secondary">
                  {formatCurrency(u.totalSpent)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
