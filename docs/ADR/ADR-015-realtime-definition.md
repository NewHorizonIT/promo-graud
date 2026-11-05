# ADR-015: Định nghĩa real-time của dashboard

**Status:** Accepted  
**Area:** Dashboard

## Context
Spec yêu cầu dashboard hiển thị "thời gian thực". Cần xác định mức độ cập nhật cụ thể.

## Decision
Dashboard cập nhật theo **near real-time — tối đa 5 phút/lần**.

## Consequences
- Giảm đáng kể độ phức tạp so với real-time thực sự (mỗi giây)
- Đủ dùng cho mục đích theo dõi chiến dịch
- Không phù hợp nếu cần phản ứng tức thời trong Flash Sale
