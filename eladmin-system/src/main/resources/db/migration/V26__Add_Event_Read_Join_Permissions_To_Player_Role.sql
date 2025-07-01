-- Assign 'event:list' and 'event:join' permissions to the 'Player' role.

-- Assign 'event:list' to 'Player' role
-- This assumes that a role named 'Player' exists and a permission 'event:list' exists in sys_menu.
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:list' LIMIT 1), 
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Player' LIMIT 1);

-- Assign 'event:join' to 'Player' role
-- This assumes that a role named 'Player' exists and a permission 'event:join' exists in sys_menu.
INSERT IGNORE INTO sys_roles_menus (menu_id, role_id)
SELECT 
    (SELECT sm.menu_id FROM sys_menu sm WHERE sm.permission = 'event:join' LIMIT 1), 
    (SELECT sr.role_id FROM sys_role sr WHERE sr.name = 'Player' LIMIT 1);
