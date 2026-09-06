-- =====================================================================
-- Asir Health Cluster - Enterprise Risk Management System (ERMS)
-- تجمع عسير الصحي - نظام إدارة المخاطر المؤسسية
-- PostgreSQL 15+ schema. Normalized to 3NF.
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ---------------------------------------------------------------------
-- 1. IDENTITY & ACCESS
-- ---------------------------------------------------------------------

CREATE TABLE roles (
    id              SMALLSERIAL PRIMARY KEY,
    code            VARCHAR(30)  NOT NULL UNIQUE,   -- ADMIN, MANAGER, RISK_OWNER, USER
    name_en         VARCHAR(100) NOT NULL,
    name_ar         VARCHAR(100) NOT NULL,
    description     TEXT
);

CREATE TABLE organizations (
    id              SERIAL PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,      -- e.g. HHC, CL(AS)
    name_en         VARCHAR(200) NOT NULL,
    name_ar         VARCHAR(200) NOT NULL,
    org_type        VARCHAR(20)  NOT NULL CHECK (org_type IN ('HOLDING','CLUSTER')),
    parent_id       INTEGER REFERENCES organizations(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sectors (                                  -- القطاع/التجمع
    id              SERIAL PRIMARY KEY,
    organization_id INTEGER NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name_en         VARCHAR(200) NOT NULL,
    name_ar         VARCHAR(200) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name_ar)
);

CREATE TABLE departments (                               -- الإدارة/القسم/الوحدة
    id              SERIAL PRIMARY KEY,
    sector_id       INTEGER REFERENCES sectors(id) ON DELETE CASCADE,
    name_en         VARCHAR(200) NOT NULL,
    name_ar         VARCHAR(200) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (sector_id, name_ar)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    job_title       VARCHAR(200),
    role_id         SMALLINT NOT NULL REFERENCES roles(id),
    organization_id INTEGER REFERENCES organizations(id) ON DELETE SET NULL,
    department_id   INTEGER REFERENCES departments(id) ON DELETE SET NULL,
    preferred_lang  VARCHAR(2) NOT NULL DEFAULT 'ar' CHECK (preferred_lang IN ('ar','en')),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_org  ON users(organization_id);

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- ---------------------------------------------------------------------
-- 2. RISK TAXONOMY (from sheet "المراجع")
-- ---------------------------------------------------------------------

CREATE TABLE risk_categories (                           -- مجال الخطر الرئيسي
    id              SERIAL PRIMARY KEY,
    name_ar         VARCHAR(200) NOT NULL UNIQUE,
    name_en         VARCHAR(200),
    description_ar  TEXT
);

CREATE TABLE risk_subcategories (                         -- مجال الخطر الفرعي
    id              SERIAL PRIMARY KEY,
    category_id     INTEGER NOT NULL REFERENCES risk_categories(id) ON DELETE CASCADE,
    name_ar         VARCHAR(300) NOT NULL,
    name_en         VARCHAR(300),
    description_ar  TEXT,
    UNIQUE (category_id, name_ar)
);

CREATE TABLE impact_areas (                               -- مناطق الأثر (24 عمود في السجل الأصلي)
    id              SERIAL PRIMARY KEY,
    name_ar         VARCHAR(200) NOT NULL UNIQUE,
    name_en         VARCHAR(200),
    display_order   SMALLINT NOT NULL DEFAULT 0
);

-- ---------------------------------------------------------------------
-- 3. RISK REGISTER (sheet "سجل المخاطر")
-- ---------------------------------------------------------------------

CREATE TABLE risks (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    risk_code                   VARCHAR(30) NOT NULL UNIQUE,        -- رمز الخطر e.g. CL(AS).R-1
    organization_id             INTEGER NOT NULL REFERENCES organizations(id),
    sector_id                   INTEGER REFERENCES sectors(id),
    department_id               INTEGER REFERENCES departments(id),
    risk_owner_id                UUID REFERENCES users(id),
    risk_owner_title             VARCHAR(200),                      -- free text if owner not a system user
    identification_date         DATE NOT NULL DEFAULT CURRENT_DATE,
    risk_statement               TEXT NOT NULL,                     -- وصف الخطر
    root_causes                  TEXT,                              -- مسببات الخطر
    category_id                  INTEGER REFERENCES risk_categories(id),
    subcategory_id               INTEGER REFERENCES risk_subcategories(id),

    likelihood_score             SMALLINT CHECK (likelihood_score BETWEEN 1 AND 5),   -- LI#
    impact_score                 SMALLINT CHECK (impact_score BETWEEN 1 AND 5),        -- IM# (max of impact area scores)
    inherent_risk_rating          VARCHAR(20),                       -- منخفض/متوسط/عالي/حرج (computed)

    control_environment_sufficiency VARCHAR(20),                    -- ضعيف/مقبول/جيد (computed from control questions)
    residual_risk_priority        VARCHAR(20),                       -- أولوية 1..10 (computed)
    residual_risk_level           VARCHAR(20),                       -- مستوى 1..4 (computed)

    status                        VARCHAR(20) NOT NULL DEFAULT 'OPEN'
                                   CHECK (status IN ('OPEN','MITIGATING','MONITORING','CLOSED')),
    is_top_risk                   BOOLEAN NOT NULL DEFAULT false,    -- ضمن سجل أهم المخاطر

    latest_update_date            DATE,
    latest_update_text            TEXT,

    created_by                    UUID REFERENCES users(id),
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risks_org        ON risks(organization_id);
CREATE INDEX idx_risks_sector     ON risks(sector_id);
CREATE INDEX idx_risks_category   ON risks(category_id);
CREATE INDEX idx_risks_owner      ON risks(risk_owner_id);
CREATE INDEX idx_risks_status     ON risks(status);
CREATE INDEX idx_risks_level      ON risks(residual_risk_level);
CREATE INDEX idx_risks_statement_trgm ON risks USING gin (risk_statement gin_trgm_ops);

CREATE TABLE risk_impact_scores (                          -- تحليل المخاطر: تقييم كل منطقة أثر تنطبق
    risk_id         UUID NOT NULL REFERENCES risks(id) ON DELETE CASCADE,
    impact_area_id  INTEGER NOT NULL REFERENCES impact_areas(id) ON DELETE CASCADE,
    score           SMALLINT NOT NULL CHECK (score BETWEEN 1 AND 5),
    PRIMARY KEY (risk_id, impact_area_id)
);

CREATE TABLE controls (                                     -- البيئة الرقابية
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    control_code              VARCHAR(30) NOT NULL UNIQUE,     -- رمز الضابط CTR-CL(AS).R-1
    risk_id                    UUID NOT NULL REFERENCES risks(id) ON DELETE CASCADE,
    description                TEXT,
    control_type               VARCHAR(20) CHECK (control_type IN ('PREVENTIVE','DETECTIVE','CORRECTIVE','NONE')),
    is_activated_approved      BOOLEAN NOT NULL DEFAULT false,   -- هل الضوابط مفعلة ومعتمدة
    is_shared_implemented      BOOLEAN NOT NULL DEFAULT false,   -- هل تمت المشاركة والتطبيق
    is_fully_committed         BOOLEAN NOT NULL DEFAULT false,   -- هل هناك التزام تام
    is_effective               BOOLEAN NOT NULL DEFAULT false,   -- هل فعّالة دون فشل
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_controls_risk ON controls(risk_id);

CREATE TABLE mitigation_plans (                             -- خطط التخفيف
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    risk_id         UUID NOT NULL REFERENCES risks(id) ON DELETE CASCADE,
    description     TEXT NOT NULL,
    owner_id        UUID REFERENCES users(id),
    due_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED'
                    CHECK (status IN ('PLANNED','IN_PROGRESS','DONE','OVERDUE','CANCELLED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mitigation_risk ON mitigation_plans(risk_id);

CREATE TABLE risk_updates (                                  -- سجل تحديثات الخطر (آخر تحديث)
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    risk_id         UUID NOT NULL REFERENCES risks(id) ON DELETE CASCADE,
    update_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    update_text     TEXT NOT NULL,
    updated_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risk_updates_risk ON risk_updates(risk_id);

-- ---------------------------------------------------------------------
-- 4. FILES, NOTIFICATIONS, AUDIT, AI
-- ---------------------------------------------------------------------

CREATE TABLE attachments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type     VARCHAR(30) NOT NULL,      -- 'risk', 'control', 'mitigation_plan'
    entity_id       UUID NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    mime_type       VARCHAR(100),
    file_size       INTEGER,
    uploaded_by     UUID REFERENCES users(id),
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_attachments_entity ON attachments(entity_type, entity_id);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    message         TEXT,
    type            VARCHAR(30) NOT NULL DEFAULT 'INFO' CHECK (type IN ('INFO','WARNING','CRITICAL')),
    entity_type     VARCHAR(30),
    entity_id       UUID,
    is_read         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    action          VARCHAR(50) NOT NULL,          -- CREATE, UPDATE, DELETE, LOGIN, EXPORT ...
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       VARCHAR(100),
    old_value       JSONB,
    new_value       JSONB,
    ip_address      VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_user   ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);

CREATE TABLE ai_queries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    provider        VARCHAR(20) NOT NULL,          -- claude, openai, gemini, ollama
    prompt          TEXT NOT NULL,
    response        TEXT,
    context_entity  VARCHAR(30),
    context_id      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ai_queries_user ON ai_queries(user_id);

-- ---------------------------------------------------------------------
-- 5. TRIGGERS: updated_at maintenance
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at   BEFORE UPDATE ON users   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_risks_updated_at  BEFORE UPDATE ON risks  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_controls_updated_at BEFORE UPDATE ON controls FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_mitigation_updated_at BEFORE UPDATE ON mitigation_plans FOR EACH ROW EXECUTE FUNCTION set_updated_at();
