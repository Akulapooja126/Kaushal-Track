-- =====================================================================
-- KaushalTrack (rebuild) - simplified PostgreSQL schema for Neon
-- Run once: psql "$DATABASE_URL" -f schema_v2.sql
-- =====================================================================

-- 1. Courses / training programs
CREATE TABLE courses (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(150) NOT NULL UNIQUE,
    sector    VARCHAR(100)
);

-- 2. Trainees (dashboard, add / edit / delete all work on this table)
CREATE TABLE trainees (
    id               SERIAL PRIMARY KEY,
    full_name        VARCHAR(150) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    phone            VARCHAR(20),
    gender           VARCHAR(20),
    district         VARCHAR(100),
    course_id        INT NOT NULL REFERENCES courses(id),
    enrolled_on      DATE NOT NULL DEFAULT CURRENT_DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'ENROLLED'
                     CHECK (status IN ('ENROLLED', 'COMPLETED', 'DROPPED_OUT')),
    attendance_pct   NUMERIC(5,2) NOT NULL DEFAULT 0
                     CHECK (attendance_pct BETWEEN 0 AND 100),
    assessment_score NUMERIC(5,2) NOT NULL DEFAULT 0
                     CHECK (assessment_score BETWEEN 0 AND 100),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 3. Outcome after the course (one row per trainee)
CREATE TABLE outcomes (
    id              SERIAL PRIMARY KEY,
    trainee_id      INT NOT NULL UNIQUE REFERENCES trainees(id) ON DELETE CASCADE,
    outcome_status  VARCHAR(20) NOT NULL
                    CHECK (outcome_status IN ('PLACED', 'SEEKING_JOB', 'SELF_EMPLOYED')),
    -- filled when PLACED
    company_name    VARCHAR(150),
    job_role        VARCHAR(150),
    monthly_salary  NUMERIC(10,2) CHECK (monthly_salary IS NULL OR monthly_salary >= 0),
    joined_on       DATE,
    -- filled when SELF_EMPLOYED
    business_name   VARCHAR(150),
    business_type   VARCHAR(100),
    updated_by      VARCHAR(10) NOT NULL DEFAULT 'ADMIN'
                    CHECK (updated_by IN ('ADMIN', 'TRAINEE')),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 4. Completion emails. The token is the secret in the trainee's link.
CREATE TABLE outcome_requests (
    id          SERIAL PRIMARY KEY,
    trainee_id  INT NOT NULL REFERENCES trainees(id) ON DELETE CASCADE,
    token       VARCHAR(64) NOT NULL UNIQUE,
    sent_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP NOT NULL DEFAULT NOW() + INTERVAL '30 days',
    email_ok    BOOLEAN NOT NULL DEFAULT FALSE,   -- did the send succeed
    used_at     TIMESTAMP                         -- set when the trainee submits
);

CREATE INDEX idx_trainees_course  ON trainees(course_id);
CREATE INDEX idx_trainees_status  ON trainees(status);
CREATE INDEX idx_requests_trainee ON outcome_requests(trainee_id);

-- 5. Risk rules (kept as a view so they are easy to explain)
--    HIGH   : attendance < 60  OR assessment < 50
--    MEDIUM : attendance < 75  OR assessment < 65
--    LOW    : otherwise
CREATE VIEW v_trainee_risk AS
SELECT
    t.*,
    c.name AS course_name,
    o.outcome_status,
    CASE
        WHEN t.attendance_pct < 60 OR t.assessment_score < 50 THEN 'HIGH'
        WHEN t.attendance_pct < 75 OR t.assessment_score < 65 THEN 'MEDIUM'
        ELSE 'LOW'
    END AS risk_level
FROM trainees t
JOIN courses c ON c.id = t.course_id
LEFT JOIN outcomes o ON o.trainee_id = t.id;
