-- =============================================
-- 多租户改造 - 数据库迁移脚本
-- 版本: V1.0
-- 日期: 2026-06-08
-- =============================================

-- =============================================
-- 1. 租户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_tenant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_code VARCHAR(50) UNIQUE NOT NULL COMMENT '租户编码（唯一，用于登录识别）',
  tenant_name VARCHAR(200) NOT NULL COMMENT '租户名称',
  contact_name VARCHAR(50) COMMENT '联系人',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  expire_time DATETIME COMMENT '租期到期时间',
  max_user_count INT DEFAULT 100 COMMENT '最大用户数',
  max_project_count INT DEFAULT 50 COMMENT '最大项目数',
  config_json JSON COMMENT '租户级扩展配置',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 插入默认租户（兼容单租户模式）
INSERT IGNORE INTO sys_tenant (id, tenant_code, tenant_name, status) VALUES (1, 'default', '默认租户', 1);

-- 幂等列/索引创建工具：兼容新库初始化和旧库补迁移重复执行
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS backfill_tenant_if_exists;

DELIMITER $$

CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition VARCHAR(255)
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    DECLARE table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name;

    IF table_exists > 0 THEN
        SELECT COUNT(*) INTO column_exists
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name;

        IF column_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$

CREATE PROCEDURE add_index_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_column_list VARCHAR(255)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    DECLARE table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name;

    IF table_exists > 0 THEN
        SELECT COUNT(*) INTO index_exists
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name;

        IF index_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX ', p_index_name, ' (', p_column_list, ')');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$

CREATE PROCEDURE backfill_tenant_if_exists(
    IN p_table_name VARCHAR(64)
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    DECLARE table_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO table_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name;

    IF table_exists > 0 THEN
        SELECT COUNT(*) INTO column_exists
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = 'tenant_id';

        IF column_exists > 0 THEN
            SET @sql = CONCAT('UPDATE ', p_table_name, ' SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$

DELIMITER ;

-- =============================================
-- 2. sys_user 加 tenant_id
-- =============================================
CALL add_column_if_not_exists('sys_user', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('sys_user', 'idx_tenant', 'tenant_id');

-- =============================================
-- 3. 业务表加 tenant_id
-- =============================================
CALL add_column_if_not_exists('project', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('project', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('project_section', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('project_section', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('project_member', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('project_member', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_point', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_point', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_point_template_binding', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_point_template_binding', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_template', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_template', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_template_version', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_template_version', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_result', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_result', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('survey_audit_record', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('survey_audit_record', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('export_task', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('export_task', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('collab_entry', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('collab_entry', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('sys_task', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('sys_task', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('message_center', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('message_center', 'idx_tenant', 'tenant_id');

CALL add_column_if_not_exists('announcement', 'tenant_id', 'BIGINT DEFAULT 1 COMMENT ''租户ID''');
CALL add_index_if_not_exists('announcement', 'idx_tenant', 'tenant_id');

-- =============================================
-- 4. 将已有数据归入默认租户
-- =============================================
CALL backfill_tenant_if_exists('sys_user');
CALL backfill_tenant_if_exists('project');
CALL backfill_tenant_if_exists('project_section');
CALL backfill_tenant_if_exists('project_member');
CALL backfill_tenant_if_exists('survey_point');
CALL backfill_tenant_if_exists('survey_point_template_binding');
CALL backfill_tenant_if_exists('survey_template');
CALL backfill_tenant_if_exists('survey_template_version');
CALL backfill_tenant_if_exists('survey_result');
CALL backfill_tenant_if_exists('survey_audit_record');
CALL backfill_tenant_if_exists('export_task');
CALL backfill_tenant_if_exists('collab_entry');
CALL backfill_tenant_if_exists('sys_task');
CALL backfill_tenant_if_exists('message_center');
CALL backfill_tenant_if_exists('announcement');
