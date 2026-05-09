-- ============================================================
-- 角色ID修复脚本（最终版）
-- 目标：将所有 role_id 统一为 1-5 的顺序自增ID
-- 步骤：
--   1. 清空 sys_user_role（保留结构）
--   2. 清空 sys_role（保留结构）
--   3. 重新插入标准角色（ID 1-5）
--   4. 为 admin 用户重新分配管理员角色
-- ============================================================

-- 关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清空用户角色关联表
DELETE FROM sys_user_role;

-- 2. 清空角色表
DELETE FROM sys_role;

-- 3. 重置自增计数器（从1开始）
ALTER TABLE sys_role AUTO_INCREMENT = 1;

-- 4. 插入标准角色（ID 会从 1 开始自增）
INSERT INTO sys_role (role_code, role_name, permissions, sort, status, create_time, update_time) VALUES
('admin', '管理员', '*', 1, 1, NOW(), NOW()),
('project_manager', '项目负责人', 'project:view,project:edit,point:view,point:edit,template:bind,export:project', 2, 1, NOW(), NOW()),
('auditor', '审核员', 'point:view,audit:view,audit:pass,audit:reject,export:audit', 3, 1, NOW(), NOW()),
('surveyor', '采集员', 'point:view,survey:create,survey:edit,survey:submit', 4, 1, NOW(), NOW()),
('collab', '第三方协作', 'point:view,survey:assist', 5, 1, NOW(), NOW());

-- 5. 为 admin 用户分配管理员角色
SET @admin_id = (SELECT id FROM sys_user WHERE username = 'admin' LIMIT 1);
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1);

INSERT INTO sys_user_role (user_id, role_id, create_time) 
VALUES (@admin_id, @admin_role_id, NOW());

-- 6. 更新 sys_user.role 废弃字段
UPDATE sys_user SET role = NULL;
UPDATE sys_user SET role = @admin_role_id WHERE username = 'admin';

-- 7. 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 8. 验证结果
SELECT '=== 修复后角色表 ===' as '';
SELECT id, role_code, role_name FROM sys_role ORDER BY id;

SELECT '=== 修复后用户角色关联 ===' as '';
SELECT ur.user_id, u.username, ur.role_id, r.role_code
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
JOIN sys_role r ON ur.role_id = r.id;
