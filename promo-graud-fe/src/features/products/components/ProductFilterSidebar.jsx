import { useState, useEffect } from 'react';
import { Filter, RotateCcw, Check } from 'lucide-react';
import PriceRangeSlider from './PriceRangeSlider';
import { PRICE_FILTER_CONFIG } from '../../../config/constants';

export default function ProductFilterSidebar({
  types = [],
  currentFilters = { typeId: '', minPrice: '', maxPrice: '' },
  onApply,
  onReset,
}) {
  const { MIN, MAX, STEP } = PRICE_FILTER_CONFIG;

  // Local draft state for filters – only applied when clicking "Áp dụng"
  const [draftTypeId, setDraftTypeId] = useState(currentFilters.typeId || '');
  const [draftPriceRange, setDraftPriceRange] = useState([
    currentFilters.minPrice !== '' && currentFilters.minPrice !== undefined
      ? Number(currentFilters.minPrice)
      : MIN,
    currentFilters.maxPrice !== '' && currentFilters.maxPrice !== undefined
      ? Number(currentFilters.maxPrice)
      : MAX,
  ]);

  // Sync draft state if currentFilters changed from outside
  useEffect(() => {
    setDraftTypeId(currentFilters.typeId || '');
    setDraftPriceRange([
      currentFilters.minPrice !== '' && currentFilters.minPrice !== undefined
        ? Number(currentFilters.minPrice)
        : MIN,
      currentFilters.maxPrice !== '' && currentFilters.maxPrice !== undefined
        ? Number(currentFilters.maxPrice)
        : MAX,
    ]);
  }, [currentFilters.typeId, currentFilters.minPrice, currentFilters.maxPrice, MIN, MAX]);

  const handleApply = (e) => {
    e?.preventDefault();
    onApply?.({
      typeId: draftTypeId,
      minPrice: draftPriceRange[0] !== MIN ? draftPriceRange[0] : '',
      maxPrice: draftPriceRange[1] !== MAX ? draftPriceRange[1] : '',
    });
  };

  const handleReset = () => {
    setDraftTypeId('');
    setDraftPriceRange([MIN, MAX]);
    onReset?.();
  };

  const isFiltered =
    Boolean(draftTypeId) || draftPriceRange[0] !== MIN || draftPriceRange[1] !== MAX;

  return (
    <aside className="w-full md:w-64 flex-shrink-0">
      <div className="bg-surface border border-border rounded-2xl p-5 md:sticky md:top-20 shadow-level-1">
        {/* Header */}
        <div className="flex items-center justify-between pb-3.5 mb-4 border-b border-border">
          <div className="flex items-center gap-2">
            <Filter size={18} className="text-primary" />
            <h3 className="font-bold text-text-primary text-base">Bộ lọc tìm kiếm</h3>
          </div>
          {isFiltered && (
            <button
              type="button"
              onClick={handleReset}
              className="text-xs text-text-muted hover:text-primary flex items-center gap-1 transition-colors"
              title="Đặt lại bộ lọc"
            >
              <RotateCcw size={12} />
              <span>Đặt lại</span>
            </button>
          )}
        </div>

        {/* Categories */}
        <div className="mb-6">
          <p className="text-xs font-semibold text-text-muted uppercase tracking-wider mb-3">
            Loại sản phẩm
          </p>
          <div className="space-y-1.5 max-h-52 overflow-y-auto pr-1">
            <label className="flex items-center gap-2.5 p-1.5 rounded-lg hover:bg-surface-muted cursor-pointer transition-colors group">
              <input
                type="radio"
                name="productType"
                value=""
                checked={draftTypeId === ''}
                onChange={() => setDraftTypeId('')}
                className="w-4 h-4 accent-primary cursor-pointer"
              />
              <span
                className={`text-sm group-hover:text-text-primary ${
                  draftTypeId === '' ? 'font-semibold text-primary' : 'text-text-secondary'
                }`}
              >
                Tất cả loại
              </span>
            </label>

            {types.map((t) => {
              const isSelected = draftTypeId === String(t.id);
              return (
                <label
                  key={t.id}
                  className="flex items-center gap-2.5 p-1.5 rounded-lg hover:bg-surface-muted cursor-pointer transition-colors group"
                >
                  <input
                    type="radio"
                    name="productType"
                    value={t.id}
                    checked={isSelected}
                    onChange={() => setDraftTypeId(String(t.id))}
                    className="w-4 h-4 accent-primary cursor-pointer"
                  />
                  <span
                    className={`text-sm group-hover:text-text-primary flex-1 ${
                      isSelected ? 'font-semibold text-primary' : 'text-text-secondary'
                    }`}
                  >
                    {t.type}
                  </span>
                </label>
              );
            })}
          </div>
        </div>

        {/* Dual Thumb Price Slider */}
        <div className="mb-6">
          <p className="text-xs font-semibold text-text-muted uppercase tracking-wider mb-3">
            Khoảng giá (VND)
          </p>
          <PriceRangeSlider
            min={MIN}
            max={MAX}
            step={STEP}
            value={draftPriceRange}
            onChange={setDraftPriceRange}
          />
        </div>

        {/* Actions */}
        <div className="space-y-2 pt-2 border-t border-border">
          <button
            type="button"
            onClick={handleApply}
            className="
              w-full py-2.5 px-4 bg-primary hover:bg-primary-hover
              text-white rounded-xl text-sm font-semibold
              flex items-center justify-center gap-2
              transition-all duration-200 hover:shadow-glow
              active:scale-[0.98]
            "
          >
            <Check size={16} />
            Áp dụng bộ lọc
          </button>

          {isFiltered && (
            <button
              type="button"
              onClick={handleReset}
              className="
                w-full py-2 px-3 text-xs font-medium text-text-secondary
                hover:text-error-text hover:bg-error-bg rounded-lg
                transition-all flex items-center justify-center gap-1.5
              "
            >
              <RotateCcw size={13} />
              Xóa bộ lọc
            </button>
          )}
        </div>
      </div>
    </aside>
  );
}
