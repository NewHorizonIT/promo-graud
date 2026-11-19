ALTER TABLE "order"
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
ADD COLUMN IF NOT EXISTS transaction_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);
-- 2. Tạo Unique Partial Index chống trùng lặp theo User + Idempotency Key
CREATE UNIQUE INDEX IF NOT EXISTS uq_order_user_idem_key
  ON "order" (user_id, idempotency_key)
  WHERE idempotency_key IS NOT NULL;
