#!/bin/bash

# ========================================
# Survey-App Docker 部署脚本
# 功能：一键部署应用到Docker环境
# ========================================

set -e

echo "=========================================="
echo "  Survey-App Docker 部署脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
    exit 1
fi

# 检查Docker Compose (V2)
if ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装，请先安装Docker Compose${NC}"
    exit 1
fi

# 定义docker-compose命令
DC_CMD="docker compose"

echo -e "${GREEN}✓ Docker环境检查通过${NC}"
echo ""

# 检查.env文件
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠ 未找到.env文件，从.env.example复制...${NC}"
    cp .env.example .env
    echo -e "${YELLOW}请编辑.env文件配置环境变量${NC}"
    echo ""
fi

# 停止并清理旧容器
echo "📦 停止旧容器..."
$DC_CMD down

# 清理未使用的镜像
echo "🧹 清理未使用的镜像..."
docker system prune -f

# 构建并启动服务
echo ""
echo "🚀 开始构建和启动服务..."
$DC_CMD up -d --build

# 等待服务启动
echo ""
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 检查服务状态..."
$DC_CMD ps

# 健康检查
echo ""
echo "🏥 执行健康检查..."

# 检查MySQL
echo -n "  MySQL: "
if $DC_CMD exec -T mysql mysqladmin ping -h localhost &> /dev/null; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${RED}✗ 启动失败${NC}"
fi

# 检查Redis
echo -n "  Redis: "
if $DC_CMD exec -T redis redis-cli -a redis_password ping &> /dev/null; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${RED}✗ 启动失败${NC}"
fi

# 检查后端服务
echo -n "  Backend: "
sleep 20  # 等待后端完全启动
if curl -s http://localhost:8080/api/v1/health | grep -q "UP"; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${YELLOW}⚠ 可能还在启动中，请稍后检查${NC}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}  部署完成！${NC}"
echo "=========================================="
echo ""
echo "📱 访问地址："
echo "   - 后端API: http://localhost:8080"
echo "   - 健康检查: http://localhost:8080/api/v1/health"
echo "   - API文档: http://localhost:8080/doc.html"
echo ""
echo "📋 常用命令："
echo "   - 查看日志: docker-compose logs -f admin-backend"
echo "   - 停止服务: docker-compose down"
echo "   - 重启服务: docker-compose restart"
echo ""
echo "📝 数据库连接信息："
echo "   - Host: localhost:3306"
echo "   - Database: survey_db"
echo "   - Username: survey_user"
echo "   - Password: survey_password"
echo ""
