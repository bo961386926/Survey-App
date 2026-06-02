-- 青泓项目勘察信息采集与审核系统 - 新增任务指派中心表
-- 日期: 2026-05-31

USE survey_db;

CREATE TABLE IF NOT EXISTS sys_task (
  id BIGINT PRIMARY KEY COMMENT '任务ID (雪花算法生成)',
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  plot_code VARCHAR(50) COMMENT '具体地块编号',
  precision_requirement VARCHAR(50) COMMENT '精度要求',
  sensor_type VARCHAR(50) COMMENT '传感器类型',
  priority TINYINT DEFAULT 0 COMMENT '0普通 1重要 2紧急',
  description TEXT COMMENT '任务目标/描述',
  status TINYINT DEFAULT 0 COMMENT '0待分配 1进行中 2已完成 3已逾期 4已终止',
  deadline DATETIME COMMENT '截止日期',
  assignee_id BIGINT COMMENT '指派执行人ID',
  category VARCHAR(100) COMMENT '任务类型/类别',
  subtasks JSON COMMENT '子任务清单，JSON数组',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_project (project_id),
  INDEX idx_assignee (assignee_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勘察指派任务表';
