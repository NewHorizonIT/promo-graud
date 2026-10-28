import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Tags, Search, Filter, X } from 'lucide-react';
import { useDeleteUser, useUsers } from '../../features/admin/users/hooks/useUsers';
import UserTable from '../../features/admin/users/components/UserTable';
import Toast from '../../components/Toast';
import { useTypeUser } from '../../features/admin/users/hooks/useTypeOfUser';

const PAGE_SIZE = 10;

export default function UserPage() {
  const [toast, setToast] = useState(null);

  const [formValues, setFormValues] = useState({ name: '', typeId: '' });
  const [queryParams, setQueryParams] = useState({ page: 1, name: '', typeId: '' });

  const params = {
    page: queryParams.page,
    size: PAGE_SIZE,
    ...(queryParams.name && { name: queryParams.name }),
    ...(queryParams.typeId && { userTypeId: queryParams.typeId }),
  };

  const { data, isLoading } = useUsers(params);
  const { data: types = [] } = useTypeUser();
  const deleteMutation = useDeleteUser();

  const users = data?.data ?? [];
  const total = data?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? 1;

  const handleDelete = (user) => {
    if (!confirm(`Xóa người dùng "${user.username}"?`)) return;
    deleteMutation.mutate(user.id, {
      onSuccess: () => setToast({ type: 'success', message: 'Xóa người dùng thành công!' }),
      onError: () => setToast({ type: 'error', message: 'Có lỗi xảy ra khi xóa.' }),
    });
  };

  const handleInputChange = (e) =>
    setFormValues((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleApplyFilters = (e) => {
    e.preventDefault();
    setQueryParams({ ...formValues, page: 1 });
  };

  const handleClearFilters = () => {
    setFormValues({ name: '', typeId: '' });
    setQueryParams({ name: '', typeId: '', page: 1 });
  };

  const inputClass =
    'w-full h-10 px-3 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all';

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{total} người dùng</p>
        <div className="flex items-center gap-2">
          <Link
            to="/admin/user-types"
            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-surface-muted text-text-secondary rounded-xl text-sm font-semibold transition-all"
          >
            <Tags size={16} /> Loại người dùng
          </Link>
          <Link
            to="/admin/users/new"
            className="flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow"
          >
            <Plus size={16} /> Tạo Người dùng
          </Link>
        </div>
      </div>

      <form
        onSubmit={handleApplyFilters}
        className="bg-surface p-4 rounded-xl border border-border flex flex-wrap gap-4 items-end shadow-sm"
      >
        <div className="flex-1 min-w-[200px]">
          <label className="block text-xs font-medium text-text-secondary mb-1.5">
            Tên người dùng
          </label>
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted"
              size={15}
            />
            <input
              type="text"
              name="name"
              value={formValues.name}
              onChange={handleInputChange}
              placeholder="Tìm kiếm..."
              className={`${inputClass} pl-9`}
            />
          </div>
        </div>
        <div className="w-full sm:w-48">
          <label className="block text-xs font-medium text-text-secondary mb-1.5">
            Loại người dùng
          </label>
          <select
            name="typeId"
            value={formValues.typeId}
            onChange={handleInputChange}
            className={inputClass}
          >
            <option value="">Tất cả</option>
            {types.map((t) => (
              <option key={t.id} value={String(t.id)}>
                {t.type}
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto mt-2 sm:mt-0">
          <button
            type="submit"
            className="flex items-center gap-2 px-4 h-10 bg-surface-muted border border-border hover:bg-border text-text-primary rounded-xl text-sm font-semibold transition-colors cursor-pointer"
          >
            <Filter size={15} /> Lọc
          </button>
          {(queryParams.name || queryParams.typeId) && (
            <button
              type="button"
              onClick={handleClearFilters}
              className="flex items-center justify-center w-10 h-10 text-error-text bg-error-bg hover:bg-error-text hover:text-white rounded-xl transition-colors cursor-pointer"
            >
              <X size={16} />
            </button>
          )}
        </div>
      </form>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <UserTable users={users} onDelete={handleDelete} />
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-2">
          {[...Array(totalPages)].map((_, i) => {
            const pg = i + 1;
            return (
              <button
                key={pg}
                type="button"
                onClick={() => setQueryParams((prev) => ({ ...prev, page: pg }))}
                className={`min-w-9 h-9 px-3 rounded-xl text-sm font-semibold transition-all ${pg === queryParams.page ? 'bg-primary text-white shadow-glow' : 'border border-border bg-surface text-text-secondary hover:bg-surface-muted'}`}
              >
                {pg}
              </button>
            );
          })}
        </div>
      )}

      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}
