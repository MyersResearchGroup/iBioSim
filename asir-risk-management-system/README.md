# Asir Health Cluster — Enterprise Risk Management System (ERMS)

نظام إدارة المخاطر المؤسسية لتجمع عسير الصحي — a full-stack rebuild of the
cluster's Excel-based risk register (`سجل مخاطر تجمع عسير الصحي`) as a
web application: risk register CRUD, automatic likelihood/impact/residual
scoring, control tracking, mitigation plans, role-based dashboards, Excel/PDF
export, audit trail, notifications, and an AI assistant that can suggest
mitigation plans using Claude, OpenAI, Google Gemini, or a fully offline
local Llama model (via Ollama).

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for the ERD, tech stack rationale,
risk-scoring logic, and full API surface.

## Project layout

```
asir-risk-management-system/
├── db/                  # schema.sql (DDL) + seed.sql (reference data & demo risks)
├── backend/             # Node.js/Express REST API
├── frontend/            # Standalone HTML/JS SPA (no build step, no CDN dependency)
├── docker-compose.yml
├── .env.example
└── .github/workflows/   # CI: backend tests + Docker image builds
```

## 1. Quick start with Docker (recommended)

```bash
cd asir-risk-management-system
cp .env.example .env
# Edit .env: set DB_PASSWORD, JWT_ACCESS_SECRET, JWT_REFRESH_SECRET
# (generate secrets with: openssl rand -hex 32)

docker compose up --build
```

- Frontend: http://localhost:8080
- Backend API: http://localhost:4000/api
- Postgres: localhost:5432

The database container automatically runs `db/schema.sql` then
`db/seed.sql` on first boot. Demo login:

| Email | Password | Role |
|---|---|---|
| admin@asirhealth.sa | ChangeMe123! | Administrator |
| erm.manager@asirhealth.sa | ChangeMe123! | Manager |
| risk.owner@asirhealth.sa | ChangeMe123! | Risk Owner |

**Change these passwords immediately in any non-throwaway environment** —
see `db/seed.sql` for the bcrypt hash used and `POST /api/auth/change-password`.

## 2. Manual local setup (without Docker)

### Database
```bash
createdb asir_erms
psql -d asir_erms -f db/schema.sql
psql -d asir_erms -f db/seed.sql
```

### Backend
```bash
cd backend
npm install
cp ../.env.example .env   # fill in DB_HOST=localhost etc.
npm run dev                # nodemon, http://localhost:4000
```

### Frontend
The frontend is fully static — no build step. Serve it with any web server
(it must be served over http(s), not opened as a `file://` URL, because it
uses ES modules):

```bash
cd frontend
python3 -m http.server 8080
# or: npx serve .
```

Open http://localhost:8080. If your backend isn't on the default
`http://localhost:4000/api`, edit `frontend/js/config.js`.

## 3. Connecting an AI provider

The AI assistant (chat widget + "Suggest Mitigations" button on each risk)
works with **any one** of four backends, selected via `AI_PROVIDER` in `.env`:

| `AI_PROVIDER` | Notes |
|---|---|
| `claude` | Set `CLAUDE_API_KEY` (console.anthropic.com). Uses the Messages API. |
| `openai` | Set `OPENAI_API_KEY`. |
| `gemini` | Set `GEMINI_API_KEY` (Google AI Studio) or point `GEMINI_BASE_URL` at a Vertex AI endpoint for Google Cloud. |
| `ollama` | No API key — runs fully offline against a local model. Install [Ollama](https://ollama.com), run `ollama pull llama3.1`, then set `OLLAMA_BASE_URL` (default `http://localhost:11434/api/generate`). |
| `none` (default) | AI features return a clear "not configured" error instead of failing silently. |

No code changes are needed to switch providers — restart the backend after
editing `.env`. See `backend/src/utils/aiProvider.js` for the adapter.

## 4. Running tests

```bash
cd backend
npm test
```

Covers the risk-scoring matrix (`riskMatrix.js`) — likelihood × impact →
inherent rating, control sufficiency, and residual level/priority
derivation. CI (`.github/workflows/ci-cd.yml`) additionally loads the real
schema/seed into a Postgres service container and builds both Docker images
on every push/PR.

## 5. Security notes

- Passwords hashed with bcrypt (cost 12).
- JWT access tokens (15 min default) + rotating, hashed refresh tokens
  (7 days default, revocable server-side).
- Role-based access control on every write endpoint (`ADMIN` / `MANAGER` /
  `RISK_OWNER` / `USER`).
- All input validated with Joi and sanitized against XSS before touching
  the database; all queries are parameterized (no string-built SQL).
- `helmet` security headers, CORS allowlist, and per-route rate limiting
  (stricter on `/auth/*`).
- Every create/update/delete/login/export is written to `audit_logs`.
- File uploads are restricted by MIME type and size, stored outside the
  web root, served only through an authenticated download endpoint.

## 6. What's a placeholder vs. production-ready

- **Seed users' passwords** (`ChangeMe123!`) — rotate before go-live.
- **Sidebar logo** — currently a text "A" badge; drop the official Asir
  Health Cluster logo file into `frontend/assets/` and reference it from
  `frontend/js/components/layout.js` if you want the real mark.
- **Reference data** — `db/seed.sql` transcribes the risk categories,
  subcategories, and 24 impact areas from the source workbook plus 3 demo
  risks; import the remaining ~500 historical risk rows via the `/api/risks`
  endpoint or a one-off script reading the original Excel file if needed.
