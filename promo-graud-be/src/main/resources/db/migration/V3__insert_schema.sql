-- 1. CẤP 0: Các bảng danh mục độc lập
INSERT INTO type_of_user (id, type, threshold) VALUES
(1, 'normal', 0.00),
(2, 'bronze', 1000000.00),
(3, 'silver', 5000000.00),
(4, 'gold', 10000000.00),
(5, 'all', 0.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO type_of_product (id, type) VALUES
(1, 'Electronics'),
(2, 'Fashion'),
(3, 'Home & Living')
ON CONFLICT (id) DO NOTHING;

INSERT INTO campaign (id, name, start_time, end_time, promotion_budget, status) VALUES
(1, 'Chiến dịch Khai trương', NOW(), NOW() + INTERVAL '30 days', 50000000.00, 'ACTIVE'),
(2, 'Black Friday Sale', NOW() + INTERVAL '10 days', NOW() + INTERVAL '15 days', 100000000.00, 'UPCOMING')
ON CONFLICT (id) DO NOTHING;

-- 2. CẤP 1: Các bảng phụ thuộc Cấp 0
INSERT INTO "user" (id, username, email, password, role, type_id) VALUES
(1, 'admin_pro', 'admin@promoguard.com', '$2a$10$e8R1/m6...hash_pass', 'ADMIN', 4),
(2, 'nguyenvana', 'ana@gmail.com', '$2a$10$e8R1/m6...hash_pass', 'USER', 1),
(3, 'tranvanb', 'vanb@gmail.com', '$2a$10$e8R1/m6...hash_pass', 'USER', 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, name, description, price, type_id) VALUES
(1, 'Tai nghe Bluetooth Pro', 'Tai nghe chống ồn cao cấp', 1500000.00, 1),
(2, 'Áo phông Unisex', 'Áo cotton 100%', 250000.00, 2),
(3, 'Nồi chiên không dầu', 'Dung tích 5L', 2000000.00, 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_campaign (id, name, campaign_id, type_of_rule, value, max_discount_value, min_order_value, type_of_user, status) VALUES
(1, 'Giảm 20% cho đơn từ 500k', 1, 'PERCENTAGE', 20.00, 100000.00, 500000.00, 'ALL', 'ACTIVE'),
(2, 'Giảm trực tiếp 50k', 1, 'FIXED_AMOUNT', 50000.00, 50000.00, 200000.00, 'ALL', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 3. CẤP 2: Bảng Voucher
INSERT INTO voucher (id, code, type, rule_id, quantity, quantity_remain, limit_client, distribution_channel, status) VALUES
(1, 'PROMO20K', 'GENERIC', 1, 100, 98, 1, 'WEBHOOK', 'ACTIVE'),
(2, 'GIAM50K', 'GENERIC', 2, 50, 50, 1, 'WEBHOOK', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 4. CẤP 3: Ví Voucher người dùng
INSERT INTO user_voucher (id, user_id, voucher_id, status) VALUES
(1, 2, 1, 'UNUSED'),
(2, 3, 2, 'UNUSED')
ON CONFLICT (id) DO NOTHING;

-- 5. Đặt lại chuỗi tự tăng (Sequence) cho 8 bảng
SELECT setval('type_of_user_id_seq', COALESCE((SELECT MAX(id) FROM type_of_user), 1));
SELECT setval('type_of_product_id_seq', COALESCE((SELECT MAX(id) FROM type_of_product), 1));
SELECT setval('campaign_id_seq', COALESCE((SELECT MAX(id) FROM campaign), 1));
SELECT setval('user_id_seq', COALESCE((SELECT MAX(id) FROM "user"), 1));
SELECT setval('product_id_seq', COALESCE((SELECT MAX(id) FROM product), 1));
SELECT setval('rule_campaign_id_seq', COALESCE((SELECT MAX(id) FROM rule_campaign), 1));
SELECT setval('voucher_id_seq', COALESCE((SELECT MAX(id) FROM voucher), 1));
SELECT setval('user_voucher_id_seq', COALESCE((SELECT MAX(id) FROM user_voucher), 1));

COMMIT;
