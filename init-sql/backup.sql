-- ============================================
-- Survey-App 备份脚本 (MySQL Shell 8.0+)
-- 使用方式: mysqlsh --sql survey_db@localhost -f backup.sql
-- 或直接: mysql -uroot -proot survey_db < backup.sql
-- ============================================

-- ============================================
-- 备份: 导出所有表结构 (用于重建数据库)
-- ============================================
-- 在 Docker 容器内执行:
--   mysqldump -uroot -proot -d survey_db > schema_backup.sql

-- ============================================
-- 备份: 导出所有数据 (不含结构)
-- ============================================
-- 在 Docker 容器内执行:
--   mysqldump -uroot -proot survey_db -t > data_backup.sql

-- ============================================
-- 完整备份命令 (推荐在宿主机执行)
-- ============================================
-- 备份整个数据库:
--   docker exec survey-mysql mysqldump -uroot -proot survey_db > survey_db_backup_$(date +%Y%m%d_%H%M%S).sql
--
-- 备份单个表:
--   docker exec survey-mysql mysqldump -uroot -proot survey_db sys_user > sys_user_backup.sql
--
-- 恢复数据库:
--   cat survey_db_backup.sql | docker exec -i survey-mysql mysql -uroot -proot survey_db

-- ============================================
-- 定期备份脚本 (保存为 backup.sh，添加 cron 定时任务)
-- ============================================
-- #!/bin/bash
-- BACKUP_DIR="/path/to/backups"
-- DATE=$(date +%Y%m%d_%H%M%S)
-- docker exec survey-mysql mysqldump -uroot -proot survey_db > "$BACKUP_DIR/survey_db_$DATE.sql"
-- find "$BACKUP_DIR" -name "survey_db_*.sql" -mtime +7 -delete  # 删除7天前的备份
