-- =============================================
-- 缺失字段修复脚本
-- 修复：project_section 缺少 is_key_area 列
-- =============================================

-- 复用 tenant.sql 中已定义的工具存储过程
CALL add_column_if_not_exists('project_section', 'is_key_area', 'TINYINT DEFAULT 0 COMMENT ''是否重点区域 0-否 1-是''');
CALL add_index_if_not_exists('project_section', 'idx_key_area', 'is_key_area');
