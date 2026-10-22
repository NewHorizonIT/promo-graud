import { useState, useEffect } from 'react';
import { useTypeProduct } from '../hooks/useProducts';

const EMPTY = { name: '', type_id: '', price: '', images: '', description: '' };

function formatPrice(raw) {
  if (!raw) return '';
  const n = Number(raw);
  if (isNaN(n)) return raw;
  return n.toLocaleString('vi-VN');
}

// Mỗi dòng một URL ảnh
const toUrlList = (text) =>
  text
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean);

export default function ProductForm({ initial = EMPTY, onSubmit, loading = false }) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});
  const [priceFocused, setPriceFocused] = useState(false);

  const { data: types = [] } = useTypeProduct();

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
    if (!form.name.trim()) e.name = 'Tên sản phẩm không được để trống';
    else if (form.name.trim().length > 255) e.name = 'Tên sản phẩm không quá 255 ký tự';
    if (!form.price || Number(form.price) <= 0) e.price = 'Giá phải lớn hơn 0';
    if (toUrlList(form.images).some((u) => !/^https?:\/\//i.test(u)))
      e.images = 'Mỗi dòng phải là một URL hợp lệ (http/https)';
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
      name: form.name.trim(),
      description: form.description.trim() || null,
      price: Number(form.price),
      images: toUrlList(form.images).join(',') || null,
      typeOfProduct: form.type_id ? Number(form.type_id) : null,
    });
  };

  const handlePriceChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '');
    set('price', raw);
  };

  const priceDisplayValue = priceFocused ? form.price || '' : formatPrice(form.price);

  const inputClass =
    'w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all';

  const textareaClass =
    'w-full px-4 py-3 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all resize-y';

  const errorClass = 'border-error-text focus:border-error-text focus:ring-error-text/20';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {/* Name */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">
          Tên sản phẩm <span className="text-error-text">*</span>
        </label>
        <input
          type="text"
          value={form.name}
          onChange={(e) => set('name', e.target.value)}
          placeholder="VD: Tai nghe Bluetooth Pulse"
          className={`${inputClass} ${errors.name ? errorClass : ''}`}
        />
        {errors.name && <p className="mt-1 text-xs text-error-text">{errors.name}</p>}
      </div>

      {/* Type + Price */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Loại sản phẩm
          </label>
          <select
            value={form.type_id}
            onChange={(e) => set('type_id', e.target.value)}
            className={inputClass}
          >
            <option value="">— Chưa phân loại —</option>
            {types.map((t) => (
              <option key={t.id} value={String(t.id)}>
                {t.type}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-text-secondary mb-1.5">
            Giá <span className="text-error-text">*</span>
          </label>
          <div className="relative">
            <input
              type="text"
              inputMode="numeric"
              value={priceDisplayValue}
              onChange={handlePriceChange}
              onFocus={() => setPriceFocused(true)}
              onBlur={() => setPriceFocused(false)}
              placeholder="VD: 1290000"
              className={`${inputClass} pr-14 ${errors.price ? errorClass : ''}`}
            />
            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-xs font-semibold text-text-muted pointer-events-none">
              VNĐ
            </span>
          </div>
          {errors.price && <p className="mt-1 text-xs text-error-text">{errors.price}</p>}
        </div>
      </div>

      {/* Images */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">Hình ảnh</label>
        <textarea
          rows={3}
          value={form.images}
          onChange={(e) => set('images', e.target.value)}
          placeholder={'Mỗi dòng một URL ảnh\nhttps://...'}
          className={`${textareaClass} ${errors.images ? errorClass : ''}`}
        />
        {errors.images && <p className="mt-1 text-xs text-error-text">{errors.images}</p>}
      </div>

      {/* Description */}
      <div>
        <label className="block text-sm font-medium text-text-secondary mb-1.5">Mô tả</label>
        <textarea
          rows={4}
          value={form.description}
          onChange={(e) => set('description', e.target.value)}
          placeholder="Mô tả ngắn về sản phẩm"
          className={textareaClass}
        />
      </div>

      {/* Submit */}
      <div className="flex justify-end gap-3 pt-2">
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] cursor-pointer"
        >
          {loading ? 'Đang lưu...' : 'Lưu Sản phẩm'}
        </button>
      </div>
    </form>
  );
}
