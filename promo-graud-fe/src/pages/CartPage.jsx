import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Trash2, CheckCircle2, Lock, LogIn } from 'lucide-react';
import { useCart } from '../hooks/useCart';
import { useAuthStore } from '../store/authStore';
import CartItem from '../features/cart/components/CartItem';
import CartSummary from '../features/cart/components/CartSummary';
import EmptyCart from '../features/cart/components/EmptyCart';
import { createOrder } from '../features/checkout/services/checkoutService';
import { formatCurrency } from '../utils/format';

export default function CartPage() {
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);
  const { cart, cartCount, updateQuantity, removeFromCart, clearCart, showToast } = useCart();

  const [checkoutSuccess, setCheckoutSuccess] = useState(false);
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [orderResult, setOrderResult] = useState(null); // { id, finalPrice }

  // 1. Kiểm tra chưa đăng nhập
  if (!token) {
    return (
      <div className="flex flex-col items-center justify-center py-24 px-4 text-center max-w-md mx-auto">
        <div className="w-20 h-20 rounded-full bg-amber-50 text-amber-500 flex items-center justify-center mb-6">
          <Lock size={36} />
        </div>
        <h2 className="text-2xl font-bold text-text-primary mb-2">Yêu cầu đăng nhập</h2>
        <p className="text-sm text-text-muted mb-8 leading-relaxed">
          Vui lòng đăng nhập tài khoản PromoWear để xem và quản lý giỏ hàng của bạn.
        </p>
        <div className="flex gap-4 w-full">
          <Link
            to="/products"
            className="flex-1 py-3 px-4 rounded-xl border border-border text-sm font-semibold text-text-secondary hover:bg-surface-muted transition-colors text-center"
          >
            Về sản phẩm
          </Link>
          <button
            type="button"
            onClick={() => navigate('/login')}
            className="flex-1 py-3 px-4 rounded-xl bg-primary hover:bg-primary-hover text-white text-sm font-semibold transition-all hover:shadow-glow flex items-center justify-center gap-2 cursor-pointer"
          >
            <LogIn size={16} />
            Đăng nhập ngay
          </button>
        </div>
      </div>
    );
  }

  // 2. Đã đặt hàng thành công
  if (checkoutSuccess) {
    return (
      <div className="flex flex-col items-center justify-center py-20 px-4 bg-surface rounded-3xl border border-border text-center shadow-level-1 max-w-lg mx-auto my-8">
        <div className="w-20 h-20 rounded-full bg-emerald-50 text-emerald-500 flex items-center justify-center mb-6">
          <CheckCircle2 size={44} />
        </div>
        <h2 className="text-2xl font-bold text-text-primary mb-2">Đặt hàng thành công!</h2>
        <p className="text-sm text-text-muted max-w-sm leading-relaxed">
          Cảm ơn bạn đã mua sắm tại PromoWear. Chúng tôi sẽ chuẩn bị và giao hàng cho bạn trong thời
          gian sớm nhất.
        </p>

        {orderResult && (
          <div className="mt-6 w-full bg-surface-muted rounded-2xl px-6 py-4 space-y-2 text-sm">
            <div className="flex justify-between text-text-secondary">
              <span>Mã đơn hàng</span>
              <span className="font-bold text-text-primary font-mono">#{orderResult.id}</span>
            </div>
            <div className="flex justify-between text-text-secondary border-t border-border pt-2">
              <span>Tổng thanh toán</span>
              <span className="font-bold text-primary text-base">
                {formatCurrency(orderResult.finalPrice)}
              </span>
            </div>
          </div>
        )}

        <div className="flex gap-3 mt-8 w-full">
          <Link
            to="/orders"
            className="flex-1 py-3 px-4 rounded-xl border border-border text-sm font-semibold text-text-secondary hover:bg-surface-muted transition-colors text-center"
          >
            Xem đơn hàng
          </Link>
          <Link
            to="/products"
            onClick={() => setCheckoutSuccess(false)}
            className="flex-1 inline-flex items-center justify-center gap-2 px-6 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow"
          >
            Tiếp tục mua sắm
          </Link>
        </div>
      </div>
    );
  }

  // 3. Giỏ hàng trống
  if (cart.length === 0) {
    return <EmptyCart />;
  }
  // 4. Xử lý đặt hàng
  const handleCheckout = async (voucherId) => {
    setCheckoutLoading(true);
    try {
      const order = await createOrder({ voucherId, items: cart });
      clearCart();
      setOrderResult({ id: order.id, finalPrice: order.finalPrice });
      setCheckoutSuccess(true);
      showToast('Đặt hàng thành công! Cảm ơn bạn.', 'success', 4000);
    } catch (err) {
      const msg = err.response?.data?.message || 'Đặt hàng thất bại. Vui lòng thử lại.';
      showToast(msg, 'error', 4000);
    } finally {
      setCheckoutLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl lg:text-3xl font-bold text-text-primary">Giỏ hàng của bạn</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-primary-light text-primary">
              {cartCount} sản phẩm
            </span>
          </div>
          <p className="text-sm text-text-muted mt-1">
            Kiểm tra và cập nhật các sản phẩm trong giỏ hàng trước khi thanh toán
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/products"
            className="flex items-center gap-1.5 text-sm font-semibold text-text-secondary hover:text-primary transition-colors"
          >
            <ArrowLeft size={16} />
            <span>Tiếp tục mua sắm</span>
          </Link>

          <span className="text-border">|</span>

          <button
            type="button"
            onClick={() => {
              if (window.confirm('Bạn có chắc chắn muốn xóa toàn bộ giỏ hàng?')) {
                clearCart();
                showToast('Đã xóa toàn bộ giỏ hàng', 'info');
              }
            }}
            className="flex items-center gap-1.5 text-sm font-medium text-text-muted hover:text-error-text transition-colors cursor-pointer"
          >
            <Trash2 size={16} />
            <span>Xóa tất cả</span>
          </button>
        </div>
      </div>

      {/* Main Grid: Left Items + Right Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Cart Item List */}
        <div className="lg:col-span-8 space-y-4">
          {cart.map((item) => (
            <CartItem
              key={item.id}
              item={item}
              onUpdateQuantity={updateQuantity}
              onRemove={removeFromCart}
            />
          ))}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-4">
          <CartSummary items={cart} onCheckout={handleCheckout} checkoutLoading={checkoutLoading} />
        </div>
      </div>
    </div>
  );
}
