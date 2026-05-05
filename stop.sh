#!/bin/bash

# ========================================
# Survey-App Docker 停止脚本
# ========================================

set -e

echo "=========================================="
echo "  Survey-App Docker 停止脚本"
echo "=========================================="
echo ""

# 停止所有容器
echo "🛑 停止所有容器..."
docker compose down

echo ""
echo "✅ 服务已停止"
echo ""
echo "📋 如需完全清理数据卷，请执行："
echo "   docker-compose down -v"
echo ""
