import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Ticket, ShoppingBag, CheckCircle2, XCircle, CheckCheck, Trash2 } from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';

function formatTimeAgo(dateStr) {
  if (!dateStr) return '';
  const diffSec = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
  if (diffSec < 60) return 'Vừa xong';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin} phút trước`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour} giờ trước`;
  return new Date(dateStr).toLocaleDateString('vi-VN');
}

function getTypeStyle(type) {
  switch (type) {
    case 'VOUCHER_NEW':
      return {
        icon: <Ticket size={16} className="text-orange-500" />,
        bg: 'bg-orange-500/10',
      };
    case 'ORDER_CREATED':
      return {
        icon: <ShoppingBag size={16} className="text-primary" />,
        bg: 'bg-primary/10',
      };
    case 'ORDER_UPDATED':
      return {
        icon: <CheckCircle2 size={16} className="text-emerald-500" />,
        bg: 'bg-emerald-500/10',
      };
    case 'ORDER_CANCELED':
      return {
        icon: <XCircle size={16} className="text-rose-500" />,
        bg: 'bg-rose-500/10',
      };
    default:
      return {
        icon: <Bell size={16} className="text-text-secondary" />,
        bg: 'bg-surface-muted',
      };
  }
}

export default function NotificationBell({ isAdmin = false }) {
  const [open, setOpen] = useState(false);
  const bellRef = useRef(null);
  const navigate = useNavigate();

  const { notifications, isLoading, unreadCount, markAsRead, markAllAsRead, deleteNotification } =
    useNotifications();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (bellRef.current && !bellRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleItemClick = (item) => {
    if (!item.isRead) {
      markAsRead(item.id);
    }
    setOpen(false);

    if (item.type === 'VOUCHER_NEW') {
      navigate('/vouchers');
    } else if (item.type === 'ORDER_CREATED') {
      navigate(
        isAdmin
          ? item.referenceId
            ? `/admin/orders/${item.referenceId}`
            : '/admin/orders'
          : '/orders',
      );
    } else if (item.type === 'ORDER_UPDATED' || item.type === 'ORDER_CANCELED') {
      navigate('/orders');
    }
  };

  return (
    <div ref={bellRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        aria-label="Thông báo"
        className="relative p-2 rounded-full hover:bg-surface-muted text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className="absolute top-0.5 right-0.5 min-w-[18px] h-[18px] px-1 bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center shadow-sm">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-surface border border-border rounded-2xl shadow-level-2 z-50 overflow-hidden">
          {/* Header Dropdown */}
          <div className="px-4 py-3 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-text-primary">Thông báo</span>
              {unreadCount > 0 && (
                <span className="px-2 py-0.5 text-[11px] font-semibold rounded-full bg-primary-light text-primary">
                  {unreadCount} mới
                </span>
              )}
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                onClick={() => markAllAsRead()}
                className="flex items-center gap-1 text-xs font-medium text-primary hover:underline cursor-pointer"
              >
                <CheckCheck size={14} />
                Đọc tất cả
              </button>
            )}
          </div>

          {/* Danh sách thông báo */}
          <div className="max-h-96 overflow-y-auto divide-y divide-border">
            {isLoading ? (
              <div className="p-4 space-y-3">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="h-14 rounded-xl bg-surface-muted animate-pulse" />
                ))}
              </div>
            ) : notifications.length === 0 ? (
              <div className="py-10 px-4 text-center">
                <Bell size={28} className="mx-auto text-text-muted opacity-50 mb-2" />
                <p className="text-sm text-text-muted">Chưa có thông báo nào</p>
              </div>
            ) : (
              notifications.map((item) => {
                const style = getTypeStyle(item.type);
                return (
                  <div
                    key={item.id}
                    onClick={() => handleItemClick(item)}
                    className={`group flex items-start gap-3 px-4 py-3 transition-colors cursor-pointer ${
                      item.isRead
                        ? 'bg-surface hover:bg-surface-muted/60'
                        : 'bg-primary-light/30 hover:bg-primary-light/50'
                    }`}
                  >
                    <div
                      className={`mt-0.5 w-8 h-8 rounded-xl shrink-0 flex items-center justify-center ${style.bg}`}
                    >
                      {style.icon}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2">
                        <p
                          className={`text-xs truncate ${
                            item.isRead
                              ? 'font-medium text-text-secondary'
                              : 'font-bold text-text-primary'
                          }`}
                        >
                          {item.title}
                        </p>
                        {!item.isRead && (
                          <span className="w-2 h-2 rounded-full bg-primary shrink-0" />
                        )}
                      </div>

                      <p className="text-xs text-text-muted line-clamp-2 mt-0.5 leading-relaxed">
                        {item.message}
                      </p>

                      <span className="text-[11px] text-text-muted mt-1 block">
                        {formatTimeAgo(item.createdAt)}
                      </span>
                    </div>

                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        deleteNotification(item.id);
                      }}
                      title="Xóa thông báo"
                      className="opacity-0 group-hover:opacity-100 p-1 rounded-lg text-text-muted hover:text-rose-500 hover:bg-surface-muted transition-all cursor-pointer"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}
