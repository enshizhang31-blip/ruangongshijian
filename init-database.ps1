# 数据库一键初始化脚本（MySQL + MongoDB）
# 使用方法: .\init-database.ps1

$ProjectRoot = Split-Path -Parent $PSCommandPath
$ComposeFile = "$ProjectRoot\Backend\docker-compose.yml"
$InitFile = "$ProjectRoot\sql\init.sql"
$MySQLContainer = "salemanager-mysql"
$MongoContainer = "salemanager-mongo"

Write-Host "========================================"
Write-Host "Database Initialization Script"
Write-Host "========================================"

# ============ MySQL ============
Write-Host ""
Write-Host "========== MySQL =========="

Write-Host "[1/6] Stopping MySQL container..."
docker compose -f $ComposeFile down mysql 2>$null

Write-Host "[2/6] Removing old MySQL data volume..."
docker volume rm backend_mysql-data 2>$null

Write-Host "[3/6] Starting MySQL container..."
docker compose -f $ComposeFile up -d mysql

Write-Host "Waiting for MySQL to be ready..."
$maxWait = 30
$waited = 0
while ($waited -lt $maxWait) {
    $result = docker exec $MySQLContainer mysqladmin ping -uroot -proot123 2>$null
    if ($result -match "mysqld is alive") {
        Write-Host "MySQL is ready!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $waited += 2
    Write-Host "Waiting... ($waited seconds)"
}

Start-Sleep -Seconds 8

Write-Host "Creating database..."
for ($i = 1; $i -le 3; $i++) {
    $result = docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 -e "DROP DATABASE IF EXISTS sale_manager; CREATE DATABASE sale_manager;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        break
    }
    Write-Host "Retry $i..."
    Start-Sleep -Seconds 3
}

Start-Sleep -Seconds 2

Write-Host "Copying init file..."
docker cp $InitFile ${MySQLContainer}:/tmp/init.sql

Write-Host "Executing init script..."
for ($i = 1; $i -le 3; $i++) {
    $result = docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 sale_manager -e "source /tmp/init.sql" 2>&1
    if ($LASTEXITCODE -eq 0) {
        break
    }
    Write-Host "Retry $i..."
    Start-Sleep -Seconds 3
}

# ============ MongoDB ============
Write-Host ""
Write-Host "========== MongoDB =========="

Write-Host "[4/6] Stopping MongoDB container..."
docker compose -f $ComposeFile down mongodb 2>$null

Write-Host "[5/6] Removing old MongoDB data volume..."
docker volume rm backend_mongo-data 2>$null

Write-Host "[6/6] Starting MongoDB container..."
docker compose -f $ComposeFile up -d mongodb

Write-Host "Waiting for MongoDB to be ready..."
$maxWait = 30
$waited = 0
while ($waited -lt $maxWait) {
    $result = docker exec $MongoContainer mongosh --quiet --eval "db.runCommand({ ping: 1 })" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "MongoDB is ready!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $waited += 2
    Write-Host "Waiting... ($waited seconds)"
}

Write-Host ""
Write-Host "========================================"
Write-Host "Database initialization completed!"
Write-Host "========================================"

Write-Host ""
Write-Host "MySQL tables created:"
docker exec $MySQLContainer mysql -uroot -proot123 sale_manager -e "SHOW TABLES;"

Write-Host ""
Write-Host "Admin account:"
Write-Host "  Username: admin"
Write-Host "  Password: 123456"

Write-Host ""
Write-Host "Data initialization: start backend then call API:"
Write-Host "  GET http://localhost:8080/api/admin/system/data/init"
