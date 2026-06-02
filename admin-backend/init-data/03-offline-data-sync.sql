-- 离线数据同步表
-- 用于支持移动端离线数据采集和同步

CREATE TABLE IF NOT EXISTS offline_data_sync (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  device_id VARCHAR(64) NOT NULL COMMENT '设备ID（移动端设备唯一标识）',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  data_type VARCHAR(50) NOT NULL COMMENT '数据类型：survey_result-勘察结果, photo-照片, location-位置',
  data_id VARCHAR(64) NOT NULL COMMENT '数据ID（业务数据ID）',
  data_content JSON COMMENT '数据内容（JSON格式）',
  sync_status TINYINT DEFAULT 0 COMMENT '同步状态：0待同步 1同步中 2已同步 3同步失败',
  retry_count INT DEFAULT 0 COMMENT '重试次数',
  max_retry_count INT DEFAULT 3 COMMENT '最大重试次数',
  error_message TEXT COMMENT '失败原因',
  client_create_time DATETIME COMMENT '客户端创建时间（离线时的时间）',
  server_receive_time DATETIME COMMENT '服务器接收时间',
  sync_complete_time DATETIME COMMENT '同步完成时间',
  version_no INT DEFAULT 1 COMMENT '版本号（用于冲突检测）',
  conflict_resolution VARCHAR(20) COMMENT '冲突解决方案：server-以服务器为准, client-以客户端为准, merge-合并',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_device_id (device_id),
  INDEX idx_user_id (user_id),
  INDEX idx_sync_status (sync_status),
  INDEX idx_data_type (data_type),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线数据同步记录表';
