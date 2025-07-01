ALTER TABLE sys_user ADD COLUMN user_type VARCHAR(50) COMMENT 'User type (PLAYER, ORGANIZER, ADMIN)';

DELETE from `tool_email_config`;
INSERT INTO `tool_email_config` VALUES (1,'chanheng.sng3@gmail.com','smtp.gmail.com','03792ACAD81EDFC53D2F19C68B451FD659C12F575B1101BA','465','SRR');