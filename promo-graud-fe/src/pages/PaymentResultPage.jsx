import { useEffect, useState, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle2, XCircle, Loader2, AlertCircle } from 'lucide-react';
import { useCart } from '../hooks/useCart';
import { formatCurrency } from '../utils/format';
import axiosInstance from '../lib/axiosInstance';
import { createVNPayUrl } from '../features/checkout/services/checkoutService';

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { clearCart, showToast } = useCart();

  const orderId = searchParams.get('orderId');
  const responseCode = searchParams.get('code') || searchParams.get('vnp_ResponseCode');

  const [payStatus, setPayStatus] = useState('UNPAID');
  const [orderInfo, setOrderInfo] = useState(null);
  const [isRepaying, setIsRepaying] = useState(false); // State chống click nhiều lần khi đang lấy link

  const pollCount = useRef(0);
  const maxPolls = 15;

  useEffect(() => {
    if (!orderId) {
      navigate('/cart');
      return;
    }

    if (responseCode && responseCode !== '00') {
      setPayStatus('RETRY');
      return;
    }

    const checkOrderStatus = async () => {
      try {
        const { data } = await axiosInstance.get(`/orders/${orderId}`);

        const currentPayStatus = data.result.paymentStatus;
        setOrderInfo(data.result);

        if (currentPayStatus !== 'UNPAID') {
          setPayStatus(currentPayStatus);

          if (currentPayStatus === 'PAID') {
            clearCart();
          } else if (currentPayStatus === 'FAILED') {
            showToast('Thanh toán thất bại, vui lòng thử lại', 'error');
          }
          return true;
        }
      } catch (error) {
        console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error);
      }
      return false;
    };

    checkOrderStatus().then((isDone) => {
      if (isDone) return;

      const intervalId = setInterval(async () => {
        pollCount.current += 1;
        const done = await checkOrderStatus();

        if (done) {
          clearInterval(intervalId);
        } else if (pollCount.current >= maxPolls) {
          clearInterval(intervalId);
          setPayStatus('TIMEOUT');
          showToast('Hệ thống đang xử lý chậm. Trạng thái sẽ được cập nhật sau', 'info');
        }
      }, 2000);

      return () => clearInterval(intervalId);
    });
  }, [orderId, responseCode, navigate, clearCart, showToast]);

  // HÀM BỊ THIẾU: Xử lý khi user bấm "Thanh toán lại"
  const handleRepay = async () => {
    setIsRepaying(true);
    try {
      // Thay đổi URL API này cho đúng với endpoint tạo link VNPay ở Backend của bạn
      const paymentUrl = await createVNPayUrl(orderId);
      window.location.href = paymentUrl;
    } catch (err) {
      showToast(err.response?.data?.message || 'Không tạo được link thanh toán', 'error');
    } finally {
      setIsRepaying(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] py-20 px-4">
      <div className="bg-surface rounded-3xl border border-border shadow-level-1 max-w-lg w-full mx-auto p-8 text-center">
        {payStatus === 'UNPAID' && (
          <>
            <div className="w-20 h-20 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center mb-6 mx-auto">
              <Loader2 size={44} className="animate-spin" />
            </div>
            <h2 className="text-2xl font-bold text-text-primary mb-2">Đang xử lý thanh toán</h2>
            <p className="text-sm text-text-muted mb-4 leading-relaxed">
              Hệ thống đang đợi xác nhận từ VNPay...
            </p>
          </>
        )}

        {payStatus === 'PAID' && (
          <>
            <div className="w-20 h-20 rounded-full bg-emerald-50 text-emerald-500 flex items-center justify-center mb-6 mx-auto">
              <CheckCircle2 size={44} />
            </div>
            <h2 className="text-2xl font-bold text-text-primary mb-2">Thanh toán thành công!</h2>
            <p className="text-sm text-emerald-600 font-medium mb-1">
              Đơn hàng của bạn đang chờ shop xác nhận và giao hàng.
            </p>
            <p className="text-xs text-text-muted mb-6">
              Bạn có thể theo dõi tiến độ trong phần Quản lý đơn hàng.
            </p>

            {orderInfo && (
              <div className="w-full bg-surface-muted rounded-2xl px-6 py-4 space-y-2 text-sm mb-6 text-left">
                <div className="flex justify-between text-text-secondary">
                  <span>Mã đơn hàng</span>
                  <span className="font-bold text-text-primary font-mono">#{orderInfo.id}</span>
                </div>
                <div className="flex justify-between text-text-secondary border-t border-border pt-2">
                  <span>Đã thanh toán</span>
                  <span className="font-bold text-primary text-base">
                    {formatCurrency(orderInfo.finalPrice)}
                  </span>
                </div>
              </div>
            )}
          </>
        )}

        {payStatus === 'RETRY' && (
          <>
            <div className="w-20 h-20 rounded-full bg-amber-50 text-amber-500 flex items-center justify-center mb-6 mx-auto">
              <AlertCircle size={44} />
            </div>
            <h2 className="text-2xl font-bold text-text-primary mb-2">
              Bạn chưa hoàn tất thanh toán
            </h2>
            <p className="text-sm text-text-muted mb-6 leading-relaxed">
              Đơn #{orderId} vẫn được giữ. Bạn có thể thanh toán lại ngay hoặc vào Lịch sử đơn hàng
              sau.
            </p>
          </>
        )}

        {/* UI BỊ THIẾU: Xử lý FAILED hoặc TIMEOUT */}
        {(payStatus === 'FAILED' || payStatus === 'TIMEOUT') && (
          <>
            <div className="w-20 h-20 rounded-full bg-red-50 text-red-500 flex items-center justify-center mb-6 mx-auto">
              <XCircle size={44} />
            </div>
            <h2 className="text-2xl font-bold text-text-primary mb-2">
              {payStatus === 'TIMEOUT' ? 'Hệ thống phản hồi chậm' : 'Thanh toán thất bại'}
            </h2>
            <p className="text-sm text-text-muted mb-6 leading-relaxed">
              {payStatus === 'TIMEOUT'
                ? 'Hệ thống đang xử lý chậm. Vui lòng kiểm tra lại trạng thái trong Lịch sử đơn hàng sau.'
                : 'Đã có lỗi xảy ra. Đơn hàng của bạn chưa được thanh toán thành công.'}
            </p>
          </>
        )}

        <div className="flex gap-3 w-full mt-4">
          <button
            onClick={() => navigate('/orders')}
            className="flex-1 py-3 px-4 rounded-xl border border-border text-sm font-semibold text-text-secondary hover:bg-surface-muted transition-colors"
          >
            Quản lý đơn hàng
          </button>

          {payStatus === 'RETRY' ? (
            <button
              onClick={handleRepay}
              disabled={isRepaying}
              className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center"
            >
              {isRepaying ? <Loader2 size={18} className="animate-spin" /> : 'Thanh toán lại'}
            </button>
          ) : payStatus === 'FAILED' || payStatus === 'TIMEOUT' ? (
            <button
              onClick={() => navigate('/cart')}
              className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow"
            >
              Về giỏ hàng thử lại
            </button>
          ) : (
            <button
              onClick={() => navigate('/products')}
              className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm transition-all hover:shadow-glow"
            >
              Tiếp tục mua sắm
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
