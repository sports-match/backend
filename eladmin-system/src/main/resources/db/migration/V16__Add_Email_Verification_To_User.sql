-- Add email_verified column to sys_user table
ALTER TABLE `sys_user` ADD COLUMN `email_verified` BOOLEAN DEFAULT FALSE;
