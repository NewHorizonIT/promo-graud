# ADR-007: Trigger phân phối mã

**Status:** Accepted  
**Area:** Batch Generation

## Context
Cần xác định ai hoặc cái gì kích hoạt việc gửi mã voucher đến khách hàng.

## Decision
Admin có thể phân phối mã theo **2 cách**: nhấn tay (manual) hoặc lên lịch theo thời gian định sẵn (scheduled).

## Consequences
- Cần UI để admin chọn giữa gửi ngay hoặc lên lịch
- Cần job scheduler xử lý việc gửi theo lịch
- Không hỗ trợ trigger tự động theo sự kiện (VD: khách đăng ký lần đầu)
