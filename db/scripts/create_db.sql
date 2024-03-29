SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `domain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `domain` ;

CREATE TABLE IF NOT EXISTS `domain` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `project`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `project` ;

CREATE TABLE IF NOT EXISTS `project` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `domain_id` INT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `psp` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_project_domain1_idx` (`domain_id` ASC),
  CONSTRAINT `fk_project_domain1`
    FOREIGN KEY (`domain_id`)
    REFERENCES `domain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget` ;

CREATE TABLE IF NOT EXISTS `budget` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `minutes` INT NOT NULL,
  `project_id` INT NOT NULL,
  `parent_id` INT NULL,
  `allow_overrun` TINYINT NOT NULL DEFAULT 0,
  `work_progress` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_budget_project1_idx` (`project_id` ASC),
  INDEX `fk_budget_budget1_idx` (`parent_id` ASC),
  CONSTRAINT `fk_budget_project1`
    FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_budget_budget1`
    FOREIGN KEY (`parent_id`)
    REFERENCES `budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `booking_template`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `booking_template` ;

CREATE TABLE IF NOT EXISTS `booking_template` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `psp` VARCHAR(45) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `type` CHAR(2) NOT NULL,
  `description` VARCHAR(255) NULL,
  `sales_representative` VARCHAR(45) NULL,
  `subproject` VARCHAR(45) NULL,
  `additional_info` VARCHAR(64) NULL,
  `search_string` VARCHAR(512) NOT NULL,
  `active` TINYINT NOT NULL DEFAULT 1,
  `budget_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_booking_template_budget1_idx` (`budget_id` ASC),
  CONSTRAINT `fk_booking_template_budget1`
    FOREIGN KEY (`budget_id`)
    REFERENCES `budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `booking`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `booking` ;

CREATE TABLE IF NOT EXISTS `booking` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `day` DATE NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `sales_representative` VARCHAR(45) NOT NULL,
  `minutes` INT NOT NULL,
  `person` VARCHAR(45) NOT NULL,
  `booking_template_id` INT NOT NULL,
  `export_state` TINYINT NOT NULL DEFAULT 0,
  `modified_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_booking_booking_template1_idx` (`booking_template_id` ASC),
  CONSTRAINT `fk_booking_booking_template1`
    FOREIGN KEY (`booking_template_id`)
    REFERENCES `booking_template` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user` ;

CREATE TABLE IF NOT EXISTS `user` (
  `name` VARCHAR(45) NOT NULL,
  `password` VARCHAR(32) NOT NULL,
  `pw_status` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user_role`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_role` ;

CREATE TABLE IF NOT EXISTS `user_role` (
  `user_name` VARCHAR(45) NOT NULL,
  `role` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`user_name`, `role`),
  INDEX `fk_user_role_user1_idx` (`user_name` ASC),
  CONSTRAINT `fk_user_role_user1`
    FOREIGN KEY (`user_name`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget_plan` ;

CREATE TABLE IF NOT EXISTS `budget_plan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `budget_id` INT NOT NULL,
  `plan_begin` INT NOT NULL,
  `plan_end` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_budget_plan_budget1_idx` (`budget_id` ASC),
  CONSTRAINT `fk_budget_plan_budget1`
    FOREIGN KEY (`budget_id`)
    REFERENCES `budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget_plan_item`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget_plan_item` ;

CREATE TABLE IF NOT EXISTS `budget_plan_item` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `budget_id` INT NOT NULL,
  `period` INT NOT NULL,
  `minutes` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_budget_plan_item_budget1_idx` (`budget_id` ASC),
  CONSTRAINT `fk_budget_plan_item_budget1`
    FOREIGN KEY (`budget_id`)
    REFERENCES `budget` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `forecast`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `forecast` ;

CREATE TABLE IF NOT EXISTS `forecast` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `fiscal_year` INT NOT NULL,
  `fc_budget_cents` INT NOT NULL,
  `cents_per_hour` INT NOT NULL,
  `cents_per_hour_ifrs` INT NOT NULL DEFAULT 0,
  `domain_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_forecast_domain1_idx` (`domain_id` ASC),
  CONSTRAINT `fk_forecast_domain1`
    FOREIGN KEY (`domain_id`)
    REFERENCES `domain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `forecast_budget_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `forecast_budget_plan` ;

CREATE TABLE IF NOT EXISTS `forecast_budget_plan` (
  `forecast_id` INT NOT NULL,
  `budget_plan_id` INT NOT NULL,
  `position` INT NULL,
  PRIMARY KEY (`forecast_id`, `budget_plan_id`),
  INDEX `fk_forecast_budget_plan_budget_plan1_idx` (`budget_plan_id` ASC),
  CONSTRAINT `fk_forecast_budget_plan_forecast1`
    FOREIGN KEY (`forecast_id`)
    REFERENCES `forecast` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_forecast_budget_plan_budget_plan1`
    FOREIGN KEY (`budget_plan_id`)
    REFERENCES `budget_plan` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domain_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `domain_user` ;

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


-- -----------------------------------------------------
-- Table `project_manager`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `project_manager` ;

CREATE TABLE IF NOT EXISTS `project_manager` (
  `project_id` INT NOT NULL,
  `user_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`project_id`, `user_name`),
  INDEX `fk_project_user_user1_idx` (`user_name` ASC),
  CONSTRAINT `fk_project_user_project1`
    FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_project_user_user1`
    FOREIGN KEY (`user_name`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `resource_plan_item`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `resource_plan_item` ;

CREATE TABLE IF NOT EXISTS `resource_plan_item` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(45) NOT NULL,
  `day` DATE NOT NULL,
  `avail` CHAR(1) NOT NULL,
  `project_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_resource_plan_item_project1_idx` (`project_id` ASC),
  INDEX `fk_resource_plan_item_user1_idx` (`user_name` ASC),
  CONSTRAINT `fk_resource_plan_item_project1`
    FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_plan_item_user1`
    FOREIGN KEY (`user_name`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `resource_team`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `resource_team` ;

CREATE TABLE IF NOT EXISTS `resource_team` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `domain_id` INT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `manager` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_resource_team_user1_idx` (`manager` ASC),
  INDEX `fk_resource_team_domain1_idx` (`domain_id` ASC),
  CONSTRAINT `fk_resource_team_user1`
    FOREIGN KEY (`manager`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_team_domain1`
    FOREIGN KEY (`domain_id`)
    REFERENCES `domain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `resource_team_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `resource_team_member` ;

CREATE TABLE IF NOT EXISTS `resource_team_member` (
  `resource_team_id` INT NOT NULL,
  `user_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`resource_team_id`, `user_name`),
  INDEX `fk_resource_team_has_user_user1_idx` (`user_name` ASC),
  INDEX `fk_resource_team_has_user_resource_team1_idx` (`resource_team_id` ASC),
  CONSTRAINT `fk_resource_team_has_user_resource_team1`
    FOREIGN KEY (`resource_team_id`)
    REFERENCES `resource_team` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_team_has_user_user1`
    FOREIGN KEY (`user_name`)
    REFERENCES `user` (`name`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `system_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `system_info` ;

CREATE TABLE IF NOT EXISTS `system_info` (
  `id` VARCHAR(45) NOT NULL,
  `value` VARCHAR(255) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
