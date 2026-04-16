CREATE DATABASE IF NOT EXISTS survey_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE survey_db;

-- 项目基础表
CREATE TABLE IF NOT EXISTS project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
  project_code VARCHAR(50) COMMENT '项目编号',
  manager VARCHAR(30) COMMENT '负责人',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 动态勘查模板
CREATE TABLE IF NOT EXISTS survey_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  fields_json JSON NOT NULL COMMENT '字段配置JSON',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查模板表';

-- 勘查点位基础信息
CREATE TABLE IF NOT EXISTS survey_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL COMMENT '项目ID',
  point_name VARCHAR(100) NOT NULL COMMENT '点位名称',
  longitude DECIMAL(12,8) COMMENT '经度',
  latitude DECIMAL(12,8) COMMENT '纬度',
  status TINYINT DEFAULT 0 COMMENT '0未勘查 1已提交 2通过 3驳回',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_project (project_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查点位表';

-- 勘查结果及版本记录
CREATE TABLE IF NOT EXISTS survey_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  point_id BIGINT NOT NULL COMMENT '点位ID',
  form_data JSON NOT NULL COMMENT '业务表单数据',
  images TEXT COMMENT '照片列表URL',
  version INT DEFAULT 1 COMMENT '版本号，驳回重提递增',
  is_latest TINYINT DEFAULT 1 COMMENT '1当前最新 0历史记录',
  audit_status TINYINT DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  audit_remark VARCHAR(500) COMMENT '驳回意见',
  survey_user VARCHAR(50) COMMENT '勘查人员',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_point_version (point_id, version),
  INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查结果表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) COMMENT '真实姓名',
  role TINYINT DEFAULT 2 COMMENT '1管理员 2勘查员 3审核员',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员
INSERT INTO sys_user (username, password, real_name, role)
VALUES ('admin', '$2a$10$N7wKfVvGZx7jZ7jZ7jZ7jO7jZ7jZ7jZ7jZ7jZ7jZ7jZ7jZ7jZ7jZ', '管理员', 1)
ON DUPLICATE KEY UPDATE username=username;
