-- ============================================
-- 检查数据字典表是否存在
-- ============================================

USE survey_db;

-- 检查表是否存在
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    CREATE_TIME,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME IN ('sys_dict', 'sys_dict_item')
ORDER BY TABLE_NAME;

-- 如果表存在，查看数据
SELECT '=== sys_dict 表数据 ===' AS info;
SELECT * FROM sys_dict LIMIT 10;

SELECT '=== sys_dict_item 表数据 ===' AS info;
SELECT * FROM sys_dict_item LIMIT 10;

-- 如果表不存在，显示提示
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '❌ 数据字典表不存在，请执行 init_dict_data.sql'
        ELSE '✅ 数据字典表存在'
    END AS check_result
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME = 'sys_dict';
