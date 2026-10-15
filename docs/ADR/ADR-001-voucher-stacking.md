# ADR-001: Số voucher áp dụng trên 1 đơn hàng

**Status:** Accepted  
**Area:** Rule Engine

## Context
Cần xác định khách hàng có thể dùng nhiều voucher cùng lúc trên 1 đơn hàng không.

## Decision
Mỗi đơn hàng chỉ được áp dụng **1 voucher duy nhất**.

## Consequences
- UI phải thông báo rõ khi khách thử nhập mã thứ 2
- Đơn giản hóa logic tính toán giảm giá
- Tránh rủi ro khách stack nhiều mã gây lỗ chiến dịch
