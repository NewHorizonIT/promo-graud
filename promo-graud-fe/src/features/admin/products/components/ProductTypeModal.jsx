import { useEffect, useRef, useState } from 'react';
import { X } from 'lucide-react';

// Render có điều kiện ở page (và đặt key) nên state luôn khởi tạo lại mỗi lần mở
export default function ProductTypeModal({
  initial = null, // null = tạo mới, object { id, type } = chỉnh sửa
  onSubmit,
  onClose,
  loading = false,
  error = '',
}) {
  const isEdit = Boolean(initial);
  const [type, setType] = useState(initial?.type ?? '');
  const [fieldError, setFieldError] = useState('');
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

  const validate = () => {
    const name = type.trim();
    if (!name) return 'Tên loại không được để trống';
    if (name.length > 100) return 'Tên loại không quá 100 ký tự';
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const err = validate();
    if (err) {
      setFieldError(err);
      return;
    }
    onSubmit?.({ type: type.trim() });
  };

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';

  const errorClass = 'border-error-text focus:border-error-text focus:ring-error-text/20';

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
        aria-labelledby="product-type-modal-title"
        className="w-full max-w-md bg-surface border border-border rounded-2xl p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4 mb-5">
          <h2 id="product-type-modal-title" className="text-xl font-bold text-text-primary">
            {isEdit ? 'Chỉnh sửa Loại sản phẩm' : 'Tạo Loại sản phẩm'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            aria-label="Đóng"
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
            <label
              htmlFor="product-type-name"
              className="block text-sm font-medium text-text-secondary mb-1.5"
            >
              Tên loại <span className="text-error-text">*</span>
            </label>
            <input
              id="product-type-name"
              ref={inputRef}
              type="text"
              value={type}
              onChange={(e) => {
                setType(e.target.value);
                if (fieldError) setFieldError('');
              }}
              placeholder="VD: Điện thoại"
              className={`${inputClass} ${fieldError ? errorClass : ''}`}
            />
            {fieldError && <p className="mt-1 text-xs text-error-text">{fieldError}</p>}
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
              className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] cursor-pointer"
            >
              {loading ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
