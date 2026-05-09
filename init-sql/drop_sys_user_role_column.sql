-- =============================================
-- 迁移脚本：删除 sys_user 表的 role 遗留字段
-- 说明：角色管理已迁移到 sys_user_role 关联表（多对多）
-- 此脚本应在确认所有用户的角色都已正确迁移到 sys_user_role 后执行
-- =============================================

-- 1. 检查是否有未迁移的用户（sys_user.role 有值但 sys_user_role 中没有对应记录的用户）
-- 如果有，请先执行 migrate_to_multi_role.sql 完成迁移
SELECT u.id, u.username, u.role, u.real_name
FROM sys_user u
WHERE u.role IS NOT NULL AND u.role > 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id
  );

-- 2. 删除 role 字段
ALTER TABLE sys_user DROP COLUMN role;
