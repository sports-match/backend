-- Insert player-answer:list permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES (NULL, 0, 2, 'Player Answer List', 'PlayerAnswerList', NULL, 999, 'eye', 'playerAnswerList', b'0', b'0', b'0', 'player-answer:list', 'admin', 'admin', NOW(), NOW());

-- Insert player-answer:create permission (for submitSelfAssessment)
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES (NULL, 0, 2, 'Player Answer Create Batch', 'PlayerAnswerCreateBatch', NULL, 999, 'form', 'playerAnswerCreateBatch', b'0', b'0', b'0', 'player-answer:create', 'admin', 'admin', NOW(), NOW());

-- Insert player-answer:add permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES (NULL, 0, 2, 'Player Answer Add', 'PlayerAnswerAdd', NULL, 999, 'plus', 'playerAnswerAdd', b'0', b'0', b'0', 'player-answer:add', 'admin', 'admin', NOW(), NOW());

-- Insert player-answer:edit permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES (NULL, 0, 2, 'Player Answer Edit', 'PlayerAnswerEdit', NULL, 999, 'edit', 'playerAnswerEdit', b'0', b'0', b'0', 'player-answer:edit', 'admin', 'admin', NOW(), NOW());

-- Insert player-answer:del permission
INSERT IGNORE INTO sys_menu (pid, sub_count, type, title, name, component, menu_sort, icon, path, i_frame, cache, hidden, permission, create_by, update_by, create_time, update_time)
VALUES (NULL, 0, 2, 'Player Answer Delete', 'PlayerAnswerDelete', NULL, 999, 'delete', 'playerAnswerDelete', b'0', b'0', b'0', 'player-answer:del', 'admin', 'admin', NOW(), NOW());

-- Assign player-answer permissions to Player role
INSERT INTO sys_roles_menus (menu_id, role_id)
SELECT menu_id, (SELECT role_id FROM sys_role WHERE name = 'Player')
FROM sys_menu
WHERE permission = 'player-answer:list';

INSERT INTO sys_roles_menus (menu_id, role_id)
SELECT menu_id, (SELECT role_id FROM sys_role WHERE name = 'Player')
FROM sys_menu
WHERE permission = 'player-answer:create';

INSERT INTO sys_roles_menus (menu_id, role_id)
SELECT menu_id, (SELECT role_id FROM sys_role WHERE name = 'Player')
FROM sys_menu
WHERE permission = 'player-answer:add';

INSERT INTO sys_roles_menus (menu_id, role_id)
SELECT menu_id, (SELECT role_id FROM sys_role WHERE name = 'Player')
FROM sys_menu
WHERE permission = 'player-answer:edit';

INSERT INTO sys_roles_menus (menu_id, role_id)
SELECT menu_id, (SELECT role_id FROM sys_role WHERE name = 'Player')
FROM sys_menu
WHERE permission = 'player-answer:del';
