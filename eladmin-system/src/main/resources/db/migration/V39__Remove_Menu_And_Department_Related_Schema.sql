-- Remove data_scope column from sys_role
ALTER TABLE `sys_role` DROP COLUMN `data_scope`;

-- Remove dept_id column from sys_user
-- Note: This implicitly drops any foreign key constraint on dept_id if the DB supports it,
-- or the subsequent drop of sys_dept will fail if the constraint is not handled.
-- It's generally safer to explicitly drop FK constraints if their names are known.
-- However, as sys_dept is being dropped, this should be okay.
ALTER TABLE `sys_user` DROP COLUMN `dept_id`;

-- Drop join tables first to avoid foreign key constraint issues
DROP TABLE IF EXISTS `sys_roles_menus`;
DROP TABLE IF EXISTS `sys_roles_depts`;

-- Drop main tables
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_dept`;
