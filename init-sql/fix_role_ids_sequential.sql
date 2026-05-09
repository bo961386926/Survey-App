-- ============================================================
-- 修复角色ID为顺序自增（v2 - 简化版）
-- 问题：SysRole.java 使用了 ASSIGN_ID 生成 Snowflake ID，
-- 导致角色 ID 是 18 位大数字而不是顺序的 1,2,3,4。
-- 同时前端的 roleOptions 需要实际的 roleId 来匹配回显。
-- ============================================================

-- 1. 创建临时映射表来记录新旧 ID 的对应关系
DROP TABLE IF EXISTS tmp_role_id_map_v2;
CREATE TABLE tmp_role_id_map_v2 (
    old_id BIGINT NOT NULL,
    new_id INT NOT NULL,
    role_code VARCHAR(50)
) ENGINE=InnoDB;

-- 按 sort 排序插入，new_id 按顺序赋值
SET @row_num = 0;
INSERT INTO tmp_role_id_map_v2 (old_id, new_id, role_code)
SELECT id, (@row_num := @row_num + 1) AS new_id, role_code
FROM sys_role
ORDER BY sort ASC, id ASC;

-- 查看映射关系
SELECT '=== 角色ID映射关系 ===' as '';
SELECT old_id, new_id, role_code FROM tmp_role_id_map_v2 ORDER BY new_id;

-- 2. 更新 sys_user_role 表：将 role_id 从旧值改为新值
UPDATE sys_user_role ur
JOIN tmp_role_id_map_v2 m ON ur.role_id = m.old_id
SET ur.role_id = m.new_id;

-- 3. 更新 sys_user 表的 role 字段（废弃字段，但为了兼容）
-- 取用户的最小 role_id
UPDATE sys_user u
SET u.role = (
    SELECT MIN(m.new_id)
    FROM sys_user_role ur
    JOIN tmp_role_id_map_v2 m ON ur.role_id = m.old_id
    WHERE ur.user_id = u.id
    LIMIT 1
);

-- 4. 更新 sys_role 表的主键
SET FOREIGN_KEY_CHECKS = 0;

UPDATE sys_role r
JOIN tmp_role_id_map_v2 m ON r.id = m.old_id
SET r.id = m.new_id;

SET FOREIGN_KEY_CHECKS = 1;

-- 5. 重置自增计数器
SET @max_id = (SELECT MAX(id) FROM sys_role);
SET @sql = CONCAT('ALTER TABLE sys_role AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 清理临时表
DROP TABLE IF EXISTS tmp_role_id_map_v2;

-- 7. 核查结果
SELECT '=== sys_role 修复后 ===' as '';
SELECT id, role_code, role_name FROM sys_role ORDER BY id;

SELECT '=== sys_user_role 修复后 ===' as '';
SELECT user_id, role_id FROM sys_user_role ORDER BY user_id, role_id;

SELECT '=== sys_user.role 修复后 ===' as '';
SELECT id, username, role FROM sys_user ORDER BY id;
