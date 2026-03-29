# Web管理端 - SN码管理模块 API

> 关联文档：[需求详细设计.md - 2.6 SN码管理](../2026-03-27-需求详细设计.md)、[数据库设计.md - SN码表](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/sn` | SN码列表（分页筛选） | sn:view |
| GET | `/api/admin/sn/{id}` | SN码详情 | sn:view |
| GET | `/api/admin/sn/code/{snCode}` | 按SN码查询 | sn:query |
| POST | `/api/admin/sn` | 单个录入 | sn:add |
| POST | `/api/admin/sn/batch` | 批量录入 | sn:add |
| POST | `/api/admin/sn/import` | CSV导入 | sn:import |
| POST | `/api/admin/sn/generate` | 自动生成 | sn:add |
| PUT | `/api/admin/sn/{id}/status` | 状态变更 | sn:status |
| PUT | `/api/admin/sn/batch/status` | 批量状态变更 | sn:status |
| GET | `/api/admin/sn/{id}/log` | SN码操作日志 | sn:view |

---

## GET /api/admin/sn - SN码列表

### 功能说明

分页查询SN码列表，支持按SKU、状态、时间等条件筛选。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | long | 否 | SKU ID |
| spuId | long | 否 | SPU ID |
| status | int | 否 | 状态：0在库 1已售 2已作废 3退货中 4已退货 |
| source | int | 否 | 来源：1手动 2CSV 3自动生成 |
| snCode | string | 否 | SN码（精确或模糊匹配） |
| startDate | date | 否 | 录入开始日期 |
| endDate | date | 否 | 录入结束日期 |
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
        "snCode": "SN202603290001",
        "skuId": 101,
        "skuCode": "IP15-BLK-256",
        "spuId": 1,
        "spuName": "iPhone 15 Pro",
        "specJson": {"颜色": "黑色", "内存": "256G"},
        "price": 6999.00,
        "status": 0,
        "statusName": "在库",
        "source": 1,
        "sourceName": "手动",
        "createdAt": "2026-03-29 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100
    },
    "statistics": {
      "totalCount": 100,
      "inStock": 80,
      "sold": 15,
      "invalid": 3,
      "returned": 2
    }
  }
}
```

---

## GET /api/admin/sn/code/{snCode} - 按SN码查询

### 功能说明

通过SN码快速查询商品信息，常用于仓库扫码。

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
    "snCode": "SN202603290001",
    "skuId": 101,
    "skuCode": "IP15-BLK-256",
    "spuId": 1,
    "spuName": "iPhone 15 Pro",
    "specJson": {"颜色": "黑色", "内存": "256G"},
    "price": 6999.00,
    "status": 0,
    "statusName": "在库",
    "createdAt": "2026-03-29 10:00:00",
    "soldAt": null
  }
}
```

### 错误响应

```json
{
  "code": 404,
  "message": "SN码不存在",
  "data": null
}
```

---

## POST /api/admin/sn - 单个录入

### 功能说明

手动录入单个SN码。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| snCode | string | 是 | SN码 |
| skuId | long | 是 | SKU ID |

**请求示例：**

```json
{
  "snCode": "SN202603290001",
  "skuId": 101
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "snCode": "SN202603290001",
    "skuId": 101,
    "status": 0,
    "createdAt": "2026-03-29 10:00:00"
  }
}
```

### 业务逻辑

1. 校验SKU是否存在
2. 校验SN码是否已存在
3. 自动填充 spuId、spuName、skuCode、specJson、price
4. 记录操作日志

---

## POST /api/admin/sn/batch - 批量录入

### 功能说明

一次性录入多个SN码，适用于扫码枪批量录入场景。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | long | 是 | SKU ID |
| snCodes | array | 是 | SN码数组（最多100条） |

**请求示例：**

```json
{
  "skuId": 101,
  "snCodes": [
    "SN202603290001",
    "SN202603290002",
    "SN202603290003"
  ]
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "success": 3,
    "failed": 0,
    "results": [
      {"snCode": "SN202603290001", "success": true},
      {"snCode": "SN202603290002", "success": true},
      {"snCode": "SN202603290003", "success": true}
    ]
  }
}
```

### 错误响应（部分失败）

```json
{
  "code": 200,
  "message": "部分失败",
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "results": [
      {"snCode": "SN202603290001", "success": true},
      {"snCode": "SN202603290002", "success": false, "error": "SN码已存在"},
      {"snCode": "SN202603290003", "success": true}
    ]
  }
}
```

---

## POST /api/admin/sn/import - CSV导入

### 功能说明

上传CSV文件批量导入SN码。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `multipart/form-data`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|
| file | file | 是 | CSV文件（不超过5MB） |
| skuId | long | 是 | SKU ID |

**CSV格式：**

```csv
sn_code
SN202603290001
SN202603290002
SN202603290003
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": "IMPORT202603290001",
    "status": "processing",
    "total": 100,
    "success": 98,
    "failed": 2,
    "failedRows": [15, 67],
    "reportUrl": "/api/admin/sn/import/{taskId}/report"
  }
}
```

### 业务说明

1. 支持异步处理大文件
2. 返回任务ID用于查询导入结果
3. 失败行会在 failedRows 中列出

---

## POST /api/admin/sn/generate - 自动生成

### 功能说明

系统自动生成一批SN码，适用于批量生产场景。

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | long | 是 | SKU ID |
| count | int | 是 | 生成数量（1-1000） |
| prefix | string | 否 | 前缀，默认"SN" |
| quantityPerSku | int | 否 | 每个SKU生成数量（为0时所有SN码关联同一SKU） |

**请求示例：**

```json
{
  "skuId": 101,
  "count": 100,
  "prefix": "SN"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "generated": [
      "SN202603290001",
      "SN202603290002",
      "SN202603290003"
    ],
    "prefix": "SN",
    "count": 100
  }
}
```

### SN码生成规则

```
格式：{前缀}{年月日}{6位序号}
示例：SN20260329000001

- 前缀：可配置，默认 SN
- 年月日：8位日期
- 序号：每天从000001开始，次日重置
```

---

## PUT /api/admin/sn/{id}/status - 状态变更

### 功能说明

变更单个SN码的状态，如作废、激活等。

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 目标状态：0在库 1已售 2已作废 3退货中 4已退货 |
| remark | string | 否 | 备注 |

**请求示例：**

```json
{
  "status": 2,
  "remark": "商品损坏作废"
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

### 状态变更规则

| 从状态 | 可变更到 | 说明 |
|--------|----------|------|
| 0 在库 | 1已售, 2已作废 | 可销售或作废 |
| 1 已售 | 3退货中, 2已作废 | 可申请退货或作废 |
| 2 已作废 | 0在库 | 可重新激活（需确认） |
| 3 退货中 | 4已退货, 1已售 | 审核后完成退货或返回销售 |
| 4 已退货 | 0在库 | 重新入库 |

### 业务逻辑

1. 校验状态变更是否合法
2. 更新SN码状态
3. 记录操作日志（operator、from_status、to_status）
4. 关联更新库存数量

---

## PUT /api/admin/sn/batch/status - 批量状态变更

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| snCodeIds | array | 是 | SN码ID数组 |
| status | int | 是 | 目标状态 |
| remark | string | 否 | 备注 |

**请求示例：**

```json
{
  "snCodeIds": [1, 2, 3, 4, 5],
  "status": 2,
  "remark": "批量作废"
}
```

---

## GET /api/admin/sn/{id}/log - SN码操作日志

### 功能说明

查看SN码的所有操作记录，包括录入、销售、作废、退货等。

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
      "snCodeId": 1,
      "snCode": "SN202603290001",
      "operation": "录入",
      "fromStatus": null,
      "toStatus": 0,
      "operatorName": "张三",
      "remark": "手动录入",
      "createdAt": "2026-03-29 10:00:00"
    },
    {
      "id": 2,
      "snCodeId": 1,
      "snCode": "SN202603290001",
      "operation": "销售",
      "fromStatus": 0,
      "toStatus": 1,
      "operatorName": "系统",
      "remark": "订单ORD2026032900001",
      "createdAt": "2026-03-29 14:00:00"
    }
  ]
}
```

---

## SN码状态说明

| 值 | 名称 | 说明 | 计入库存 |
|------|------|------|----------|
| 0 | 在库 | 可销售 | ✅ |
| 1 | 已售 | 已卖出 | ❌ |
| 2 | 已作废 | 无效码 | ❌ |
| 3 | 退货中 | 申请退货 | ❌ |
| 4 | 已退货 | 已退回 | ✅ |

### 库存计算

```
可用库存 = 状态为0（，在库）的SN码数量
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | SN码不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 3001 | SN码已存在 |
| 3002 | SKU不存在 |
| 3003 | 状态变更不合法 |
| 3004 | 库存不足（预留时） |
| 3005 | 导入文件格式错误 |

---

## 备注

1. SN码必须全局唯一，建议使用有规律的编码便于管理
2. CSV导入建议单次不超过1000条，大文件请使用异步任务
3. 作废操作需要谨慎，建议记录详细备注
4. 操作日志永久保留，便于追溯