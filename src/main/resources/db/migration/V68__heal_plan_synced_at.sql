-- Cura banners "Atualizar plano" grudados após saves de bookkeeping (ex.: meta de água no GET)
-- que avançaram updated_at sem realinhar plan_synced_at.
UPDATE nutrition_profiles
SET plan_synced_at = updated_at
WHERE plan_synced_at IS NULL
   OR plan_synced_at < updated_at;
