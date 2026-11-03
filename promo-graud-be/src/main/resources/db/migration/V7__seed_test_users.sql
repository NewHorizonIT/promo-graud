INSERT INTO "user" (
    id,
    username,
    email,
    password,
    role,
    type_id
)
SELECT
    i,
    'test_user_' || i,
    'test_user' || i || '@gmail.com',
    '$2a$10$e8R1/m6...hash_pass',
    'USER',
    CASE
        WHEN i % 2 = 0 THEN 1
        ELSE 2
    END
FROM generate_series(4, 10000) AS i
ON CONFLICT DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('"user"', 'id'),
    COALESCE((SELECT MAX(id) FROM "user"), 1)
);
