import { useEffect, useRef, useState } from 'react';
import { X } from 'lucide-react';

export default function UserTypeModal({
  initial = null,
  onSubmit,
  onClose,
  loading = false,
  error = '',
}) {
  const isEdit = Boolean(initial);
  const [form, setForm] = useState({
    type: initial?.type ?? 'NORMAL',
    threshold: initial?.threshold ?? 0,
  });
  const inputRef = useRef(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === 'Escape' && !loading) onClose?.();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [onClose, loading]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit?.({ type: form.type, threshold: Number(form.threshold) });
  };

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !loading) onClose?.();
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        className="w-full max-w-md bg-surface border border-border rounded-2xl p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4 mb-5">
          <h2 className="text-xl font-bold text-text-primary">
            {isEdit ? 'Chỉnh sửa Loại Người dùng' : 'Tạo Loại Người dùng'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer disabled:opacity-60"
          >
            <X size={18} />
          </button>
        </div>

        {error && (
          <div className="mb-5 px-4 py-3 rounded-xl bg-error-bg border border-error-text/20 text-sm text-error-text">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              Tên loại <span className="text-error-text">*</span>
            </label>
            <select
              ref={inputRef}
              value={form.type}
              onChange={(e) => setForm((prev) => ({ ...prev, type: e.target.value }))}
              className={inputClass}
            >
              <option value="NORMAL">NORMAL</option>
              <option value="BRONZE">BRONZE</option>
              <option value="SILVER">SILVER</option>
              <option value="GOLD">GOLD</option>
              <option value="ALL">ALL</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              Ngưỡng nâng cấp (Threshold) <span className="text-error-text">*</span>
            </label>
            <input
              type="number"
              min="0"
              step="0.01"
              required
              value={form.threshold}
              onChange={(e) => setForm((prev) => ({ ...prev, threshold: e.target.value }))}
              className={inputClass}
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-5 py-2.5 border border-border bg-surface hover:bg-surface-muted text-text-secondary rounded-xl font-semibold text-sm transition-all disabled:opacity-60 cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 cursor-pointer"
            >
              {loading ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
