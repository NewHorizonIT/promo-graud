# PromoGuard

**Smart Multi-Channel Voucher & Anti-Fraud Promotion Engine**

PromoGuard cho phép admin tạo chiến dịch khuyến mãi với rule engine linh hoạt, phân phối mã voucher đa kênh, và chống gian lận theo thời gian thực.

---

## Tech Stack

| Layer | Công nghệ |
|-------|-----------|
| Backend | Spring Boot 4 · Java 21 |
| Frontend | React 19 · TypeScript · TanStack Query · shadcn/ui |
| Database | PostgreSQL 16 (Flyway migration) |
| Cache / Lock | Redis — Upstash Serverless |
| Email | Resend / Gmail SMTP |
| Background Jobs | JobRunr · Spring Batch |
| Hosting | Railway (BE + DB) · Vercel (FE) |
| CI | GitHub Actions |

---

## Tính năng chính

- **Campaign & Rule Engine** — 5 loại rule: giảm trực tiếp, giảm phần trăm, mua X tặng Y, tiered, flash sale
- **Voucher Generation** — Generic (dùng chung) và Unique (riêng từng người), sinh hàng loạt qua Spring Batch
- **Multi-channel Distribution** — Email (Resend / SMTP) và Webhook, retry tối đa 3 lần
- **Redeem với Distributed Lock** — Redis SETNX chống race condition, FCFS tại 500 req/s
- **Anti-fraud** — Rate limiting (sliding window), multi-account detection, block im lặng
- **Analytics Dashboard** — ROI, redemption rate, fraud stats; cache Redis ≤ 5 phút

---

## Cấu trúc repo

```
promo-graud/
├── promo-graud-be/          # Spring Boot 3 — Java 21
│   └── src/main/java/
│       └── modules/
│           ├── auth/
│           ├── campaigns/
│           ├── voucher/
│           ├── orders/
│           ├── products/
│           ├── rules/
│           ├── user/
│           └── analytics/
├── promo-graud-fe/          # React + TypeScript + Vite
│   └── src/
│       ├── pages/
│       ├── components/
│       ├── features/
│       ├── services/
│       └── config/
├── docs/
│   ├── prd.md
│   ├── architecture.md
│   ├── database.md
│   ├── api_design.swagger.yml
│   └── adr/                 # 17 ADRs
├── docker-compose.yml       # PostgreSQL + Redis cho local dev
└── Makefile                 # Shortcut commands
```

---

## Bắt đầu nhanh (Local)

### Yêu cầu

- Java 21+
- Node.js 22+
- Docker & Docker Compose

### 1. Clone và cấu hình env

```bash
git clone https://github.com/NewHorizonIT/promo-graud.git
cd promo-graud

# Điền các giá trị vào .env
cp .env.example .env

# Điền VITE_API_BASE vào frontend env
cp promo-graud-fe/.env.example promo-graud-fe/.env.local
```

### 2. Khởi động PostgreSQL + Redis

```bash
make up
```

### 3. Chạy backend

```bash
make be-run
# Flyway tự chạy migration, Swagger UI: http://localhost:8080/swagger-ui.html
```

### 4. Chạy frontend

```bash
make fe-install
make fe-dev
# http://localhost:5173
```

Hoặc chạy cả hai cùng lúc:

```bash
make dev
```

---

## Makefile commands

| Command | Mô tả |
|---------|-------|
| `make up` | Khởi động Docker (PostgreSQL + Redis) |
| `make down` | Dừng Docker |
| `make be-run` | Chạy backend (dev mode) |
| `make be-test` | Chạy unit tests |
| `make be-build` | Build JAR (skip tests) |
| `make fe-dev` | Chạy frontend dev server |
| `make fe-build` | Build frontend production |
| `make fe-lint` | Chạy ESLint |
| `make dev` | `make up` + `make be-run` |

---

## Environment Variables

### Backend (`.env`)

| Biến | Mô tả |
|------|-------|
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `REDIS_URL` | Upstash Redis URL (`rediss://...`) — prod; hoặc dùng `REDIS_HOST` + `REDIS_PORT` cho local |
| `JWT_SECRET` | Secret ký JWT (≥ 64 bytes, base64) |
| `JWT_REFRESH_SECRET` | Secret ký refresh token |
| `JWT_VALID_DURATION` | Thời hạn access token (giây, default: 3600) |
| `JWT_REFRESH_DURATION` | Thời hạn refresh token (giây, default: 72000) |
| `CORS_ALLOWED_ORIGINS` | URL frontend, cách nhau bởi dấu phẩy |
| `ADMIN_USERNAME` | Tài khoản admin khởi tạo |
| `ADMIN_PASSWORD` | Mật khẩu admin |
| `ADMIN_EMAIL` | Email admin |

Sinh JWT secret:

```bash
openssl rand -base64 64
```

### Frontend (`promo-graud-fe/.env.local`)

| Biến | Mô tả |
|------|-------|
| `VITE_API_BASE` | URL backend API, ví dụ `https://api.example.com/api/v1` |

---

## CI/CD

| Branch | Trigger | Kết quả |
|--------|---------|---------|
| `develop` (push) | GitHub Actions CI | Build + Checkstyle + Test → auto-deploy **staging** (Railway + Vercel) |
| `main` (push) | GitHub Actions CI | Build + Checkstyle + Test → auto-deploy **production** (Railway + Vercel) |

CI pipeline: `.github/workflows/ci.yml`

---

## API Documentation

Swagger UI (khi chạy local): `http://localhost:8080/swagger-ui.html`

OpenAPI spec: `docs/api_design.swagger.yml`

Base URL: `/api/v1`

---

## Git Workflow

```
main
 └── develop
      └── feature/[module]-[feature-name]
```

- Conventional Commits: `feat:`, `fix:`, `chore:`, `test:`, `docs:`
- Không push thẳng lên `develop`/`main`
- Bắt buộc PR + 1 reviewer + CI pass trước khi merge

---

## Tài liệu

| File | Nội dung |
|------|---------|
| `docs/architecture.md` | Kiến trúc, component diagram, sequence diagram |
| `docs/database.md` | Schema đầy đủ, ERD, DBML |
| `docs/api_design.swagger.yml` | API contract (OpenAPI 3.0) |
| `docs/adr/` | 17 Architecture Decision Records |
| `docs/prd.md` | Product Requirements Document |
