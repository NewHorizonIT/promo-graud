import axiosInstance from '../../../lib/axiosInstance';
import { getVisitorId } from '../../../utils/deviceFingerprint';

export async function previewOrder({ voucherId, items }) {
  const { data } = await axiosInstance.post('/orders/preview', {
    voucherId: voucherId ?? null,
    items: items.map((item) => ({
      productId: item.id,
      quantity: item.quantity,
      price: item.price,
    })),
  });
  return data.result;
}

export async function createOrder({ voucherId, items }) {
  const headers = {};
  if (voucherId) {
    headers['X-Visitor-Id'] = await getVisitorId();
  }

  const { data } = await axiosInstance.post(
    '/orders',
    {
      voucherId: voucherId ?? null,
      items: items.map((item) => ({
        productId: item.id,
        quantity: item.quantity,
        price: item.price,
      })),
    },
    { headers },
  );
  return data.result;
}
