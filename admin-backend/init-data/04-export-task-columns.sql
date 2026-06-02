-- Migration: Add missing columns to export_task table for PDF export support
-- Created: 2026-06-01

ALTER TABLE export_task ADD COLUMN IF NOT EXISTS point_id BIGINT COMMENT '点位ID（用于PDF导出）' AFTER project_id;
ALTER TABLE export_task ADD COLUMN IF NOT EXISTS result_id BIGINT COMMENT '勘察结果ID（用于PDF导出）' AFTER point_id;
ALTER TABLE export_task ADD COLUMN IF NOT EXISTS file_name VARCHAR(255) COMMENT '磁盘文件名' AFTER file_url;
