#!/bin/bash

# ========================================
# Survey-App 快速部署脚本
# 用途：重新构建并部署后端服务
# ========================================

set -e  # 遇到错误立即退出

echo "======================================"
echo "  Survey-App 部署脚本"
echo "======================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Docker是否运行
echo -e "${YELLOW}[1/5] 检查Docker环境...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker未运行，请先启动Docker Desktop${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker运行正常${NC}"
echo ""

# 停止现有服务
echo -e "${YELLOW}[2/5] 停止现有服务...${NC}"
docker compose down
echo -e "${GREEN}✓ 服务已停止${NC}"
echo ""

# 重新构建镜像
echo -e "${YELLOW}[3/5] 重新构建后端镜像（这可能需要3-5分钟）...${NC}"
docker compose build admin-backend
echo -e "${GREEN}✓ 镜像构建完成${NC}"
echo ""

# 启动所有服务
echo -e "${YELLOW}[4/5] 启动所有服务...${NC}"
docker compose up -d
echo -e "${GREEN}✓ 服务已启动${NC}"
echo ""

# 等待服务就绪
echo -e "${YELLOW}[5/5] 等待服务就绪...${NC}"
echo "等待MySQL和Redis健康检查..."
sleep 15

# 检查服务状态
echo ""
echo "======================================"
echo "  服务状态"
echo "======================================"
docker compose ps

echo ""
echo "======================================"
echo "  访问地址"
echo "======================================"
echo -e "${GREEN}后端API:${NC} http://localhost:8081"
echo -e "${GREEN}API文档:${NC} http://localhost:8081/doc.html"
echo -e "${GREEN}健康检查:${NC} http://localhost:8081/api/v1/health"
echo ""

# 测试健康检查
echo "======================================"
echo "  健康检查测试"
echo "======================================"
if curl -s http://localhost:8081/api/v1/health | grep -q "UP"; then
    echo -e "${GREEN}✓ 后端服务运行正常${NC}"
else
    echo -e "${YELLOW}⚠ 服务可能还在启动中，请稍后刷新测试${NC}"
fi

echo ""
echo "======================================"
echo -e "${GREEN}  部署完成！${NC}"
echo "======================================"
echo ""
echo "常用命令："
echo "  查看日志:     docker compose logs -f admin-backend"
echo "  停止服务:     docker compose down"
echo "  重启服务:     docker compose restart"
echo "  查看状态:     docker compose ps"
echo ""
