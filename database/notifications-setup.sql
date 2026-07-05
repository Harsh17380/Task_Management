-- Notifications table setup
-- Run this once on your Supabase PostgreSQL database.

CREATE TABLE IF NOT EXISTS notifications (
    id          SERIAL PRIMARY KEY,
    user_id     INT         NOT NULL REFERENCES users(id),
    company_id  INT         REFERENCES companies(id),
    message     TEXT        NOT NULL,
    type        VARCHAR(50) NOT NULL DEFAULT 'INFO',
    reference_id INT,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_read
    ON notifications(user_id, is_read);

CREATE INDEX IF NOT EXISTS idx_notifications_user_created
    ON notifications(user_id, created_at DESC);
