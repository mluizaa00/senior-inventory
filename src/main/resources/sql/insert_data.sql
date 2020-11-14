INSERT INTO `inventory_data` VALUES (?, ?, ?, ?)
ON DUPLICATE KEY UPDATE items=?;