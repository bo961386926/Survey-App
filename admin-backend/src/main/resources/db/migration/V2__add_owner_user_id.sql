CREATE TABLE IF NOT EXISTS sys_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
  project_id BIGINT COMMENT '所属项目 ID',
  plot_code VARCHAR(255) COMMENT '地块编号',
  precision_requirement VARCHAR(255) COMMENT '精度要求',
  sensor_type VARCHAR(255) COMMENT '传感器类型',
  priority INT COMMENT '优先级:0普通 1重要 2紧急',
  description TEXT COMMENT '任务描述',
  status INT COMMENT '状态:0待分配 1进行中 2已完成 3已逾期 4已终止',
  deadline DATETIME COMMENT '截止日期',
  assignee_id BIGINT COMMENT '指派执行人 ID',
  owner_user_id BIGINT COMMENT '创建人/负责人 ID',
  subtasks TEXT COMMENT '子任务清单 JSON 字符串',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘察指派任务表';

-- Add owner_user_id column if it does not exist (for existing tables)
ALTER TABLE sys_task ADD COLUMN IF NOT EXISTS owner_user_id BIGINT COMMENT '创建人/负责人 ID';
