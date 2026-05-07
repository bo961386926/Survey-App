-- ============================================
-- 多角色功能数据迁移脚本
-- 说明：将 sys_user.role 单字段迁移到 sys_user_role 多对多关联表
-- 执行时间: 2026-05-06
-- ============================================

USE survey_db;

-- 1. 备份现有数据（可选，但强烈推荐）
CREATE TABLE IF NOT EXISTS sys_user_backup AS SELECT * FROM sys_user;

-- 2. 查看当前有多少用户有角色
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN role IS NOT NULL AND role > 0 THEN 1 ELSE 0 END) as users_with_role,
    SUM(CASE WHEN role IS NULL OR role = 0 THEN 1 ELSE 0 END) as users_without_role
FROM sys_user;

-- 3. 查看角色分布
SELECT 
    role,
    COUNT(*) as user_count
FROM sys_user
WHERE role IS NOT NULL AND role > 0
GROUP BY role
ORDER BY role;

-- 4. 执行迁移：将 sys_user.role 插入到 sys_user_role 表
-- 注意：使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time)
SELECT 
    id as user_id,
    role as role_id,
    NOW() as create_time
FROM sys_user
WHERE role IS NOT NULL AND role > 0;

-- 5. 验证迁移结果
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.role as old_role_field,
    COUNT(ur.role_id) as new_role_count,
    GROUP_CONCAT(ur.role_id ORDER BY ur.role_id) as role_ids
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
GROUP BY u.id
ORDER BY u.id;

-- 6. 检查是否有用户的旧 role 字段与新表不一致
SELECT 
    u.id,
    u.username,
    u.role as old_role,
    ur.role_id as new_role
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
WHERE u.role IS NOT NULL AND u.role > 0 AND ur.role_id IS NULL;

-- 如果上面的查询返回结果，说明有不一致的数据，需要手动修复

-- 7. 统计迁移后的角色分配情况
SELECT 
    r.id as role_id,
    r.role_code,
    r.role_name,
    COUNT(ur.user_id) as user_count
FROM sys_role r
LEFT JOIN sys_user_role ur ON r.id = ur.role_id
GROUP BY r.id
ORDER BY r.id;

-- 8. 完成提示
SELECT '✅ 数据迁移完成！' as message;
SELECT '请检查上述结果，确认迁移是否正确。' as next_step;
SELECT '确认无误后，可以删除 sys_user_backup 表。' as cleanup;

-- 9. （可选）清理备份表 - 确认迁移成功后再执行
-- DROP TABLE IF EXISTS sys_user_backup;
