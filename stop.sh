#!/bin/bash

# ========================================
# Survey-App Docker 停止脚本
# ========================================

set -e

echo "=========================================="
echo "  Survey-App Docker 停止脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 docker compose 是否可用
if ! docker compose version &> /dev/null; then
    echo "错误: Docker Compose V2 不可用"
    exit 1
fi

# 停止所有容器
echo "🛑 停止所有容器..."
docker compose down

echo ""
echo -e "${GREEN}✅ 服务已停止${NC}"
echo ""
echo "📋 如需完全清理数据（包括数据库和缓存），请执行："
echo "   docker compose down -v"
echo ""
echo "📋 如需重新启动，请执行："
echo "   ./deploy.sh"
echo ""
