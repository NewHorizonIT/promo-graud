# ADR-010: Định nghĩa gian lận

**Status:** Accepted  
**Area:** Anti-Fraud

## Context
Cần xác định rõ hành vi nào được coi là gian lận để hệ thống phát hiện và xử lý đúng.

## Decision
Hệ thống phát hiện **3 loại gian lận**:
1. Dùng nhiều tài khoản để lấy nhiều mã
2. Dùng VPN thay đổi IP liên tục
3. Dùng script/bot tự động

## Consequences
- Cần theo dõi hành vi theo IP, tần suất request, và pattern tài khoản
- Không xử lý các hình thức gian lận khác (VD: mua bán mã)
