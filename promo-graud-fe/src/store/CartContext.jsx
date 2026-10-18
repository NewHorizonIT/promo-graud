import { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getStoredCart,
  saveStoredCart,
  clearStoredCart,
} from '../features/cart/services/cartService';
import { useAuthStore } from './authStore';
import Toast from '../components/Toast';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);
  const [cart, setCart] = useState([]);
  const [toast, setToast] = useState(null);

  // Sync cart with login/logout state
  useEffect(() => {
    if (token) {
      setCart(getStoredCart());
    } else {
      clearStoredCart();
      setCart([]);
    }
  }, [token]);

  const showToast = useCallback((message, type = 'info', duration = 3000) => {
    setToast({ message, type, duration });
  }, []);

  const hideToast = useCallback(() => {
    setToast(null);
  }, []);

  /**
   * Thêm sản phẩm vào giỏ hàng:
   * 1. Kiểm tra đăng nhập -> nếu chưa thì báo "Vui lòng đăng nhập" và chuyển hướng /login.
   * 2. Kiểm tra trùng id -> nếu có rồi thì báo "Sản phẩm đã có trong giỏ hàng".
   * 3. Thêm mới -> lưu vào localStorage và cập nhật state.
   */
  const addToCart = useCallback(
    (product) => {
      if (!product || !product.id) return { success: false };

      if (!token) {
        showToast('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!', 'warning', 3000);
        navigate('/login');
        return { success: false, reason: 'unauthenticated' };
      }

      // Kiểm tra trùng id trong giỏ hàng
      const isExisted = cart.some((item) => item.id === product.id);
      if (isExisted) {
        showToast('Sản phẩm đã có trong giỏ hàng!', 'warning', 3000);
        return { success: false, reason: 'duplicate' };
      }

      const newItem = {
        id: product.id,
        name: product.name,
        price: product.price,
        images: product.images,
        typeName: product.typeName || product.type || '',
        quantity: 1,
      };

      const nextCart = [newItem, ...cart];
      setCart(nextCart);
      saveStoredCart(nextCart);
      showToast(`Đã thêm "${product.name}" vào giỏ hàng!`, 'success', 2500);

      return { success: true };
    },
    [cart, navigate, showToast, token],
  );

  // Cập nhật số lượng
  const updateQuantity = useCallback((productId, newQuantity) => {
    if (newQuantity < 1) return;
    setCart((prev) => {
      const nextCart = prev.map((item) =>
        item.id === productId ? { ...item, quantity: newQuantity } : item,
      );
      saveStoredCart(nextCart);
      return nextCart;
    });
  }, []);

  // Xóa 1 sản phẩm khỏi giỏ
  const removeFromCart = useCallback(
    (productId) => {
      setCart((prev) => {
        const nextCart = prev.filter((item) => item.id !== productId);
        saveStoredCart(nextCart);
        return nextCart;
      });
      showToast('Đã xóa sản phẩm khỏi giỏ hàng', 'info', 2000);
    },
    [showToast],
  );

  // Xóa toàn bộ giỏ hàng
  const clearCart = useCallback(() => {
    setCart([]);
    clearStoredCart();
  }, []);

  // Xóa giỏ hàng khi người dùng đăng xuất
  const handleLogoutCart = useCallback(() => {
    setCart([]);
    clearStoredCart();
  }, []);

  // Tổng số lượng và tổng tiền
  const cartCount = useMemo(() => {
    return cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
  }, [cart]);

  const cartTotal = useMemo(() => {
    return cart.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 1), 0);
  }, [cart]);

  const value = useMemo(
    () => ({
      cart,
      cartCount,
      cartTotal,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      handleLogoutCart,
      showToast,
    }),
    [
      cart,
      cartCount,
      cartTotal,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      handleLogoutCart,
      showToast,
    ],
  );

  return (
    <CartContext.Provider value={value}>
      {children}
      <Toast toast={toast} onClose={hideToast} />
    </CartContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}
