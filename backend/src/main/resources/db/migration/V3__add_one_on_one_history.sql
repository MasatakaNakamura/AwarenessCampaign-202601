CREATE TABLE IF NOT EXISTS one_on_one_history (
    id BIGSERIAL PRIMARY KEY,
    one_on_one_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(120),
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(1024),
    CONSTRAINT fk_one_on_one_history_one_on_one
        FOREIGN KEY (one_on_one_id) REFERENCES one_on_one(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_one_on_one_history_one_on_one_id
    ON one_on_one_history (one_on_one_id);
CREATE INDEX IF NOT EXISTS idx_one_on_one_history_changed_at
    ON one_on_one_history (changed_at);
