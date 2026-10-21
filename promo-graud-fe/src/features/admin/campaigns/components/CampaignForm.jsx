import { useState, useEffect } from 'react';

const EMPTY = { name: '', start_time: '', end_time: '', promotion_budget: '' };

function formatBudget(raw) {
  if (!raw) return '';
  const n = Number(raw);
  if (isNaN(n)) return raw;
  return n.toLocaleString('en-US');
}

export default function CampaignForm({ initial = EMPTY, onSubmit, loading = false }) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});
  const [budgetFocused, setBudgetFocused] = useState(false);

  // Sync khi initial thay đổi (edit mode load async)
  useEffect(() => {
    if (initial && initial !== EMPTY) setForm(initial);
  }, [initial]);

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Tên campaign không được để trống';
    else if (form.name.trim().length < 5) e.name = 'Tên campaign phải ít nhất 5 ký tự';
    else if (form.name.trim().length > 100) e.name = 'Tên campaign không quá 100 ký tự';
    if (!form.start_time) e.start_time = 'Chọn ngày bắt đầu';
    if (!form.end_time) e.end_time = 'Chọn ngày kết thúc';
    if (form.start_time && form.end_time && form.end_time <= form.start_time)
      e.end_time = 'Ngày kết thúc phải sau ngày bắt đầu';
    if (!form.promotion_budget || Number(form.promotion_budget) <= 0)
      e.promotion_budget = 'Ngân sách phải lớn hơn 0';
    return e;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    const toBeDate = (val) => val.slice(0, 16).replace('T', ' ') + ':00';
    onSubmit?.({
      name: form.name.trim(),
      startTime: toBeDate(form.start_time),
      endTime: toBeDate(form.end_time),
      promotionBudget: Number(form.promotion_budget),
    });
  };

  const handleBudgetChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '');
    set('promotion_budget', raw);
  };

  const budgetDisplayValue = budgetFocused
    ? form.promotion_budget || ''
    : formatBudget(form.promotion_budget);

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';

  const errorClass = 'border-error-text focus:border-error-text focus:ring-error-text/20';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {/* Name */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Tên Campaign <span className="text-error-text">*</span>
        </label>
        <input
          type="text"
          value={form.name}
          onChange={(e) => set('name', e.target.value)}
          placeholder="VD: Flash Sale Tháng 10"
          className={`${inputClass} ${errors.name ? errorClass : ''}`}
        />
        {errors.name && <p className="mt-1 text-xs text-error-text">{errors.name}</p>}
      </div>

      {/* Date range */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Ngày bắt đầu <span className="text-error-text">*</span>
          </label>
          <input
            type="datetime-local"
            value={form.start_time}
            onChange={(e) => set('start_time', e.target.value)}
            className={`${inputClass} ${errors.start_time ? errorClass : ''}`}
          />
          {errors.start_time && <p className="mt-1 text-xs text-error-text">{errors.start_time}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Ngày kết thúc <span className="text-error-text">*</span>
          </label>
          <input
            type="datetime-local"
            value={form.end_time}
            onChange={(e) => set('end_time', e.target.value)}
            className={`${inputClass} ${errors.end_time ? errorClass : ''}`}
          />
          {errors.end_time && <p className="mt-1 text-xs text-error-text">{errors.end_time}</p>}
        </div>
      </div>

      {/* Budget */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Ngân sách <span className="text-error-text">*</span>
        </label>
        <div className="relative">
          <input
            type="text"
            inputMode="numeric"
            value={budgetDisplayValue}
            onChange={handleBudgetChange}
            onFocus={() => setBudgetFocused(true)}
            onBlur={() => setBudgetFocused(false)}
            placeholder="VD: 50000000"
            className={`${inputClass} pr-14 ${errors.promotion_budget ? errorClass : ''}`}
          />
          <span className="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-semibold text-text-muted pointer-events-none">
            VNĐ
          </span>
        </div>
        {errors.promotion_budget && (
          <p className="mt-1 text-xs text-error-text">{errors.promotion_budget}</p>
        )}
      </div>

      {/* Submit */}
      <div className="flex justify-end gap-3 pt-2">
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] cursor-pointer"
        >
          {loading ? 'Đang lưu...' : 'Lưu Campaign'}
        </button>
      </div>
    </form>
  );
}
