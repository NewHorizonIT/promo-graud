import { useId } from 'react';
import { formatCurrency } from '../../../utils/format';

export default function PriceRangeSlider({
  min = 0,
  max = 2000000,
  step = 20000,
  value = [0, 2000000],
  onChange,
}) {
  const minVal = value[0] ?? min;
  const maxVal = value[1] ?? max;

  const minInputId = useId();
  const maxInputId = useId();

  const minPercent = Math.min(100, Math.max(0, ((minVal - min) / (max - min)) * 100));
  const maxPercent = Math.min(100, Math.max(0, ((maxVal - min) / (max - min)) * 100));

  const handleMinChange = (e) => {
    const val = Number(e.target.value);
    const clampedVal = Math.min(val, maxVal - step);
    onChange?.([clampedVal, maxVal]);
  };

  const handleMaxChange = (e) => {
    const val = Number(e.target.value);
    const clampedVal = Math.max(val, minVal + step);
    onChange?.([minVal, clampedVal]);
  };

  const handlePreset = (newMin, newMax) => {
    onChange?.([newMin, newMax]);
  };

  return (
    <div className="space-y-4">
      {/* Current values badges */}
      <div className="flex items-center justify-between text-xs">
        <div className="bg-surface-muted border border-border px-2.5 py-1.5 rounded-lg text-text-primary font-medium">
          <span className="text-text-muted block text-[10px]">Tối thiểu</span>
          <span>{formatCurrency(minVal)}</span>
        </div>
        <span className="text-text-muted font-bold">—</span>
        <div className="bg-surface-muted border border-border px-2.5 py-1.5 rounded-lg text-text-primary font-medium text-right">
          <span className="text-text-muted block text-[10px]">Tối đa</span>
          <span>{formatCurrency(maxVal)}</span>
        </div>
      </div>

      {/* Dual slider bar */}
      <div className="relative py-3">
        {/* Track background */}
        <div className="relative w-full h-2 rounded-full bg-border">
          {/* Active highlighted range */}
          <div
            className="absolute h-full bg-primary rounded-full transition-all duration-75"
            style={{
              left: `${minPercent}%`,
              width: `${Math.max(0, maxPercent - minPercent)}%`,
            }}
          />
        </div>

        {/* Min thumb input */}
        <input
          id={minInputId}
          type="range"
          min={min}
          max={max}
          step={step}
          value={minVal}
          onChange={handleMinChange}
          aria-label="Giá tối thiểu"
          className="range-slider-thumb z-20"
        />

        {/* Max thumb input */}
        <input
          id={maxInputId}
          type="range"
          min={min}
          max={max}
          step={step}
          value={maxVal}
          onChange={handleMaxChange}
          aria-label="Giá tối đa"
          className="range-slider-thumb z-30"
        />
      </div>

      {/* Quick presets */}
      <div className="grid grid-cols-2 gap-1.5 pt-1">
        <button
          type="button"
          onClick={() => handlePreset(min, 200000)}
          className={`px-2 py-1 text-[11px] font-medium rounded-lg border transition-all text-left ${
            minVal === min && maxVal === 200000
              ? 'bg-primary-light border-primary text-primary'
              : 'border-border text-text-secondary hover:bg-surface-muted'
          }`}
        >
          Dưới 200K
        </button>
        <button
          type="button"
          onClick={() => handlePreset(200000, 500000)}
          className={`px-2 py-1 text-[11px] font-medium rounded-lg border transition-all text-left ${
            minVal === 200000 && maxVal === 500000
              ? 'bg-primary-light border-primary text-primary'
              : 'border-border text-text-secondary hover:bg-surface-muted'
          }`}
        >
          200K - 500K
        </button>
        <button
          type="button"
          onClick={() => handlePreset(500000, 1000000)}
          className={`px-2 py-1 text-[11px] font-medium rounded-lg border transition-all text-left ${
            minVal === 500000 && maxVal === 1000000
              ? 'bg-primary-light border-primary text-primary'
              : 'border-border text-text-secondary hover:bg-surface-muted'
          }`}
        >
          500K - 1M
        </button>
        <button
          type="button"
          onClick={() => handlePreset(1000000, max)}
          className={`px-2 py-1 text-[11px] font-medium rounded-lg border transition-all text-left ${
            minVal === 1000000 && maxVal === max
              ? 'bg-primary-light border-primary text-primary'
              : 'border-border text-text-secondary hover:bg-surface-muted'
          }`}
        >
          Trên 1M
        </button>
      </div>
    </div>
  );
}
