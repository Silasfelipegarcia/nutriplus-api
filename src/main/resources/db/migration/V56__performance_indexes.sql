-- V56: performance indexes for fdloggers and high-frequency queries
-- Addresses P99 latency spikes (10-20s) on security_events and daily_meal_checkins

-- Composite index for security_events: user timeline queries (fdloggers)
CREATE INDEX idx_security_events_user_created ON security_events(user_id, created_at DESC);

-- Composite index for security_events: filtering by event type per user
CREATE INDEX idx_security_events_user_event_type ON security_events(user_id, action);

-- Composite index for daily_meal_checkins: user + date + meal_type lookups
CREATE INDEX idx_checkins_user_date_meal_type ON daily_meal_checkins(user_id, checkin_date DESC, meal_type);

-- Index for meals: join from meal_plans (covers mealLoader.mealsForPlan queries)
CREATE INDEX idx_meals_meal_plan_id ON meals(meal_plan_id);

-- Composite index for progress_reviews: user latest-first queries
CREATE INDEX idx_progress_reviews_user_created ON progress_reviews(user_id, created_at DESC);
