-- Apply once to the existing MySQL schema before using Idempotency-Key on POST /api/sales.
CREATE TABLE IF NOT EXISTS sale_submissions (
    submission_id VARCHAR(57) PRIMARY KEY,
    fingerprint VARCHAR(64) NOT NULL,
    sale_id BIGINT NOT NULL
) ENGINE=InnoDB;
