-- =============================================
-- 数据库迁移：项目表新增字段
-- 委托单位(client_name) 和 项目描述(description)
-- =============================================

USE survey_db;

ALTER TABLE project
  ADD COLUMN client_name VARCHAR(200) COMMENT '委托单位' AFTER region,
  ADD COLUMN description TEXT COMMENT '项目描述/备注' AFTER client_name;
