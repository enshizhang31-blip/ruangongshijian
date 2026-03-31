# Docker 部署指南

## 快速启动

### 1. 启动数据库容器

```bash
cd Backend
docker-compose up -d
```

### 2. 查看容器状态

```bash
docker-compose ps
```

### 3. 查看日志

```bash
# MySQL 日志
docker-compose logs mysql

# MongoDB 日志
docker-compose logs mongodb

# Redis 日志
docker-compose logs redis
```

---

## 服务信息

| 服务 | 端口 | 用户名 | 密码 |
|------|------|--------|------|
| MySQL | 3306 | salemanager | salemanager123 |
| MongoDB | 27017 | salemanager | salemanager123 |
| Redis | 6379 | - | - |
| Root(MySQL) | 3306 | root | root123 |

---

## 初始化数据库

首次启动后，执行初始化SQL：

```bash
# 导入数据库结构
docker-compose exec mysql mysql -usalemanager -psalemanager123 sale_manager < ../sql/init.sql
```

或通过 MySQL 客户端连接后执行 `sql/init.sql`。

---

## 停止服务

```bash
docker-compose down
```

---

## 重新构建后端镜像

```bash
docker build -t salemanager-backend:latest .
```

---

## Docker Compose 说明

### 创建的网络
- `salemanager-network`：bridge 网络，容器间互通

### 数据卷
- `mysql-data`：MySQL 数据持久化
- `mongo-data`：MongoDB 数据持久化
- `redis-data`：Redis 数据持久化

---

## Redis 使用场景

| 用途 | 说明 |
|------|------|
| 汇率缓存 | 存储汇率数据，2小时过期 |
| 关联模块 | 国际化 - 小程序端商品多货币显示 |
