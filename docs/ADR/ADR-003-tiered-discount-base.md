# ADR-003: Cơ sở tính Tiered Discount

**Status:** Accepted  
**Area:** Rule Engine

## Context
Khi áp dụng Tiered Discount cùng với voucher khác, cần xác định tính bậc giảm giá dựa trên giá trị nào của giỏ hàng.

## Decision
Tiered Discount luôn tính trên **giá trị gốc** của giỏ hàng, trước khi áp dụng bất kỳ giảm giá nào.

## Consequences
- Kết quả nhất quán, dễ dự đoán cho cả admin và khách hàng
- Tránh tình huống giảm giá kép làm lỗ chiến dịch ngoài dự kiến
