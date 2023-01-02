UPDATE `pets_info` SET `version` = 2;
ALTER TABLE `Pets` ADD COLUMN `quote` VARCHAR;
COMMIT;
