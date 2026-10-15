# ADR-002: Xử lý xung đột giữa các rule

**Status:** Accepted  
**Area:** Rule Engine

## Context
Một khách hàng có thể thỏa mãn nhiều rule cùng lúc (VD: vừa VIP vừa đang Flash Sale). Cần xác định rule nào được áp dụng.

## Decision
Hệ thống tự động chọn rule có **mức giảm cao nhất** cho khách hàng.

## Consequences
- Khách hàng luôn được hưởng mức giảm tốt nhất
- Không cần admin cấu hình priority thủ công
- Cần tính toán mức giảm của tất cả rule thỏa điều kiện trước khi chọn
