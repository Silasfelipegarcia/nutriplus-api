ALTER TABLE body_measurement_sessions
    ADD COLUMN recorded_by_nutritionist_id BIGINT NULL,
    ADD CONSTRAINT fk_body_measurement_nutritionist
        FOREIGN KEY (recorded_by_nutritionist_id) REFERENCES nutritionists(id) ON DELETE SET NULL;
