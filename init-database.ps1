# MySQL 数据库一键初始化脚本
# 使用方法: .\init-database.ps1

$ProjectRoot = Split-Path -Parent $PSCommandPath
$ComposeFile = "$ProjectRoot\Backend\docker-compose.yml"
$InitFile = "$ProjectRoot\sql\init.sql"
$MySQLContainer = "salemanager-mysql"

Write-Host "========================================"
Write-Host "MySQL Database Initialization Script"
Write-Host "========================================"

# 1. Stop and remove MySQL container
Write-Host "[1/3] Stopping MySQL container..."
docker compose -f $ComposeFile down mysql

# 2. Remove old data volume
Write-Host "[2/3] Removing old data volume..."
docker volume rm backend_mysql-data

# 3. Start MySQL container
Write-Host "[3/3] Starting MySQL container..."
docker compose -f $ComposeFile up -d mysql

# Wait for MySQL to be ready
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

# Wait more for MySQL to be fully ready
Start-Sleep -Seconds 8

# Execute init script with retry
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

# 导入示例数据
Write-Host "Copying seed data file..."
docker cp "$ProjectRoot\sql\seed-data.sql" ${MySQLContainer}:/tmp/seed-data.sql

Write-Host "Importing seed data..."
for ($i = 1; $i -le 3; $i++) {
    $result = docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 sale_manager -e "source /tmp/seed-data.sql" 2>&1
    if ($LASTEXITCODE -eq 0) {
        break
    }
    Write-Host "Retry $i..."
    Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "========================================"
Write-Host "Database initialization completed!"
Write-Host "========================================"
Write-Host ""
Write-Host "Tables created:"
docker exec $MySQLContainer mysql -uroot -proot123 sale_manager -e "SHOW TABLES;"
Write-Host ""
Write-Host "Admin account:"
Write-Host "  Username: admin"
Write-Host "  Password: 123456"

Write-Host ""
Write-Host "Data summary:"
docker exec $MySQLContainer mysql -uroot -proot123 sale_manager -e "SELECT 'customer' as tbl, COUNT(*) as cnt FROM customer UNION ALL SELECT 'goods', COUNT(*) FROM goods UNION ALL SELECT 'sku', COUNT(*) FROM sku UNION ALL SELECT 'order', COUNT(*) FROM \`order\` UNION ALL SELECT 'order_item', COUNT(*) FROM order_item;"
