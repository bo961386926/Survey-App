-- 补充数据插入脚本（适配实际表结构）
USE survey_db;

-- 插入测试项目数据
INSERT INTO project (project_name, project_code, manager, status) VALUES
('青泓河水质监测项目', 'QH2024001', '张志明', 1),
('工业园区土壤污染调查', 'GY2024002', '李晓华', 1),
('城市空气质量监测网', 'CQ2024003', '王建国', 1),
('农业用地重金属检测', 'NY2024004', '陈美丽', 1);

-- 插入测试标段数据
INSERT INTO project_section (project_id, section_name, section_code, manager_id) VALUES
(1, '青泓河上游段', 'QH-S01', 2),
(1, '青泓河中游段', 'QH-S02', 2),
(1, '青泓河下游段', 'QH-S03', 2),
(2, 'A区', 'GY-S01', 3),
(2, 'B区', 'GY-S02', 3);

-- 插入测试点位数据（适配实际表结构）
INSERT INTO survey_point (project_id, point_name, longitude, latitude, status) VALUES
(1, '青泓河入河口', 120.12345678, 30.12345678, 2),
(1, '青泓河中游监测点', 120.23456789, 30.23456789, 3),
(1, '青泓河下游出口', 120.34567890, 30.34567890, 0),
(2, '工业区A区采样点1', 120.45678901, 30.45678901, 2),
(2, '工业区B区采样点2', 120.56789012, 30.56789012, 0),
(3, '市中心监测站', 120.67890123, 30.67890123, 3),
(3, '郊区监测站', 120.78901234, 30.78901234, 2);

-- 插入测试勘查结果数据（适配实际表结构）
INSERT INTO survey_result (point_id, form_data, images, version, is_latest, audit_status, audit_remark, survey_user) VALUES
(1, '{"ph": "7.2", "temperature": "25.5", "dissolved_oxygen": "8.1", "cod": "15.3", "bod5": "3.2"}', '["https://example.com/image1.jpg"]', 1, 1, 1, '数据正常', 'surveyor1'),
(2, '{"ph": "7.8", "temperature": "24.8", "dissolved_oxygen": "7.9", "cod": "18.7", "bod5": "4.1"}', '["https://example.com/image2.jpg","https://example.com/image3.jpg"]', 1, 1, 1, '审核通过', 'surveyor1'),
(4, '{"heavy_metals": {"lead": "25.3", "cadmium": "0.8", "chromium": "15.2"}, "organic_pollutants": {"pahs": "2.1", "pcbs": "0.5"}}', '["https://example.com/soil1.jpg"]', 1, 1, 0, NULL, 'surveyor1'),
(6, '{"pm25": "35.2", "pm10": "68.4", "so2": "12.3", "no2": "28.7", "co": "1.2", "o3": "85.3"}', '["https://example.com/air1.jpg"]', 1, 1, 2, 'PM2.5数值偏高，需要复测', 'surveyor2'),
(7, '{"pm25": "42.1", "pm10": "75.8", "so2": "15.6", "no2": "32.1", "co": "1.8", "o3": "78.9"}', '["https://example.com/air2.jpg"]', 1, 1, 0, NULL, 'surveyor3');

-- 验证结果
SELECT '数据插入完成！' AS status;
SELECT '项目:' AS info; SELECT * FROM project;
SELECT '标段:' AS info; SELECT * FROM project_section;
SELECT '点位:' AS info; SELECT * FROM survey_point;
SELECT '勘查结果:' AS info; SELECT * FROM survey_result;
