import axiosInstance from '../../../lib/axiosInstance';
import { getVisitorId } from '../../../utils/deviceFingerprint';

function idemStorageKey(voucherId, method, items) {
  const parts = items.map((i) => `${i.id}x${i.quantity}`).sort();
  return `idem:${method}|${voucherId ?? 'none'}|${parts.join(',')}`;
}

function getIdemKey(storageKey) {
  try {
    let k = sessionStorage.getItem(storageKey);
    if (!k) {
      k = crypto.randomUUID();
      sessionStorage.setItem(storageKey, k);
    }
    return k;
  } catch {
    return crypto.randomUUID();
  }
}

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

export async function createOrder({ voucherId, paymentMethod, items }) {
  const method = paymentMethod || 'COD';
  const storageKey = idemStorageKey(voucherId, method, items);
  const key = getIdemKey(storageKey);

  const headers = { 'Idempotency-Key': key };
  if (voucherId) {
    headers['X-Visitor-Id'] = await getVisitorId();
  }

  const { data } = await axiosInstance.post(
    '/orders',
    {
      voucherId: voucherId ?? null,
      paymentMethod: method,
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

export async function createVNPayUrl(orderId) {
  const { data } = await axiosInstance.get(`/payments/vnpay/create-url?orderId=${orderId}`);
  return data.result.url;
}

export function clearAllIdempotencyKeys() {
  Object.keys(sessionStorage).forEach((key) => {
    if (key.startsWith('idem:')) {
      sessionStorage.removeItem(key);
    }
  });
}
