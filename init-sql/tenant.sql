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

-- =============================================
-- 2. sys_user 加 tenant_id
-- =============================================
ALTER TABLE sys_user ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE sys_user ADD INDEX idx_tenant (tenant_id);

-- =============================================
-- 3. 业务表加 tenant_id
-- =============================================
ALTER TABLE project ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE project ADD INDEX idx_tenant (tenant_id);

ALTER TABLE project_section ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE project_section ADD INDEX idx_tenant (tenant_id);

ALTER TABLE project_member ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE project_member ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_point ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_point ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_point_template_binding ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_point_template_binding ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_template ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_template ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_template_version ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_template_version ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_result ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_result ADD INDEX idx_tenant (tenant_id);

ALTER TABLE survey_audit_record ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE survey_audit_record ADD INDEX idx_tenant (tenant_id);

ALTER TABLE export_task ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE export_task ADD INDEX idx_tenant (tenant_id);

ALTER TABLE collab_entry ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE collab_entry ADD INDEX idx_tenant (tenant_id);

ALTER TABLE sys_task ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE sys_task ADD INDEX idx_tenant (tenant_id);

ALTER TABLE message_center ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE message_center ADD INDEX idx_tenant (tenant_id);

ALTER TABLE announcement ADD COLUMN tenant_id BIGINT DEFAULT 1 COMMENT '租户ID';
ALTER TABLE announcement ADD INDEX idx_tenant (tenant_id);

-- =============================================
-- 4. 将已有数据归入默认租户
-- =============================================
UPDATE sys_user SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE project SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE project_section SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE project_member SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_point SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_point_template_binding SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_template SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_template_version SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_result SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE survey_audit_record SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE export_task SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE collab_entry SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE sys_task SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE message_center SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
UPDATE announcement SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;
