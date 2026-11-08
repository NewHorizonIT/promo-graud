import { ArrowUp, ArrowDown } from 'lucide-react';
import VoucherStatusBadge from '../../vouchers/components/VoucherStatusBadge';

const VOUCHER_TYPE_LABEL = { GENERIC: 'Generic', UNIQUE: 'Unique' };

function SortButton({ label, field, sortBy, sortDir, onSort }) {
  const active = sortBy === field;
  return (
    <button
      type="button"
      onClick={() => onSort(field)}
      className={`inline-flex items-center gap-1 text-xs font-semibold transition-colors cursor-pointer ${
        active ? 'text-primary' : 'text-text-muted hover:text-text-primary'
      }`}
    >
      {label}
      {active ? (
        sortDir === 'asc' ? (
          <ArrowUp size={12} />
        ) : (
          <ArrowDown size={12} />
        )
      ) : (
        <ArrowDown size={12} className="opacity-30" />
      )}
    </button>
  );
}

export default function VoucherUtilizationTable({ data = [], sortBy, sortDir, onSort }) {
  if (!data.length) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center bg-surface-muted rounded-xl">
        <p className="text-sm font-semibold text-text-primary">Chưa có voucher</p>
        <p className="text-xs text-text-muted mt-1">Campaign này chưa có voucher nào</p>
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
                Code
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Loại
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Phát hành
              </th>
              <th className="text-right py-3 px-4">
                <SortButton
                  label="Còn lại"
                  field="quantity_remain"
                  sortBy={sortBy}
                  sortDir={sortDir}
                  onSort={onSort}
                />
              </th>
              <th className="text-right py-3 px-4">
                <SortButton
                  label="Tỉ lệ dùng"
                  field="usage_rate"
                  sortBy={sortBy}
                  sortDir={sortDir}
                  onSort={onSort}
                />
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Trạng thái
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {data.map((v) => (
              <tr key={v.voucherId} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4 font-mono text-xs text-text-primary">{v.code}</td>
                <td className="py-3 px-4 text-text-secondary">
                  {VOUCHER_TYPE_LABEL[v.type] ?? v.type}
                </td>
                <td className="py-3 px-4 text-right text-text-secondary">{v.quantity}</td>
                <td className="py-3 px-4 text-right font-semibold text-text-primary">
                  {v.quantityRemain}
                </td>
                <td className="py-3 px-4 text-right">
                  <div className="flex items-center justify-end gap-2">
                    <div className="w-20 h-1.5 bg-surface-muted rounded-full overflow-hidden">
                      <div
                        className="h-full bg-primary rounded-full transition-all"
                        style={{ width: `${Math.min(v.usageRate, 100)}%` }}
                      />
                    </div>
                    <span className="text-xs font-semibold text-text-primary w-10 text-right">
                      {v.usageRate}%
                    </span>
                  </div>
                </td>
                <td className="py-3 px-4">
                  <VoucherStatusBadge status={v.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
