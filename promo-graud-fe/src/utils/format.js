export const formatCurrency = (val) => {
  if (val === null || val === undefined || isNaN(val)) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(val);
};

export const formatShortCurrency = (val) => {
  if (!val || isNaN(val)) return "0đ";
  if (val >= 1_000_000) return `${(val / 1_000_000).toFixed(1).replace(/\.0$/, "")}M`;
  if (val >= 1_000) return `${(val / 1_000).toFixed(0)}K`;
  return `${val}đ`;
};
