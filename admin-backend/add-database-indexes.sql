-- ========================================
-- 数据库索引优化脚本
-- 创建时间: 2026-05-05
-- 说明: 根据架构评审报告添加的数据库索引
-- 注意: MySQL 5.7+ 不支持 CREATE INDEX IF NOT EXISTS
--       使用存储过程实现条件创建
-- ========================================

-- 创建存储过程来条件创建索引
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

DELIMITER $$

CREATE PROCEDURE add_index_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_column_list VARCHAR(255)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    
    -- 检查索引是否已存在
    SELECT COUNT(*) INTO index_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND index_name = p_index_name;
    
    -- 如果索引不存在，则创建
    IF index_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX ', p_index_name, ' (', p_column_list, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('✓ 索引创建成功: ', p_index_name, ' ON ', p_table_name, '(', p_column_list, ')') AS result;
    ELSE
        SELECT CONCAT('○ 索引已存在: ', p_index_name) AS result;
    END IF;
END$$

DELIMITER ;

-- 1. 用户表索引
CALL add_index_if_not_exists('sys_user', 'idx_username', 'username');
CALL add_index_if_not_exists('sys_user', 'idx_phone', 'phone');
CALL add_index_if_not_exists('sys_user', 'idx_status', 'status');
CALL add_index_if_not_exists('sys_user', 'idx_status_role', 'status,role');

-- 2. 项目表索引
CALL add_index_if_not_exists('project', 'idx_project_code', 'project_code');
CALL add_index_if_not_exists('project', 'idx_project_status', 'status');
CALL add_index_if_not_exists('project', 'idx_create_time', 'create_time');

-- 3. 勘察点位表索引
CALL add_index_if_not_exists('survey_point', 'idx_point_project_id', 'project_id');
CALL add_index_if_not_exists('survey_point', 'idx_point_status', 'status');
CALL add_index_if_not_exists('survey_point', 'idx_project_status_point', 'project_id,status');

-- 4. 勘察结果表索引
-- 注意: survey_result表的字段是audit_status而非result_status
CALL add_index_if_not_exists('survey_result', 'idx_result_point_id', 'point_id');
CALL add_index_if_not_exists('survey_result', 'idx_result_audit_status', 'audit_status');
CALL add_index_if_not_exists('survey_result', 'idx_point_audit_status', 'point_id,audit_status');
CALL add_index_if_not_exists('survey_result', 'idx_result_version', 'point_id,version_no');
CALL add_index_if_not_exists('survey_result', 'idx_result_is_latest', 'point_id,is_latest');

-- 5. 离线数据同步表索引
CALL add_index_if_not_exists('offline_data_sync', 'idx_device_sync_status', 'device_id,sync_status');
CALL add_index_if_not_exists('offline_data_sync', 'idx_sync_create_time', 'create_time');
CALL add_index_if_not_exists('offline_data_sync', 'idx_device_create_time', 'device_id,create_time');

-- 6. 模板表索引
CALL add_index_if_not_exists('survey_template', 'idx_template_name', 'template_name');
CALL add_index_if_not_exists('survey_template', 'idx_template_status', 'status');

-- ========================================
-- 清理存储过程
-- ========================================
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

-- ========================================
-- 验证索引创建结果
-- ========================================

-- 查看所有索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM 
    information_schema.STATISTICS
WHERE 
    TABLE_SCHEMA = 'survey_db'
ORDER BY 
    TABLE_NAME, 
    INDEX_NAME, 
    SEQ_IN_INDEX;

-- ========================================
-- 性能测试建议
-- ========================================

-- 1. 测试用户登录查询
EXPLAIN SELECT * FROM sys_user WHERE username = 'admin' AND status = 1;

-- 2. 测试项目列表查询
EXPLAIN SELECT * FROM project WHERE status = 1 AND region = '北京' ORDER BY create_time DESC LIMIT 10;

-- 3. 测试离线数据同步查询
EXPLAIN SELECT * FROM offline_data_sync WHERE device_id = 'device-001' AND sync_status = 0 ORDER BY create_time ASC;

-- 4. 测试勘察结果查询
EXPLAIN SELECT * FROM survey_result WHERE point_id = 123 AND result_status = 3 ORDER BY version_no DESC LIMIT 1;

-- ========================================
-- 注意事项
-- ========================================
-- 1. 索引会占用额外磁盘空间，请确保磁盘充足
-- 2. 索引会略微降低写入性能（INSERT/UPDATE/DELETE），但大幅提升查询性能
-- 3. 建议在生产环境低峰期执行此脚本
-- 4. 执行前建议备份数据库
-- 5. 大表创建索引可能需要较长时间，请耐心等待
-- 6. 可使用 SHOW PROCESSLIST 查看索引创建进度
