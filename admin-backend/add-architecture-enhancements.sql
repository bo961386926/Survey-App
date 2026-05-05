-- ========================================
-- 企业级架构增强 - 数据库变更脚本
-- 执行时间: 2026-05-05
-- 执行人: DBA
-- 说明: 添加软删除、乐观锁、审计字段
-- ========================================

USE survey_db;

-- ========================================
-- 1. 软删除字段
-- ========================================

-- 用户表
ALTER TABLE sys_user 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER status,
ADD COLUMN deleted_time DATETIME COMMENT '删除时间' AFTER is_deleted,
ADD COLUMN deleted_by VARCHAR(64) COMMENT '删除人' AFTER deleted_time;

-- 项目表
ALTER TABLE project 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER status,
ADD COLUMN deleted_time DATETIME COMMENT '删除时间' AFTER is_deleted,
ADD COLUMN deleted_by VARCHAR(64) COMMENT '删除人' AFTER deleted_time;

-- 点位表
ALTER TABLE survey_point 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER status,
ADD COLUMN deleted_time DATETIME COMMENT '删除时间' AFTER is_deleted,
ADD COLUMN deleted_by VARCHAR(64) COMMENT '删除人' AFTER deleted_time;

-- 勘察结果表
ALTER TABLE survey_result 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER audit_status,
ADD COLUMN deleted_time DATETIME COMMENT '删除时间' AFTER is_deleted,
ADD COLUMN deleted_by VARCHAR(64) COMMENT '删除人' AFTER deleted_time;

-- ========================================
-- 2. 乐观锁版本号字段
-- ========================================

ALTER TABLE sys_user ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;
ALTER TABLE project ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;
ALTER TABLE survey_point ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;
ALTER TABLE survey_result ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;

-- ========================================
-- 3. 审计字段（如果不存在则添加）
-- ========================================

-- 用户表审计字段
ALTER TABLE sys_user 
ADD COLUMN create_by VARCHAR(64) COMMENT '创建人' AFTER create_time,
ADD COLUMN update_by VARCHAR(64) COMMENT '更新人' AFTER update_time;

-- 项目表审计字段
ALTER TABLE project 
ADD COLUMN create_by VARCHAR(64) COMMENT '创建人' AFTER create_time,
ADD COLUMN update_by VARCHAR(64) COMMENT '更新人' AFTER update_time;

-- 点位表审计字段
ALTER TABLE survey_point 
ADD COLUMN create_by VARCHAR(64) COMMENT '创建人' AFTER create_time,
ADD COLUMN update_by VARCHAR(64) COMMENT '更新人' AFTER update_time;

-- 勘察结果表审计字段
ALTER TABLE survey_result 
ADD COLUMN create_by VARCHAR(64) COMMENT '创建人' AFTER create_time,
ADD COLUMN update_by VARCHAR(64) COMMENT '更新人' AFTER update_time;

-- 审核记录表审计字段
ALTER TABLE survey_audit_record 
ADD COLUMN create_by VARCHAR(64) COMMENT '创建人' AFTER create_time,
ADD COLUMN update_by VARCHAR(64) COMMENT '更新人' AFTER update_time;

-- ========================================
-- 4. 索引优化
-- ========================================

-- 软删除索引
CREATE INDEX idx_user_is_deleted ON sys_user(is_deleted);
CREATE INDEX idx_project_is_deleted ON project(is_deleted);
CREATE INDEX idx_point_is_deleted ON survey_point(is_deleted);
CREATE INDEX idx_result_is_deleted ON survey_result(is_deleted);

-- 审计字段索引
CREATE INDEX idx_user_create_time ON sys_user(create_time);
CREATE INDEX idx_user_update_time ON sys_user(update_time);
CREATE INDEX idx_project_create_time ON project(create_time);
CREATE INDEX idx_project_update_time ON project(update_time);

-- ========================================
-- 5. 数据初始化
-- ========================================

-- 更新现有数据的is_deleted字段为0（未删除）
UPDATE sys_user SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE project SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE survey_point SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE survey_result SET is_deleted = 0 WHERE is_deleted IS NULL;

-- 更新现有数据的version字段为0
UPDATE sys_user SET version = 0 WHERE version IS NULL;
UPDATE project SET version = 0 WHERE version IS NULL;
UPDATE survey_point SET version = 0 WHERE version IS NULL;
UPDATE survey_result SET version = 0 WHERE version IS NULL;

-- ========================================
-- 6. 验证脚本
-- ========================================

-- 检查字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME IN ('sys_user', 'project', 'survey_point', 'survey_result')
  AND COLUMN_NAME IN ('is_deleted', 'version', 'create_by', 'update_by', 'deleted_time', 'deleted_by')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- 检查索引是否创建成功
SELECT INDEX_NAME, COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME IN ('sys_user', 'project', 'survey_point', 'survey_result')
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- ========================================
-- 执行完成提示
-- ========================================
SELECT '数据库变更执行完成！' AS message;
