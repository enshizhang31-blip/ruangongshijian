# 小程序端 - 订单与收货地址模块 API

> 关联文档：[需求详细设计.md](../2026-03-27-需求详细设计.md)、[数据库设计.md](../2026-03-28-数据库设计.md)

---

## 接口列表

### 收货地址

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/address` | 地址列表 | 需登录 |
| POST | `/api/address` | 新增地址 | 需登录 |
| PUT | `/api/address/{id}` | 编辑地址 | 需登录 |
| DELETE | `/api/address/{id}` | 删除地址 | 需登录 |
| PUT | `/api/address/{id}/default` | 设为默认 | 需登录 |

### 订单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/order` | 创建订单 | 需登录 |
| GET | `/api/order` | 订单列表 | 需登录 |
| GET | `/api/order/{id}` | 订单详情 | 需登录 |
| POST | `/api/order/{id}/pay` | 支付订单 | 需登录 |
| POST | `/api/order/{id}/cancel` | 取消订单 | 需登录 |
| POST | `/api/order/{id}/refund` | 申请退款 | 需登录 |

---

## 收货地址接口

### GET /api/address - 地址列表

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
      "receiverName": "张三",
      "phone": "13800138000",
      "province": "广东省",
      "city": "深圳市",
      "district": "南山区",
      "detail": "科技园路1号",
      "isDefault": 1,
      "fullAddress": "广东省 深圳市 南山区 科技园路1号"
    }
  ]
}
```

---

### POST /api/address - 新增地址

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverName | string | 是 | 收货人姓名 |
| phone | string | 是 | 联系电话 |
| province | string | 是 | 省份 |
| city | string | 是 | 城市 |
| district | string | 是 | 区/县 |
| detail | string | 是 | 详细地址 |
| isDefault | int | 否 | 是否默认（0否 1是），默认0 |

**请求示例：**

```json
{
  "receiverName": "张三",
  "phone": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detail": "科技园路1号",
  "isDefault": 0
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "receiverName": "张三",
    "phone": "13800138000",
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "detail": "科技园路1号",
    "isDefault": 0
  }
}
```

### 业务逻辑

- 如果 `isDefault=1`，自动取消其他地址的默认状态
- 如果是第一个地址，自动设为默认

---

### PUT /api/address/{id} - 编辑地址

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverName | string | 是 | 收货人姓名 |
| phone | string | 是 | 联系电话 |
| province | string | 是 | 省份 |
| city | string | 是 | 城市 |
| district | string | 是 | 区/县 |
| detail | string | 是 | 详细地址 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### DELETE /api/address/{id} - 删除地址

### 请求

- Method: `DELETE`
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

### PUT /api/address/{id}/default - 设为默认

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`

### 业务逻辑

1. 取消当前会员所有地址的默认状态
2. 将指定地址设为默认

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 订单接口

### POST /api/order - 创建订单

### 功能说明

将购物车中的商品创建为订单，扣减库存（SN码）。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| addressId | long | 是 | 收货地址ID |
| cartItemIds | array | 是 | 购物车商品ID列表（为空则使用全部） |
| remark | string | 否 | 订单备注 |

**请求示例：**

```json
{
  "addressId": 1,
  "cartItemIds": [1, 2, 3],
  "remark": "请尽快发货"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 123,
    "orderNo": "ORD2026032900001",
    "totalAmount": 13998.00,
    "discountAmount": 0,
    "payAmount": 13998.00,
    "payDeadline": "2026-03-29 15:00:00"
  }
}
```

### 业务逻辑

1. 校验地址有效性
2. 计算商品总价（含会员折扣）
3. 校验 SKU 库存（预留 SN 码）
4. 创建订单和订单明细
5. 清空已下单的购物车商品
6. 返回订单信息（待支付状态）

---

### GET /api/order - 订单列表

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 否 | 订单状态：0待付款 1已付款 2已完成 3已取消 4退款中 5已退款 |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |

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
        "payAmount": 13998.00,
        "status": 0,
        "statusName": "待付款",
        "itemCount": 3,
        "createdAt": "2026-03-29 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 5
    }
  }
}
```

---

### GET /api/order/{id} - 订单详情

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
    "status": 0,
    "statusName": "待付款",
    "totalAmount": 13998.00,
    "discountAmount": 0,
    "payAmount": 13998.00,
    "payType": 1,
    "payTypeName": "余额支付",
    "remark": "请尽快发货",
    "address": {
      "receiverName": "张三",
      "phone": "13800138000",
      "fullAddress": "广东省 深圳市 南山区 科技园路1号"
    },
    "items": [
      {
        "id": 1,
        "skuId": 101,
        "spuName": "iPhone 15 Pro",
        "skuSpec": "黑色 / 256G",
        "skuImage": "https://xxx.com/iphone.jpg",
        "price": 6999.00,
        "quantity": 2,
        "subtotal": 13998.00
      }
    ],
    "createdAt": "2026-03-29 10:00:00",
    "payDeadline": "2026-03-29 15:00:00",
    "paidAt": null,
    "completedAt": null
  }
}
```

---

### POST /api/order/{id}/pay - 支付订单

### 功能说明

使用会员余额支付订单。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| payPassword | string | 否 | 支付密码（预留，后续实现） |

### 响应

**成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 123,
    "orderNo": "ORD2026032900001",
    "paidAt": "2026-03-29 10:30:00"
  }
}
```

### 业务逻辑

1. 校验订单状态（必须为待付款）
2. 校验支付截止时间
3. 校验会员余额是否充足
4. 扣减会员余额
5. 更新订单状态为已付款
6. 释放未付款超时订单的 SN 码（定时任务）

---

### POST /api/order/{id}/cancel - 取消订单

### 功能说明

取消未支付的订单。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 否 | 取消原因 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 业务逻辑

1. 校验订单状态（必须为待付款）
2. 释放预留的 SN 码
3. 更新订单状态为已取消

---

### POST /api/order/{id}/refund - 申请退款

### 功能说明

对已付款订单申请退款。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 退款原因 |

**请求示例：**

```json
{
  "reason": "商品损坏"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "refundId": 1,
    "status": 0,
    "statusName": "待审核"
  }
}
```

### 业务逻辑

1. 校验订单状态（必须为已付款）
2. 创建退款记录（状态为待审核）
3. 更新订单状态为退款中
4. 释放 SN 码（改为在库状态）

---

## 订单状态说明

| 值 | 名称 | 说明 | 可进行的操作 |
|------|------|------|-------------|
| 0 | 待付款 | 等待用户支付 | 支付、取消 |
| 1 | 已付款 | 已完成支付 | 申请退款 |
| 2 | 已完成 | 交易完成 | - |
| 3 | 已取消 | 订单已取消 | - |
| 4 | 退款中 | 退款申请审核中 | - |
| 5 | 已退款 | 退款已完成 | - |

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限访问该订单 |
| 404 | 地址/订单不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 4001 | 余额不足 |
| 4002 | 订单已超时 |
| 4003 | 订单状态不允许此操作 |
| 4004 | 库存不足 |