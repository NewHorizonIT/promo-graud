import { Pencil, Trash2 } from 'lucide-react';

export default function ProductTypeTable({ types = [], onEdit, onDelete }) {
  if (!types.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          🏷️
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có loại sản phẩm nào</p>
        <p className="text-xs text-text-muted mt-1">Tạo loại đầu tiên để phân nhóm sản phẩm</p>
      </div>
    );
  }

  return (
    <div className="bg-surface border border-border rounded-xl overflow-hidden shadow-level-1">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-muted border-b border-border">
            <tr>
              <th className="text-left py-3 px-4 w-24 text-xs font-semibold text-text-muted uppercase tracking-wider">
                ID
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Tên loại
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {types.map((t) => (
              <tr key={t.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4 text-text-muted text-xs">#{t.id}</td>
                <td className="py-3 px-4">
                  <span className="font-medium text-text-primary">{t.type}</span>
                </td>
                <td className="py-3 px-4">
                  <div className="flex items-center justify-end gap-1">
                    <button
                      type="button"
                      onClick={() => onEdit?.(t)}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer"
                      title="Chỉnh sửa"
                      aria-label="Chỉnh sửa"
                    >
                      <Pencil size={15} />
                    </button>
                    <button
                      type="button"
                      onClick={() => onDelete?.(t)}
                      className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer"
                      title="Xóa"
                      aria-label="Xóa"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
