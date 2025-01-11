CREATE TABLE IF NOT EXISTS `sh_audit` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `action` VARCHAR(15) NOT NULL,
    `user_id` INT NOT NULL,
    `target_id` INT NULL DEFAULT NULL,
    `status` INT NULL DEFAULT NULL,
    `reason` TEXT NULL DEFAULT NULL,
    `privs` TEXT NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;