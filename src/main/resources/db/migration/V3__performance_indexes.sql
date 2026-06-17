-- Indexes adicionais para queries de performance

CREATE INDEX idx_meal_plans_user_date ON meal_plans(user_id, plan_date DESC);
CREATE INDEX idx_ai_log_created ON ai_requests_log(created_at DESC);
