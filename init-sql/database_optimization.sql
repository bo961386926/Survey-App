-- ============================================
-- Survey-App 数据库优化脚本
-- 适用版本: MySQL 8.0+
-- 创建时间: 2026-05-06
-- ============================================

-- 1. 查看所有表的状态
SELECT 
    TABLE_NAME AS '表名',
    TABLE_ROWS AS '预估行数',
    DATA_LENGTH / 1024 / 1024 AS '数据大小(MB)',
    INDEX_LENGTH / 1024 / 1024 AS '索引大小(MB)',
    (DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024 AS '总大小(MB)',
    TABLE_COLLATION AS '字符集',
    CREATE_OPTIONS AS '创建选项'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'survey_db'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- ============================================
-- 2. 为关键字段添加索引（如果尚未存在）
-- ============================================

-- sys_user 表索引
ALTER TABLE sys_user ADD INDEX idx_username (username);
ALTER TABLE sys_user ADD INDEX idx_phone (phone);
ALTER TABLE sys_user ADD INDEX idx_email (email);
ALTER TABLE sys_user ADD INDEX idx_status (status);
ALTER TABLE sys_user ADD INDEX idx_deleted (is_deleted);

-- survey_record 表索引
ALTER TABLE survey_record ADD INDEX idx_surveyor (surveyor_id);
ALTER TABLE survey_record ADD INDEX idx_status (status);
ALTER TABLE survey_record ADD INDEX idx_create_time (create_time);

-- equipment 表索引
ALTER TABLE equipment ADD INDEX idx_survey_record (survey_record_id);

-- ============================================
-- 3. 查看慢查询日志配置
-- ============================================
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';
SHOW VARIABLES LIKE 'log_output';

-- 4. 开启慢查询日志（用于优化）
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- 超过2秒的查询记录

-- ============================================
-- 5. 查看连接数和内存使用
-- ============================================
SHOW STATUS LIKE 'Threads_connected';   -- 当前连接数
SHOW STATUS LIKE 'Max_used_connections'; -- 历史最大连接数
SHOW VARIABLES LIKE 'max_connections';   -- 最大连接数限制
SHOW VARIABLES LIKE 'innodb_buffer_pool_size'; -- InnoDB缓冲池大小

-- ============================================
-- 6. 查看各表的字段完整信息
-- ============================================
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'survey_db'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
