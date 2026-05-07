#!/bin/bash

# ========================================
# 快速部署脚本 - 优化版
# 特点：利用Maven本地缓存，加速构建
# ========================================

set -e

echo "======================================"
echo "  Survey-App 快速部署（优化版）"
echo "======================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    exit 1
fi

# 检查.env
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo -e "${YELLOW}⚠ 未找到.env文件，从.env.example复制...${NC}"
        cp .env.example .env
        echo -e "${YELLOW}请编辑.env文件配置环境变量 ${NC}"
    else
        echo -e "${RED}错误: 缺少 .env.example 文件${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${YELLOW}选择构建方式：${NC}"
echo "1) 本地编译 + Docker打包（推荐，最快 ~30秒）"
echo "2) Docker完整构建（较慢 ~3-5分钟）"
echo ""
read -p "请选择 [1/2] (默认1): " choice

if [ "$choice" = "2" ]; then
    # 方案2: 完整Docker构建
    echo ""
    echo -e "${CYAN}正在使用Docker完整构建...${NC}"
    docker compose down
    docker compose up -d --build
    echo ""
    echo -e "${GREEN}✓ 部署完成！${NC}"
else
    # 方案1: 本地编译（利用Maven缓存）
    echo ""
    echo -e "${CYAN}[1/4] 本地编译Java项目...${NC}"
    cd admin-backend

    # 检查Maven
    if command -v mvn &> /dev/null; then
        MAVEN_CMD="mvn"
    elif [ -f "apache-maven-3.9.6/bin/mvn" ]; then
        MAVEN_CMD="./apache-maven-3.9.6/bin/mvn"
    else
        echo -e "${YELLOW}未找到Maven，切换为Docker完整构建...${NC}"
        cd ..
        docker compose down
        docker compose up -d --build
        exit 0
    fi

    # 本地编译（利用~/.m2缓存）
    $MAVEN_CMD clean package -DskipTests -q
    echo -e "${GREEN}✓ 编译完成${NC}"
    cd ..

    echo ""
    echo -e "${CYAN}[2/4] 停止现有服务...${NC}"
    docker compose down

    echo ""
    echo -e "${CYAN}[3/4] 快速构建Docker镜像（仅打包）...${NC}"
    # 使用优化版Dockerfile，打标签让 docker compose 能引用
    docker build -f admin-backend/Dockerfile.fast -t survey-admin-backend:latest ./admin-backend

    echo ""
    echo -e "${CYAN}[4/4] 启动服务...${NC}"
    docker compose up -d

    echo ""
    echo -e "${GREEN}✓ 部署完成！${NC}"
fi

echo ""
echo "等待服务启动..."
sleep 10

echo ""
echo "======================================"
echo "  服务状态"
echo "======================================"
docker compose ps

echo ""
echo "======================================"
echo "  访问地址"
echo "======================================"
BACKEND_PORT=${BACKEND_PORT:-8081}
echo -e "${GREEN}后端API:${NC} http://localhost:${BACKEND_PORT}"
echo -e "${GREEN}API文档:${NC} http://localhost:${BACKEND_PORT}/doc.html"
echo -e "${GREEN}健康检查:${NC} http://localhost:${BACKEND_PORT}/api/v1/health"
echo ""
