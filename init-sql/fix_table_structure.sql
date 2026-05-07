-- 修复所有表结构以匹配后端代码
USE survey_db;

-- 1. 修复 survey_point 表
ALTER TABLE survey_point
  ADD COLUMN point_code VARCHAR(50) COMMENT '点位编号' AFTER id,
  ADD COLUMN section_id BIGINT COMMENT '标段ID' AFTER project_id,
  ADD COLUMN outfall_type VARCHAR(50) COMMENT '排口类型' AFTER section_id,
  ADD COLUMN region VARCHAR(100) COMMENT '行政区' AFTER latitude,
  ADD COLUMN assignee_id BIGINT COMMENT '分配人ID' AFTER region,
  ADD COLUMN collector_id BIGINT COMMENT '采集人ID' AFTER assignee_id,
  ADD COLUMN abnormal_tag VARCHAR(200) COMMENT '异常标签' AFTER status,
  ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '1已删除 0未删除' AFTER abnormal_tag,
  ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
  ADD INDEX idx_section (section_id),
  ADD INDEX idx_outfall_type (outfall_type),
  ADD INDEX idx_collector (collector_id);

-- 2. 修复 survey_result 表
ALTER TABLE survey_result
  CHANGE COLUMN version version_no INT DEFAULT 1 COMMENT '版本号' AFTER point_id,
  CHANGE COLUMN survey_user survey_user_id BIGINT COMMENT '勘查人员ID' AFTER audit_remark,
  ADD COLUMN template_version_id BIGINT COMMENT '模板版本ID' AFTER version_no,
  ADD COLUMN result_status TINYINT DEFAULT 0 COMMENT '结果状态：0草稿 1已提交 2待审核 3已通过 4已驳回 5已归档' AFTER images,
  ADD COLUMN optimistic_lock_version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER survey_user_id,
  ADD COLUMN submit_time DATETIME COMMENT '提交时间' AFTER optimistic_lock_version,
  ADD COLUMN audit_time DATETIME COMMENT '审核时间' AFTER submit_time,
  ADD COLUMN auditor_id BIGINT COMMENT '审核人ID' AFTER audit_time,
  ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '1已删除 0未删除' AFTER auditor_id,
  ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
  DROP COLUMN is_latest,
  ADD INDEX idx_result_status (result_status),
  ADD INDEX idx_survey_user (survey_user_id),
  ADD INDEX idx_auditor (auditor_id);

-- 3. 检查 sys_user 表，添加缺失的字段
-- 注意: 字段名映射: realName -> real_name
ALTER TABLE sys_user
  CHANGE COLUMN real_name real_name VARCHAR(50) COMMENT '真实姓名';

-- 4. 验证表结构
SELECT '=== survey_point 表结构 ===' AS info;
SHOW COLUMNS FROM survey_point;

SELECT '=== survey_result 表结构 ===' AS info;
SHOW COLUMNS FROM survey_result;

SELECT '=== sys_user 表结构 ===' AS info;
SHOW COLUMNS FROM sys_user;

SELECT '所有表结构修复完成！' AS status;
