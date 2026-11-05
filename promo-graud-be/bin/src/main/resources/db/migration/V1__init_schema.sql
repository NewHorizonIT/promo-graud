-- V1: Khởi tạo toàn bộ schema PromoGuard

CREATE TABLE IF NOT EXISTS type_of_user (
    id        SERIAL PRIMARY KEY,
    type      VARCHAR(20)    NOT NULL,
    -- type: normal, bronze, silver, gold
    threshold DECIMAL(15, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS "user" (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(100)  NOT NULL UNIQUE,
    email    VARCHAR(255)  NOT NULL UNIQUE,
    password VARCHAR(255)  NOT NULL,
    role     VARCHAR(10)   NOT NULL DEFAULT 'user',
    -- role: user, admin
    type_id  INT           REFERENCES type_of_user (id),
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS campaign (
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(255)   NOT NULL,
    start_time       TIMESTAMP      NOT NULL,
    end_time         TIMESTAMP      NOT NULL,
    promotion_budget DECIMAL(15, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'upcoming',
    -- status: upcoming | active | ended
    is_deleted       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rule_campaign (
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(255)   NOT NULL,
    campaign_id       INT            NOT NULL REFERENCES campaign (id),
    type_of_rule      VARCHAR(30)    NOT NULL,
    value             DECIMAL(15, 2),
    max_discount_value DECIMAL(15, 2),
    min_order_value   DECIMAL(15, 2),
    type_of_user      VARCHAR(20)    NOT NULL DEFAULT 'all',
    start_time        TIMESTAMP,
    end_time          TIMESTAMP,
    payload           JSONB,
    status            VARCHAR(20)    NOT NULL DEFAULT 'active',
     -- status: active | inactive
     is_deleted       BOOLEAN        NOT NULL DEFAULT FALSE,
     created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS voucher (
    id                   SERIAL PRIMARY KEY,
    code                 VARCHAR(50)  NOT NULL UNIQUE,
    type                 VARCHAR(10)  NOT NULL,
    rule_id              INT          NOT NULL REFERENCES rule_campaign (id),
    quantity             INT          NOT NULL,
    quantity_remain      INT          NOT NULL,
    limit_client         INT          NOT NULL DEFAULT 1,
    expired_at           TIMESTAMP    NOT NULL,
    distribution_channel VARCHAR(10)  NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'active'
    -- status: active | disabled | blocked
);

CREATE TABLE IF NOT EXISTS user_voucher (
    id           SERIAL PRIMARY KEY,
    user_id      INT         NOT NULL REFERENCES "user" (id),
    voucher_id   INT         NOT NULL REFERENCES voucher (id),
    collected_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    status       VARCHAR(20) NOT NULL DEFAULT 'unused'
    -- status: unused | used | expired
);

CREATE TABLE IF NOT EXISTS type_of_product (
    id   SERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    images      TEXT,
    description TEXT,
    price       DECIMAL(15, 2) NOT NULL,
    type_id     INT            REFERENCES type_of_product (id),
    is_deleted       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "order" (
    id          SERIAL PRIMARY KEY,
    user_id     INT            NOT NULL REFERENCES "user" (id),
    total_price DECIMAL(15, 2) NOT NULL,
    final_price DECIMAL(15, 2) NOT NULL,
    voucher_id  INT            REFERENCES voucher (id),
    status      VARCHAR(20)    NOT NULL DEFAULT 'success',
    -- status: success | failed | pending | cancelled
    is_deleted       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_item (
    id         SERIAL PRIMARY KEY,
    order_id   INT            NOT NULL REFERENCES "order" (id),
    product_id INT            NOT NULL REFERENCES product (id),
    quantity   INT            NOT NULL,
    price      DECIMAL(15, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS voucher_redemption (
    id                 SERIAL PRIMARY KEY,
    user_id            INT          NOT NULL REFERENCES "user" (id),
    order_id           INT          NOT NULL REFERENCES "order" (id),
    voucher_id         INT          NOT NULL REFERENCES voucher (id),
    ip_address         VARCHAR(45)  NOT NULL,
    device_fingerprint VARCHAR(255),
    status_of_order    VARCHAR(20)  NOT NULL,
     -- status_of_order: success | failed | pending | cancelled
    reason             TEXT
);

CREATE TABLE IF NOT EXISTS distribution_log (
    id              SERIAL PRIMARY KEY,
    voucher_id      INT          NOT NULL REFERENCES voucher (id),
    channel         VARCHAR(10)  NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'pending',
    -- status: pending | sent | failed
    retry_count     INT          NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    error_message   TEXT
);

CREATE TABLE IF NOT EXISTS fraud_log (
    id                 SERIAL PRIMARY KEY,
    user_id            INT         REFERENCES "user" (id),
    ip_address         VARCHAR(45) NOT NULL,
    device_fingerprint VARCHAR(255),
    reason             VARCHAR(50) NOT NULL,
    blocked_at         TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Indexes hot path
CREATE INDEX IF NOT EXISTS idx_voucher_code ON voucher (code);
CREATE INDEX IF NOT EXISTS idx_voucher_redemption_ip ON voucher_redemption (ip_address);
