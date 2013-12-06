SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `project`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `project` ;

CREATE  TABLE IF NOT EXISTS `project` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget` ;

CREATE  TABLE IF NOT EXISTS `budget` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NOT NULL ,
  `minutes` INT NOT NULL ,
  `project_id` INT NOT NULL ,
  `parent_id` INT NULL ,
  `allow_overrun` TINYINT NOT NULL DEFAULT 0 ,
  `work_progress` INT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_budget_project1_idx` (`project_id` ASC) ,
  INDEX `fk_budget_budget1_idx` (`parent_id` ASC) ,
  CONSTRAINT `fk_budget_project1`
    FOREIGN KEY (`project_id` )
    REFERENCES `project` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_budget_budget1`
    FOREIGN KEY (`parent_id` )
    REFERENCES `budget` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `booking_template`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `booking_template` ;

CREATE  TABLE IF NOT EXISTS `booking_template` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `psp` VARCHAR(45) NOT NULL ,
  `name` VARCHAR(64) NOT NULL ,
  `type` CHAR(2) NOT NULL ,
  `description` VARCHAR(255) NULL ,
  `sales_representative` VARCHAR(45) NULL ,
  `subproject` VARCHAR(45) NULL ,
  `additional_info` VARCHAR(64) NULL ,
  `search_string` VARCHAR(512) NOT NULL ,
  `active` TINYINT NOT NULL DEFAULT 1 ,
  `budget_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_booking_template_budget1_idx` (`budget_id` ASC) ,
  CONSTRAINT `fk_booking_template_budget1`
    FOREIGN KEY (`budget_id` )
    REFERENCES `budget` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `booking`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `booking` ;

CREATE  TABLE IF NOT EXISTS `booking` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `day` DATE NOT NULL ,
  `description` VARCHAR(255) NOT NULL ,
  `sales_representative` VARCHAR(45) NOT NULL ,
  `minutes` INT NOT NULL ,
  `person` VARCHAR(45) NOT NULL ,
  `booking_template_id` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_booking_booking_template1_idx` (`booking_template_id` ASC) ,
  CONSTRAINT `fk_booking_booking_template1`
    FOREIGN KEY (`booking_template_id` )
    REFERENCES `booking_template` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user` ;

CREATE  TABLE IF NOT EXISTS `user` (
  `name` VARCHAR(45) NOT NULL ,
  `password` VARCHAR(32) NOT NULL ,
  `pw_status` TINYINT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`name`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `user_role`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user_role` ;

CREATE  TABLE IF NOT EXISTS `user_role` (
  `user_name` VARCHAR(45) NOT NULL ,
  `role` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`user_name`, `role`) ,
  INDEX `fk_user_role_user1_idx` (`user_name` ASC) ,
  CONSTRAINT `fk_user_role_user1`
    FOREIGN KEY (`user_name` )
    REFERENCES `user` (`name` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget_plan` ;

CREATE  TABLE IF NOT EXISTS `budget_plan` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `budget_id` INT NOT NULL ,
  `plan_begin` INT NOT NULL ,
  `plan_end` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_budget_plan_budget1_idx` (`budget_id` ASC) ,
  CONSTRAINT `fk_budget_plan_budget1`
    FOREIGN KEY (`budget_id` )
    REFERENCES `budget` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `budget_plan_item`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `budget_plan_item` ;

CREATE  TABLE IF NOT EXISTS `budget_plan_item` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `budget_id` INT NOT NULL ,
  `period` INT NOT NULL ,
  `minutes` INT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_budget_plan_item_budget1_idx` (`budget_id` ASC) ,
  CONSTRAINT `fk_budget_plan_item_budget1`
    FOREIGN KEY (`budget_id` )
    REFERENCES `budget` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `forecast`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `forecast` ;

CREATE  TABLE IF NOT EXISTS `forecast` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL ,
  `fiscal_year` INT NOT NULL ,
  `fc_budget_cents` INT NOT NULL ,
  `cents_per_hour` INT NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `forecast_budget_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `forecast_budget_plan` ;

CREATE  TABLE IF NOT EXISTS `forecast_budget_plan` (
  `forecast_id` INT NOT NULL ,
  `budget_plan_id` INT NOT NULL ,
  `position` INT NULL ,
  PRIMARY KEY (`forecast_id`, `budget_plan_id`) ,
  INDEX `fk_forecast_budget_plan_budget_plan1_idx` (`budget_plan_id` ASC) ,
  CONSTRAINT `fk_forecast_budget_plan_forecast1`
    FOREIGN KEY (`forecast_id` )
    REFERENCES `forecast` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_forecast_budget_plan_budget_plan1`
    FOREIGN KEY (`budget_plan_id` )
    REFERENCES `budget_plan` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
