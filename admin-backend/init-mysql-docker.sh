#!/bin/bash

# MySQL Docker初始化脚本
# 用于快速启动MySQL并初始化数据库

echo "🚀 开始初始化MySQL数据库..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 检查MySQL容器是否已存在
if docker ps -a | grep -q survey-mysql; then
    echo "⚠️  MySQL容器已存在"
    read -p "是否删除并重新创建？(y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  删除旧容器..."
        docker rm -f survey-mysql
    else
        echo "✅ 使用现有容器"
        MYSQL_RUNNING=$(docker ps | grep survey-mysql | wc -l)
        if [ "$MYSQL_RUNNING" -eq 0 ]; then
            echo "🔄 启动容器..."
            docker start survey-mysql
        fi
        echo "✅ MySQL容器已就绪"
        echo "📌 连接信息:"
        echo "   Host: localhost"
        echo "   Port: 3306"
        echo "   Database: survey_db"
        echo "   Username: root"
        echo "   Password: root"
        exit 0
    fi
fi

# 启动MySQL容器
echo "📦 启动MySQL容器..."
docker run -d \
  --name survey-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=survey_db \
  -p 3306:3306 \
  -v $(pwd)/init-data:/docker-entrypoint-initdb.d \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

echo "⏳ 等待MySQL启动..."
sleep 15

# 创建初始化数据目录
mkdir -p init-data

# 复制SQL文件
echo "📋 准备初始化脚本..."
cp ../init-sql/init.sql init-data/01-init.sql
cp ../init-sql/role_tables.sql init-data/02-role-tables.sql
cp ../init-sql/offline_data_sync.sql init-data/03-offline-data-sync.sql

# 重启容器以执行初始化脚本
echo "🔄 重启容器以执行初始化..."
docker rm -f survey-mysql
docker run -d \
  --name survey-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=survey_db \
  -p 3306:3306 \
  -v $(pwd)/init-data:/docker-entrypoint-initdb.d \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

echo "⏳ 等待MySQL初始化完成..."
sleep 20

# 验证数据库
echo "✅ 验证数据库初始化..."
docker exec survey-mysql mysql -uroot -proot -e "USE survey_db; SHOW TABLES;"

echo ""
echo "=========================================="
echo "✅ MySQL数据库初始化完成！"
echo "=========================================="
echo ""
echo "📌 连接信息:"
echo "   Host: localhost"
echo "   Port: 3306"
echo "   Database: survey_db"
echo "   Username: root"
echo "   Password: root"
echo ""
echo "📌 已初始化的表:"
docker exec survey-mysql mysql -uroot -proot survey_db -e "SHOW TABLES;" 2>/dev/null | tail -n +2
echo ""
echo "💡 提示:"
echo "   - 停止容器: docker stop survey-mysql"
echo "   - 启动容器: docker start survey-mysql"
echo "   - 删除容器: docker rm -f survey-mysql"
echo "   - 查看日志: docker logs survey-mysql"
echo ""
