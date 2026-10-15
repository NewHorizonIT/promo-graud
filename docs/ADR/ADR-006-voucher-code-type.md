# ADR-006: Loại mã voucher

**Status:** Accepted  
**Area:** Batch Generation

## Context
Hệ thống cần xác định hỗ trợ mã Generic (dùng chung) hay Unique (riêng từng người) hay cả hai.

## Decision
Hỗ trợ **cả hai loại** — Generic và Unique — phân biệt qua cột `type` trong bảng Voucher.

| Loại | Mô tả | Khi dùng |
|------|-------|---------|
| `generic` | 1 mã, nhiều user dùng chung | Flash Sale công khai |
| `unique` | N mã, mỗi user 1 mã riêng | Gửi cá nhân hóa qua Email/Webhook |

## Consequences
- Voucher table thêm cột `type` (`generic` / `unique`)
- Unique code cần Batch Generation để sinh hàng loạt mã không trùng
- User_voucher gắn từng unique code với từng user cụ thể
- Batch Generation có ý nghĩa thực sự với Unique code
