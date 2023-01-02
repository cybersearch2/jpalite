ALTER TABLE `Pets` ADD COLUMN `quote` VARCHAR;
UPDATE `pets_info` SET `version` = 2;
