-- =============================================
-- 修复 sys_role.permissions 数据格式
-- 将 JSON 数组格式转为纯逗号分隔字符串
-- 原格式: '["project:view","project:edit"]' → 'project:view,project:edit'
-- =============================================

-- 1. 去除前后方括号
UPDATE sys_role 
SET permissions = TRIM(BOTH '[]' FROM permissions)
WHERE permissions LIKE '[%';

-- 2. 去除所有双引号
UPDATE sys_role 
SET permissions = REPLACE(permissions, '"', '')
WHERE permissions LIKE '%"%';

-- 3. 去除多余空格
UPDATE sys_role 
SET permissions = TRIM(permissions);

-- 4. 验证结果
SELECT id, role_code, role_name, permissions FROM sys_role ORDER BY id;
