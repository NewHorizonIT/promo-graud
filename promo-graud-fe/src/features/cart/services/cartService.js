const CART_STORAGE_KEY = 'promo_cart';

export function getStoredCart() {
  try {
    const raw = localStorage.getItem(CART_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (err) {
    console.error('Failed to parse cart from localStorage:', err);
    return [];
  }
}

export function saveStoredCart(cart) {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
  } catch (err) {
    console.error('Failed to save cart to localStorage:', err);
  }
}

export function clearStoredCart() {
  try {
    localStorage.removeItem(CART_STORAGE_KEY);
  } catch (err) {
    console.error('Failed to clear cart from localStorage:', err);
  }
}
