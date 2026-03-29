# 小程序端 - 认证与会员模块 API

> 关联文档：[需求详细设计.md](../2026-03-27-需求详细设计.md)、[数据库设计.md](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 微信登录（自动注册） | 无 |
| GET | `/api/member/info` | 会员信息 | 需登录 |
| GET | `/api/member/balance` | 余额查询 | 需登录 |
| POST | `/api/member/recharge` | 余额充值 | 需登录 |
| GET | `/api/member/points` | 积分查询 | 需登录 |
| GET | `/api/member/points/history` | 积分明细 | 需登录 |

---

## POST /api/auth/login - 微信登录

### 功能说明

微信登录接口，使用微信授权 code 登录。首次登录时自动创建会员记录。

### 请求

- Method: `POST`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | 微信授权 code（通过 wx.login 获取） |

**请求示例：**

```json
{
  "code": "xxxxxxxxxxxxx"
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
    "member": {
      "id": 1,
      "openid": "oXXXXXX",
      "nickname": "用户昵称",
      "avatar": "https://xxx.com/avatar.png",
      "phone": "13800138000",
      "memberLevel": 1,
      "memberLevelName": "普通",
      "balance": 0.00,
      "points": 0
    }
  }
}
```

**失败响应：**

```json
{
  "code": 401,
  "message": "无效的授权code",
  "data": null
}
```

### 业务逻辑

1. 调用微信接口使用 code 换取 openid
2. 查询数据库是否存在该 openid 的会员
3. 如不存在，创建新会员记录
4. 生成 JWT token 返回

---

## GET /api/member/info - 会员信息

### 功能说明

获取当前登录会员的详细信息。

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
    "nickname": "用户昵称",
    "avatar": "https://xxx.com/avatar.png",
    "phone": "13800138000",
    "memberLevel": 2,
    "memberLevelName": "银卡",
    "balance": 1000.00,
    "points": 500,
    "totalConsume": 1500.00,
    "totalPoints": 600,
    "discount": 0.95,
    "pointsRate": 1.5
  }
}
```

### 会员等级信息

| level | name | discount | pointsRate |
|-------|------|----------|------------|
| 1 | 普通 | 1.00 | 1 |
| 2 | 银卡 | 0.95 | 1.5 |
| 3 | 金卡 | 0.90 | 2 |
| 4 | 钻石 | 0.85 | 3 |

---

## GET /api/member/balance - 余额查询

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "balance": 1000.00,
    "rechargeAmount": 1500.00,
    "consumeAmount": 500.00
  }
}
```

---

## POST /api/member/recharge - 余额充值

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| amount | decimal | 是 | 充值金额（正数） |
| paymentMethod | string | 否 | 支付方式，默认 wechat |

**请求示例：**

```json
{
  "amount": 100.00,
  "paymentMethod": "wechat"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "balance": 1100.00,
    "transactionId": "TXN202603290001"
  }
}
```

### 错误码

| code | 说明 |
|------|------|
| 400 | 充值金额必须大于0 |
| 500 | 充值失败 |

---

## GET /api/member/points - 积分查询

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "points": 500,
    "totalEarned": 600,
    "totalUsed": 100
  }
}
```

---

## GET /api/member/points/history - 积分明细

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |
| type | int | 否 | 类型：1获得 2抵扣 3过期 4调整 |

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
      "pageSize": 10,
      "total": 25
    }
  }
}
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

---

## 备注

1. 所有需要登录的接口，都需要在 Header 中携带 `Authorization: Bearer {token}`
2. token 有效期为 7 天，过期后需要重新登录
3. 会员升级自动触发，当 `totalConsume` 达到等级门槛时自动升级