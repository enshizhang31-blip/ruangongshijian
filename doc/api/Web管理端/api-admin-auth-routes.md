# Web管理端 - 认证与动态路由模块 API

> 关联文档：[需求详细设计.md - 第七章权限模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 权限模块](../2026-03-28-数据库设计.md)、[CLAUDE.md - 权限控制设计](../CLAUDE.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/admin/auth/login` | 员工登录 | 无 |
| GET | `/api/admin/auth/current` | 当前用户信息 | 需登录 |
| POST | `/api/admin/auth/logout` | 登出 | 需登录 |
| GET | `/api/admin/auth/routes` | 获取路由+权限数组 | 需登录 |

---

## POST /api/admin/auth/login - 员工登录

### 功能说明

Web管理端员工登录接口，使用用户名密码认证。

### 请求

- Method: `POST`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

### 响应

**成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400,
    "admin": {
      "id": 1,
      "username": "admin",
      "realName": "超级管理员",
      "roleId": 1,
      "roleName": "超级管理员",
      "roleCode": "SUPER_ADMIN",
      "permissions": ["*"],
      "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/system"]
    }
  }
}
```

**失败响应：**

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

### 业务逻辑

1. 校验用户名密码（BCrypt加密比对）
2. 查询员工角色和权限
3. 生成 JWT token（有效期24小时）
4. 更新最后登录时间
5. 记录操作日志

---

## GET /api/admin/auth/current - 当前用户信息

### 功能说明

获取当前登录员工的信息。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "超级管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "roleId": 1,
    "roleName": "超级管理员",
    "roleCode": "SUPER_ADMIN",
    "permissions": ["*"],
    "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/system"],
    "lastLoginAt": "2026-03-29 10:00:00"
  }
}
```

---

## POST /api/admin/auth/logout - 登出

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## GET /api/admin/auth/routes - 获取路由+权限数组

### 功能说明

获取当前员工的动态路由和权限数组，用于前端路由注册和按钮权限控制。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "routes": [
      {
        "path": "/dashboard",
        "name": "Dashboard",
        "component": "dashboard/index",
        "meta": { "title": "仪表盘", "icon": "HomeIcon" }
      },
      {
        "path": "/product",
        "name": "Product",
        "component": "product/index",
        "meta": { "title": "商品管理", "icon": "CubeIcon" },
        "children": [
          {
            "path": "/product/list",
            "name": "ProductList",
            "component": "product/List",
            "meta": { "title": "商品列表" }
          }
        ]
      },
      {
        "path": "/sn",
        "name": "SN",
        "component": "sn/index",
        "meta": { "title": "SN码管理", "icon": "QrCodeIcon" }
      },
      {
        "path": "/customer",
        "name": "Customer",
        "component": "customer/index",
        "meta": { "title": "客户管理", "icon": "UsersIcon" }
      },
      {
        "path": "/order",
        "name": "Order",
        "component": "order/index",
        "meta": { "title": "订单管理", "icon": "ShoppingCartIcon" }
      },
      {
        "path": "/statistics",
        "name": "Statistics",
        "component": "statistics/index",
        "meta": { "title": "统计报表", "icon": "ChartBarIcon" }
      },
      {
        "path": "/system",
        "name": "System",
        "component": "system/index",
        "meta": { "title": "系统管理", "icon": "CogIcon" }
      }
    ],
    "permissions": ["*"]
  }
}
```

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| `*` | 所有权限（超级管理员） |
| `spu:*` | 商品模块所有权限 |
| `spu:view` | 查看商品列表 |
| `spu:add` | 新增商品 |
| `spu:edit` | 编辑商品 |
| `spu:delete` | 删除商品 |
| `sku:*` | SKU模块所有权限 |
| `sn:*` | SN码模块所有权限 |
| `customer:*` | 客户模块所有权限 |
| `order:*` | 订单模块所有权限 |

### 前端使用示例

```javascript
// router.beforeEach 路由守卫
router.beforeEach((to, from, next) => {
  const { routes } = useUserStore()
  if (to.path === '/login' || routes.some(r => r.path === to.path)) {
    next()
  } else {
    next('/dashboard')
  }
})

// v-permission 按钮权限
<button v-permission="'spu:add'">新增商品</button>
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或token过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1003 | 账号已被禁用 |
| 1004 | 角色不存在 |

---

## 备注

1. JWT token 有效期为 24 小时
2. 前端需要存储 token，并添加在每次请求的 Header 中
3. 路由从数据库的 role.routes 字段读取，支持动态配置
4. 权限从数据库的 role.permissions 字段读取，用于按钮级控制
5. 登出后 token 失效，前端清除本地存储