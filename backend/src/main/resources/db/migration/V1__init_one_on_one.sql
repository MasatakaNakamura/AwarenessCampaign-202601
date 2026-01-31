CREATE TABLE IF NOT EXISTS one_on_one (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    organizer VARCHAR(120),
    participant VARCHAR(120),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    location VARCHAR(255),
    status VARCHAR(20),
    tags VARCHAR(255),
    notes VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_one_on_one_start_at ON one_on_one (start_at);
CREATE INDEX IF NOT EXISTS idx_one_on_one_status ON one_on_one (status);
