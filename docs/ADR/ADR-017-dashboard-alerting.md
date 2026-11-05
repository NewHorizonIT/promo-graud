# ADR-017: Cảnh báo chủ động từ dashboard

**Status:** Accepted  
**Area:** Dashboard

## Context
Cần xác định dashboard có tự động gửi cảnh báo cho admin khi phát hiện bất thường (fraud tăng đột biến, voucher sắp hết) không.

## Decision
**Out of scope** — dashboard chỉ hiển thị dữ liệu khi admin chủ động vào xem, không có cảnh báo tự động.

## Consequences
- Giảm độ phức tạp hệ thống (không cần notification service)
- Admin cần chủ động theo dõi dashboard trong các chiến dịch lớn
- Có thể bổ sung trong phiên bản sau
