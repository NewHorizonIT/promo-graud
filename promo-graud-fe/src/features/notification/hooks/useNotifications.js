import { useEffect, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../../../store/authStore';
import { notificationService } from '../services/notificationService';
import { createNotificationSocket } from '../../../store/notificationSocket';

export const NOTIFICATION_KEYS = {
  list: ['notifications', 'list'],
  unreadCount: ['notifications', 'unread-count'],
};

export function useNotifications() {
  const token = useAuthStore((s) => s.token);
  const queryClient = useQueryClient();

  // 1. Lấy danh sách thông báo
  const listQuery = useQuery({
    queryKey: NOTIFICATION_KEYS.list,
    queryFn: notificationService.getMyNotifications,
    enabled: !!token,
  });

  // 2. Lấy số lượng thông báo chưa đọc
  const unreadQuery = useQuery({
    queryKey: NOTIFICATION_KEYS.unreadCount,
    queryFn: notificationService.getUnreadCount,
    enabled: !!token,
  });

  // 3. Xử lý thông báo nhận từ WebSocket
  const handleIncomingNotification = useCallback(
    (notification) => {
      console.log('Received notification:', notification);

      // Cập nhật danh sách thông báo
      queryClient.setQueryData(NOTIFICATION_KEYS.list, (old = []) => {
        if (old.some((item) => item.id === notification.id)) {
          return old;
        }

        return [notification, ...old].slice(0, 20);
      });

      // Đơn hàng thay đổi trạng thái
      if (notification.type === 'ORDER_UPDATED' || notification.type === 'ORDER_CANCELED') {
        // Làm mới lịch sử đơn hàng của khách hàng
        queryClient.invalidateQueries({
          queryKey: ['orders', 'me'],
        });
      }

      // 3. Admin nhận thông báo khi có đơn hàng mới
      if (notification.type === 'ORDER_CREATED') {
        queryClient.invalidateQueries({
          queryKey: ['admin', 'orders'],
        });
      }

      // Cập nhật số thông báo chưa đọc từ backend
      queryClient.invalidateQueries({
        queryKey: NOTIFICATION_KEYS.unreadCount,
      });
    },
    [queryClient],
  );

  // 4. Kết nối WebSocket
  useEffect(() => {
    if (!token) return;

    const client = createNotificationSocket({
      token,
      onNotification: handleIncomingNotification,
    });

    return () => {
      client?.deactivate();
    };
  }, [token, handleIncomingNotification]);

  // 5. Đánh dấu một thông báo đã đọc
  const markAsReadMutation = useMutation({
    mutationFn: notificationService.markAsRead,
    onSuccess: (_, id) => {
      queryClient.setQueryData(NOTIFICATION_KEYS.list, (old = []) =>
        old.map((item) => (item.id === id ? { ...item, isRead: true } : item)),
      );

      queryClient.invalidateQueries({
        queryKey: NOTIFICATION_KEYS.unreadCount,
      });
    },
  });

  // 6. Đánh dấu tất cả đã đọc
  const markAllAsReadMutation = useMutation({
    mutationFn: notificationService.markAllAsRead,
    onSuccess: () => {
      queryClient.setQueryData(NOTIFICATION_KEYS.list, (old = []) =>
        old.map((item) => ({ ...item, isRead: true })),
      );

      queryClient.setQueryData(NOTIFICATION_KEYS.unreadCount, 0);
    },
  });

  // 7. Xóa thông báo
  const deleteMutation = useMutation({
    mutationFn: notificationService.deleteNotification,
    onSuccess: (_, id) => {
      const currentList = queryClient.getQueryData(NOTIFICATION_KEYS.list) ?? [];

      const target = currentList.find((item) => item.id === id);

      queryClient.setQueryData(
        NOTIFICATION_KEYS.list,
        currentList.filter((item) => item.id !== id),
      );

      if (target && !target.isRead) {
        queryClient.invalidateQueries({
          queryKey: NOTIFICATION_KEYS.unreadCount,
        });
      }
    },
  });

  return {
    notifications: listQuery.data ?? [],
    isLoading: listQuery.isLoading,
    unreadCount: unreadQuery.data ?? 0,
    markAsRead: markAsReadMutation.mutate,
    markAllAsRead: markAllAsReadMutation.mutate,
    deleteNotification: deleteMutation.mutate,
  };
}
