import { useState } from 'react';

const EMPTY = {
  rule_id: '',
  type: 'generic',
  quantity: '',
  limit_client: '',
  expired_at: '',
  distribution_channel: 'email',
};

export default function VoucherForm({ rules = [], initial = EMPTY, onSubmit, loading = false }) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.rule_id) e.rule_id = 'Chọn rule cho voucher';
    if (!form.quantity || Number(form.quantity) <= 0) e.quantity = 'Số lượng phải lớn hơn 0';
    if (!form.expired_at) e.expired_at = 'Chọn ngày hết hạn';
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
      rule_id: Number(form.rule_id),
      type: form.type,
      quantity: Number(form.quantity),
      limit_client: form.limit_client ? Number(form.limit_client) : undefined,
      expired_at: form.expired_at,
      distribution_channel: form.distribution_channel,
    });
  };

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {/* Rule */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Rule <span className="text-error-text">*</span>
        </label>
        <select
          value={form.rule_id}
          onChange={(e) => set('rule_id', e.target.value)}
          className={`${inputClass} ${errors.rule_id ? 'border-error-text' : ''}`}
        >
          <option value="">— Chọn rule —</option>
          {rules.map((r) => (
            <option key={r.id} value={r.id}>
              {r.name}
            </option>
          ))}
        </select>
        {errors.rule_id && <p className="mt-1 text-xs text-error-text">{errors.rule_id}</p>}
      </div>

      <div className="grid grid-cols-2 gap-4">
        {/* Type */}
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Loại Voucher
          </label>
          <select
            value={form.type}
            onChange={(e) => set('type', e.target.value)}
            className={inputClass}
          >
            <option value="generic">Generic — 1 mã nhiều người dùng</option>
            <option value="unique">Unique — N mã riêng từng người</option>
          </select>
        </div>

        {/* Quantity */}
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            {form.type === 'generic' ? 'Số lượt dùng' : 'Số mã cần sinh'}{' '}
            <span className="text-error-text">*</span>
          </label>
          <input
            type="number"
            min={1}
            value={form.quantity}
            onChange={(e) => set('quantity', e.target.value)}
            placeholder="VD: 100"
            className={`${inputClass} ${errors.quantity ? 'border-error-text' : ''}`}
          />
          {errors.quantity && <p className="mt-1 text-xs text-error-text">{errors.quantity}</p>}
        </div>

        {/* Limit per client */}
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Giới hạn / user
          </label>
          <input
            type="number"
            min={1}
            value={form.limit_client}
            onChange={(e) => set('limit_client', e.target.value)}
            placeholder="Không giới hạn"
            className={inputClass}
          />
        </div>

        {/* Expiry */}
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Hết hạn <span className="text-error-text">*</span>
          </label>
          <input
            type="datetime-local"
            value={form.expired_at}
            onChange={(e) => set('expired_at', e.target.value)}
            className={`${inputClass} ${errors.expired_at ? 'border-error-text' : ''}`}
          />
          {errors.expired_at && <p className="mt-1 text-xs text-error-text">{errors.expired_at}</p>}
        </div>
      </div>

      {/* Distribution channel */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Kênh phân phối
        </label>
        <div className="flex gap-3">
          {['email', 'webhook'].map((ch) => (
            <label
              key={ch}
              className={`flex-1 flex items-center gap-3 px-4 py-3 border rounded-xl cursor-pointer transition-all ${
                form.distribution_channel === ch
                  ? 'border-primary bg-primary-light text-primary'
                  : 'border-border bg-surface hover:bg-surface-muted text-text-secondary'
              }`}
            >
              <input
                type="radio"
                name="channel"
                value={ch}
                checked={form.distribution_channel === ch}
                onChange={() => set('distribution_channel', ch)}
                className="hidden"
              />
              <span className="text-sm font-semibold capitalize">{ch}</span>
            </label>
          ))}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 active:scale-[0.98] cursor-pointer"
        >
          {loading ? 'Đang tạo...' : 'Tạo Voucher'}
        </button>
      </div>
    </form>
  );
}
