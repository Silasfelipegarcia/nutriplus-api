ALTER TABLE nutrition_profiles
    ADD COLUMN plan_synced_at TIMESTAMP NULL;

-- Cura banners "Atualizar plano" grudados: a geração salva o perfil depois do plano,
-- então updated_at > plan.created_at ficava sempre verdadeiro. Alinha o marcador ao estado atual.
UPDATE nutrition_profiles np
    INNER JOIN meal_plans mp ON mp.nutrition_profile_id = np.id
SET np.plan_synced_at = np.updated_at
WHERE np.plan_synced_at IS NULL;
