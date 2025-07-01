ALTER TABLE `player`
ADD COLUMN `gender` ENUM('MALE', 'FEMALE', 'OTHER') DEFAULT NULL COMMENT 'sex' AFTER `user_id`,
ADD COLUMN `date_of_birth` DATE DEFAULT NULL COMMENT 'birthday' AFTER `gender`;

ALTER TABLE `sys_user`
DROP COLUMN `gender`;
