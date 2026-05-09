-- ============================================
-- 权限体系完整修复脚本
-- 说明：确保角色数据正确，支持多角色功能
-- ============================================

USE survey_db;

-- =============================================
-- 第1步：检查当前角色数据
-- =============================================

SELECT '========== 检查 sys_role 表 ==========' as step;
SELECT id, role_code, role_name, status FROM sys_role ORDER BY id;

SELECT '========== 检查 admin 用户的角色分配 ==========' as step;
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    r.id as role_id,
    r.role_code,
    r.role_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- =============================================
-- 第2步：确保角色数据存在且正确
-- =============================================

SELECT '========== 创建/更新角色数据 ==========' as step;

-- 删除旧的角色数据（如果有）
DELETE FROM sys_role WHERE role_code IN ('ADMIN', 'PROJECT_MANAGER', 'AUDITOR', 'COLLECTOR', 'THIRD_PARTY', 'SUPER');

-- 插入正确的角色数据
INSERT INTO sys_role (role_code, role_name, permissions, sort, status, create_time, update_time) VALUES
('ADMIN', '超级管理员', '*', 1, 1, NOW(), NOW()),
('PROJECT_MANAGER', '项目经理', 'project:view,project:create,project:update,point:view,point:create,point:update,task:view', 2, 1, NOW(), NOW()),
('AUDITOR', '审核员', 'point:view,point:audit,task:view,result:view', 3, 1, NOW(), NOW()),
('COLLECTOR', '采集员', 'point:view,point:create,point:update,task:view,task:complete', 4, 1, NOW(), NOW()),
('THIRD_PARTY', '第三方协作', 'point:view,task:view', 5, 1, NOW(), NOW());

-- =============================================
-- 第3步：为 admin 用户分配 ADMIN 角色
-- =============================================

SELECT '========== 为 admin 分配角色 ==========' as step;

-- 先删除 admin 的旧角色分配
DELETE FROM sys_user_role WHERE user_id = (SELECT id FROM sys_user WHERE username = 'admin');

-- 重新分配 ADMIN 角色
INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT u.id, r.id, NOW()
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

-- =============================================
-- 第4步：验证修复结果
-- =============================================

SELECT '========== 验证 admin 用户的角色 ==========' as step;
SELECT 
    u.username,
    GROUP_CONCAT(r.role_code SEPARATOR ', ') as roles
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin'
GROUP BY u.username;

SELECT '========== 所有用户的角色分配 ==========' as step;
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    GROUP_CONCAT(r.role_code SEPARATOR ', ') as roles
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.real_name
ORDER BY u.id;

-- =============================================
-- 完成报告
-- =============================================

SELECT '========================================' as message;
SELECT '✅ 权限体系修复完成！' as message;
SELECT '========================================' as message;
SELECT '请执行以下步骤：' as next_step;
SELECT '1. 重启后端服务' as step1;
SELECT '2. 清除浏览器缓存' as step2;
SELECT '3. 重新登录 admin 账号' as step3;
SELECT '4. 查看控制台日志，确认角色映射正确' as step4;
