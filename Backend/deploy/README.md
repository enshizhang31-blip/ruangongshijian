# deploy 目录说明

本目录上传到服务器，放在 `app.jar` 同级。

---

## 目录结构

```
服务器目录 (如 /www/wwwroot/salemanager)/
├── app.jar                    ← 手动上传
├── deploy.sh                  ← 部署 (停止→初始化DB→启动)
├── init-database.sh           ← 数据库初始化 (MySQL + MongoDB)
├── mongo-init.js              ← MongoDB 集合+索引
└── sql/
    ├── schema.sql             ← MySQL DDL (23张表)
    └── seed-data.sql          ← MySQL DML (角色,admin,菜单)
```

---

## 部署

```bash
# 1. 构建 JAR
cd Backend && mvn clean package -DskipTests

# 2. 上传
scp target/*.jar user@server:/www/wwwroot/salemanager/app.jar
scp -r deploy/* user@server:/www/wwwroot/salemanager/

# 3. 执行
ssh user@server "cd /www/wwwroot/salemanager && bash deploy.sh"
```

## deploy.sh 流程

1. 停止旧进程
2. 初始化数据库 (MySQL + MongoDB)
3. 启动 nohup java -jar
4. curl 验证

## 运维

```bash
tail -f logs/app.log          # 日志
pkill -f java.*app.jar        # 停止
bash deploy.sh                # 重启
bash init-database.sh         # 重建数据库
```