-- ============================================================================
-- Migration: Create announcement table
-- Created : 2026-06-15
-- Purpose : System announcement / notification management
-- ============================================================================

USE survey_db;

CREATE TABLE IF NOT EXISTS announcement (
  id              BIGINT       PRIMARY KEY               COMMENT '公告ID (雪花ID)',
  title           VARCHAR(200) NOT NULL                  COMMENT '公告标题',
  type            VARCHAR(30)  NOT NULL DEFAULT 'system_notification' COMMENT '类型: system_notification / work_spec / maintenance_reminder',
  content         TEXT                                   COMMENT '公告内容',
  publisher_id    BIGINT                                 COMMENT '发布人ID',
  status          TINYINT      NOT NULL DEFAULT 0        COMMENT '0草稿 1定时发布 2已发布 3已撤回',
  publish_time    DATETIME                               COMMENT '定时发布时间',
  target_scope    VARCHAR(30)  DEFAULT 'all'             COMMENT '受众范围: all / admin / operator',
  tenant_id       BIGINT       DEFAULT 1                 COMMENT '租户ID',
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status),
  INDEX idx_tenant (tenant_id),
  INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';
