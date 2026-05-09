-- =============================================
-- admin 账号修复脚本
-- 功能：1. 修复密码为 BCrypt 加密哈希
--       2. 确保 admin 拥有管理员角色
-- 使用：mysql -u root -p survey_db < fix_admin_account.sql
-- =============================================

USE survey_db;

-- =============================================
-- 1. 修复密码（BCrypt 加密）
--    密码: Admin@123
--    后端使用 passwordEncoder.encode() 加密后存储
-- =============================================
UPDATE sys_user 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    update_time = NOW()
WHERE username = 'admin';

-- =============================================
-- 2. 确保 admin 拥有管理员角色
--    sys_role 表中 role_code = 'admin' 的是管理员角色
-- =============================================

-- 删除 admin 的所有已有角色分配（清除错误的多选）
DELETE ur FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
WHERE u.username = 'admin';

-- 重新分配管理员角色（role_code = 'admin'）
INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT u.id, r.id, NOW()
FROM sys_user u, sys_role r
WHERE u.username = 'admin'
  AND r.role_code = 'admin';

-- =============================================
-- 3. 验证修复结果
-- =============================================
SELECT '--- 密码修复状态 ---' as '';
SELECT u.username, 
       CASE 
         WHEN u.password LIKE '$2a$%' THEN '✅ BCrypt 已加密' 
         ELSE '❌ 未加密（明文）' 
       END as password_status,
       u.update_time as last_updated
FROM sys_user u
WHERE u.username = 'admin';

SELECT '--- 角色分配状态 ---' as '';
SELECT u.username, r.role_code, r.role_name
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

SELECT '✅ 修复完成！请使用密码 Admin@123 登录 admin 账号。' as message;
