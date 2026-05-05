-- 测试数据

-- 插入测试用户（密码: admin123）
INSERT INTO sys_user (user_id, username, password, real_name, phone, status, first_login) VALUES
('admin-001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 1, 0),
('user-001', 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800138001', 1, 0);

-- 插入测试角色
INSERT INTO sys_role (role_code, role_name, status) VALUES
('admin', '系统管理员', 1),
('reviewer', '审核员', 1),
('collector', '采集员', 1);

-- 分配用户角色
INSERT INTO sys_user_role (user_id, role_code) VALUES
('admin-001', 'admin'),
('user-001', 'collector');
