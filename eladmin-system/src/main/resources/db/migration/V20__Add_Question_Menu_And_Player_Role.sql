-- Create player role if not exists
INSERT IGNORE INTO sys_role (name, level, description, data_scope, create_by, update_by,
                                 create_time, update_time)
values ('Player', 3, 'Player role with self-assessment permissions', '本级', 'admin', 'admin', NOW(), NOW());

-- Insert question:list permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon,
                          path, i_frame, cache, hidden, permission, create_by, update_by,
                          create_time, update_time)
VALUES (NULL, 0, 2, 'Question View', 'Question View', NULL, 999, 'question', 'question', b'0', b'0', b'0', 'question:list',
        'admin', 'admin', NOW(), NOW());

-- Insert player:list permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon,
                          path, i_frame, cache, hidden, permission, create_by, update_by,
                          create_time, update_time)
VALUES (NULL, 0, 2, 'Player View', 'Player View', NULL, 999, 'people', 'player', b'0', b'0', b'0', 'player:list', 'admin',
        'admin', NOW(), NOW());

-- Assign permissions to player role
INSERT INTO sys_roles_menus (menu_id, role_id)
Values ((select menu_id from sys_menu where name = 'Question View'),
       (select role_id from sys_role where name = 'Player'));

-- Assign permissions to player role
INSERT INTO sys_roles_menus (menu_id, role_id)
Values ((select menu_id from sys_menu where name = 'Player View'),
       (select role_id from sys_role where name = 'Player'));