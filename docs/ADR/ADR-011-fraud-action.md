# ADR-011: Hành động khi phát hiện gian lận

**Status:** Accepted  
**Area:** Anti-Fraud

## Context
Khi hệ thống phát hiện hành vi gian lận, cần xác định hành động xử lý.

## Decision
**Block thẳng**, không giải thích lý do cho người dùng.

## Consequences
- Kẻ gian không biết bị phát hiện, khó tìm cách vượt qua
- Người dùng bị block nhầm không biết lý do (chấp nhận theo ADR-012)
- Không cần UI xác minh (OTP, Captcha)
