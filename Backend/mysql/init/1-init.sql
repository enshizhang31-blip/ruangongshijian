-- 初始化脚本：为 root 用户授权远程访问
-- 此脚本在容器首次初始化时自动执行

-- 授予 root 从任意主机连接的权限
ALTER USER 'root'@'%' IDENTIFIED BY 'root123';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
