import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronLeft, Plus } from 'lucide-react';
import UserTypeTable from '../../features/admin/users/components/UserTypeTable';
import UserTypeModal from '../../features/admin/users/components/UserTypeModal';
import {
  useCreateTypeUser,
  useTypeUser,
  useUpdateTypeUser,
} from '../../features/admin/users/hooks/useTypeOfUser';
import Toast from '../../components/Toast';

export default function UserTypePage() {
  const { data: types = [], isLoading } = useTypeUser();
  const createMutation = useCreateTypeUser();
  const updateMutation = useUpdateTypeUser();

  const [modal, setModal] = useState({ open: false, item: null });
  const [error, setError] = useState('');
  const [toast, setToast] = useState(null);

  const saving = createMutation.isPending || updateMutation.isPending;

  const openCreate = () => {
    setError('');
    setModal({ open: true, item: null });
  };
  const openEdit = (item) => {
    setError('');
    setModal({ open: true, item });
  };
  const closeModal = () => setModal({ open: false, item: null });

  const handleSubmit = (body) => {
    setError('');
    const options = {
      onSuccess: () => {
        closeModal();
        setToast({ type: 'success', message: 'Lưu loại người dùng thành công!' }); // Bật Toast xanh
      },
      onError: (err) => {
        setError(err?.response?.data?.message ?? 'Có lỗi xảy ra.');
        setToast({ type: 'error', message: 'Lưu thất bại!' }); // Bật Toast đỏ
      },
    };
    if (modal.item) updateMutation.mutate({ id: modal.item.id, body }, options);
    else createMutation.mutate(body, options);
  };

  return (
    <div className="space-y-5">
      <Link
        to="/admin/users"
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> Danh sách Người dùng
      </Link>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-sm text-text-muted">{types.length} loại người dùng</p>
        <button
          onClick={openCreate}
          className="flex items-center gap-2 px-4 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow cursor-pointer"
        >
          <Plus size={16} /> Tạo Loại
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-14 bg-surface-muted rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <UserTypeTable types={types} onEdit={openEdit} />
      )}

      {modal.open && (
        <UserTypeModal
          key={modal.item?.id ?? 'new'}
          initial={modal.item}
          onSubmit={handleSubmit}
          onClose={closeModal}
          loading={saving}
          error={error}
        />
      )}

      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}
