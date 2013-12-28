CREATE TABLE domain (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(64) NOT NULL, PRIMARY KEY (id)) ENGINE = InnoDB;

INSERT INTO domain (name) VALUES ('Default Domain');

ALTER TABLE project ADD domain_id INT NOT NULL DEFAULT 1;
ALTER TABLE project ADD INDEX fk_project_domain1_idx (domain_id ASC);
ALTER TABLE project ADD CONSTRAINT fk_project_domain1 FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE forecast ADD domain_id INT NOT NULL DEFAULT 1;
ALTER TABLE forecast ADD INDEX fk_forecast_domain1_idx (domain_id ASC);
ALTER TABLE forecast ADD CONSTRAINT fk_forecast_domain1 FOREIGN KEY (domain_id) REFERENCES domain (id) ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS `domain_user` (
  `domain_id` INT NOT NULL,
  `user_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`domain_id`, `user_name`),
  INDEX `fk_domain_user_user1_idx` (`user_name` ASC),
  CONSTRAINT `fk_domain_user_domain1`
    FOREIGN KEY (`domain_id`)
    REFERENCES `domain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_domain_user_user1`
    FOREIGN KEY (`user_name`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
