# Database Schema

## 1. Overview

Hệ thống sử dụng quan hệ database với 13 bảng chính, tổ chức xoay quanh 3 nhóm:
- **User:** Quản lý tài khoản và phân loại khách hàng
- **Campaign & Voucher:** Quản lý chiến dịch, rule và mã giảm giá
- **Order & Fraud:** Quản lý đơn hàng, lịch sử phân phối và kiểm soát gian lận

---

## 2. Entity Relationship Diagram (ERD)

```
Type_of_User <── User ──< User_voucher >── Voucher ──< Voucher_redemption
                  │                           │              │
                  └──< Order >── Order_Item   │           Fraud_log
                                    │         └── Rule_Campaign >── Campaign
                              Type_of_Product              │
                                    │               Distribution_Log
                                 Product
```

---

## 3. Tables

### User
Quản lý thông tin tài khoản người dùng.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| username | varchar | Tên đăng nhập |
| email | varchar | Email |
| password | varchar | Mật khẩu (đã hash BCrypt) |
| role | varchar | `admin` hoặc `user` |
| type_id | int (FK) | Loại khách hàng → Type_of_User |
| created_at | timestamp | Thời điểm tạo tài khoản |

---

### Type_of_User
Phân loại khách hàng theo mức chi tiêu.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| type | varchar | `binh_thuong`, `dong`, `bac`, `vang` |
| threshold | decimal | Ngưỡng chi tiêu để đạt loại (VD: 1.000.000đ) |

---

### Campaign
Quản lý các chiến dịch khuyến mãi.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| name | varchar | Tên chiến dịch |
| start_time | timestamp | Thời gian bắt đầu |
| end_time | timestamp | Thời gian kết thúc |
| promotion_budget | decimal | Ngân sách tối đa cho khuyến mãi (VD: 50.000.000đ) |
| status | varchar | `UPCOMING`, `ACTIVE`, `ENDED` — DEFAULT `UPCOMING` |
| created_at | timestamp | Thời điểm tạo |

> `revenue` đã bỏ — tính động từ bảng Order khi cần để tránh write contention.

---

### Rule_Campaign
Quy tắc khuyến mãi gắn với chiến dịch.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| name | varchar | Tên rule |
| campaign_id | int (FK) | Chiến dịch → Campaign |
| type_of_rule | varchar | `PERCENTAGE`, `FIXED_AMOUNT`, `TIERED`, `BUY_X_GET_Y`, `FLASH_SALE` |
| value | decimal | Giá trị giảm (số tiền hoặc %) |
| max_discount_value | decimal | Mức giảm tối đa |
| min_order_value | decimal | Giá trị đơn hàng tối thiểu để áp dụng |
| type_of_user | varchar | Loại khách hàng được áp dụng |
| start_time | timestamp | Giờ bắt đầu có hiệu lực (khung giờ vàng) |
| end_time | timestamp | Giờ kết thúc có hiệu lực |
| payload | text | JSON lưu dữ liệu đặc thù theo loại rule (xem bên dưới) |
| status | varchar | Trạng thái rule |

**Cấu trúc payload theo từng loại rule:**
```json
// mua_x_tang_y
{ "product_buy": { "id": 1, "quantity": 2 }, "product_get": { "id": 5, "quantity": 1 } }

// tiered
{ "tiers": [
    { "min_order_value": 200000, "discount_value": 20000 },
    { "min_order_value": 500000, "discount_value": 70000 },
    { "min_order_value": 1000000, "discount_value": 200000 }
]}
```

---

### Voucher
Mã giảm giá phát hành theo chiến dịch. Hỗ trợ 2 loại: Generic (dùng chung) và Unique (riêng từng người).

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| code | varchar | Mã giảm giá (unique) — **INDEX** |
| type | varchar | `generic` hoặc `unique` |
| rule_id | int (FK) | Rule áp dụng → Rule_Campaign |
| quantity | int | Tổng số lượt sử dụng |
| quantity_remain | int | Số lượt còn lại |
| limit_client | int | Số lần 1 user được dùng |
| expired_at | timestamp | Hạn sử dụng |
| distribution_channel | varchar | Kênh phân phối: `email`, `webhook` |
| status | varchar | Trạng thái voucher |
| created_at | timestamp | Thời điểm tạo |

**Hành vi theo từng loại:**

| | Generic | Unique |
|--|---------|--------|
| Số dòng / campaign | 1 dòng | N dòng (1 dòng / user) |
| quantity | Đếm ngược từ N | Luôn = 1 |
| Ai dùng được | Bất kỳ ai còn lượt | Chỉ đúng 1 user được gán |

---

### User_voucher
Lưu voucher mà user đã nhận.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| user_id | int (FK) | Người dùng → User |
| voucher_id | int (FK) | Voucher → Voucher |
| collected_at | timestamp | Thời điểm nhận mã |
| status | varchar | `chua_dung`, `da_dung` |

---

### Order
Đơn hàng của khách hàng.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| user_id | int (FK) | Người đặt → User |
| total_price | decimal | Giá trị gốc trước khi giảm (VNĐ) |
| voucher_id | int (FK) | Voucher được áp dụng → Voucher (nullable) |
| status | varchar | Trạng thái đơn hàng |
| created_at | timestamp | Thời điểm tạo đơn |

---

### Order_Item
Chi tiết sản phẩm trong đơn hàng.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| order_id | int (FK) | Đơn hàng → Order |
| product_id | int (FK) | Sản phẩm → Product |
| quantity | int | Số lượng |
| price | decimal | Giá tại thời điểm đặt hàng |

---

### Product
Danh mục sản phẩm.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| name | varchar | Tên sản phẩm |
| images | text | Đường dẫn ảnh |
| description | text | Mô tả |
| price | decimal | Giá bán |
| type_id | int (FK) | Loại sản phẩm → Type_of_Product |

---

### Type_of_Product
Phân loại sản phẩm.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| type | varchar | Loại sản phẩm |

---

### Voucher_redemption
Lịch sử sử dụng voucher — dùng cho chống gian lận và audit.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| order_id | int (FK) | Đơn hàng → Order |
| ip_address | varchar | IP của request (IPv4/IPv6) — **INDEX** |
| device_fingerprint | varchar | Dấu vân tay thiết bị |
| status_of_order | varchar | Trạng thái kết quả tại thời điểm ghi |
| reason | text | Lý do thất bại nếu bị từ chối (nội bộ, không expose cho user) |
| created_at | timestamp | Thời điểm attempt |

---

### Distribution_Log *(mới)*
Theo dõi lịch sử phân phối mã qua Email/Webhook, phục vụ retry và báo lỗi cho admin.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| voucher_id | int (FK) | Voucher → Voucher |
| channel | varchar | `email`, `webhook` |
| recipient | varchar | Email hoặc Webhook URL nhận mã |
| status | varchar | `pending`, `success`, `failed` |
| retry_count | int | Số lần đã retry (0 = chưa retry, tối đa 3) |
| created_at | timestamp | Thời điểm log được tạo (khi queue lần đầu) |
| last_attempt_at | timestamp | Thời điểm gửi/retry gần nhất |
| error_message | text | Lý do thất bại (nếu có) |

---

### Fraud_log *(mới)*
Lưu danh sách IP và user bị block do hành vi gian lận, phục vụ admin xem và audit.

| Column | Type | Description |
|--------|------|-------------|
| id | int (PK) | Auto increment |
| user_id | int (FK) | Người dùng bị block → User (nullable) |
| ip_address | varchar | IP bị block |
| device_fingerprint | varchar | Dấu vân tay thiết bị (nếu có) |
| voucher_code | varchar | Mã voucher đang được redeem khi phát hiện gian lận (nullable) |
| reason | varchar | `rate_limit`, `multiple_accounts`, `vpn_detected`, `bot_script`, `blocked_ip` |
| blocked_at | timestamp | Thời điểm bị block |

---

## 4. Indexes

| Bảng | Cột | Lý do |
|------|-----|-------|
| Voucher | code | Query theo mã khi redeem — hot path 500 req/s |
| Voucher_redemption | ip_address | Fraud detection query liên tục theo IP |

---

## 5. Relationships

| Quan hệ | Mô tả |
|---------|-------|
| User → Type_of_User | Nhiều user thuộc 1 loại khách hàng |
| User → User_voucher | 1 user nhận nhiều voucher |
| Voucher → User_voucher | 1 voucher được nhiều user nhận |
| User → Order | 1 user có nhiều đơn hàng |
| Voucher → Order | 1 voucher được dùng trong nhiều đơn (nullable — NULL nếu không dùng mã) |
| Order → Order_Item | 1 đơn có nhiều sản phẩm |
| Product → Order_Item | 1 sản phẩm xuất hiện trong nhiều đơn |
| Product → Type_of_Product | Nhiều sản phẩm thuộc 1 loại |
| Rule_Campaign → Voucher | 1 rule gắn với nhiều voucher |
| Campaign → Rule_Campaign | 1 chiến dịch có nhiều rule |
| Voucher → Distribution_Log | 1 voucher có nhiều lần gửi được log |
| Voucher_redemption → Order | Ghi lại mỗi lần redeem attempt (user/voucher tra qua Order) |
| Fraud_log | Lưu danh sách bị block để admin theo dõi |

---

## 6. Source (DBML)

```dbml
Table "User" {
  id int [pk, increment]
  username varchar
  email varchar
  password varchar
  role varchar
  type_id int
  created_at timestamp
}

Table User_voucher {
  id int [pk, increment]
  user_id int
  voucher_id int
  collected_at timestamp
  status varchar
}

Table Type_of_User {
  id int [pk, increment]
  type varchar
  threshold decimal
}

Table Product {
  id int [pk, increment]
  name varchar
  images text
  description text
  price decimal
  type_id int
}

Table Type_of_Product {
  id int [pk, increment]
  type varchar
}

Table "Order" {
  id int [pk, increment]
  user_id int
  total_price decimal
  voucher_id int [null]
  status varchar
  created_at timestamp
}

Table Order_Item {
  id int [pk, increment]
  order_id int
  product_id int
  quantity int
  price decimal
}

Table Campaign {
  id int [pk, increment]
  name varchar
  start_time timestamp
  end_time timestamp
  promotion_budget decimal
  status varchar [note: "UPCOMING | ACTIVE | ENDED"]
  created_at timestamp
}

Table Voucher {
  id int [pk, increment]
  code varchar [unique, note: "INDEX"]
  type varchar [note: "generic | unique"]
  rule_id int
  quantity int
  quantity_remain int
  limit_client int
  expired_at timestamp
  distribution_channel varchar
  status varchar
  created_at timestamp
}

Table Rule_Campaign {
  id int [pk, increment]
  name varchar
  campaign_id int
  type_of_rule varchar
  value decimal
  max_discount_value decimal
  min_order_value decimal
  type_of_user varchar
  start_time timestamp
  end_time timestamp
  payload text
  status varchar
}

Table Voucher_redemption {
  id int [pk, increment]
  order_id int
  ip_address varchar [note: "INDEX"]
  device_fingerprint varchar
  status_of_order varchar
  reason text
  created_at timestamp
}

Table Distribution_Log {
  id int [pk, increment]
  voucher_id int
  channel varchar
  recipient varchar
  status varchar
  retry_count int
  created_at timestamp
  last_attempt_at timestamp
  error_message text
}

Table Fraud_log {
  id int [pk, increment]
  user_id int
  ip_address varchar
  device_fingerprint varchar
  voucher_code varchar
  reason varchar [note: "rate_limit | multiple_accounts | vpn_detected | bot_script | blocked_ip"]
  blocked_at timestamp
}

Ref: "User".type_id > Type_of_User.id
Ref: User_voucher.user_id > "User".id
Ref: User_voucher.voucher_id > Voucher.id
Ref: "Order".user_id > "User".id
Ref: "Order".voucher_id > Voucher.id
Ref: Order_Item.order_id > "Order".id
Ref: Order_Item.product_id > Product.id
Ref: Product.type_id > Type_of_Product.id
Ref: Voucher.rule_id > Rule_Campaign.id
Ref: Rule_Campaign.campaign_id > Campaign.id
Ref: Voucher_redemption.order_id > "Order".id
Ref: Distribution_Log.voucher_id > Voucher.id
Ref: Fraud_log.user_id > "User".id
```