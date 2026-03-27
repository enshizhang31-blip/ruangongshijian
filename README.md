# 销售管理系统 (SaleManager)

企业级销售管理平台，支持小程序 + Web 端管理。

## 项目结构

```
SaleManagerSys
├── Backed/                 # 后端服务 (Java SpringBoot)
├── Fronted/                # Web 管理端 (Vue3)
├── Uniapp/                 # 小程序端 (uni-app)
├── docs/                   # 项目文档
└── sql/                    # 数据库脚本
```

## 技术栈

### 后端 (Backed)
- SpringBoot 2.7.x
- MyBatis-Plus
- MySQL 8.0
- SpringSecurity
- Redis

### 前端 (Fronted)
- Vue 3.5.x + TypeScript
- Vue Router 5
- Pinia 3 (状态管理)
- Vite 7 (构建工具)
- Tailwind CSS (UI 样式)
- ESLint + Prettier (代码规范)

### 小程序 (Uniapp)
- uni-app
- uView 2.0

## 模块功能

| 模块 | 说明 |
|------|------|
| 用户管理 | 管理员、销售人员账号与权限 |
| 商品管理 | 商品类别、商品信息 |
| 客户管理 | 客户资料、新增、编辑、归档 |
| 销售管理 | 销售明细录入、多方式录入 |
| 数据统计 | 多维度销售数据统计 |

## 开发

```bash
# 前端依赖安装
cd Fronted
npm install

# 前端开发
npm run dev

# 前端构建生产
npm run build
```
