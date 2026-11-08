import { formatCurrency } from '../../../../utils/format';

const RULE_TYPE_LABEL = {
  PERCENTAGE: 'Giảm %',
  FIXED_AMOUNT: 'Giảm cố định',
  BUY_X_GET_Y: 'Mua X Tặng Y',
  TIERED: 'Theo bậc',
  FLASH_SALE: 'Flash Sale',
};

const RULE_TYPE_CLASS = {
  PERCENTAGE: 'bg-blue-50 text-blue-700 border-blue-200',
  FIXED_AMOUNT: 'bg-purple-50 text-purple-700 border-purple-200',
  BUY_X_GET_Y: 'bg-amber-50 text-amber-700 border-amber-200',
  TIERED: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  FLASH_SALE: 'bg-red-50 text-red-700 border-red-200',
};

export default function RuleBreakdownTable({ data = [] }) {
  if (!data.length) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center bg-surface-muted rounded-xl">
        <p className="text-sm font-semibold text-text-primary">Chưa có dữ liệu</p>
        <p className="text-xs text-text-muted mt-1">Chưa có lượt redeem nào cho campaign này</p>
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
                Rule
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Loại
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Lượt dùng
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Tiền giảm
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {data.map((r) => {
              const cls =
                RULE_TYPE_CLASS[r.typeOfRule] ?? 'bg-surface-muted text-text-muted border-border';
              return (
                <tr key={r.ruleId} className="hover:bg-surface-muted/40 transition-colors">
                  <td className="py-3 px-4 font-medium text-text-primary">{r.ruleName}</td>
                  <td className="py-3 px-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${cls}`}
                    >
                      {RULE_TYPE_LABEL[r.typeOfRule] ?? r.typeOfRule}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-right font-semibold text-text-primary">
                    {r.redemptionCount}
                  </td>
                  <td className="py-3 px-4 text-right text-text-secondary">
                    {formatCurrency(r.totalDiscount)}
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
