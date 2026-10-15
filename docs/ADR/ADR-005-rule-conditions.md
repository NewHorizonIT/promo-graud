# ADR-005: Số điều kiện trong 1 rule

**Status:** Accepted  
**Area:** Rule Engine

## Context
Cần xác định 1 rule có thể kết hợp nhiều điều kiện (VIP + giờ vàng + đơn trên 300k) hay chỉ có 1 điều kiện duy nhất.

## Decision
Mỗi voucher chỉ gắn với **1 rule duy nhất**, rule không kết hợp nhiều điều kiện.

## Consequences
- Logic Rule Engine đơn giản hơn đáng kể
- Admin muốn tạo rule phức tạp cần tạo nhiều voucher riêng biệt
- Giảm rủi ro cấu hình sai khi kết hợp nhiều điều kiện
