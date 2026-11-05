# ADR-012: Risk tolerance chống gian lận

**Status:** Accepted  
**Area:** Anti-Fraud

## Context
Hệ thống phát hiện gian lận có thể block nhầm người dùng thật. Cần xác định team chấp nhận rủi ro theo hướng nào.

## Decision
Ưu tiên **bảo vệ hệ thống** — thà chặn nhầm 1 người thật còn hơn để lọt 1 kẻ gian.

## Consequences
- Ngưỡng phát hiện gian lận được đặt chặt hơn
- Một số người dùng thật có thể bị block nhầm
- Chấp nhận rủi ro này vì không có tính năng khiếu nại (ADR-013)
