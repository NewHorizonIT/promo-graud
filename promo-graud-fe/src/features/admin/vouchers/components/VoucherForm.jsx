import { useState } from 'react';
import { Loader2 } from 'lucide-react';

const EMPTY = {
  campaign_id: '',
  rule_id: '',
  type: 'GENERIC',
  quantity: '',
  limit_client: '',
  distribution_channel: 'IN_APP',
  prefix: '',
  target_mode: 'none',
  target_user_type: 'ALL',
  target_user_ids: '',
};

export default function VoucherForm({
  campaigns = [],
  rules = [],
  loadingRules = false,
  onCampaignChange,
  initial = EMPTY,
  onSubmit,
  loading = false,
}) {
  const [form, setForm] = useState({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState({});

  const set = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const handleTypeChange = (newType) => {
    setForm((prev) => ({
      ...prev,
      type: newType,
      distribution_channel:
        newType === 'GENERIC'
          ? prev.distribution_channel === 'EMAIL'
            ? 'IN_APP'
            : prev.distribution_channel
          : prev.distribution_channel === 'IN_APP'
            ? 'EMAIL'
            : prev.distribution_channel,
    }));
  };
  const handleCampaignChange = (e) => {
    const campaignId = e.target.value;
    setForm((prev) => ({ ...prev, campaign_id: campaignId, rule_id: '' }));
    setErrors((prev) => ({ ...prev, campaign_id: '', rule_id: '' }));
    onCampaignChange?.(campaignId ? Number(campaignId) : null);
  };

  const isUnique = form.type === 'UNIQUE';
  const needsQuantity = !isUnique || form.target_mode === 'none';

  const parsedUserIds = form.target_user_ids
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
    .map(Number)
    .filter((n) => n > 0);

  const validate = () => {
    const e = {};
    if (!form.campaign_id) e.campaign_id = 'Chọn campaign';
    if (!form.rule_id) e.rule_id = 'Chọn rule cho voucher';
    if (needsQuantity && (!form.quantity || Number(form.quantity) <= 0)) {
      e.quantity = 'Số lượng phải lớn hơn 0';
    }
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
      {/* ── Step 1: Campaign ── */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Campaign <span className="text-error-text">*</span>
        </label>
        <select
          value={form.campaign_id}
          onChange={handleCampaignChange}
          className={`${inputClass} ${errors.campaign_id ? 'border-error-text' : ''}`}
        >
          <option value="">— Chọn campaign —</option>
          {campaigns.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
              {c.status && c.status !== 'ACTIVE' ? ` (${c.status})` : ''}
            </option>
          ))}
        </select>
        {errors.campaign_id && <p className="mt-1 text-xs text-error-text">{errors.campaign_id}</p>}
      </div>

      {/* ── Step 2: Rule (phụ thuộc campaign) ── */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Rule <span className="text-error-text">*</span>
        </label>
        <div className="relative">
          <select
            value={form.rule_id}
            onChange={(e) => set('rule_id', e.target.value)}
            disabled={!form.campaign_id || loadingRules}
            className={`${inputClass} ${errors.rule_id ? 'border-error-text' : ''} disabled:opacity-50 disabled:cursor-not-allowed`}
          >
            {!form.campaign_id ? (
              <option value="">— Chọn campaign trước —</option>
            ) : loadingRules ? (
              <option value="">Đang tải rule...</option>
            ) : rules.length === 0 ? (
              <option value="">Campaign này chưa có rule</option>
            ) : (
              <>
                <option value="">— Chọn rule —</option>
                {rules.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.name}
                    {r.typeOfRule ? ` — ${r.typeOfRule}` : ''}
                  </option>
                ))}
              </>
            )}
          </select>
          {loadingRules && (
            <Loader2
              size={14}
              className="absolute right-10 top-1/2 -translate-y-1/2 text-text-muted animate-spin pointer-events-none"
            />
          )}
        </div>
        {errors.rule_id && <p className="mt-1 text-xs text-error-text">{errors.rule_id}</p>}
      </div>

      {/* ── Prefix ── */}
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
            onChange={(e) => handleTypeChange(e.target.value)}
            className={inputClass}
          >
            <option value="GENERIC">Generic — 1 mã nhiều người dùng</option>
            <option value="UNIQUE">Unique — N mã riêng từng người</option>
          </select>
        </div>

        {/* Quantity */}
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
      </div>

      {/* ── Distribution channel ── */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Kênh phân phối
        </label>
        <div className="flex gap-3">
          {(isUnique
            ? [
                { value: 'EMAIL', label: 'Email' },
                { value: 'WEBHOOK', label: 'Webhook' },
              ]
            : [
                { value: 'IN_APP', label: 'Thông báo trong hệ thống' },
                { value: 'WEBHOOK', label: 'Webhook' },
              ]
          ).map((ch) => (
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
