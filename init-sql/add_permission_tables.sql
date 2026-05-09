-- =============================================
-- 迁移脚本：新增权限表体系
-- 说明：
--   sys_permission: 权限码目录表，记录所有可用的权限
--   sys_role_permission: 角色-权限关联表（可选，如果保留 role.permissions 字符串可暂不启用）
-- =============================================

-- 1. 权限目录表
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

-- 2. 角色-权限关联表（可选：当前角色权限仍存储在 sys_role.permissions 字符串中）
--    此表用于权限选择器界面显示角色的关联关系，方便查询
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm (role_id, perm_code),
  INDEX idx_role (role_id),
  INDEX idx_perm (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
