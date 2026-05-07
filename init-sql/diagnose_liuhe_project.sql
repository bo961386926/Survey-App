-- ============================================
-- 诊断"六合项目"数据问题
-- 使用方式: 
--   1. MySQL命令行: mysql -uroot -proot survey_db < diagnose_liuhe_project.sql
--   2. 或者在MySQL客户端中直接执行
-- ============================================

USE survey_db;

-- ============================================
-- 1. 查看"六合项目"的基本信息
-- ============================================
SELECT 
    id AS '项目ID',
    project_name AS '项目名称',
    project_code AS '项目编号',
    manager AS '负责人',
    status AS '状态码',
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '进行中'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已完成'
        WHEN 4 THEN '已归档'
        ELSE '未知状态'
    END AS '状态说明',
    region AS '区域',
    start_date AS '开始日期',
    end_date AS '结束日期',
    point_count AS '点位总数',
    completed_count AS '已完成数',
    pending_audit_count AS '待审核数',
    create_time AS '创建时间',
    update_time AS '更新时间'
FROM project
WHERE project_name LIKE '%六合%'
   OR project_code LIKE '%六合%'
ORDER BY create_time DESC;

-- ============================================
-- 2. 查看该项目关联的所有点位
-- ============================================
SELECT 
    sp.id AS '点位ID',
    sp.point_code AS '点位编号',
    sp.point_name AS '点位名称',
    sp.status AS '点位状态',
    sp.longitude AS '经度',
    sp.latitude AS '纬度',
    sp.create_time AS '创建时间'
FROM survey_point sp
INNER JOIN project p ON sp.project_id = p.id
WHERE p.project_name LIKE '%六合%'
   OR p.project_code LIKE '%六合%'
ORDER BY sp.create_time DESC;

-- ============================================
-- 3. 查看该项目关联的模板绑定
-- ============================================
SELECT 
    sptb.id AS '绑定ID',
    sptb.outfall_type AS '排口类型',
    sptb.template_id AS '模板ID',
    sptb.template_version_id AS '模板版本ID',
    st.template_name AS '模板名称'
FROM survey_point_template_binding sptb
INNER JOIN project p ON sptb.project_id = p.id
LEFT JOIN survey_template st ON sptb.template_id = st.id
WHERE p.project_name LIKE '%六合%'
   OR p.project_code LIKE '%六合%';

-- ============================================
-- 4. 查看所有项目的状态分布（用于对比）
-- ============================================
SELECT 
    status AS '状态码',
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '进行中'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已完成'
        WHEN 4 THEN '已归档'
        ELSE '未知'
    END AS '状态名称',
    COUNT(*) AS '项目数量',
    GROUP_CONCAT(project_name SEPARATOR ', ') AS '项目名称列表'
FROM project
GROUP BY status
ORDER BY status;

-- ============================================
-- 5. 诊断结论
-- ============================================
SELECT '=== 诊断说明 ===' AS '';
SELECT '如果发现六合项目的状态是 1（进行中），则需要先暂停才能删除' AS '';
SELECT '暂停方法：UPDATE project SET status = 2 WHERE project_name LIKE "%六合%";' AS '';
SELECT '然后删除：DELETE FROM project WHERE project_name LIKE "%六合%";' AS '';
SELECT '' AS '';
SELECT '如果项目状态不是1，请检查是否有其他约束导致删除失败' AS '';
