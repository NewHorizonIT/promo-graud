-- 1. Chuẩn hóa type_of_user
UPDATE type_of_user
SET type = UPPER(type)
WHERE type <> UPPER(type);

-- 2. Chuẩn hóa các trường enum dạng VARCHAR
UPDATE "user" SET role = UPPER(role)
WHERE role <> UPPER(role);

UPDATE campaign SET status = UPPER(status)
WHERE status <> UPPER(status);

UPDATE rule_campaign SET
    type_of_rule = UPPER(type_of_rule),
    type_of_user = UPPER(type_of_user),
    status = UPPER(status);

UPDATE voucher SET
    type = UPPER(type),
    distribution_channel = UPPER(distribution_channel),
    status = UPPER(status);

UPDATE user_voucher SET status = UPPER(status)
WHERE status <> UPPER(status);

UPDATE "order" SET status = UPPER(status)
WHERE status <> UPPER(status);

UPDATE voucher_redemption
SET status_of_order = UPPER(status_of_order)
WHERE status_of_order <> UPPER(status_of_order);

UPDATE distribution_log SET status = UPPER(status)
WHERE status <> UPPER(status);
