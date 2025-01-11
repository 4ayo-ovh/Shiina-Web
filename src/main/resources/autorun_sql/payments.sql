CREATE TABLE IF NOT EXISTS `sh_payments`(
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `months` INT NOT NULL,
    `total` DECIMAL(10, 2) NOT NULL,
    `payment_id` TEXT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;