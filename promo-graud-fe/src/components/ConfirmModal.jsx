import { useEffect } from 'react';
import { AlertTriangle, X } from 'lucide-react';

export default function ConfirmModal({
  title = 'Xác nhận',
  message,
  confirmLabel = 'Xóa',
  cancelLabel = 'Hủy',
  onConfirm,
  onClose,
  loading = false,
}) {
  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === 'Escape' && !loading) onClose?.();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [onClose, loading]);

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
        className="w-full max-w-sm bg-surface border border-border rounded-2xl p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="flex items-center gap-3">
            <div className="flex-shrink-0 w-10 h-10 rounded-full bg-error-bg flex items-center justify-center">
              <AlertTriangle size={20} className="text-error-text" />
            </div>
            <h2 className="text-base font-bold text-text-primary">{title}</h2>
          </div>
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

        {message && <p className="text-sm text-text-secondary mb-6 pl-[52px]">{message}</p>}

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-5 py-2.5 border border-border bg-surface hover:bg-surface-muted text-text-secondary rounded-xl font-semibold text-sm transition-all disabled:opacity-60 cursor-pointer"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className="px-5 py-2.5 bg-error-text hover:bg-red-700 text-white rounded-xl font-semibold text-sm transition-all disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] cursor-pointer"
          >
            {loading ? 'Đang xử lý...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
