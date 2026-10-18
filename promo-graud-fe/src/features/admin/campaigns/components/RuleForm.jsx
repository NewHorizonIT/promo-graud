import { useState } from 'react';

const RULE_TYPES = [
  { value: 'giam_truc_tiep', label: 'Giảm trực tiếp (VNĐ)' },
  { value: 'giam_phan_tram', label: 'Giảm phần trăm (%)' },
  { value: 'mua_x_tang_y', label: 'Mua X tặng Y' },
  { value: 'tiered', label: 'Tiered Discount' },
];

const USER_TYPES = [
  { value: 'all', label: 'Tất cả' },
  { value: 'binh_thuong', label: 'Bình thường' },
  { value: 'dong', label: 'Đồng' },
  { value: 'bac', label: 'Bạc' },
  { value: 'vang', label: 'Vàng' },
];

const EMPTY = {
  name: '',
  type_of_rule: 'giam_phan_tram',
  value: '',
  max_discount_value: '',
  min_order_value: '',
  type_of_user: 'all',
  start_time: '',
  end_time: '',
  payload: '',
};

export default function RuleForm({ initial = EMPTY, onSubmit, onCancel, loading = false }) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });

  const set = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = (e) => {
    e.preventDefault();
    const body = {
      name: form.name.trim(),
      type_of_rule: form.type_of_rule,
      value: Number(form.value),
      type_of_user: form.type_of_user,
    };
    if (form.max_discount_value) body.max_discount_value = Number(form.max_discount_value);
    if (form.min_order_value) body.min_order_value = Number(form.min_order_value);
    if (form.start_time) body.start_time = form.start_time;
    if (form.end_time) body.end_time = form.end_time;
    if (form.payload) {
      try {
        body.payload = JSON.parse(form.payload);
      } catch {
        /* invalid json — skip */
      }
    }
    onSubmit?.(body);
  };

  const inputClass =
    'w-full h-10 px-3 rounded-xl border border-border bg-surface text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all';

  const needsPayload = ['tiered', 'mua_x_tang_y'].includes(form.type_of_rule);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-3">
        {/* Name */}
        <div className="col-span-2">
          <label className="block text-xs font-medium text-text-secondary mb-1">Tên rule *</label>
          <input
            type="text"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
            placeholder="VD: Giảm 10% cho đơn từ 500k"
            className={inputClass}
            required
          />
        </div>

        {/* Type */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">Loại rule *</label>
          <select
            value={form.type_of_rule}
            onChange={(e) => set('type_of_rule', e.target.value)}
            className={inputClass}
          >
            {RULE_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </div>

        {/* Value */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">Giá trị *</label>
          <input
            type="number"
            min={0}
            value={form.value}
            onChange={(e) => set('value', e.target.value)}
            placeholder={form.type_of_rule === 'giam_phan_tram' ? 'VD: 10 (10%)' : 'VD: 50000'}
            className={inputClass}
            required
          />
        </div>

        {/* Max discount */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Giảm tối đa (VNĐ)
          </label>
          <input
            type="number"
            min={0}
            value={form.max_discount_value}
            onChange={(e) => set('max_discount_value', e.target.value)}
            placeholder="Không giới hạn"
            className={inputClass}
          />
        </div>

        {/* Min order */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Đơn tối thiểu (VNĐ)
          </label>
          <input
            type="number"
            min={0}
            value={form.min_order_value}
            onChange={(e) => set('min_order_value', e.target.value)}
            placeholder="Không yêu cầu"
            className={inputClass}
          />
        </div>

        {/* User type */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Loại khách hàng
          </label>
          <select
            value={form.type_of_user}
            onChange={(e) => set('type_of_user', e.target.value)}
            className={inputClass}
          >
            {USER_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </div>

        {/* Dates */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">Ngày bắt đầu</label>
          <input
            type="datetime-local"
            value={form.start_time}
            onChange={(e) => set('start_time', e.target.value)}
            className={inputClass}
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Ngày kết thúc
          </label>
          <input
            type="datetime-local"
            value={form.end_time}
            onChange={(e) => set('end_time', e.target.value)}
            className={inputClass}
          />
        </div>

        {/* Payload — only for tiered / mua_x_tang_y */}
        {needsPayload && (
          <div className="col-span-2">
            <label className="block text-xs font-medium text-text-secondary mb-1">
              Payload (JSON) — bắt buộc với loại <em>{form.type_of_rule}</em>
            </label>
            <textarea
              rows={3}
              value={form.payload}
              onChange={(e) => set('payload', e.target.value)}
              placeholder='{"tiers": [{"min": 0, "discount": 5}, {"min": 500000, "discount": 10}]}'
              className="w-full px-3 py-2 rounded-xl border border-border bg-surface text-xs font-mono text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all resize-none"
            />
          </div>
        )}
      </div>

      <div className="flex justify-end gap-2 pt-1">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 border border-border bg-surface-muted hover:bg-border text-sm font-semibold text-text-primary rounded-xl transition-colors cursor-pointer"
        >
          Hủy
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow disabled:opacity-60 cursor-pointer"
        >
          {loading ? 'Đang lưu...' : 'Lưu Rule'}
        </button>
      </div>
    </form>
  );
}
