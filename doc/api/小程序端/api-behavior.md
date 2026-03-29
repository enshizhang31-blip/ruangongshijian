# 小程序端 - 行为埋点模块 API

> 关联文档：[需求详细设计.md - 第八章数据埋点模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 用户行为日志表](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/behavior/log` | 上报行为日志 | 可选 |

---

## POST /api/behavior/log - 上报行为日志

### 功能说明

小程序端用户行为数据采集接口，用于统计分析。

### 请求

- Method: `POST`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| events | array | 是 | 事件列表（支持批量上报） |

**events 数组中每个事件对象：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| eventType | string | 是 | 事件类型：page_view / click / custom |
| eventName | string | 是 | 事件名称 |
| pageUrl | string | 否 | 页面路径 |
| pageTitle | string | 否 | 页面标题 |
| duration | int | 否 | 停留时长（秒），page_leave 时必填 |
| referrer | string | 否 | 上一个页面 |
| extraData | object | 否 | 扩展数据（JSON对象） |

**请求示例：**

```json
{
  "events": [
    {
      "eventType": "page_view",
      "eventName": "page_enter",
      "pageUrl": "/pages/goods/detail",
      "pageTitle": "商品详情",
      "referrer": "/pages/goods/list"
    },
    {
      "eventType": "click",
      "eventName": "add_to_cart",
      "pageUrl": "/pages/goods/detail",
      "extraData": {
        "goodsId": 1,
        "skuId": 101
      }
    },
    {
      "eventType": "page_view",
      "eventName": "page_leave",
      "pageUrl": "/pages/goods/detail",
      "duration": 30
    }
  ]
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "received": 3
  }
}
```

---

## 事件类型与事件名称

### page_view - 页面访问

| eventName | 说明 | 必填字段 |
|------------|------|----------|
| page_enter | 进入页面 | pageUrl, pageTitle |
| page_leave | 离开页面 | pageUrl, duration |

### click - 点击行为

| eventName | 说明 | 必填字段 |
|------------|------|----------|
| goods_click | 商品点击 | extraData.goodsId |
| cart_add | 加入购物车 | extraData.goodsId, extraData.skuId |
| order_submit | 提交订单 | extraData.orderId |
| pay_click | 点击支付 | extraData.orderId |
| favorite | 收藏商品 | extraData.goodsId |
| share | 分享行为 | extraData.goodsId |
| address_select | 选择收货地址 | extraData.addressId |

### custom - 自定义事件

| eventName | 说明 | 必填字段 |
|------------|------|----------|
| search | 搜索商品 | extraData.keyword |
| login | 登录 | - |
| register | 注册 | - |
| recharge | 余额充值 | extraData.amount |
| points_exchange | 积分兑换 | extraData.points |

---

## extraData 扩展数据示例

### 商品点击

```json
{
  "eventType": "click",
  "eventName": "goods_click",
  "pageUrl": "/pages/goods/detail",
  "extraData": {
    "goodsId": 1,
    "spuName": "iPhone 15 Pro"
  }
}
```

### 加入购物车

```json
{
  "eventType": "click",
  "eventName": "cart_add",
  "pageUrl": "/pages/goods/detail",
  "extraData": {
    "goodsId": 1,
    "skuId": 101,
    "spuName": "iPhone 15 Pro",
    "price": 6999.00
  }
}
```

### 提交订单

```json
{
  "eventType": "click",
  "eventName": "order_submit",
  "pageUrl": "/pages/order/confirm",
  "extraData": {
    "orderId": 123,
    "totalAmount": 13998.00,
    "itemCount": 3
  }
}
```

### 搜索商品

```json
{
  "eventType": "custom",
  "eventName": "search",
  "pageUrl": "/pages/goods/list",
  "extraData": {
    "keyword": "iPhone",
    "resultCount": 20
  }
}
```

---

## 小程序端埋点时机

### 1. 页面进入/离开

```javascript
// pages.json 中配置 onShow/onHide
Page({
  onShow() {
    this.data.pageEnterTime = Date.now()
    this.reportEvent('page_view', 'page_enter', {
      pageUrl: getCurrentPages()[0].route,
      pageTitle: getCurrentPages()[0].options.title || ''
    })
  },
  onHide() {
    const duration = Math.floor((Date.now() - this.data.pageEnterTime) / 1000)
    this.reportEvent('page_view', 'page_leave', { duration })
  }
})
```

### 2. 关键按钮点击

```javascript
// 加入购物车
addToCart() {
  // ... 业务逻辑
  this.reportEvent('click', 'cart_add', {
    goodsId: this.data.goodsId,
    skuId: this.data.skuId
  })
}

// 提交订单
submitOrder() {
  // ... 业务逻辑
  this.reportEvent('click', 'order_submit', {
    orderId: res.data.orderId,
    totalAmount: res.data.payAmount
  })
}
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

---

## 备注

1. 埋点接口通常不需要登录，收集 openid 用于用户识别
2. 支持批量上报，减少网络请求次数
3. 建议在本地缓存事件，延迟上报（批量发送）
4. 敏感信息（如密码、token）不要放入 extraData
5. duration 字段用于计算页面停留时长，必需