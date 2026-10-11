import { useEffect } from "react";
import { CheckCircle2, AlertCircle, Info, X } from "lucide-react";

export default function Toast({ toast, onClose }) {
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => {
      onClose();
    }, toast.duration || 3000);
    return () => clearTimeout(timer);
  }, [toast, onClose]);

  if (!toast) return null;

  const isSuccess = toast.type === "success";
  const isError = toast.type === "error";
  const isWarning = toast.type === "warning";

  const bgBorderClass = isSuccess
    ? "bg-white border-green-500/30 text-green-700"
    : isError
    ? "bg-white border-red-500/30 text-red-600"
    : isWarning
    ? "bg-white border-amber-500/30 text-amber-700"
    : "bg-white border-border text-text-primary";

  const IconComponent = isSuccess
    ? CheckCircle2
    : isError
    ? AlertCircle
    : isWarning
    ? AlertCircle
    : Info;

  const iconColor = isSuccess
    ? "text-emerald-500"
    : isError
    ? "text-red-500"
    : isWarning
    ? "text-amber-500"
    : "text-primary";

  return (
    <div className="fixed top-20 right-5 z-50 animate-in fade-in slide-in-from-top-5 duration-300">
      <div
        className={`flex items-center gap-3 px-4 py-3 rounded-2xl border shadow-level-2 max-w-sm ${bgBorderClass}`}
      >
        <IconComponent size={20} className={`${iconColor} shrink-0`} />
        <div className="text-sm font-medium pr-2 text-text-primary">
          {toast.message}
        </div>
        <button
          type="button"
          onClick={onClose}
          className="text-text-muted hover:text-text-primary p-0.5 rounded-lg transition-colors ml-auto cursor-pointer"
        >
          <X size={16} />
        </button>
      </div>
    </div>
  );
}
