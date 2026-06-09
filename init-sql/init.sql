-- 青泓项目勘察信息采集与审核系统 数据库初始化脚本
-- 版本: V3.2
-- 日期: 2026-04-30

CREATE DATABASE IF NOT EXISTS survey_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE survey_db;

-- =============================================
-- 1. 项目基础表
-- =============================================
CREATE TABLE IF NOT EXISTS project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
  project_code VARCHAR(50) UNIQUE COMMENT '项目编号',
  manager VARCHAR(30) COMMENT '负责人',
  region VARCHAR(100) COMMENT '所属区域',
  client_name VARCHAR(200) COMMENT '委托单位',
  description TEXT COMMENT '项目描述/备注',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  status TINYINT DEFAULT 0 COMMENT '0草稿 1进行中 2已暂停 3已完成 4已归档',
  template_count INT DEFAULT 0 COMMENT '绑定模板数',
  point_count INT DEFAULT 0 COMMENT '点位总数',
  completed_count INT DEFAULT 0 COMMENT '已完成数量',
  pending_audit_count INT DEFAULT 0 COMMENT '待审核数量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_manager (manager)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- =============================================
-- 2. 标段/区域管理表
-- =============================================
CREATE TABLE IF NOT EXISTS project_section (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL COMMENT '项目ID',
  section_name VARCHAR(100) NOT NULL COMMENT '标段名称',
  section_code VARCHAR(50) COMMENT '标段编号',
  manager_id BIGINT COMMENT '标段负责人ID',
  description TEXT COMMENT '描述',
  is_key_area TINYINT DEFAULT 0 COMMENT '是否重点区域 0-否 1-是',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_project (project_id),
  INDEX idx_manager (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标段表';

-- =============================================
-- 3. 动态勘查模板主表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_code VARCHAR(50) UNIQUE COMMENT '模板编码',
  description TEXT COMMENT '模板描述',
  status TINYINT DEFAULT 0 COMMENT '0草稿 1已发布 2已停用',
  current_version_id BIGINT COMMENT '当前版本ID',
  creator_id BIGINT COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查模板主表';

-- =============================================
-- 4. 模板版本表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_template_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_id BIGINT NOT NULL COMMENT '模板ID',
  version_no INT NOT NULL COMMENT '版本号',
  fields_json JSON NOT NULL COMMENT '字段配置JSON',
  rules_json JSON COMMENT '校验规则JSON',
  linkage_rules_json JSON COMMENT '联动规则JSON',
  status TINYINT DEFAULT 0 COMMENT '0草稿 1已发布',
  publish_time DATETIME COMMENT '发布时间',
  creator_id BIGINT COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_template_version (template_id, version_no),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板版本表';

-- =============================================
-- 5. 排口类型与模板版本绑定表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_point_template_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL COMMENT '项目ID',
  section_id BIGINT COMMENT '标段ID',
  outfall_type VARCHAR(50) NOT NULL COMMENT '排口类型',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  template_version_id BIGINT NOT NULL COMMENT '模板版本ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_section_type (project_id, section_id, outfall_type),
  INDEX idx_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点位模板绑定表';

-- =============================================
-- 6. 勘查点位表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  point_code VARCHAR(50) UNIQUE COMMENT '点位编号',
  point_name VARCHAR(100) NOT NULL COMMENT '点位名称',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  section_id BIGINT COMMENT '标段ID',
  outfall_type VARCHAR(50) COMMENT '排口类型(雨水排口/污水排口/雨污混流口等)',
  longitude DECIMAL(12,8) COMMENT '经度',
  latitude DECIMAL(12,8) COMMENT '纬度',
  region VARCHAR(100) COMMENT '行政区',
  assignee_id BIGINT COMMENT '分配人ID',
  collector_id BIGINT COMMENT '采集人ID',
  status TINYINT DEFAULT 0 COMMENT '0待采集 1草稿中 2待审核 3审核通过 4驳回待修改 5已归档 6作废',
  abnormal_tag VARCHAR(200) COMMENT '异常标签',
  is_deleted TINYINT DEFAULT 0 COMMENT '1已删除 0未删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_project (project_id),
  INDEX idx_status (status),
  INDEX idx_collector (collector_id),
  INDEX idx_outfall_type (outfall_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查点位表';

-- =============================================
-- 7. 勘查结果及版本记录表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  point_id BIGINT NOT NULL COMMENT '点位ID',
  version_no INT NOT NULL DEFAULT 1 COMMENT '版本号，按点位自增，从1开始',
  template_version_id BIGINT COMMENT '使用的模板版本ID',
  form_data JSON NOT NULL COMMENT '业务表单数据',
  images TEXT COMMENT '照片列表URL，JSON数组',
  result_status TINYINT DEFAULT 0 COMMENT '0草稿 1已提交 2待审核 3已通过 4已驳回 5已归档',
  audit_status TINYINT DEFAULT 0 COMMENT '0待审 1通过 2驳回(冗余字段，便于查询)',
  audit_remark VARCHAR(500) COMMENT '驳回意见',
  survey_user_id BIGINT COMMENT '勘查人员ID',
  optimistic_lock_version INT DEFAULT 0 COMMENT '乐观锁版本号',
  submit_time DATETIME COMMENT '提交时间',
  audit_time DATETIME COMMENT '审核时间',
  auditor_id BIGINT COMMENT '审核人ID',
  is_deleted TINYINT DEFAULT 0 COMMENT '1已删除 0未删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_point_version (point_id, version_no),
  INDEX idx_result_status (result_status),
  INDEX idx_audit_status (audit_status),
  INDEX idx_survey_user (survey_user_id),
  INDEX idx_auditor (auditor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘查结果表';

-- =============================================
-- 8. 审核记录表
-- =============================================
CREATE TABLE IF NOT EXISTS survey_audit_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  result_id BIGINT NOT NULL COMMENT '采集结果ID',
  point_id BIGINT NOT NULL COMMENT '点位ID',
  auditor_id BIGINT NOT NULL COMMENT '审核人ID',
  action VARCHAR(20) NOT NULL COMMENT '操作：pass/reject/transfer(预留)',
  audit_comment TEXT COMMENT '审核意见',
  reject_template_id BIGINT COMMENT '驳回模板ID(常用驳回原因模板)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_result (result_id),
  INDEX idx_point (point_id),
  INDEX idx_auditor (auditor_id),
  INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- =============================================
-- 9. 位置纠偏日志表
-- =============================================
CREATE TABLE IF NOT EXISTS location_correction_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  point_id BIGINT NOT NULL COMMENT '点位ID',
  result_id BIGINT COMMENT '关联的采集结果ID',
  original_lng DECIMAL(12,8) COMMENT '原始经度',
  original_lat DECIMAL(12,8) COMMENT '原始纬度',
  corrected_lng DECIMAL(12,8) COMMENT '纠偏后经度',
  corrected_lat DECIMAL(12,8) COMMENT '纠偏后纬度',
  user_id BIGINT COMMENT '操作人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_point (point_id),
  INDEX idx_result (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='位置纠偏日志表';

-- =============================================
-- 10. 导出任务表
-- =============================================
CREATE TABLE IF NOT EXISTS export_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
  task_type VARCHAR(50) NOT NULL COMMENT '任务类型：point_list/audit_result/pdf_single/pdf_batch',
  project_id BIGINT COMMENT '项目ID',
  status TINYINT DEFAULT 0 COMMENT '0待生成 1生成中 2已完成 3失败 4已过期',
  file_url VARCHAR(500) COMMENT '文件下载URL',
  file_size BIGINT COMMENT '文件大小(字节)',
  expire_time DATETIME COMMENT '链接过期时间',
  error_msg TEXT COMMENT '错误信息',
  creator_id BIGINT COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出任务表';

-- =============================================
-- 11. 第三方协作入口表
-- =============================================
CREATE TABLE IF NOT EXISTS collab_entry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entry_name VARCHAR(100) NOT NULL COMMENT '入口名称',
  token VARCHAR(128) UNIQUE NOT NULL COMMENT '协作访问令牌',
  project_ids TEXT COMMENT '授权项目ID列表，JSON数组',
  point_ids TEXT COMMENT '授权点位ID列表，JSON数组',
  permissions JSON COMMENT '权限范围配置',
  expire_time DATETIME COMMENT '有效期',
  status TINYINT DEFAULT 0 COMMENT '0未启用 1启用中 2已过期 3已撤销',
  creator_id BIGINT COMMENT '创建人ID',
  access_count INT DEFAULT 0 COMMENT '访问次数',
  last_access_time DATETIME COMMENT '最后访问时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_token (token),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作入口表';

-- =============================================
-- 12. 操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  module VARCHAR(50) COMMENT '模块',
  action VARCHAR(50) COMMENT '操作类型',
  target_type VARCHAR(50) COMMENT '目标类型',
  target_id BIGINT COMMENT '目标ID',
  detail TEXT COMMENT '操作详情',
  ip VARCHAR(50) COMMENT 'IP地址',
  user_agent VARCHAR(500) COMMENT '用户代理',
  risk_level TINYINT DEFAULT 0 COMMENT '风险等级：0低 1中 2高',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_module_action (module, action),
  INDEX idx_risk_level (risk_level),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =============================================
-- 13. 登录日志表
-- =============================================
CREATE TABLE IF NOT EXISTS login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  login_type VARCHAR(20) COMMENT '登录类型：internal/collab',
  status TINYINT COMMENT '0成功 1失败',
  fail_reason VARCHAR(200) COMMENT '失败原因',
  ip VARCHAR(50) COMMENT 'IP地址',
  user_agent VARCHAR(500) COMMENT '用户代理',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_status (status),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =============================================
-- 14. 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
  role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
  permissions JSON COMMENT '权限列表',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 15. 用户角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 16. 权限目录表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码，如 point:view',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限名称，如 查看点位',
  module VARCHAR(50) COMMENT '所属模块，如 point/audit/user',
  description VARCHAR(255) COMMENT '权限描述',
  status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_module (module),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限目录表';

-- =============================================
-- 17. 角色权限关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm (role_id, perm_code),
  INDEX idx_role (role_id),
  INDEX idx_perm (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =============================================
-- 18. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  real_name VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  is_first_login TINYINT DEFAULT 1 COMMENT '1首次登录 0非首次',
  login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
  lock_time DATETIME COMMENT '锁定时间',
  last_login_time DATETIME COMMENT '最后登录时间',
  tenant_id BIGINT DEFAULT 1 COMMENT '租户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '1已删除 0未删除',
  version INT DEFAULT 0 COMMENT '乐观锁版本',
  create_by VARCHAR(50) COMMENT '创建者',
  update_by VARCHAR(50) COMMENT '更新者',
  deleted_time DATETIME COMMENT '删除时间',
  deleted_by VARCHAR(50) COMMENT '删除者',
  INDEX idx_username (username),
  INDEX idx_status (status),
  INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 19. 消息中心表
-- =============================================
CREATE TABLE IF NOT EXISTS message_center (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '接收用户ID',
  msg_type VARCHAR(50) NOT NULL COMMENT '消息类型：audit_reminder/project_delay/template_publish/export_complete/collab_expire/risk_alert',
  msg_title VARCHAR(200) NOT NULL COMMENT '消息标题',
  msg_content TEXT COMMENT '消息内容',
  target_type VARCHAR(50) COMMENT '关联目标类型',
  target_id BIGINT COMMENT '关联目标ID',
  is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
  read_time DATETIME COMMENT '阅读时间',
  push_status TINYINT DEFAULT 0 COMMENT '0未推送 1已推送 2推送失败',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_msg_type (msg_type),
  INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心表';

-- =============================================
-- 20. 数据字典表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_dict (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
  dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_id BIGINT NOT NULL COMMENT '字典ID',
  item_label VARCHAR(100) NOT NULL COMMENT '选项标签',
  item_value VARCHAR(100) NOT NULL COMMENT '选项值',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_dict (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- =============================================
-- 21. 协作访问日志表
-- =============================================
CREATE TABLE IF NOT EXISTS collab_access_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entry_id BIGINT NOT NULL COMMENT '协作入口ID',
  token VARCHAR(128) COMMENT '访问令牌',
  ip VARCHAR(50) COMMENT '访问IP',
  user_agent VARCHAR(500) COMMENT '用户代理',
  request_path VARCHAR(200) COMMENT '请求路径',
  response_code INT COMMENT '响应状态码',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_entry (entry_id),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协作访问日志表';

-- =============================================
-- 22. 文件存储表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(200) NOT NULL COMMENT '文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
  file_size BIGINT COMMENT '文件大小',
  file_type VARCHAR(50) COMMENT '文件类型',
  biz_type VARCHAR(50) COMMENT '业务类型：survey_photo/template_image',
  biz_id BIGINT COMMENT '业务ID',
  creator_id BIGINT COMMENT '上传人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_biz (biz_type, biz_id),
  INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储表';

-- =============================================
-- 23. 系统配置表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string/json/number',
  description VARCHAR(200) COMMENT '配置说明',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员用户 (密码: admin123)
INSERT IGNORE INTO sys_user (username, password, real_name, is_first_login) 
VALUES ('admin', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '系统管理员', 0);

-- 插入测试用户数据 (密码: admin123)
-- 注意：角色通过 sys_user_role 关联表分配，见下方 INSERT
INSERT IGNORE INTO sys_user (username, password, real_name, status) VALUES
('surveyor1', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '张志明', 1),
('surveyor2', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '李晓华', 1),
('surveyor3', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '王建国', 1),
('reviewer1', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '陈美丽', 1),
('reviewer2', '$2a$10$FwiFldcnaa2.sWJAhbU4RerIxA/stp.xq0iX/50/fMxbtdmFuq/yW', '刘强', 1);

-- 插入默认角色
INSERT IGNORE INTO sys_role (role_code, role_name, permissions) VALUES
('admin', '管理员', '*'),
('project_manager', '项目负责人', 'project:view,project:edit,point:view,point:edit,template:view,template:edit,template:bind,task:view,task:edit,task:assign,export:project'),
('auditor', '审核员', 'point:view,task:view,audit:view,audit:pass,audit:reject,export:audit'),
('surveyor', '采集员', 'point:view,template:view,task:view,task:edit,survey:create,survey:edit,survey:submit'),
('collab', '第三方协作', 'point:view,template:view,survey:assist');

-- 关联用户与角色（通过 sys_user_role 中间表）
-- admin(user_id=1) -> admin(role_id=1)
INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time) VALUES
(1, 1, NOW()),
(2, 4, NOW()),
(3, 4, NOW()),
(4, 4, NOW()),
(5, 3, NOW()),
(6, 3, NOW());

-- 插入排口类型字典
INSERT IGNORE INTO sys_dict (dict_code, dict_name) VALUES ('outfall_type', '排口类型');
INSERT IGNORE INTO sys_dict_item (dict_id, item_label, item_value, sort_order) VALUES
(1, '雨水排口', 'rainwater', 1),
(1, '污水排口', 'sewage', 2),
(1, '雨污混流口', 'mixed', 3),
(1, '工业废水排口', 'industrial', 4);

-- 插入测试项目数据
INSERT IGNORE INTO project (project_name, project_code, manager, region, start_date, end_date, status) VALUES
('青泓河水质监测项目', 'QH2024001', '张志明', '青泓区', '2024-01-01', '2024-12-31', 1),
('工业园区土壤污染调查', 'GY2024002', '李晓华', '工业园区', '2024-02-01', '2024-08-31', 1),
('城市空气质量监测网', 'CQ2024003', '王建国', '全市', '2024-03-01', '2024-12-31', 1),
('农业用地重金属检测', 'NY2024004', '陈美丽', '农业区', '2024-04-01', '2024-10-31', 2);

-- 插入测试标段数据
INSERT IGNORE INTO project_section (project_id, section_name, section_code, manager_id) VALUES
(1, '青泓河上游段', 'QH-S01', 1),
(1, '青泓河中游段', 'QH-S02', 1),
(1, '青泓河下游段', 'QH-S03', 1),
(2, 'A区', 'GY-S01', 2),
(2, 'B区', 'GY-S02', 2);

-- 插入测试点位数据
INSERT IGNORE INTO survey_point (point_code, point_name, project_id, section_id, outfall_type, longitude, latitude, region, collector_id, status) VALUES
('QH-P001', '青泓河入河口', 1, 1, 'rainwater', 120.12345678, 30.12345678, '青泓区', 2, 2),
('QH-P002', '青泓河中游监测点', 1, 2, 'rainwater', 120.23456789, 30.23456789, '青泓区', 2, 3),
('QH-P003', '青泓河下游出口', 1, 3, 'sewage', 120.34567890, 30.34567890, '青泓区', 3, 0),
('GY-P001', '工业区A区采样点1', 2, 4, 'industrial', 120.45678901, 30.45678901, '工业园区', 2, 2),
('GY-P002', '工业区B区采样点2', 2, 5, 'industrial', 120.56789012, 30.56789012, '工业园区', 3, 0),
('CQ-P001', '市中心监测站', 3, NULL, 'mixed', 120.67890123, 30.67890123, '市中心', 3, 3),
('CQ-P002', '郊区监测站', 3, NULL, 'mixed', 120.78901234, 30.78901234, '郊区', 3, 2);

-- 插入测试勘查结果数据
INSERT IGNORE INTO survey_result (point_id, version_no, form_data, images, result_status, audit_status, audit_remark, survey_user_id, submit_time, audit_time, auditor_id) VALUES
(1, 1, '{"ph": "7.2", "temperature": "25.5", "dissolved_oxygen": "8.1", "cod": "15.3", "bod5": "3.2"}', '["https://example.com/image1.jpg"]', 3, 1, '数据正常', 2, '2024-03-01 10:00:00', '2024-03-01 14:00:00', 4),
(2, 1, '{"ph": "7.8", "temperature": "24.8", "dissolved_oxygen": "7.9", "cod": "18.7", "bod5": "4.1"}', '["https://example.com/image2.jpg","https://example.com/image3.jpg"]', 3, 1, '审核通过', 2, '2024-03-02 10:00:00', '2024-03-02 14:00:00', 4),
(4, 1, '{"heavy_metals": {"lead": "25.3", "cadmium": "0.8", "chromium": "15.2"}, "organic_pollutants": {"pahs": "2.1", "pcbs": "0.5"}}', '["https://example.com/soil1.jpg"]', 2, 0, NULL, 2, '2024-03-03 10:00:00', NULL, NULL),
(6, 1, '{"pm25": "35.2", "pm10": "68.4", "so2": "12.3", "no2": "28.7", "co": "1.2", "o3": "85.3"}', '["https://example.com/air1.jpg"]', 4, 2, 'PM2.5数值偏高，需要复测', 3, '2024-03-04 10:00:00', '2024-03-04 14:00:00', 5),
(7, 1, '{"pm25": "42.1", "pm10": "75.8", "so2": "15.6", "no2": "32.1", "co": "1.8", "o3": "78.9"}', '["https://example.com/air2.jpg"]', 2, 0, NULL, 3, '2024-03-05 10:00:00', NULL, NULL);

-- =============================================
-- 20. 公告表
-- =============================================
CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '公告标题',
  type VARCHAR(50) NOT NULL COMMENT '公告类型：work_spec/maintenance_reminder/system_notification',
  content TEXT COMMENT '公告内容',
  publisher_id BIGINT COMMENT '发布人ID',
  status TINYINT DEFAULT 0 COMMENT '0草稿 1定时发布 2已发布 3已撤回',
  publish_time DATETIME COMMENT '定时发布时间',
  target_scope VARCHAR(200) COMMENT '受众范围',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =============================================
-- 24. 项目成员关联表
-- =============================================
CREATE TABLE IF NOT EXISTS project_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(50) NOT NULL COMMENT '角色: admin-管理员, collector-采集员, auditor-审核员, viewer-查看者',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_user (project_id, user_id),
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员关联表';
