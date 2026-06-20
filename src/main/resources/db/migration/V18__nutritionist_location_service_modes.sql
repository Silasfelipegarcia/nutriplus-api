-- V18: Localidade e modalidade de atendimento

ALTER TABLE nutritionists
    ADD COLUMN service_modes VARCHAR(64) NOT NULL DEFAULT 'ONLINE,IN_PERSON' AFTER specialties,
    ADD COLUMN city VARCHAR(120) NULL AFTER service_modes,
    ADD COLUMN state_code CHAR(2) NULL AFTER city,
    ADD COLUMN neighborhood VARCHAR(120) NULL AFTER state_code,
    ADD COLUMN whatsapp_phone VARCHAR(20) NULL AFTER neighborhood;

ALTER TABLE care_relationships
    ADD COLUMN preferred_care_mode ENUM('ONLINE', 'IN_PERSON', 'EITHER') NULL AFTER source;

CREATE INDEX idx_nutritionists_state ON nutritionists(state_code);
