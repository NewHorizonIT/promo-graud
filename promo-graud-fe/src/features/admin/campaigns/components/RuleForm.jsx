import { useState } from 'react';

const RULE_TYPES = [
  { value: 'PERCENTAGE', label: 'Giảm phần trăm (%)' },
  { value: 'FIXED_AMOUNT', label: 'Giảm trực tiếp (VNĐ)' },
  { value: 'FLASH_SALE', label: 'Flash Sale (%)' },
  { value: 'BUY_X_GET_Y', label: 'Mua X tặng Y' },
  { value: 'TIERED', label: 'Tiered Discount' },
];

const USER_TYPES = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'NORMAL', label: 'Bình thường' },
  { value: 'BRONZE', label: 'Đồng' },
  { value: 'SILVER', label: 'Bạc' },
  { value: 'GOLD', label: 'Vàng' },
];

const EMPTY = {
  name: '',
  campaign_id: '',
  type_of_rule: 'PERCENTAGE',
  value: '',
  max_discount_value: '',
  min_order_value: '',
  type_of_user: 'ALL',
  start_time: '',
  end_time: '',
  payload: '',
};

const PAYLOAD_PLACEHOLDER = {
  TIERED:
    '{"tiers":[{"min_order_value":200000,"discount_value":20000},{"min_order_value":500000,"discount_value":70000}]}',
  BUY_X_GET_Y:
    '{"x_product":{"product_id":1,"product_quantity":2},"y_product":{"product_id":5,"product_quantity":1}}',
};

// useEffect removed — parent uses key={rule.id} to remount on edit target change
export default function RuleForm({
  initial = EMPTY,
  campaigns,
  onSubmit,
  onCancel,
  loading = false,
  isEdit = false,
}) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const isPayloadType = ['TIERED', 'BUY_X_GET_Y'].includes(form.type_of_rule);
  const isPercentageLike = ['PERCENTAGE', 'FLASH_SALE'].includes(form.type_of_rule);
  const nowStr = new Date().toISOString().slice(0, 16);

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Tên rule không được để trống';
    if (!form.start_time) {
      e.start_time = 'Chọn ngày bắt đầu';
    } else if (!isEdit && form.start_time < nowStr) {
      // chỉ check trong create mode — edit không cần vì rule có thể đang chạy
      e.start_time = 'Ngày bắt đầu phải ở hiện tại hoặc tương lai';
    }
    if (!form.end_time) e.end_time = 'Chọn ngày kết thúc';
    if (form.start_time && form.end_time && form.end_time <= form.start_time)
      e.end_time = 'Ngày kết thúc phải sau ngày bắt đầu';
    if (!isPayloadType) {
      if (!form.value || Number(form.value) <= 0) e.value = 'Giá trị phải lớn hơn 0';
      if (isPercentageLike) {
        if (Number(form.value) > 100) e.value = 'Phần trăm không được vượt quá 100';
        if (!form.max_discount_value || Number(form.max_discount_value) <= 0)
          e.max_discount_value = 'Giảm tối đa bắt buộc phải nhập';
      }
    }
    return e;
  };

  const toDateTime = (val) => (val ? val.slice(0, 16) + ':00' : undefined);

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }

    const body = {
      name: form.name.trim(),
      typeOfRule: form.type_of_rule,
      typeOfUser: form.type_of_user,
    };

    if (campaigns && form.campaign_id) body.campaignId = Number(form.campaign_id);

    if (isPayloadType) {
      try {
        body.payload = JSON.parse(form.payload);
      } catch {
        setErrors((prev) => ({ ...prev, payload: 'JSON không hợp lệ. Kiểm tra lại định dạng.' }));
        return;
      }
    } else {
      body.value = Number(form.value);
      if (isPercentageLike && form.max_discount_value)
        body.maxDiscountValue = Number(form.max_discount_value);
      if (form.min_order_value) body.minOrderValue = Number(form.min_order_value);
    }

    if (form.start_time) body.startTime = toDateTime(form.start_time);
    if (form.end_time) body.endTime = toDateTime(form.end_time);

    onSubmit?.(body);
  };

  const inputClass =
    'w-full h-10 px-3 rounded-xl border border-border bg-surface text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all';
  const errBorder = 'border-error-text focus:border-error-text focus:ring-error-text/20';

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
            className={`${inputClass} ${errors.name ? errBorder : ''}`}
          />
          {errors.name && <p className="mt-1 text-xs text-error-text">{errors.name}</p>}
        </div>

        {/* Campaign selector */}
        {campaigns && (
          <div className="col-span-2">
            <label className="block text-xs font-medium text-text-secondary mb-1">
              Campaign *
            </label>
            <select
              value={form.campaign_id}
              onChange={(e) => set('campaign_id', e.target.value)}
              className={inputClass}
              required
            >
              <option value="">-- Chọn campaign --</option>
              {campaigns.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Rule type + User type */}
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

        {/* PERCENTAGE / FLASH_SALE fields */}
        {isPercentageLike && (
          <>
            <div>
              <label className="block text-xs font-medium text-text-secondary mb-1">
                Giảm (%) *
              </label>
              <input
                type="number"
                min={1}
                max={100}
                value={form.value}
                onChange={(e) => set('value', e.target.value)}
                placeholder="VD: 10"
                className={`${inputClass} ${errors.value ? errBorder : ''}`}
              />
              {errors.value && <p className="mt-1 text-xs text-error-text">{errors.value}</p>}
            </div>
            <div>
              <label className="block text-xs font-medium text-text-secondary mb-1">
                Giảm tối đa (VNĐ) *
              </label>
              <input
                type="number"
                min={1}
                value={form.max_discount_value}
                onChange={(e) => set('max_discount_value', e.target.value)}
                placeholder="VD: 50000"
                className={`${inputClass} ${errors.max_discount_value ? errBorder : ''}`}
              />
              {errors.max_discount_value && (
                <p className="mt-1 text-xs text-error-text">{errors.max_discount_value}</p>
              )}
            </div>
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
          </>
        )}

        {/* FIXED_AMOUNT fields */}
        {form.type_of_rule === 'FIXED_AMOUNT' && (
          <>
            <div>
              <label className="block text-xs font-medium text-text-secondary mb-1">
                Số tiền giảm (VNĐ) *
              </label>
              <input
                type="number"
                min={1}
                value={form.value}
                onChange={(e) => set('value', e.target.value)}
                placeholder="VD: 50000"
                className={`${inputClass} ${errors.value ? errBorder : ''}`}
              />
              {errors.value && <p className="mt-1 text-xs text-error-text">{errors.value}</p>}
            </div>
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
          </>
        )}

        {/* TIERED / BUY_X_GET_Y payload */}
        {isPayloadType && (
          <div className="col-span-2">
            <label className="block text-xs font-medium text-text-secondary mb-1">
              Payload (JSON) *
            </label>
            <textarea
              rows={4}
              value={form.payload}
              onChange={(e) => set('payload', e.target.value)}
              placeholder={PAYLOAD_PLACEHOLDER[form.type_of_rule]}
              className={`w-full px-3 py-2 rounded-xl border bg-surface text-xs font-mono text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all resize-none ${errors.payload ? 'border-error-text' : 'border-border'}`}
            />
            {errors.payload && <p className="mt-1 text-xs text-error-text">{errors.payload}</p>}
          </div>
        )}

        {/* Dates */}
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Ngày bắt đầu *
          </label>
          <input
            type="datetime-local"
            value={form.start_time}
            min={isEdit ? undefined : nowStr}
            onChange={(e) => set('start_time', e.target.value)}
            className={`${inputClass} ${errors.start_time ? errBorder : ''}`}
          />
          {errors.start_time && (
            <p className="mt-1 text-xs text-error-text">{errors.start_time}</p>
          )}
        </div>
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Ngày kết thúc *
          </label>
          <input
            type="datetime-local"
            value={form.end_time}
            min={form.start_time || nowStr}
            onChange={(e) => set('end_time', e.target.value)}
            className={`${inputClass} ${errors.end_time ? errBorder : ''}`}
          />
          {errors.end_time && <p className="mt-1 text-xs text-error-text">{errors.end_time}</p>}
        </div>
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
