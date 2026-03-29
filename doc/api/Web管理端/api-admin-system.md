# Web管理端 - 系统管理模块 API

> 关联文档：[需求详细设计.md - 第七章权限模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 权限模块](../2026-03-28-数据库设计.md)、[CLAUDE.md - 权限控制设计](../CLAUDE.md)

---

## 接口列表

### 员工管理（含权限配置）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/user` | 员工列表 | system:user |
| GET | `/api/admin/user/{id}` | 员工详情（含权限明细） | system:user |
| POST | `/api/admin/user` | 新增员工（分配角色） | system:user |
| PUT | `/api/admin/user/{id}` | 编辑员工基本信息 | system:user |
| DELETE | `/api/admin/user/{id}` | 删除员工 | system:user |
| POST | `/api/admin/user/{id}/resetpwd` | 重置密码 | system:user |
| GET | `/api/admin/user/{id}/permissions` | 获取员工权限明细 | system:user |
| PUT | `/api/admin/user/{id}/permissions` | 更新员工权限 | system:user |
| PUT | `/api/admin/user/{id}/routes` | 更新员工路由 | system:user |

### 角色管理（仅模板）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/role` | 角色模板列表 | system:role |
| GET | `/api/admin/role/{id}` | 角色模板详情 | system:role |

> **说明**：角色仅作为预设模板使用，不可新增、编辑、删除（预设角色固定）。

### 菜单管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/menu` | 菜单列表 | system:menu |

> **说明**：菜单仅用于展示，不可新增、编辑、删除。

### 操作日志

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/log` | 操作日志列表 | system:log |
| GET | `/api/admin/log/{id}` | 日志详情 | system:log |

---

## 权限模型说明

```
┌─────────────────────────────────────────────────────────────────────┐
│  角色（预设模板）                                                     │
│  - 预设权限数组 permissions                                          │
│  - 预设路由数组 routes                                              │
│  - 不可修改：SUPER_ADMIN, OPERATOR, INPUTTER                       │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼ 创建时复制
┌─────────────────────────────────────────────────────────────────────┐
│  员工                                                               │
│  - role_id: 记录所属角色（仅用于显示）                                  │
│  - permissions: 权限数组                                          │
│  - routes: 路由数组                                                │
│                                                                  │
│  有效权限 = 员工的 permissions                                       │
│  有效路由 = 员工的 routes                                       │
│                                                                  │
│  ⚠️ 创建后与角色无关，修改角色不影响现有员工                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 员工管理接口

### GET /api/admin/user - 员工列表

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 搜索：用户名/姓名/手机号 |
| roleId | long | 否 | 角色ID |
| status | int | 否 | 状态：0禁用 1启用 |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "realName": "超级管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "roleId": 1,
        "roleName": "超级管理员",
        "roleCode": "SUPER_ADMIN",
        "departmentId": 1,
        "departmentName": "技术部",
        "status": 1,
        "statusName": "正常",
        "lastLoginAt": "2026-03-29 10:00:00",
        "createdAt": "2026-03-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 10
    }
  }
}
```

---

### GET /api/admin/user/{id} - 员工详情

### 功能说明

获取员工完整信息，包括角色和权限明细。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "zhangsan",
    "realName": "张三",
    "phone": "13900139000",
    "email": "zhangsan@example.com",
    "roleId": 2,
    "roleName": "运营主管",
    "roleCode": "OPERATOR",
    "departmentId": 1,
    "departmentName": "技术部",
    "status": 1,
    "lastLoginAt": "2026-03-28 10:00:00",
    "createdAt": "2026-03-01 10:00:00",
    "permissions": ["spu:*", "sku:*", "sn:*", "customer:*", "order:*", "product:export"],
    "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/product/export"]
  }
}
```

### 字段说明

| 字段 | 说明 |
|------|------|
| permissions | 员工持有的权限数组 |
| routes | 员工持有的路由数组 |

---

### POST /api/admin/user - 新增员工

### 功能说明

创建新员工，自动继承所选角色的权限和路由。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名（唯一） |
| password | string | 是 | 初始密码 |
| realName | string | 是 | 真实姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| roleId | long | 是 | 角色ID（决定初始权限） |
| departmentId | long | 否 | 部门ID |

**请求示例：**

```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "phone": "13900139000",
  "email": "zhangsan@example.com",
  "roleId": 2,
  "departmentId": 1
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "zhangsan",
    "roleName": "运营主管",
    "createdAt": "2026-03-29 10:00:00"
  }
}
```

### 业务逻辑

1. 校验用户名唯一性
2. 密码使用BCrypt加密存储
3. 自动继承角色的 permissions 和 routes 到员工记录
4. 记录操作日志

---

### PUT /api/admin/user/{id} - 编辑员工

### 功能说明

编辑员工的基本信息，不包括权限（权限单独接口）。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | string | 否 | 真实姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| roleId | long | 否 | 角色ID（变更会重新继承权限） |
| departmentId | long | 否 | 部门ID |

**请求示例：**

```json
{
  "realName": "张三（修改）",
  "phone": "13900139001",
  "roleId": 3
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务逻辑

- roleId 仅用于显示所属角色，不影响权限
- 权限直接存储在员工记录中

---

### GET /api/admin/user/{id}/permissions - 获取员工权限明细

### 功能说明

获取员工的权限数组。

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "role": {
      "id": 2,
      "name": "运营主管",
      "permissions": ["spu:*", "sku:*", "sn:*", "customer:*", "order:*"]
    },
    "permissions": ["spu:*", "sku:*", "sn:*", "customer:*", "order:*", "product:export", "statistics:view"]
  }
}
```

---

### PUT /api/admin/user/{id}/permissions - 更新员工权限

### 功能说明

直接管理员工的权限数组。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：add / remove / set |
| permissions | array | 是 | 权限数组 |

**请求示例：**

```json
{
  "action": "add",
  "permissions": ["spu:delete", "order:refund"]
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "permissions": ["spu:*", "sku:*", "sn:*", "customer:*", "order:*", "product:export", "spu:delete", "order:refund"]
  }
}
```

### action 说明

| action | 说明 |
|--------|------|
| add | 追加权限 |
| remove | 移除权限 |
| set | 直接设置权限（替换现有） |

---

### PUT /api/admin/user/{id}/routes - 更新员工路由

### 功能说明

直接管理员工的路由数组。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | string | 是 | 操作类型：add / remove / set |
| routes | array | 是 | 路由数组 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/product/export"]
  }
}
```

---

### POST /api/admin/user/{id}/resetpwd - 重置密码

### 功能说明

重置员工密码。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newPassword | string | 否 | 新密码（不填则使用默认密码） |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "message": "密码已重置为：123456"
  }
}
```

---

## 角色管理接口

> **说明**：角色仅作为预设模板，不可增删改。预设角色定义员工的初始权限。

### GET /api/admin/role - 角色模板列表

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "超级管理员",
      "code": "SUPER_ADMIN",
      "description": "拥有所有权限",
      "permissions": ["*"],
      "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/system"],
      "isPreset": 1,
      "userCount": 1
    },
    {
      "id": 2,
      "name": "运营主管",
      "code": "OPERATOR",
      "description": "商品/订单/客户/统计管理",
      "permissions": ["spu:*", "sku:*", "category:*", "spec:*", "sn:*", "customer:*", "order:*", "statistics:*"],
      "routes": ["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics"],
      "isPreset": 1,
      "userCount": 3
    },
    {
      "id": 3,
      "name": "录入员",
      "code": "INPUTTER",
      "description": "商品录入/SN码操作",
      "permissions": ["spu:add", "spu:edit", "spu:view", "sku:add", "sku:edit", "sku:view", "sn:add", "sn:import", "sn:view", "sn:query"],
      "routes": ["/dashboard", "/product", "/sn"],
      "isPreset": 1,
      "userCount": 5
    }
  ]
}
```

---

## 菜单管理接口

> **说明**：菜单仅用于前端展示，定义权限标识，不可增删改。

### GET /api/admin/menu - 菜单列表

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "仪表盘",
      "path": "/dashboard",
      "component": "dashboard/index",
      "icon": "HomeIcon",
      "sort": 1,
      "type": 1,
      "permission": "dashboard:view"
    },
    {
      "id": 10,
      "name": "商品管理",
      "path": "/product",
      "icon": "CubeIcon",
      "sort": 10,
      "type": 1,
      "permission": "spu:view",
      "children": [
        {
          "id": 11,
          "name": "商品列表",
          "path": "/product/list",
          "component": "product/List",
          "type": 1,
          "permission": "spu:list"
        }
      ]
    }
  ]
}
```

---

## 操作日志接口

### GET /api/admin/log - 操作日志列表

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| module | string | 否 | 模块：product, sn, customer, order, system |
| adminId | long | 否 | 操作人ID |
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "adminId": 1,
        "adminName": "超级管理员",
        "module": "product",
        "operation": "新增商品",
        "targetId": 1,
        "targetDesc": "iPhone 15 Pro",
        "requestMethod": "POST",
        "requestUrl": "/api/admin/spu",
        "ip": "192.168.1.100",
        "status": 1,
        "createdAt": "2026-03-29 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100
    }
  }
}
```

---

## 权限标识说明

| 标识 | 说明 |
|------|------|
| `*` | 所有权限（超级管理员专用） |
| `spu:*` | SPU模块所有权限 |
| `spu:view` | 查看商品列表 |
| `spu:add` | 新增商品 |
| `spu:edit` | 编辑商品 |
| `spu:delete` | 删除商品 |
| `sku:*` | SKU模块所有权限 |
| `category:*` | 分类模块所有权限 |
| `spec:*` | 规格模块所有权限 |
| `sn:*` | SN码模块所有权限 |
| `customer:*` | 客户模块所有权限 |
| `order:*` | 订单模块所有权限 |
| `statistics:*` | 统计模块所有权限 |
| `system:user` | 员工管理权限 |

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 5001 | 用户名已存在 |
| 5002 | 角色不存在 |

---

## 备注

1. **角色只是模板**：预设角色不可修改，员工入职时继承角色权限
2. **权限在员工上**：真正权限管理在员工级别，可单独增删
3. **计算公式**：
   - 有效权限 = 角色权限 ∪ 员工额外权限
   - 有效路由 = 角色路由 ∪ 员工额外路由
4. 变更角色会重新继承新角色的基础权限，额外权限保留
5. 操作日志记录保留180天