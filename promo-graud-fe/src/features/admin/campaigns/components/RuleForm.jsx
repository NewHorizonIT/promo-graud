import { useState } from 'react';

const RULE_TYPES = [
  { value: 'PERCENTAGE', label: 'Giảm phần trăm (%)' },
  { value: 'FIXED_AMOUNT', label: 'Giảm trực tiếp (VNĐ)' },
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
  TIERED: '{"tiers":[{"min":0,"discount":5},{"min":500000,"discount":10}]}',
  BUY_X_GET_Y: '{"buyQuantity":2,"getQuantity":1,"productId":5}',
};

/**
 * @param {object} props
 * @param {object}  [props.initial]      - pre-fill values for edit mode
 * @param {Array}   [props.campaigns]    - [{id, name}] list; when provided, shows campaign selector
 * @param {function} props.onSubmit
 * @param {function} props.onCancel
 * @param {boolean} [props.loading]
 */
export default function RuleForm({ initial = EMPTY, campaigns, onSubmit, onCancel, loading = false }) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });

  const set = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const isPayloadType = ['TIERED', 'BUY_X_GET_Y'].includes(form.type_of_rule);
  const isPercentage = form.type_of_rule === 'PERCENTAGE';

  const toDateTime = (val) => (val ? val.slice(0, 16) + ':00' : undefined);

  const handleSubmit = (e) => {
    e.preventDefault();

    const body = {
      name: form.name.trim(),
      typeOfRule: form.type_of_rule,
      typeOfUser: form.type_of_user,
    };

    if (campaigns && form.campaign_id) {
      body.campaignId = Number(form.campaign_id);
    }

    if (isPayloadType) {
      try {
        body.payload = JSON.parse(form.payload);
      } catch {
        alert('Payload JSON không hợp lệ. Vui lòng kiểm tra lại định dạng.');
        return;
      }
    } else {
      body.value = Number(form.value);
      if (isPercentage && form.max_discount_value) {
        body.maxDiscountValue = Number(form.max_discount_value);
      }
      if (form.min_order_value) {
        body.minOrderValue = Number(form.min_order_value);
      }
    }

    if (form.start_time) body.startTime = toDateTime(form.start_time);
    if (form.end_time) body.endTime = toDateTime(form.end_time);

    onSubmit?.(body);
  };

  const inputClass =
    'w-full h-10 px-3 rounded-xl border border-border bg-surface text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all';

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

        {/* Campaign selector — only shown when campaigns list is provided */}
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

        {/* ── PERCENTAGE fields ── */}
        {isPercentage && (
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
                className={inputClass}
                required
              />
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
                className={inputClass}
                required
              />
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

        {/* ── FIXED_AMOUNT fields ── */}
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
                className={inputClass}
                required
              />
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

        {/* ── TIERED / BUY_X_GET_Y: payload only ── */}
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
              className="w-full px-3 py-2 rounded-xl border border-border bg-surface text-xs font-mono text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all resize-none"
              required
            />
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
            onChange={(e) => set('start_time', e.target.value)}
            className={inputClass}
            required
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-text-secondary mb-1">
            Ngày kết thúc *
          </label>
          <input
            type="datetime-local"
            value={form.end_time}
            onChange={(e) => set('end_time', e.target.value)}
            className={inputClass}
            required
          />
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
