-- Create menu items for user permissions if they don't already exist
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES 
(NULL, 0, 2, 'User List', 'UserList', NULL, 999, 'user', 'userList', b'0', b'0', b'0', 'user:list', 'admin', 'admin', NOW(), NOW()),
(NULL, 0, 2, 'User Add', 'UserAdd', NULL, 999, 'user', 'userAdd', b'0', b'0', b'0', 'user:add', 'admin', 'admin', NOW(), NOW()),
(NULL, 0, 2, 'User Edit', 'UserEdit', NULL, 999, 'user', 'userEdit', b'0', b'0', b'0', 'user:edit', 'admin', 'admin', NOW(), NOW()),
(NULL, 0, 2, 'User Delete', 'UserDelete', NULL, 999, 'user', 'userDelete', b'0', b'0', b'0', 'user:del', 'admin', 'admin', NOW(), NOW());

-- Assign user permissions to Player role
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT m.menu_id, r.role_id
FROM sys_menu m, sys_role r
WHERE m.permission = 'user:list' AND r.name = 'Player';

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT m.menu_id, r.role_id
FROM sys_menu m, sys_role r
WHERE m.permission = 'user:add' AND r.name = 'Player';

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT m.menu_id, r.role_id
FROM sys_menu m, sys_role r
WHERE m.permission = 'user:edit' AND r.name = 'Player';

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT m.menu_id, r.role_id
FROM sys_menu m, sys_role r
WHERE m.permission = 'user:del' AND r.name = 'Player';
