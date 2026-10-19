import axiosInstance from '../../../lib/axiosInstance';

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
  const { data } = await axiosInstance.post('/orders', {
    voucherId: voucherId ?? null,
    items: items.map((item) => ({
      productId: item.id,
      quantity: item.quantity,
      price: item.price,
    })),
  });
  return data.result;
}
