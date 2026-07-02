-- Permite excluir conta mantendo histórico financeiro anonimizado.
ALTER TABLE payment_orders
    DROP FOREIGN KEY fk_payment_orders_user;

ALTER TABLE payment_orders
    MODIFY user_id BIGINT NULL;

ALTER TABLE payment_orders
    ADD CONSTRAINT fk_payment_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;

-- Avaliações de cuidado seguem o paciente removido.
ALTER TABLE care_ratings
    DROP FOREIGN KEY fk_care_ratings_patient;

ALTER TABLE care_ratings
    ADD CONSTRAINT fk_care_ratings_patient
        FOREIGN KEY (patient_id) REFERENCES users (id) ON DELETE CASCADE;

-- Referências administrativas não bloqueiam exclusão.
ALTER TABLE users
    DROP FOREIGN KEY fk_users_login_enabled_by;

ALTER TABLE users
    ADD CONSTRAINT fk_users_login_enabled_by
        FOREIGN KEY (login_enabled_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE app_feature_flags
    DROP FOREIGN KEY fk_feature_flags_updated_by;

ALTER TABLE app_feature_flags
    ADD CONSTRAINT fk_feature_flags_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE subscription_plan_catalog
    DROP FOREIGN KEY fk_subscription_plan_catalog_updated_by;

ALTER TABLE subscription_plan_catalog
    ADD CONSTRAINT fk_subscription_plan_catalog_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;
