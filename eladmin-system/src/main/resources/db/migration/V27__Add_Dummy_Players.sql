SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Add 50 Dummy Players

DROP PROCEDURE IF EXISTS AddDummyPlayers;

DELIMITER //

CREATE PROCEDURE AddDummyPlayers()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE p_username VARCHAR(255);
    DECLARE p_nickname VARCHAR(255);
    DECLARE p_email VARCHAR(255);
    DECLARE p_phone VARCHAR(20);
    DECLARE player_role_id BIGINT;

    -- Get Player role_id
    SELECT role_id INTO player_role_id FROM sys_role WHERE name = 'Player' LIMIT 1;

    WHILE i <= 50 DO
        SET p_username = CONCAT('player', i);
        SET p_nickname = CONCAT('Dummy Player ', i);
        SET p_email = CONCAT('player', i, '@example.com');
        SET p_phone = CONCAT('138000000', LPAD(i, 2, '0'));

        -- Insert into sys_user (gender will be dropped by V30 later)
        INSERT INTO sys_user (username, nick_name, email, phone, password, enabled, gender, user_type, create_by, update_by, create_time, update_time, pwd_reset_time, is_admin, avatar_name, avatar_path, email_verified)
        VALUES (p_username, p_nickname, p_email, p_phone, '$2a$10$Egp1/fTw2AdS2vA1i2gPru9Wn2es8X32Y5323bV22wz8I2x23X.gC', b'1', '未知', 'PLAYER', 'admin', 'admin', NOW(), NOW(), NOW(), b'0', NULL, NULL, b'0');

        SET @last_user_id = LAST_INSERT_ID();

        -- Insert into player table (gender and date_of_birth columns don't exist when V27 runs)
        INSERT INTO player (name, description, latitude, longitude, profile_image, create_time, update_time, rate_score, user_id)
        VALUES (p_nickname, CONCAT('Description for ', p_nickname), NULL, NULL, NULL, NOW(), NOW(), FLOOR(1 + RAND() * 1000), @last_user_id);

        -- Assign 'Player' role
        IF player_role_id IS NOT NULL THEN
            INSERT INTO sys_users_roles (user_id, role_id)
            VALUES (@last_user_id, player_role_id);
        END IF;

        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- Call the procedure
CALL AddDummyPlayers();

-- Clean up the procedure
DROP PROCEDURE IF EXISTS AddDummyPlayers;

SET FOREIGN_KEY_CHECKS = 1;
