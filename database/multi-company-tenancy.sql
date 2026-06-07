-- PostgreSQL / Supabase migration for company-based tenant isolation.
-- Run this once before starting the updated backend.

BEGIN;

CREATE TABLE IF NOT EXISTS companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO companies (name, status)
SELECT 'Legacy Company', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM companies WHERE LOWER(name) = LOWER('Legacy Company')
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS company_id INT;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS company_id INT;

UPDATE users
SET company_id = (SELECT id FROM companies WHERE LOWER(name) = LOWER('Legacy Company'))
WHERE company_id IS NULL
  AND LOWER(email) <> LOWER('admin@corequeue.com');

UPDATE users
SET role = 'SUPER_ADMIN',
    company_id = NULL,
    status = TRUE
WHERE LOWER(email) = LOWER('admin@corequeue.com');

UPDATE tasks t
SET company_id = COALESCE(
    (SELECT u.company_id FROM users u WHERE u.id = t.created_by),
    (SELECT u.company_id FROM users u WHERE u.id = t.assigned_to),
    (SELECT id FROM companies WHERE LOWER(name) = LOWER('Legacy Company'))
)
WHERE t.company_id IS NULL;

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_users_company;

ALTER TABLE users
    ADD CONSTRAINT fk_users_company
        FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE tasks
    DROP CONSTRAINT IF EXISTS fk_tasks_company;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_company
        FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE tasks
    ALTER COLUMN company_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_company_role
    ON users(company_id, role);

CREATE INDEX IF NOT EXISTS idx_tasks_company
    ON tasks(company_id);

COMMIT;
