-- V32__Grant_Organizer_Verify_Permission.sql

-- 1. Add the 'organizer:verify' menu item (type 2 for permission/button)
-- Following V23 pattern, pid=0 for such entries. `name` should be unique.
-- `menu_sort` chosen to be after V23's entries (100-105).
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES
(0, 0, 2, 'Verify Organizer', 'OrganizerVerify', NULL, 106, 'el-icon-check', 'organizerVerifyPath', b'0', b'0', b'0', 'organizer:verify', 'admin', 'admin', NOW(), NOW());

-- 2. Assign the 'organizer:verify' menu/permission to the 'ORGANIZER' role
-- This uses a subquery approach to get IDs, similar to V23.
-- It assumes the 'ORGANIZER' role and the new menu item exist.
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'organizer:verify' AND sm.name = 'OrganizerVerify' AND sm.type = 2 AND sm.pid = 0 LIMIT 1),
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'ORGANIZER' LIMIT 1);
