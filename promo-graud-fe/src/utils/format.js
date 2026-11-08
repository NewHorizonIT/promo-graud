export const formatCurrency = (val) => {
  if (val === null || val === undefined || isNaN(val)) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
};

export const formatShortCurrency = (val) => {
  if (!val || isNaN(val)) return '0đ';
  if (val >= 1_000_000) return `${(val / 1_000_000).toFixed(1).replace(/\.0$/, '')}M`;
  if (val >= 1_000) return `${(val / 1_000).toFixed(0)}K`;
  return `${val}đ`;
};

export const formatDiscount = (rule) => {
  if (!rule) return null;
  switch (rule.typeOfRule) {
    case 'PERCENTAGE':
      return `Giảm ${rule.value}%`;
    case 'FIXED_AMOUNT':
      return `Giảm ${formatCurrency(rule.value)}`;
    case 'BUY_X_GET_Y':
      return 'Mua X tặng Y';
    case 'TIERED':
      return 'Giảm theo bậc';
    case 'FLASH_SALE':
      return 'Flash Sale';
    default:
      return null;
  }
};

export const formatDiscountFull = (rule) => {
  const base = formatDiscount(rule);
  if (!base || !rule) return base;
  if (rule.maxDiscountValue && Number(rule.maxDiscountValue) > 0) {
    return `${base} (tối đa ${formatShortCurrency(rule.maxDiscountValue)})`;
  }
  return base;
};
