import { Client } from '@stomp/stompjs';

export const createNotificationSocket = ({ token, onNotification }) => {
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_URL,

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,

    onConnect: () => {
      console.log('WebSocket connected');

      client.subscribe('/user/queue/notifications', (message) => {
        const notification = JSON.parse(message.body);

        onNotification?.(notification);
      });
    },

    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers.message);
    },

    onWebSocketError: (error) => {
      console.error('WebSocket error:', error);
    },
  });

  client.activate();

  return client;
};
