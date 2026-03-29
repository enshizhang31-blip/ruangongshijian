# Web管理端 - 客户管理模块 API

> 关联文档：[需求详细设计.md - 第三章客户/会员模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 客户/会员模块](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/customer` | 客户列表 | customer:view |
| GET | `/api/admin/customer/{id}` | 客户详情 | customer:view |
| PUT | `/api/admin/customer/{id}` | 编辑客户信息 | customer:edit |
| PUT | `/api/admin/customer/{id}/balance` | 调整客户余额 | customer:balance |
| PUT | `/api/admin/customer/{id}/points` | 调整客户积分 | customer:points |
| PUT | `/api/admin/customer/{id}/status` | 禁用/启用客户 | customer:disable |
| GET | `/api/admin/customer/{id}/balance/history` | 余额变动记录 | customer:view |
| GET | `/api/admin/customer/{id}/points/history` | 积分变动记录 | customer:view |
| GET | `/api/admin/customer/{id}/orders` | 客户订单列表 | customer:view |

---

## GET /api/admin/customer - 客户列表

### 功能说明

分页查询客户/会员列表，支持按等级、手机号、状态等条件筛选。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 搜索：昵称/手机号/openid |
| memberLevel | int | 否 | 会员等级：1普通 2银卡 3金卡 4钻石 |
| status | int | 否 | 状态：0禁用 1启用 |
| startDate | date | 否 | 注册开始日期 |
| endDate | date | 否 | 注册结束日期 |
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
        "openid": "oXXXXXX",
        "nickname": "张三",
        "avatar": "https://xxx.com/avatar.png",
        "phone": "13800138000",
        "memberLevel": 2,
        "memberLevelName": "银卡",
        "balance": 1000.00,
        "points": 500,
        "totalConsume": 1500.00,
        "totalPoints": 600,
        "status": 1,
        "statusName": "正常",
        "createdAt": "2026-03-01 10:00:00",
        "lastOrderAt": "2026-03-25 14:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100
    },
    "statistics": {
      "totalCount": 100,
      "activeCount": 95,
      "vipCount": 30
    }
  }
}
```

---

## GET /api/admin/customer/{id} - 客户详情

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
    "openid": "oXXXXXX",
    "nickname": "张三",
    "avatar": "https://xxx.com/avatar.png",
    "phone": "13800138000",
    "memberLevel": 2,
    "memberLevelName": "银卡",
    "discount": 0.95,
    "pointsRate": 1.5,
    "balance": 1000.00,
    "points": 500,
    "totalConsume": 1500.00,
    "totalPoints": 600,
    "orderCount": 5,
    "status": 1,
    "statusName": "正常",
    "createdAt": "2026-03-01 10:00:00",
    "updatedAt": "2026-03-25 14:00:00"
  }
}
```

---

## PUT /api/admin/customer/{id} - 编辑客户信息

### 功能说明

编辑客户的基本信息（管理员操作）。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| phone | string | 否 | 手机号 |
| memberLevel | int | 否 | 会员等级 |

**请求示例：**

```json
{
  "nickname": "VIP客户",
  "phone": "13900139000",
  "memberLevel": 3
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

---

## PUT /api/admin/customer/{id}/balance - 调整客户余额

### 功能说明

管理员手动调整客户余额（充值、扣款），常用于线下充值、补偿等场景。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 是 | 类型：1充值 4调整 |
| amount | decimal | 是 | 金额（正数） |
| remark | string | 是 | 原因/备注 |

**请求示例：**

```json
{
  "type": 4,
  "amount": 100.00,
  "remark": "系统补偿"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "balance": 1100.00,
    "changeAmount": 100.00,
    "changeType": 4
  }
}
```

### 业务逻辑

1. 校验操作人权限（customer:balance）
2. 更新会员余额
3. 创建余额变动记录（balance_record）
4. 记录操作日志

---

## PUT /api/admin/customer/{id}/points - 调整客户积分

### 功能说明

管理员手动调整客户积分。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 是 | 类型：1获得 4调整 |
| amount | int | 是 | 积分数量（正数） |
| remark | string | 是 | 原因/备注 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "points": 600,
    "changeAmount": 100,
    "changeType": 4
  }
}
```

---

## PUT /api/admin/customer/{id}/status - 禁用/启用客户

### 功能说明

启用或禁用会员账号。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 状态：0禁用 1启用 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务说明

- 禁用的会员无法登录和下单
- 已下的订单不受影响

---

## GET /api/admin/customer/{id}/balance/history - 余额变动记录

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 否 | 类型：1充值 2消费 3退款 4调整 |
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
        "type": 1,
        "typeName": "充值",
        "amount": 100.00,
        "balance": 1100.00,
        "source": "admin",
        "paymentMethod": "wechat",
        "remark": "线下充值",
        "createdAt": "2026-03-29 10:00:00"
      },
      {
        "id": 2,
        "type": 2,
        "typeName": "消费",
        "amount": -50.00,
        "balance": 1050.00,
        "source": "order",
        "sourceId": 123,
        "remark": "订单消费",
        "createdAt": "2026-03-28 14:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50
    }
  }
}
```

---

## GET /api/admin/customer/{id}/points/history - 积分变动记录

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 否 | 类型：1获得 2抵扣 3过期 4调整 |
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
        "type": 1,
        "typeName": "获得",
        "amount": 50,
        "balance": 500,
        "source": "order",
        "sourceId": 123,
        "remark": "订单返积分",
        "createdAt": "2026-03-29 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 30
    }
  }
}
```

---

## GET /api/admin/customer/{id}/orders - 客户订单列表

### 功能说明

查看指定客户的订单记录。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 否 | 订单状态 |
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
        "id": 123,
        "orderNo": "ORD2026032900001",
        "totalAmount": 13998.00,
        "payAmount": 13298.10,
        "status": 2,
        "statusName": "已完成",
        "itemCount": 3,
        "createdAt": "2026-03-29 10:00:00",
        "completedAt": "2026-03-29 15:00:00"
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

## 会员等级说明

| level | name | 升级门槛(累计消费) | 折扣 | 积分倍率 |
|-------|------|-------------------|------|----------|
| 1 | 普通 | 0 | 100% | 1倍 |
| 2 | 银卡 | 1000 | 95% | 1.5倍 |
| 3 | 金卡 | 5000 | 90% | 2倍 |
| 4 | 钻石 | 20000 | 85% | 3倍 |

### 等级自动升级

当 `totalConsume` 达到更高等级的门槛时，自动升级会员等级。

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 客户不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 4001 | 余额调整失败 |
| 4002 | 积分调整失败 |
| 4003 | 会员等级不存在 |

---

## 备注

1. 余额和积分调整需要填写详细备注，便于审计
2. 会员等级调整不自动触发价格变化，仅影响后续订单折扣
3. 已禁用会员的所有操作记录仍可查询