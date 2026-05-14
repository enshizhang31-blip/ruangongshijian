# deploy 目录说明

本目录可直接拷贝到服务器，包含运行全套服务的所有文件。

---

## 目录结构

```
deploy/                        ← 直接上传此目录到服务器
├── app.jar                    ← 构建产物 (mvn package 后放入)
├── Dockerfile                 ← 生产镜像 (基于预构建 JAR)
├── docker-compose.yml         ← 全套服务编排 (MySQL+Mongo+Redis+App)
├── application-prod.yml       ← 生产环境配置 (环境变量注入)
├── deploy.sh                  ← 一键部署脚本
├── mongo-init.js              ← MongoDB 初始化 (translation_units 集合+索引)
├── .env.example               ← 环境变量模板
├── .env                       ← 实际环境变量 (不上传git, 在服务器上创建)
└── sql/
    ├── schema.sql             ← MySQL DDL (23张表)
    └── seed-data.sql          ← MySQL DML (角色/管理员/菜单)
```

## 部署步骤

### 1. 构建 JAR (开发机)
```bash
cd Backend
./mvnw clean package -DskipTests
cp target/Backend-0.0.1-SNAPSHOT.jar deploy/app.jar
```

### 2. 上传到服务器
```bash
# 先打包
tar -czf salemanager-deploy.tar.gz deploy/

# 上传
scp salemanager-deploy.tar.gz user@your-server:/srv/

# 服务器上解压
ssh user@your-server
cd /srv && tar -xzf salemanager-deploy.tar.gz
cd deploy
```

### 3. 配置环境变量 (服务器上)
```bash
cp .env.example .env
vim .env   # 修改 MySQL/Mongo/Redis 密码和 JWT_SECRET
```

### 4. 启动
```bash
docker compose up -d
```

### 5. 验证
```bash
docker compose ps          # 所有服务 running
curl http://localhost:8080/actuator/health
```

## 常用运维命令

```bash
# 查看日志
docker compose logs -f app

# 重启单个服务
docker compose restart app

# 重新构建并启动 (代码更新后)
docker compose up -d --build app

# 停止全部
docker compose down

# 备份数据
docker compose exec mysql mysqldump -u salemanager -p sale_manager > backup.sql
```

## 首次部署后初始化种子数据
```bash
# 获取 token
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 用返回的 token 初始化完整数据
curl -X POST http://localhost:8080/api/admin/system/data/init \
  -H "Authorization: Bearer <token>"
```
