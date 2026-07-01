ALTER TABLE nutritionists
    ADD COLUMN formation TEXT,
    ADD COLUMN experience_years INT,
    ADD COLUMN approach VARCHAR(500),
    ADD COLUMN languages VARCHAR(128);

CREATE TABLE nutritionist_portfolio_items (
    id BIGSERIAL PRIMARY KEY,
    nutritionist_id BIGINT NOT NULL REFERENCES nutritionists (id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    summary TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_nutritionist ON nutritionist_portfolio_items (nutritionist_id, sort_order);

ALTER TABLE meal_plan_generation_jobs
    ADD COLUMN nutritionist_notes TEXT;
