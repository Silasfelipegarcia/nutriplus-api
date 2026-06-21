ALTER TABLE shopping_list_items
    ADD COLUMN swap_group VARCHAR(64) NULL AFTER alternatives_json,
    ADD COLUMN swap_options_json JSON NULL AFTER swap_group,
    ADD COLUMN market_tips_json JSON NULL AFTER swap_options_json,
    ADD COLUMN selected_swap_id VARCHAR(64) NULL AFTER market_tips_json,
    ADD COLUMN default_option_id VARCHAR(64) NULL AFTER selected_swap_id,
    ADD COLUMN recommended_option_id VARCHAR(64) NULL AFTER default_option_id;
