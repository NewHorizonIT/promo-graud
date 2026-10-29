import { useState, useEffect } from 'react';
import { useTypeOfUsers } from '../../../user/hooks/useTypeOfUsers';

const EMPTY = { username: '', email: '', password: '', role: 'USER', type_id: '' };

export default function UserForm({ initial = EMPTY, onSubmit, loading = false, isEdit = false }) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});

  const { data: types = [] } = useTypeOfUsers();

  useEffect(() => {
    if (initial && initial !== EMPTY) setForm(initial);
  }, [initial]);

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.username.trim()) e.username = 'Tên đăng nhập không được để trống';
    else if (form.username.length < 3) e.username = 'Tên đăng nhập tối thiểu 3 ký tự';

    if (!form.email.trim()) e.email = 'Email không được để trống';
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Email không đúng định dạng';

    if (!isEdit && !form.password) e.password = 'Mật khẩu không được để trống';
    if (form.password && form.password.length < 6) e.password = 'Mật khẩu phải có ít nhất 6 ký tự';

    if (!form.type_id) e.type_id = 'Vui lòng chọn loại người dùng';
    return e;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    onSubmit?.({
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password, // Ở Page sẽ tự động xóa nếu isEdit và rỗng
      role: form.role,
      typeOfUser: Number(form.type_id),
    });
  };

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';
  const errorClass = 'border-error-text focus:border-error-text focus:ring-error-text/20';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Tên người dùng (Username) <span className="text-error-text">*</span>
          </label>
          <input
            type="text"
            value={form.username}
            onChange={(e) => set('username', e.target.value)}
            placeholder="VD: nguyenvan_a"
            className={`${inputClass} ${errors.username ? errorClass : ''}`}
          />
          {errors.username && <p className="mt-1 text-xs text-error-text">{errors.username}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Email <span className="text-error-text">*</span>
          </label>
          <input
            type="email"
            value={form.email}
            onChange={(e) => set('email', e.target.value)}
            placeholder="VD: email@example.com"
            className={`${inputClass} ${errors.email ? errorClass : ''}`}
          />
          {errors.email && <p className="mt-1 text-xs text-error-text">{errors.email}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Loại người dùng <span className="text-error-text">*</span>
          </label>
          <select
            value={form.type_id}
            onChange={(e) => set('type_id', e.target.value)}
            className={`${inputClass} ${errors.type_id ? errorClass : ''}`}
          >
            <option value="">— Chọn loại người dùng —</option>
            {types.map((t) => (
              <option key={t.id} value={String(t.id)}>
                {t.type}
              </option>
            ))}
          </select>
          {errors.type_id && <p className="mt-1 text-xs text-error-text">{errors.type_id}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Vai trò <span className="text-error-text">*</span>
          </label>
          <select
            value={form.role}
            onChange={(e) => set('role', e.target.value)}
            className={inputClass}
          >
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Mật khẩu {isEdit ? '' : <span className="text-error-text">*</span>}
        </label>
        <input
          type="password"
          value={form.password}
          onChange={(e) => set('password', e.target.value)}
          placeholder={
            isEdit ? 'Để trống nếu không muốn đổi mật khẩu' : 'Nhập mật khẩu (ít nhất 6 ký tự)'
          }
          className={`${inputClass} ${errors.password ? errorClass : ''}`}
        />
        {errors.password && <p className="mt-1 text-xs text-error-text">{errors.password}</p>}
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] cursor-pointer"
        >
          {loading ? 'Đang lưu...' : 'Lưu Người dùng'}
        </button>
      </div>
    </form>
  );
}
