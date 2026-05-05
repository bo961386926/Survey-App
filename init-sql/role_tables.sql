-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) UNIQUE NOT NULL,
  role_name VARCHAR(100) NOT NULL,
  permissions JSON,
  status TINYINT DEFAULT 1,
  sort INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入角色数据
INSERT IGNORE INTO sys_role (role_code, role_name, permissions, sort) VALUES
('admin', '管理员', '["*"]', 1),
('project_manager', '项目负责人', '["project:view","project:edit","point:view","point:edit","template:bind","export:project"]', 2),
('auditor', '审核员', '["point:view","audit:view","audit:pass","audit:reject","export:audit"]', 3),
('surveyor', '采集员', '["point:view","survey:create","survey:edit","survey:submit"]', 4),
('collab', '第三方协作', '["point:view","survey:assist"]', 5);

-- 为admin用户分配管理员角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
