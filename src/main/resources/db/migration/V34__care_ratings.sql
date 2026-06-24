CREATE TABLE care_ratings (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    care_relationship_id    BIGINT NOT NULL,
    patient_id              BIGINT NOT NULL,
    nutritionist_id         BIGINT NOT NULL,
    stars                   TINYINT NOT NULL,
    comment                 VARCHAR(2000) NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_care_ratings_relationship UNIQUE (care_relationship_id),
    CONSTRAINT fk_care_ratings_relationship FOREIGN KEY (care_relationship_id) REFERENCES care_relationships (id),
    CONSTRAINT fk_care_ratings_patient FOREIGN KEY (patient_id) REFERENCES users (id),
    CONSTRAINT fk_care_ratings_nutritionist FOREIGN KEY (nutritionist_id) REFERENCES nutritionists (id),
    CONSTRAINT chk_care_ratings_stars CHECK (stars BETWEEN 1 AND 5)
);

CREATE INDEX idx_care_ratings_nutritionist ON care_ratings (nutritionist_id);
