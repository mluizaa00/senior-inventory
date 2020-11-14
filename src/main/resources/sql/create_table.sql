CREATE TABLE IF NOT EXISTS `inventory_data` (
    id CHAR(36) PRIMARY KEY NOT NULL,
    player_id CHAR(36) NOT NULL,
    items TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);