import { Pencil, Trash2, Eye } from 'lucide-react';
import UserTypeBadge from './UserTypeBadge';
import { Link } from 'react-router-dom';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN');
}

export default function UserTable({ users = [], onDelete }) {
  if (!users.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center bg-surface border border-border rounded-xl">
        <div className="w-14 h-14 rounded-full bg-surface-muted flex items-center justify-center mb-3 text-2xl">
          👤
        </div>
        <p className="text-sm font-semibold text-text-primary">Chưa có người dùng nào</p>
        <p className="text-xs text-text-muted mt-1">Tạo người dùng đầu tiên để quản lý hệ thống</p>
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
                Người dùng
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Loại
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Vai trò
              </th>
              <th className="text-left py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Ngày tạo
              </th>
              <th className="text-right py-3 px-4 text-xs font-semibold text-text-muted uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {users.map((u) => (
              <tr key={u.id} className="hover:bg-surface-muted/40 transition-colors">
                <td className="py-3 px-4">
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="shrink-0 w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-lg">
                      {u.username?.charAt(0).toUpperCase()}
                    </div>
                    <div className="min-w-0 max-w-xs">
                      <p className="font-medium text-text-primary truncate">{u.username}</p>
                    </div>
                  </div>
                </td>
                <td className="py-3 px-4">
                  <UserTypeBadge name={u.typeOfUserType} />
                </td>
                <td className="py-3 px-4 font-medium text-text-primary">{u.role}</td>
                <td className="py-3 px-4 text-text-secondary text-xs">{formatDate(u.createdAt)}</td>
                <td className="py-3 px-4">
                  <div className="flex items-center justify-end gap-1">
                    <Link
                      to={`/admin/users/${u.id}`}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                      title="Xem chi tiết"
                    >
                      <Eye size={15} />
                    </Link>
                    <Link
                      to={`/admin/users/${u.id}/edit`}
                      className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors"
                      title="Chỉnh sửa"
                    >
                      <Pencil size={15} />
                    </Link>
                    <button
                      type="button"
                      onClick={() => onDelete?.(u)}
                      className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer"
                      title="Xóa"
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
