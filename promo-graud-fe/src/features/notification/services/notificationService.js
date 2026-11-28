import axiosInstance from '../../../lib/axiosInstance';

export const notificationService = {
  getMyNotifications: async () => {
    const res = await axiosInstance.get('/notifications');
    return res.data?.result ?? res.result ?? [];
  },

  getUnreadCount: async () => {
    const res = await axiosInstance.get('/notifications/unread-count');
    return res.data?.result ?? res.result ?? 0;
  },

  markAsRead: async (id) => {
    const res = await axiosInstance.patch(`/notifications/${id}/read`);
    return res.data?.result ?? res.result;
  },

  markAllAsRead: async () => {
    const res = await axiosInstance.patch('/notifications/read-all');
    return res.data?.result ?? res.result;
  },

  deleteNotification: async (id) => {
    const res = await axiosInstance.delete(`/notifications/${id}`);
    return res.data?.result ?? res.result;
  },
};
