# ADR-009: Xử lý race condition khi redeem

**Status:** Accepted  
**Area:** Anti-Fraud

## Context
Khi Flash Sale, hàng nghìn người cùng claim voucher cuối cùng. Cần xác định cơ chế xử lý để đảm bảo không vượt quá giới hạn sử dụng.

## Decision
Áp dụng **First Come First Served (FCFS)** kết hợp Distributed Lock — ai gửi request đến server trước thì được.

## Consequences
- Sử dụng Redis Distributed Lock để tránh race condition
- Người thua nhận thông báo "Hết lượt" ngay lập tức
- Hiệu suất cao, phù hợp với yêu cầu 500 req/s
