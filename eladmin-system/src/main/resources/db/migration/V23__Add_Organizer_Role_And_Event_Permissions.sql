-- Add 'Organizer' role
INSERT IGNORE INTO sys_role (name, level, description, data_scope, create_by, update_by, create_time, update_time)
VALUES ( 'Organizer', 2, 'Event Organizer', '全部', 'admin', 'admin', NOW(), NOW());

-- Create menu items for event permissions if they don't already exist
-- Assuming pid for top-level menu items is 0, and type 2 is for permissions.
-- Adjust menu_sort values as needed to avoid conflicts.
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES 
(0, 0, 2, 'Event List', 'EventList', NULL, 100, 'el-icon-s-order', 'eventList', b'0', b'0', b'0', 'event:list', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Event Add', 'EventAdd', NULL, 101, 'el-icon-plus', 'eventAdd', b'0', b'0', b'0', 'event:add', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Event Edit', 'EventEdit', NULL, 102, 'el-icon-edit', 'eventEdit', b'0', b'0', b'0', 'event:edit', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Event Join', 'EventJoin', NULL, 103, 'el-icon-user', 'eventJoin', b'0', b'0', b'0', 'event:join', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Event Admin', 'EventAdmin', NULL, 104, 'el-icon-setting', 'eventAdmin', b'0', b'0', b'0', 'event:admin', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Event Delete', 'EventDelete', NULL, 105, 'el-icon-delete', 'eventDelete', b'0', b'0', b'0', 'event:del', 'admin', 'admin', NOW(), NOW());

-- Assign event permissions to Organizer role
-- Ensure that the subqueries for menu_id and role_id return exactly one row.
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:list' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:add' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:edit' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:join' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:admin' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:del' LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);
