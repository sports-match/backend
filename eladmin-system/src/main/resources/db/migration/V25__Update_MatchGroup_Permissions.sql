-- 1. Add new 'matchgroup' permissions to sys_menu
-- Using menu_sort values 114 and 115, assuming V24 used up to 113.
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES
(0, 0, 2, 'Generate Match Group', 'MatchGroupGenerate', NULL, 114, 'el-icon-plus', 'matchGroupGenerate', b'0', b'0', b'0', 'matchgroup:generate', 'admin', 'admin', NOW(), NOW()),
(0, 0, 2, 'Read Match Group', 'MatchGroupRead', NULL, 115, 'el-icon-view', 'matchGroupRead', b'0', b'0', b'0', 'matchgroup:read', 'admin', 'admin', NOW(), NOW());

-- 2. Assign new 'matchgroup' permissions to Organizer role
-- Assign matchgroup:generate
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'matchgroup:generate' LIMIT 1), 
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);

-- Assign matchgroup:read
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'matchgroup:read' LIMIT 1), 
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Organizer' LIMIT 1);