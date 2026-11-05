# System Architecture

## 1. Tổng quan

|                  |                                                          |
| ---------------- | -------------------------------------------------------- |
| **Kiến trúc**    | Modular Monolithic                                       |
| **Repo**         | Mono Repo                                                |
| **Backend**      | Spring Boot 3 · Java 21                                  |
| **Frontend**     | React · TypeScript · TanStack · React Router · shadcn/ui |
| **Database**     | PostgreSQL                                               |
| **Cache / Lock** | Redis (Upstash Serverless)                               |
| **Email**        | Resend / Gmail SMTP                                      |

---

## 2. Module Breakdown

| Module           | Chịu trách nhiệm                         |
| ---------------- | ---------------------------------------- |
| **User**         | Đăng nhập, xác thực JWT, phân quyền      |
| **Campaign**     | Quản lý chiến dịch, Rule Engine          |
| **Voucher**      | Sinh mã, phân phối, redeem               |
| **Fraud**        | Phát hiện gian lận, Rate Limiting, block |
| **Notification** | Gửi Email, gọi Webhook                   |
| **Analytics**    | Thống kê ROI, Redemption Rate, Dashboard |

---

## 3. Component Diagram

```mermaid
graph TB
    FE["Frontend\nReact · TypeScript · TanStack · shadcn/ui"]

    subgraph BE["Backend — Modular Monolithic (Spring Boot 3 · Java 21)"]
        API["API Layer\n(REST Controllers · JWT Filter)"]

        subgraph MOD["Modules"]
            USR["User Module\nAuth · JWT"]
            CAM["Campaign Module\nRule Engine"]
            VOU["Voucher Module\nCode Gen · Redeem"]
            FRA["Fraud Module\nDetector · Rate Limiter"]
            NOT["Notification Module\nEmail · Webhook"]
            ANA["Analytics Module\nROI · Stats"]
        end
    end

    PG[("PostgreSQL")]
    RD[("Redis · Upstash")]
    EML["Resend / Gmail SMTP"]
    WHK["Webhook Endpoints"]

    FE -->|"HTTPS / REST"| API

    API --> USR
    API --> CAM
    API --> VOU
    API --> ANA

    CAM -->|"rule lookup"| VOU
    VOU -->|"fraud check"| FRA
    VOU -->|"send code"| NOT

    FRA -->|"rate limit · block"| RD
    VOU -->|"distributed lock"| RD
    USR -->|"token cache"| RD

    USR & CAM & VOU & FRA & NOT & ANA --> PG

    NOT -->|"email"| EML
    NOT -->|"push"| WHK
```

---

## 4. Luồng xử lý chính

### Redeem Voucher (Hot Path — 500 req/s)

```mermaid
sequenceDiagram
    participant C as Client
    participant API as API Layer
    participant FRA as Fraud Module
    participant VOU as Voucher Module
    participant RD as Redis
    participant DB as PostgreSQL

    C->>API: POST /vouchers/:code/redeem

    API->>FRA: Check IP · rate limit · device fingerprint
    alt Fraud detected
        FRA-->>API: BLOCKED
        API-->>C: 403 Forbidden
    end
    FRA-->>API: OK

    API->>VOU: Process redeem

    VOU->>DB: Validate voucher (tồn tại · chưa hết hạn · quantity_remain > 0)
    alt Voucher không hợp lệ
        DB-->>VOU: Not found / expired / depleted
        VOU-->>API: Error
        API-->>C: 400 Bad Request
    end

    VOU->>RD: Acquire distributed lock (key = voucher_code)
    alt Không lấy được lock
        RD-->>VOU: Timeout
        VOU-->>API: Retry
        API-->>C: 429 Too Many Requests
    end

    VOU->>DB: Re-check quantity_remain > 0 (bên trong lock)
    alt quantity_remain = 0
        VOU->>RD: Release lock
        VOU-->>API: Hết lượt
        API-->>C: 400 Voucher depleted
    end

    VOU->>DB: Tạo Order (status = pending)
    VOU->>DB: Tạo Order_Items

    alt Order thành công
        VOU->>DB: Update Order → status = thanh_cong
        VOU->>DB: Decrement quantity_remain
        VOU->>DB: Insert Voucher_redemption (success)
        VOU->>RD: Release lock
        API-->>C: 200 OK + discount amount
    else Order thất bại
        VOU->>DB: Update Order → status = that_bai
        VOU->>DB: Insert Voucher_redemption (failed)
        VOU->>RD: Release lock
        API-->>C: 400 Order failed
    end
```

### Phân phối mã

```
Admin trigger → Voucher Module → Notification Module → Email / Webhook
                                                      → Distribution_Log (DB)
```

### Dashboard

```
Client → API → Analytics Module → PostgreSQL (aggregate query)
                                → Response (≤ 5 phút cache)
```

---

## 5. Cấu trúc Mono Repo

```
promo-graud/
├── backend/          ← Spring Boot 3
│   └── src/main/java/
│       ├── module/
│       │   ├── user/
│       │   ├── campaign/
│       │   ├── voucher/
│       │   ├── fraud/
│       │   ├── notification/
│       │   └── analytics/
│       └── shared/   ← dùng chung giữa các module
├── frontend/         ← React + TypeScript
│   └── src/
│       ├── pages/
│       ├── components/
│       └── lib/
└── docs/
```

---

## 6. Môi trường

| Môi trường     | Mục đích                   | Infrastructure                      |
| -------------- | -------------------------- | ----------------------------------- |
| **Local**      | Dev từng thành viên        | Docker Compose (PostgreSQL + Redis) |
| **Staging**    | Test tích hợp, demo sprint | Railway / Render (free tier)        |
| **Production** | Nộp bài, demo giảng viên   | Railway / Render (free tier)        |

---

## 7. CI/CD Pipeline

### Công cụ

- **CI/CD:** GitHub Actions
- **Container:** Docker + Docker Compose

---

### CI — Chạy khi có PR hoặc push lên `develop`

```mermaid
flowchart LR
    A[Push / PR] --> B[Checkout code]
    B --> C[Build Docker image]
    C --> D[Run Unit Tests]
    D --> E[Run Integration Tests]
    E --> F{Pass?}
    F -->|Yes| G[✅ PR ready to merge]
    F -->|No| H[❌ Block merge]
```

**GitHub Actions workflow — CI:**

```yaml
# .github/workflows/ci.yml
on:
  pull_request:
    branches: [develop, main]
  push:
    branches: [develop]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: promoquard_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
      redis:
        image: redis:7
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: "21", distribution: "temurin" }
      - name: Build & Test
        run: ./mvnw verify
```

---

### CD — Chạy khi merge vào `develop` hoặc `main`

```mermaid
flowchart LR
    A[Merge to develop] --> B[Build Docker image]
    B --> C[Push to Registry]
    C --> D[Deploy to Staging]

    E[Merge to main] --> F[Build Docker image]
    F --> G[Push to Registry]
    G --> H[Deploy to Production]
```

**GitHub Actions workflow — CD:**

```yaml
# .github/workflows/cd.yml
on:
  push:
    branches: [develop, main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker image
        run: docker build -t promoquard-backend ./backend
      - name: Deploy to Railway
        run: railway up
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
```

---

### Git Branching Strategy

```
main          ← production · deploy khi merge từ develop
  └── develop ← staging · tích hợp tất cả feature
        ├── feature/campaign-module
        ├── feature/voucher-module
        ├── feature/fraud-module
        └── feature/analytics-module
```

| Nhánh       | Mục đích           | CI/CD                    |
| ----------- | ------------------ | ------------------------ |
| `main`      | Production         | CD → deploy production   |
| `develop`   | Staging · tích hợp | CI + CD → deploy staging |
| `feature/*` | Từng tính năng     | CI chạy khi tạo PR       |

---

## 8. Infrastructure

| Thành phần | Local                    | Staging / Production           |
| ---------- | ------------------------ | ------------------------------ |
| PostgreSQL | Docker Compose           | Railway managed DB             |
| Redis      | Docker Compose           | Upstash Serverless (free tier) |
| Backend    | `./mvnw spring-boot:run` | Docker container trên Railway  |
| Frontend   | `npm run dev`            | Docker container trên Railway  |
| Email      | Resend sandbox           | Resend / Gmail SMTP            |
