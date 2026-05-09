-- ============================================================
-- 权限系统修复脚本
-- 问题：多个 SQL 脚本使用不同大小写的 role_code（admin vs ADMIN
-- project_manager vs PROJECT_MANAGER），导致角色编码混乱。
-- 
-- 修复方案：
-- 1. 统一所有 role_code 为小写英文 + 下划线
-- 2. 合并重复角色
-- 3. 重建用户-角色关联
-- 4. 更新 sys_user.role 废弃字段
-- ============================================================

-- =================================================================
-- 第一步：查看当前角色数据
-- =================================================================
SELECT '=== 修复前角色列表 ===' as '';
SELECT id, role_code, role_name, sort FROM sys_role ORDER BY id;

SELECT '=== 修复前用户角色关联 ===' as '';
SELECT ur.user_id, u.username, ur.role_id, r.role_code
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
JOIN sys_role r ON ur.role_id = r.id
ORDER BY u.username, r.role_code;

-- =================================================================
-- 第二步：标准化 role_code（小写 + 下划线）
-- =================================================================
UPDATE sys_role SET role_code = LOWER(role_code);

-- 合并重复角色：如果存在大小写相同的不同记录，保留 ID 较小的
DELETE r1 FROM sys_role r1
INNER JOIN sys_role r2 
WHERE r1.role_code = r2.role_code 
  AND r1.id > r2.id;

-- =================================================================
-- 第三步：确保基础角色存在（标准编码）
-- =================================================================
INSERT IGNORE INTO sys_role (role_code, role_name, permissions, sort, status) VALUES
('admin', '管理员', '*', 1, 1),
('project_manager', '项目负责人', 'project:view,project:edit,point:view,point:edit,template:bind,export:project', 2, 1),
('auditor', '审核员', 'point:view,audit:view,audit:pass,audit:reject,export:audit', 3, 1),
('surveyor', '采集员', 'point:view,survey:create,survey:edit,survey:submit', 4, 1),
('collab', '第三方协作', 'point:view,survey:assist', 5, 1);

-- =================================================================
-- 第四步：确保 admin 用户拥有管理员角色
-- =================================================================
SET @admin_user_id = (SELECT id FROM sys_user WHERE username = 'admin');
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1);

-- 如果 admin 已有角色关联，清除旧关联并只保留管理员角色
DELETE FROM sys_user_role WHERE user_id = @admin_user_id;
INSERT INTO sys_user_role (user_id, role_id, create_time)
VALUES (@admin_user_id, @admin_role_id, NOW());

-- =================================================================
-- 第五步：更新 sys_user.role 废弃字段
-- =================================================================
UPDATE sys_user u
SET u.role = (
    SELECT MIN(r.id)
    FROM sys_user_role ur
    JOIN sys_role r ON ur.role_id = r.id
    WHERE ur.user_id = u.id
    LIMIT 1
);

-- =================================================================
-- 第六步：查看修复结果
-- =================================================================
SELECT '=== 修复后角色列表 ===' as '';
SELECT id, role_code, role_name, sort FROM sys_role ORDER BY id;

SELECT '=== 修复后用户角色关联 ===' as '';
SELECT ur.user_id, u.username, ur.role_id, r.role_code
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
JOIN sys_role r ON ur.role_id = r.id
ORDER BY u.username, r.role_code;

SELECT '=== 修复完成 ===' as '';
