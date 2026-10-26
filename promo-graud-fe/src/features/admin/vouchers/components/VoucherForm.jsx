import { useState } from 'react';

const USER_TYPES = [
  { value: 'ALL', label: 'Tất cả user' },
  { value: 'NORMAL', label: 'Bình thường' },
  { value: 'BRONZE', label: 'Đồng' },
  { value: 'SILVER', label: 'Bạc' },
  { value: 'GOLD', label: 'Vàng' },
];

const EMPTY = {
  rule_id: '',
  type: 'GENERIC',
  quantity: '',
  limit_client: '',
  expired_at: '',
  distribution_channel: 'EMAIL',
  prefix: '',
  // targeting — chỉ dùng cho UNIQUE
  target_mode: 'none',        // 'none' | 'user_type' | 'user_ids'
  target_user_type: 'ALL',
  target_user_ids: '',        // chuỗi IDs cách nhau bởi dấu phẩy
};

export default function VoucherForm({ rules = [], initial = EMPTY, onSubmit, loading = false }) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const isUnique = form.type === 'UNIQUE';
  const needsQuantity =
    !isUnique || form.target_mode === 'none';

  const parsedUserIds = form.target_user_ids
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
    .map(Number)
    .filter((n) => n > 0);

  const validate = () => {
    const e = {};
    if (!form.rule_id) e.rule_id = 'Chọn rule cho voucher';
    if (needsQuantity && (!form.quantity || Number(form.quantity) <= 0)) {
      e.quantity = 'Số lượng phải lớn hơn 0';
    }
    if (!form.expired_at) e.expired_at = 'Chọn ngày hết hạn';
    if (isUnique && form.target_mode === 'user_ids' && parsedUserIds.length === 0) {
      e.target_user_ids = 'Nhập ít nhất 1 user ID hợp lệ';
    }
    return e;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }

    const body = {
      ruleId: Number(form.rule_id),
      type: form.type,
      prefix: form.prefix ?? '',
      limitClient: form.limit_client ? Number(form.limit_client) : 1,
      expiredAt: form.expired_at,
      distributionChannel: form.distribution_channel,
    };

    if (needsQuantity) {
      body.quantity = Number(form.quantity);
    }

    if (isUnique) {
      if (form.target_mode === 'user_type') {
        body.targetUserType = form.target_user_type;
      } else if (form.target_mode === 'user_ids') {
        body.targetUserIds = parsedUserIds;
        body.quantity = parsedUserIds.length;
      }
    }

    onSubmit?.(body);
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
              {r.campaignName ? `[${r.campaignName}] ${r.name}` : r.name}
            </option>
          ))}
        </select>
        {errors.rule_id && <p className="mt-1 text-xs text-error-text">{errors.rule_id}</p>}
      </div>

      {/* Prefix */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Prefix mã voucher
          <span className="font-normal text-text-muted ml-1">— tuỳ chọn, VD: SUMMER2026</span>
        </label>
        <input
          type="text"
          value={form.prefix}
          onChange={(e) => set('prefix', e.target.value)}
          placeholder="Để trống → mã ngẫu nhiên 8 ký tự"
          className={inputClass}
          maxLength={20}
        />
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
            <option value="GENERIC">Generic — 1 mã nhiều người dùng</option>
            <option value="UNIQUE">Unique — N mã riêng từng người</option>
          </select>
        </div>

        {/* Quantity — ẩn khi UNIQUE + có targeting tự động */}
        {needsQuantity ? (
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              {isUnique ? 'Số mã cần sinh' : 'Số lượt dùng'}{' '}
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
        ) : (
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              Số mã cần sinh
            </label>
            <div className={`${inputClass} flex items-center text-text-muted italic`}>
              {form.target_mode === 'user_ids'
                ? `${parsedUserIds.length} mã (theo danh sách)`
                : 'Tự động theo số user'}
            </div>
          </div>
        )}

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
          {errors.expired_at && (
            <p className="mt-1 text-xs text-error-text">{errors.expired_at}</p>
          )}
        </div>
      </div>

      {/* ── Targeting — chỉ hiện khi UNIQUE ── */}
      {isUnique && (
        <div className="border border-border rounded-xl p-4 space-y-3 bg-surface">
          <p className="text-sm font-semibold text-text-primary">Đối tượng nhận voucher</p>

          <div className="flex flex-col gap-2">
            {[
              { value: 'none', label: 'Không chỉ định — chỉ sinh mã, phân phối sau' },
              { value: 'user_type', label: 'Theo loại user — phân phối tự động cho 1 loại' },
              { value: 'user_ids', label: 'Danh sách user — nhập ID cụ thể' },
            ].map((opt) => (
              <label
                key={opt.value}
                className={`flex items-center gap-3 px-3 py-2.5 border rounded-xl cursor-pointer transition-all ${
                  form.target_mode === opt.value
                    ? 'border-primary bg-primary-light'
                    : 'border-border hover:bg-surface-muted'
                }`}
              >
                <input
                  type="radio"
                  name="target_mode"
                  value={opt.value}
                  checked={form.target_mode === opt.value}
                  onChange={() => set('target_mode', opt.value)}
                  className="accent-primary"
                />
                <span
                  className={`text-sm font-medium ${
                    form.target_mode === opt.value ? 'text-primary' : 'text-text-secondary'
                  }`}
                >
                  {opt.label}
                </span>
              </label>
            ))}
          </div>

          {/* Loại user */}
          {form.target_mode === 'user_type' && (
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">
                Loại user <span className="text-error-text">*</span>
              </label>
              <select
                value={form.target_user_type}
                onChange={(e) => set('target_user_type', e.target.value)}
                className={inputClass}
              >
                {USER_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Danh sách user IDs */}
          {form.target_mode === 'user_ids' && (
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">
                User IDs <span className="text-error-text">*</span>
                <span className="font-normal text-text-muted ml-1">
                  — cách nhau bởi dấu phẩy
                </span>
              </label>
              <textarea
                rows={3}
                value={form.target_user_ids}
                onChange={(e) => set('target_user_ids', e.target.value)}
                placeholder="VD: 1, 2, 3, 4, 5"
                className={`w-full px-4 py-2.5 rounded-xl border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all resize-none ${
                  errors.target_user_ids ? 'border-error-text' : 'border-border'
                }`}
              />
              {parsedUserIds.length > 0 && (
                <p className="mt-1 text-xs text-text-muted">{parsedUserIds.length} user hợp lệ</p>
              )}
              {errors.target_user_ids && (
                <p className="mt-1 text-xs text-error-text">{errors.target_user_ids}</p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Distribution channel */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Kênh phân phối
        </label>
        <div className="flex gap-3">
          {[
            { value: 'EMAIL', label: 'Email' },
            { value: 'WEBHOOK', label: 'Webhook' },
          ].map((ch) => (
            <label
              key={ch.value}
              className={`flex-1 flex items-center gap-3 px-4 py-3 border rounded-xl cursor-pointer transition-all ${
                form.distribution_channel === ch.value
                  ? 'border-primary bg-primary-light text-primary'
                  : 'border-border bg-surface hover:bg-surface-muted text-text-secondary'
              }`}
            >
              <input
                type="radio"
                name="channel"
                value={ch.value}
                checked={form.distribution_channel === ch.value}
                onChange={() => set('distribution_channel', ch.value)}
                className="hidden"
              />
              <span className="text-sm font-semibold">{ch.label}</span>
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
