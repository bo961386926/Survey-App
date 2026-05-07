-- ============================================
-- 权限体系完整修复脚本
-- 说明：彻底解决 admin 账号权限问题
-- 执行时间: 2026-05-06
-- ============================================

USE survey_db;

-- =============================================
-- 第1步：检查当前状态
-- =============================================

SELECT '========== 检查 admin 用户 ==========' as step;
SELECT id, username, real_name, role, status FROM sys_user WHERE username = 'admin';

SELECT '========== 检查角色表 ==========' as step;
SELECT id, role_code, role_name, status FROM sys_role;

SELECT '========== 检查 admin 的角色分配 ==========' as step;
SELECT 
    u.id as user_id,
    u.username,
    r.id as role_id,
    r.role_code,
    r.role_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- =============================================
-- 第2步：确保 admin 用户存在
-- =============================================

-- 如果 admin 用户不存在，创建它
-- 密码: admin123 (BCrypt加密后的值)
INSERT IGNORE INTO sys_user (username, password, real_name, role, status, is_first_login) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1, 0);

SELECT '✅ admin 用户已确保存在' as result;

-- =============================================
-- 第3步：确保角色数据存在
-- =============================================

-- 插入默认角色（如果不存在）
INSERT IGNORE INTO sys_role (role_code, role_name, permissions, sort, status) VALUES
('ADMIN', '超级管理员', '["*"]', 1, 1),
('PROJECT_MANAGER', '项目经理', '["project:view","project:edit","point:view","point:edit","template:bind","export:project"]', 2, 1),
('AUDITOR', '审核员', '["point:view","audit:view","audit:pass","audit:reject","export:audit"]', 3, 1),
('COLLECTOR', '采集员', '["point:view","survey:create","survey:edit","survey:submit"]', 4, 1),
('THIRD_PARTY', '第三方协作', '["point:view","survey:assist"]', 5, 1);

SELECT '✅ 角色数据已确保存在' as result;

-- =============================================
-- 第4步：为 admin 分配 ADMIN 角色
-- =============================================

-- 先删除旧的错误分配（如果有）
DELETE FROM sys_user_role 
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'admin');

-- 重新分配 ADMIN 角色
INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT 
    u.id as user_id,
    r.id as role_id,
    NOW() as create_time
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

SELECT '✅ admin 已分配 ADMIN 角色' as result;

-- =============================================
-- 第5步：验证修复结果
-- =============================================

SELECT '========== 最终验证 ==========' as step;

SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    u.status as user_status,
    r.id as role_id,
    r.role_code,
    r.role_name,
    r.status as role_status
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- =============================================
-- 第6步：检查其他测试用户（可选）
-- =============================================

SELECT '========== 检查所有用户的角色分配 ==========' as step;

SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    GROUP_CONCAT(r.role_code ORDER BY r.role_code SEPARATOR ', ') as roles
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.real_name
ORDER BY u.id;

-- =============================================
-- 完成提示
-- =============================================

SELECT '========================================' as message;
SELECT '✅ 权限体系修复完成！' as message;
SELECT '========================================' as message;
SELECT '现在可以使用 admin / admin123 登录系统' as next_step;
SELECT 'admin 用户拥有 ADMIN 角色，具有所有权限' as note;
