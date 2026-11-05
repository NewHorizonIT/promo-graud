import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useCreateUser, useUpdateUser, useUser } from '../../features/admin/users/hooks/useUsers';
import UserForm from '../../features/admin/users/components/UserForm';
import Toast from '../../components/Toast';

export default function UserFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [initial, setInitial] = useState(null);
  const [toast, setToast] = useState(null);
  const initializedIdRef = useRef(null);

  const { data: user, isLoading: fetchLoading } = useUser(id);
  const createMutation = useCreateUser();
  const updateMutation = useUpdateUser();

  useEffect(() => {
    setToast(null);
  }, [id]);

  useEffect(() => {
    if (!user || initializedIdRef.current === id) return;
    initializedIdRef.current = id;
    setInitial({
      username: user.username ?? '',
      email: user.email ?? '',
      password: '',
      role: user.role ?? 'USER',
      type_id: String(user.typeOfUserId ?? ''),
    });
  }, [user, id]);

  const loading = createMutation.isPending || updateMutation.isPending;

  const handleSubmit = (body) => {
    setToast(null);
    if (isEdit) {
      if (!body.password) delete body.password;
      updateMutation.mutate(
        { id, body },
        {
          onSuccess: () => {
            setToast({ type: 'success', message: 'Cập nhật thành công!' });
            setTimeout(() => navigate(`/admin/users/${id}`), 1000);
          },
          onError: (err) =>
            setToast({ type: 'error', message: err?.response?.data?.message ?? 'Lỗi cập nhật' }),
        },
      );
    } else {
      createMutation.mutate(body, {
        onSuccess: () => {
          setToast({ type: 'success', message: 'Tạo người dùng thành công!' });
          setTimeout(() => navigate('/admin/users'), 1000);
        },
        onError: (err) =>
          setToast({ type: 'error', message: err?.response?.data?.message ?? 'Lỗi tạo mới' }),
      });
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      <Link
        to={isEdit ? `/admin/users/${id}` : '/admin/users'}
        className="inline-flex items-center gap-1.5 text-sm text-text-secondary hover:text-text-primary transition-colors"
      >
        <ChevronLeft size={16} /> {isEdit ? 'Quay lại chi tiết' : 'Quay lại danh sách'}
      </Link>

      <div className="bg-surface border border-border rounded-2xl p-6 shadow-level-1 relative">
        <h2 className="text-xl font-bold text-text-primary mb-6">
          {isEdit ? 'Chỉnh sửa Người dùng' : 'Tạo Người dùng mới'}
        </h2>
        {isEdit && fetchLoading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-11 bg-surface-muted rounded-xl animate-pulse" />
            ))}
          </div>
        ) : (
          <UserForm
            initial={initial ?? undefined}
            onSubmit={handleSubmit}
            loading={loading}
            isEdit={isEdit}
          />
        )}
      </div>
      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}
