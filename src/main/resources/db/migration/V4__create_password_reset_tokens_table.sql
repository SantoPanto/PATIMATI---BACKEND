CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id SERIAL PRIMARY KEY,
                                                     token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_password_reset_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens (token);