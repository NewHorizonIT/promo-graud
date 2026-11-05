# ADR-008: Xử lý khi phân phối thất bại

**Status:** Accepted  
**Area:** Batch Generation

## Context
Khi gửi mã qua Email hoặc Webhook bị thất bại, cần xác định hệ thống xử lý thế nào.

## Decision
Hệ thống **retry tối đa 3 lần**. Nếu vẫn thất bại sau 3 lần thì báo lỗi cho admin.

## Consequences
- Admin cần có màn hình theo dõi các lần gửi thất bại
- Cần log đầy đủ lý do thất bại để admin xử lý
- Cần cơ chế retry thủ công cho admin nếu muốn gửi lại
