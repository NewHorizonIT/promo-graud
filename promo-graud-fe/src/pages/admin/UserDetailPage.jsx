import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, DollarSign, Pencil, Target } from 'lucide-react';
import UserTypeBadge from '../../features/admin/users/components/UserTypeBadge';
import { useUser } from '../../features/admin/users/hooks/useUsers';
import { formatCurrency } from '../../utils/format';

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—';
}

export default function UserDetailPage() {
  const { id } = useParams();
  const { data: user, isLoading } = useUser(id);

  if (isLoading)
    return (
      <div className="space-y-4">
        {[1, 2].map((i) => (
          <div key={i} className="h-32 bg-surface-muted rounded-xl animate-pulse" />
        ))}
      </div>
    );

  if (!user) return <p className="text-sm text-text-muted">Không tìm thấy người dùng.</p>;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <Link
          to="/admin/users"
          className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
        >
          <ChevronLeft size={16} /> Danh sách Người dùng
        </Link>
        <Link
          to={`/admin/users/${id}/edit`}
          className="flex items-center gap-1.5 px-3 py-1.5 border border-border bg-surface hover:bg-surface-muted text-sm font-semibold text-text-primary rounded-xl transition-colors"
        >
          <Pencil size={14} /> Chỉnh sửa
        </Link>
      </div>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-3xl">
              {user.username?.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="text-2xl font-bold text-text-primary">{user.username}</h2>
              <p className="text-sm text-text-muted mt-1">ID: #{user.id}</p>
            </div>
          </div>
          <UserTypeBadge name={user.typeOfUserType} />
        </div>

        <div className="grid grid-cols-2 gap-4 mt-8">
          <div className="bg-primary/5 border border-primary/20 rounded-xl p-5 flex items-center gap-4">
            <div className="p-3 bg-primary/10 text-primary rounded-lg">
              <DollarSign size={24} />
            </div>
            <div>
              <p className="text-sm font-semibold text-text-secondary">Tổng chi tiêu</p>
              <p className="text-2xl font-bold text-primary">
                {user.totalSpending ? formatCurrency(user.totalSpending) : '0 ₫'}
              </p>
            </div>
          </div>

          <div className="bg-surface-muted border border-border rounded-xl p-5 flex items-center gap-4">
            <div className="p-3 bg-white text-text-secondary rounded-lg border border-border">
              <Target size={24} />
            </div>
            <div>
              <p className="text-sm font-semibold text-text-secondary">
                Ngưỡng nâng cấp (Loại {user.typeOfUserType})
              </p>
              <p className="text-xl font-bold text-text-primary">
                {user.typeOfUserThreshold ? formatCurrency(user.typeOfUserThreshold) : '0 ₫'}
              </p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 mt-8 p-6 bg-surface-muted rounded-xl border border-border/50">
          <div>
            <p className="text-xs text-text-muted mb-1">Email</p>
            <p className="text-sm font-medium text-text-primary">{user.email}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-1">Vai trò</p>
            <p className="text-sm font-medium text-text-primary">{user.role}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-1">Ngày tạo</p>
            <p className="text-sm font-medium text-text-primary">{formatDate(user.createdAt)}</p>
          </div>
          <div>
            <p className="text-xs text-text-muted mb-1">Cập nhật lần cuối</p>
            <p className="text-sm font-medium text-text-primary">{formatDate(user.updateAt)}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
