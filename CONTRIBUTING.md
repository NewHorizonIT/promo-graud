# Contributing Guide

## Setup (chạy 1 lần sau khi clone)

```bash
git config core.hooksPath .githooks
```

Hook sẽ tự động format Java code bằng Spotless trước mỗi commit. Không cần làm thêm gì.

---

## Branch

```
main       ← production (merge từ develop khi release)
develop    ← integration (không commit trực tiếp)
feature/   ← feature mới
fix/       ← bug fix
chore/     ← config, tooling
```

Pattern: `feature/[module]-[mô-tả]` · `fix/[module]-[mô-tả]`

```
feature/campaign-crud-api
fix/auth-jwt-expiry
chore/testcontainers-setup
```

---

## Commit

```
<type>(<scope>): <mô tả ngắn, tiếng Anh, lowercase>
```

| Type | Dùng khi |
|------|----------|
| `feat` | thêm chức năng |
| `fix` | sửa bug |
| `test` | thêm/sửa test |
| `chore` | config, setup |
| `docs` | chỉ thay đổi docs |
| `refactor` | refactor, không đổi behavior |

Scope: `auth` · `campaign` · `voucher` · `fraud` · `notification` · `analytics` · `fe` · `infra`

```
feat(campaign): add CRUD endpoints
fix(rule-engine): correct tiered discount boundary
chore(infra): configure testcontainers
```

---

## Workflow

```
1. git checkout develop && git pull
2. git checkout -b feature/campaign-crud-api
3. code + commit (git add <file>, không dùng git add .)
4. git fetch origin && git rebase origin/develop
5. git push origin feature/campaign-crud-api
6. Tạo PR → develop, chờ CI pass + 1 approve → merge
```

---

## Pull Request

- Tiêu đề giống commit message: `feat(campaign): implement CRUD API`
- Điền đầy đủ PR template
- Self-review trước khi tạo, CI pass ở local trước khi push
- Điều kiện merge: **CI pass + 1 approve + 0 unresolved comment**

| PR của | Reviewer |
|--------|----------|
| A | B |
| B | A |
| C | A hoặc B |
| D | A |

Review trong vòng **24 giờ**. Label comment: `[BLOCK]` / `[NIT]` / `[Q]`

---

## Merge & Conflict

- `develop` ← **Squash and Merge**
- `main` ← **Merge Commit**

Conflict với develop:
```
git fetch origin
git rebase origin/develop
git push --force-with-lease
```

Conflict phức tạp → ping người liên quan, không tự resolve code của người khác.

---

## Không được

- Commit thẳng vào `develop` / `main`
- `git add .` hay commit file `.env` / secret
- Force push lên `develop` / `main`
- Merge khi CI fail hoặc chưa có approve
- Tự merge PR của chính mình
