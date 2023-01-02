ALTER TABLE `People` ADD COLUMN `quote` VARCHAR;
UPDATE `people_info` SET `version` = 2;
