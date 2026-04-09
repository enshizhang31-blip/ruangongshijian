# Web管理端 - 订单管理模块 API

> 关联文档：[需求详细设计.md - 第六章订单模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 订单模块](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/order` | 订单列表 | order:view |
| GET | `/api/admin/order/{id}` | 订单详情 | order:view |
| PUT | `/api/admin/order/{id}/status` | 订单状态更新 | order:edit |
| PUT | `/api/admin/order/{id}/refund` | 退款审核 | order:refund |
| GET | `/api/admin/order/{id}/logs` | 订单操作日志 | order:view |

---

## GET /api/admin/order - 订单列表

### 功能说明

分页查询订单列表，支持按状态、订单号、客户等条件筛选。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 否 | 订单编号 |
| customerId | long | 否 | 客户ID |
| customerPhone | string | 否 | 客户手机号 |
| status | int | 否 | 订单状态：0待支付 1已支付 2已发货 3已完成 4已取消 5退款中 6已退款 |
| startDate | date | 否 | 下单开始日期 |
| endDate | date | 否 | 下单结束日期 |
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
        "customerId": 1,
        "customerName": "张三",
        "customerPhone": "13800138000",
        "totalAmount": 13998.00,
        "discountAmount": 700.00,
        "payAmount": 13298.10,
        "payType": 1,
        "payTypeName": "余额支付",
        "status": 1,
        "statusName": "已付款",
        "itemCount": 3,
        "createdAt": "2026-03-29 10:00:00",
        "paidAt": "2026-03-29 10:30:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50
    },
    "statistics": {
      "totalCount": 50,
      "totalAmount": 699900.00,
      "pendingPay": 5,
      "pendingRefund": 2
    }
  }
}
```

---

## GET /api/admin/order/{id} - 订单详情

### 功能说明

查看订单的完整信息，包括商品明细、收货地址、支付信息等。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "orderNo": "ORD2026032900001",
    "customerId": 1,
    "customerName": "张三",
    "customerPhone": "13800138000",
    "memberLevel": 2,
    "memberLevelName": "银卡",
    "totalAmount": 13998.00,
    "discountAmount": 700.00,
    "payAmount": 13298.10,
    "payType": 1,
    "payTypeName": "余额支付",
    "status": 1,
    "statusName": "已付款",
    "remark": "请尽快发货",
    "address": {
      "receiverName": "张三",
      "phone": "13800138000",
      "province": "广东省",
      "city": "深圳市",
      "district": "南山区",
      "detail": "科技园路1号",
      "fullAddress": "广东省 深圳市 南山区 科技园路1号"
    },
    "items": [
      {
        "id": 1,
        "skuId": 101,
        "spuName": "iPhone 15 Pro",
        "skuCode": "IP15-BLK-256",
        "skuSpec": "黑色 / 256G",
        "skuImage": "https://xxx.com/iphone.jpg",
        "price": 6999.00,
        "quantity": 2,
        "subtotal": 13998.00,
        "snCodeIds": [1, 2]
      }
    ],
    "paymentInfo": {
      "payAmount": 13298.10,
      "balanceUsed": 13298.10,
      "discountAmount": 700.00,
      "paidAt": "2026-03-29 10:30:00"
    },
    "refund": null,
    "createdAt": "2026-03-29 10:00:00",
    "paidAt": "2026-03-29 10:30:00",
    "completedAt": null
  }
}
```

---

## PUT /api/admin/order/{id}/status - 订单状态更新

### 功能说明

更新订单状态，包括发货、确认收货等操作。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 目标状态：2已完成 |
| remark | string | 否 | 备注 |

**请求示例：**

```json
{
  "status": 2,
  "remark": "客户确认收货"
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

### 状态变更说明

| 操作 | 变更前 → 变更后 | 说明 |
|------|-----------------|------|
| 确认收货 | 1已付款 → 2已完成 | 客户确认或系统超时 |

### 业务逻辑

1. 校验状态变更合法性
2. 更新订单状态和完成时间
3. 记录操作日志

---

## PUT /api/admin/order/{id}/refund - 退款审核

### 功能说明

审核客户的退款申请，同意或拒绝。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 审核状态：1同意 2拒绝 |
| rejectReason | string | 条件必填 | 拒绝原因（status=2时必填） |

**请求示例：**

```json
{
  "status": 1,
  "rejectReason": null
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

**同意退款 (status=1)：**
1. 更新订单状态为已退款
2. 退回会员余额（pay_amount）
3. 退回会员积分（如有）
4. 释放SN码（状态改为在库）
5. 创建退款记录

**拒绝退款 (status=2)：**
1. 更新订单状态为已付款
2. 更新退款记录状态为已拒绝
3. 记录拒绝原因

---

## GET /api/admin/order/{id}/logs - 订单操作日志

### 功能说明

查看订单的所有操作记录。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "operation": "创建订单",
      "operatorName": "张三",
      "remark": "订单创建",
      "createdAt": "2026-03-29 10:00:00"
    },
    {
      "id": 2,
      "operation": "支付",
      "operatorName": "系统",
      "remark": "余额支付 13298.10",
      "createdAt": "2026-03-29 10:30:00"
    },
    {
      "id": 3,
      "operation": "退款申请",
      "operatorName": "张三",
      "remark": "商品损坏",
      "createdAt": "2026-03-29 12:00:00"
    },
    {
      "id": 4,
      "operation": "退款审核",
      "operatorName": "李四",
      "remark": "同意退款",
      "createdAt": "2026-03-29 14:00:00"
    }
  ]
}
```

---

## 订单状态说明

| 值 | 名称 | 说明 | 可进行的操作 |
|------|------|------|-------------|
| 0 | 待付款 | 等待用户支付 | - |
| 1 | 已付款 | 已完成支付 | 退款审核 |
| 2 | 已完成 | 交易完成 | - |
| 3 | 已取消 | 订单已取消 | - |
| 4 | 退款中 | 退款申请审核中 | 退款审核 |
| 5 | 已退款 | 退款已完成 | - |

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 订单不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 4001 | 订单状态不允许此操作 |
| 4002 | 退款金额超出支付金额 |
| 4003 | 订单已完成无法退款 |

---

## 备注

1. 订单状态由用户端和系统自动触发，Web管理端主要负责退款审核
2. 退款时需要确保SN码正确释放回库存
3. 所有操作都需要记录日志便于追溯