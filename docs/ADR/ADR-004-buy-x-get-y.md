# ADR-004: Sản phẩm tặng trong rule Mua X Tặng Y

**Status:** Accepted  
**Area:** Rule Engine

## Context
Rule Mua X Tặng Y cần xác định sản phẩm Y là gì và ai quyết định.

## Decision
Sản phẩm tặng (Y) là **sản phẩm cố định do admin chọn sẵn** khi cấu hình rule.

## Consequences
- Admin phải chỉ định sản phẩm tặng kèm khi tạo rule
- Rule Engine cần liên kết với danh mục sản phẩm
- Khách hàng không có quyền chọn quà
