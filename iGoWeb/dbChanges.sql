ALTER TABLE `igo`.`restaurants` 
CHANGE COLUMN 

`restrauntid` `restrauntid` INT(11) NOT NULL AUTO_INCREMENT ;


ALTER TABLE `igo`.`restaurants` 
CHANGE COLUMN `restrauntid` `restrauntid` INT(11) NOT NULL AUTO_INCREMENT ;

ALTER TABLE `igo`.`restaurants` 
ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC);

ALTER TABLE `igo`.`hospitals` 
ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC);


ALTER TABLE `igo`.`hospitals` 
CHANGE COLUMN `name` `name` VARCHAR(200) NULL DEFAULT NULL ;

