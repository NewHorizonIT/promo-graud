-- 1. Chuẩn hóa type_of_user
UPDATE type_of_user
SET type = UPPER(type)
WHERE type <> UPPER(type);
