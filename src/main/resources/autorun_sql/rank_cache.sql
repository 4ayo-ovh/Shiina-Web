CREATE TABLE IF NOT EXISTS `sh_rank_cache` (
    user_id INT NOT NULL,
    date DATE NOT NULL,
    mode INT NOT NULL,
    `rank` INT NOT NULL, 
    PRIMARY KEY (user_id, date, mode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
