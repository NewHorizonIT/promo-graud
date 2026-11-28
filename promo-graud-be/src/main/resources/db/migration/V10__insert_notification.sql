-- 1. Bảng lưu nội dung thông báo gốc (1 sự kiện = 1 bản ghi)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng lưu danh sách người nhận và trạng thái đọc/xóa riêng của từng user (1 - N)
CREATE TABLE notification_recipients (
    id SERIAL PRIMARY KEY,
    notification_id INT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    recipient_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Đảm bảo mỗi user chỉ nhận tối đa 1 bản ghi cho cùng 1 thông báo
    CONSTRAINT uq_notification_recipient UNIQUE (notification_id, recipient_id)
);

-- 3. Index tối ưu truy vấn hiển thị danh sách thông báo của từng user
CREATE INDEX idx_notification_recipients_user_feed
    ON notification_recipients (recipient_id, is_deleted, created_at DESC);

-- Partial Index tối ưu tốc độ đếm số thông báo chưa đọc hiển thị lên icon chuông
CREATE INDEX idx_notification_recipients_unread
    ON notification_recipients (recipient_id)
    WHERE is_read = FALSE AND is_deleted = FALSE;

CREATE INDEX idx_notification_recipients_notification_id
    ON notification_recipients (notification_id);
