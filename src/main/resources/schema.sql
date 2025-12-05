CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL,
    last_refreshed_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_status ON user_sessions(status);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);