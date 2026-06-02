-- ============================================================================
-- Migration: Database performance indexes (Task #7)
-- Created : 2026-06-01
-- Purpose : Add missing indexes for paginated list endpoints and frequent
--           filter/sort columns. Idempotent — uses a stored procedure that
--           checks information_schema before issuing CREATE INDEX so the
--           script can safely run multiple times.
--
-- Notes on column-name mapping:
--   * The task description references *_id / *_at conventions but the
--     existing schema (see 01-init.sql / 03-offline-data-sync.sql) uses
--     create_time, manager (VARCHAR), assignee_id, creator_id, etc.
--     Indexes below target the actual column names that exist in the DB.
-- ============================================================================

USE survey_db;

-- ---------------------------------------------------------------------------
-- Idempotent index creator
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

DELIMITER $$

CREATE PROCEDURE add_index_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_column_list VARCHAR(255)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    DECLARE table_exists INT DEFAULT 0;

    -- Skip silently when target table is not present in this environment
    SELECT COUNT(*) INTO table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name   = p_table_name;

    IF table_exists = 0 THEN
        SELECT CONCAT('- skipped (table not found): ', p_table_name) AS result;
    ELSE
        SELECT COUNT(*) INTO index_exists
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name   = p_table_name
          AND index_name   = p_index_name;

        IF index_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', p_table_name,
                              ' ADD INDEX ', p_index_name,
                              ' (', p_column_list, ')');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT CONCAT('+ created: ', p_index_name,
                          ' ON ', p_table_name, '(', p_column_list, ')') AS result;
        ELSE
            SELECT CONCAT('= exists : ', p_index_name) AS result;
        END IF;
    END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------------
-- 1. project — status / manager / create_time
--    (schema uses VARCHAR `manager` instead of manager_id)
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('project', 'idx_project_status',      'status');
CALL add_index_if_not_exists('project', 'idx_project_manager',     'manager');
CALL add_index_if_not_exists('project', 'idx_project_create_time', 'create_time');

-- ---------------------------------------------------------------------------
-- 2. survey_point — composite (project_id,status) + assignee/outfall_type/time
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('survey_point', 'idx_sp_project_status', 'project_id,status');
CALL add_index_if_not_exists('survey_point', 'idx_sp_assignee',       'assignee_id');
CALL add_index_if_not_exists('survey_point', 'idx_sp_outfall_type',   'outfall_type');
CALL add_index_if_not_exists('survey_point', 'idx_sp_create_time',    'create_time');

-- ---------------------------------------------------------------------------
-- 3. survey_result — composite (point_id,version_no) + filter columns
--    (schema's "status" is split into result_status and audit_status —
--     index both since both are used by paginated list queries.)
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('survey_result', 'idx_sr_point_version', 'point_id,version_no');
CALL add_index_if_not_exists('survey_result', 'idx_sr_survey_user',   'survey_user_id');
CALL add_index_if_not_exists('survey_result', 'idx_sr_result_status', 'result_status');
CALL add_index_if_not_exists('survey_result', 'idx_sr_audit_status',  'audit_status');
CALL add_index_if_not_exists('survey_result', 'idx_sr_create_time',   'create_time');

-- ---------------------------------------------------------------------------
-- 4. survey_audit_record — result/point/auditor/time
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('survey_audit_record', 'idx_sar_result',      'result_id');
CALL add_index_if_not_exists('survey_audit_record', 'idx_sar_point',       'point_id');
CALL add_index_if_not_exists('survey_audit_record', 'idx_sar_auditor',     'auditor_id');
CALL add_index_if_not_exists('survey_audit_record', 'idx_sar_create_time', 'create_time');

-- ---------------------------------------------------------------------------
-- 5. operation_log — user / module / create_time / risk_level
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('operation_log', 'idx_oplog_user_id',     'user_id');
CALL add_index_if_not_exists('operation_log', 'idx_oplog_module',      'module');
CALL add_index_if_not_exists('operation_log', 'idx_oplog_create_time', 'create_time');
CALL add_index_if_not_exists('operation_log', 'idx_oplog_risk_level',  'risk_level');

-- ---------------------------------------------------------------------------
-- 6. offline_data_sync — composite (sync_status,retry_count) + user/type
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('offline_data_sync', 'idx_ods_status_retry', 'sync_status,retry_count');
CALL add_index_if_not_exists('offline_data_sync', 'idx_ods_user_id',      'user_id');
CALL add_index_if_not_exists('offline_data_sync', 'idx_ods_data_type',    'data_type');

-- ---------------------------------------------------------------------------
-- 7. export_task — creator_id (acts as user_id) / status / create_time
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('export_task', 'idx_export_creator',     'creator_id');
CALL add_index_if_not_exists('export_task', 'idx_export_status',      'status');
CALL add_index_if_not_exists('export_task', 'idx_export_create_time', 'create_time');

-- ---------------------------------------------------------------------------
-- 8. message_center — composite (user_id,is_read) + create_time
-- ---------------------------------------------------------------------------
CALL add_index_if_not_exists('message_center', 'idx_msg_user_read',    'user_id,is_read');
CALL add_index_if_not_exists('message_center', 'idx_msg_create_time',  'create_time');

-- ---------------------------------------------------------------------------
-- Cleanup
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

-- ---------------------------------------------------------------------------
-- Verification helper (manual): list resulting indexes for the touched tables
--
-- SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) cols
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME IN ('project','survey_point','survey_result','survey_audit_record',
--                      'operation_log','offline_data_sync','export_task','message_center')
-- GROUP BY TABLE_NAME, INDEX_NAME
-- ORDER BY TABLE_NAME, INDEX_NAME;
