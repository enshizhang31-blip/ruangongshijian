<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { saleApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatMoney, formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card, Modal, Form, FormItem, Select, InputNumber, DatePicker, Message, Empty } from '@arco-design/web-vue'
import type { SaleOrder, SaleOrderItem } from '@/types'
import { PlusIcon, EyeIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(saleApi.list)
const keyword = ref('')
const showDetailModal = ref(false)
const showEditModal = ref(false)
const viewingOrder = ref<SaleOrder | null>(null)
const editingOrder = ref<Partial<SaleOrder>>({})

// 订单明细
const orderItems = ref<SaleOrderItem[]>([])
const loadingItems = ref(false)

// 时间筛选
const startDate = ref('')
const endDate = ref('')

const itemColumns = [
    { title: '商品名称', dataIndex: 'spuName', width: 150 },
    { title: '规格', dataIndex: 'skuSpec', width: 150 },
    { title: '单价', dataIndex: 'price', width: 100, render: (v: number) => `¥${formatMoney(v)}` },
    { title: '数量', dataIndex: 'quantity', width: 80 },
    { title: '小计', dataIndex: 'subtotal', width: 100, render: (v: number) => `¥${formatMoney(v)}` },
]

// 订单状态 - 与数据库 order.status 一致
// 0:待付款 1:已付款 2:已完成 3:已取消 4:退款中 5:已退款
const orderStatusMap: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '已完成',
    3: '已取消',
    4: '退款中',
    5: '已退款',
}

const paymentStatusMap: Record<number, string> = {
    1: '未支付',
    2: '已支付',
}

const statusColorMap: Record<number, string> = {
    0: 'orange',
    1: 'green',
    2: 'arcoblue',
    3: 'gray',
    4: 'purple',
    5: 'red',
}

const columns = [
    { title: '订单号', dataIndex: 'orderNo', width: 150 },
    { title: '客户', dataIndex: 'customerName' },
    { title: '订单金额', dataIndex: 'totalAmount', render: (v: number) => `¥${formatMoney(v)}` },
    { title: '实付金额', dataIndex: 'payAmount', render: (v: number) => `¥${formatMoney(v)}` },
    {
        title: '订单状态',
        dataIndex: 'status',
        render: (status: number) => h(Tag, { color: statusColorMap[status] || 'gray' }, () => orderStatusMap[status] || '未知')
    },
    {
        title: '支付状态',
        dataIndex: 'payType',
        render: (payType: number) => h(Tag, { color: payType === 2 ? 'green' : 'orange' }, () => paymentStatusMap[payType] || '未知')
    },
    { title: '下单时间', dataIndex: 'createdAt', render: (t: string) => formatDate(t) },
    { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]

onMounted(() => {
    load()
})

function handleSearch() {
    Object.assign(query.value, {
        keyword: keyword.value,
        startDate: startDate.value,
        endDate: endDate.value,
        page: 1,
    })
    load()
}

function handleReset() {
    keyword.value = ''
    startDate.value = ''
    endDate.value = ''
    setKeyword('')
}

async function handleView(record: SaleOrder) {
    viewingOrder.value = record
    orderItems.value = []
    showDetailModal.value = true
    // 加载订单明细
    loadingItems.value = true
    try {
        orderItems.value = await saleApi.getItems(record.id)
    } catch {
        Message.error('加载订单明细失败')
    } finally {
        loadingItems.value = false
    }
}

function handleEdit(record: SaleOrder) {
    editingOrder.value = { ...record }
    showEditModal.value = true
}

async function handleStatusChange(order: SaleOrder, newStatus: number) {
    try {
        await saleApi.update({ ...order, status: newStatus })
        Message.success('状态更新成功')
        load()
    } catch (e: any) {
        Message.error(e?.message || '更新失败')
    }
}

async function handleSubmitEdit() {
    if (!editingOrder.value.id) {
        return false
    }

    // 状态值校验
    const validStatuses = [0, 1, 2, 3, 4, 5]
    if (editingOrder.value.status !== undefined && !validStatuses.includes(editingOrder.value.status)) {
        Message.warning('订单状态值无效')
        return false
    }

    // 备注长度校验
    if (editingOrder.value.remark && editingOrder.value.remark.length > 256) {
        Message.warning('备注不能超过256个字符')
        return false
    }

    try {
        await saleApi.update(editingOrder.value as SaleOrder)
        Message.success('更新成功')
        showEditModal.value = false
        load()
    } catch (e: any) {
        Message.error(e?.message || '更新失败')
        return false
    }
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">销售管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理销售订单</p>
            </div>
            <Button type="primary" @click="() => { }">
                <template #icon>
                    <PlusIcon class="w-4 h-4" />
                </template>
                新建订单
            </Button>
        </div>

        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索订单号或客户名称..." class="w-64!" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <DatePicker v-model="startDate" placeholder="开始日期" class="!w-36" @change="handleSearch" />
                <span class="text-gray-400">-</span>
                <DatePicker v-model="endDate" placeholder="结束日期" class="!w-36" @change="handleSearch" />
                <Button type="primary" @click="handleSearch">搜索</Button>
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1100 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleView(record)">
                            <EyeIcon class="w-4 h-4" />
                        </Button>
                        <Button type="text" size="small" @click="handleEdit(record)">
                            <PencilIcon class="w-4 h-4" />
                        </Button>
                    </Space>
                </template>
            </Table>

            <div class="flex justify-end mt-4">
                <Space direction="horizontal">
                    <span class="text-sm text-gray-500">共 {{ total }} 条</span>
                    <Button :disabled="(query.page || 1) <= 1" @click="setPage((query.page || 1) - 1)">上一页</Button>
                    <span class="text-sm py-2">第 {{ query.page || 1 }} / {{ Math.ceil(total / (query.pageSize || 20)) ||
                        1 }}
                        页</span>
                    <Button :disabled="(query.page || 1) >= Math.ceil(total / (query.pageSize || 20))"
                        @click="setPage((query.page || 1) + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <!-- 订单详情弹窗 -->
    <Modal v-model:visible="showDetailModal" title="订单详情" :width="700">
        <div v-if="viewingOrder">
            <!-- 收货信息 -->
            <div class="mb-4">
                <h4 class="text-sm font-medium text-gray-700 mb-2">收货信息</h4>
                <div class="grid grid-cols-3 gap-4 bg-gray-50 p-3 rounded-lg">
                    <div>
                        <div class="text-xs text-gray-500">收货人</div>
                        <div class="text-sm">{{ viewingOrder.receiverName || '-' }}</div>
                    </div>
                    <div>
                        <div class="text-xs text-gray-500">联系电话</div>
                        <div class="text-sm">{{ viewingOrder.receiverPhone || '-' }}</div>
                    </div>
                    <div class="col-span-1">
                        <div class="text-xs text-gray-500">收货地址</div>
                        <div class="text-sm">{{ viewingOrder.receiverAddress || '-' }}</div>
                    </div>
                </div>
            </div>

            <!-- 订单信息 -->
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div>
                    <div class="text-xs text-gray-500">订单号</div>
                    <div class="text-sm font-medium">{{ viewingOrder.orderNo }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">订单状态</div>
                    <Tag :color="statusColorMap[viewingOrder.status || 0]">
                        {{ orderStatusMap[viewingOrder.status || 0] }}
                    </Tag>
                </div>
                <div>
                    <div class="text-xs text-gray-500">客户</div>
                    <div class="text-sm">{{ viewingOrder.customerName || '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">下单时间</div>
                    <div class="text-sm">{{ viewingOrder.createdAt ? formatDate(viewingOrder.createdAt) : '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">订单金额</div>
                    <div class="text-sm font-medium">¥{{ formatMoney(viewingOrder.totalAmount) }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">实付金额</div>
                    <div class="text-sm font-medium text-green-600">¥{{ formatMoney(viewingOrder.payAmount) }}</div>
                </div>
            </div>

            <!-- 订单商品明细 -->
            <div class="mb-4">
                <h4 class="text-sm font-medium text-gray-700 mb-2">商品明细</h4>
                <Table :loading="loadingItems" :columns="itemColumns" :data="orderItems" :pagination="false" size="small">
                    <template #empty>
                        <span class="text-gray-400">暂无明细数据</span>
                    </template>
                </Table>
            </div>

            <!-- 备注 -->
            <div v-if="viewingOrder.remark" class="mb-4">
                <div class="text-xs text-gray-500">备注</div>
                <div class="text-sm">{{ viewingOrder.remark }}</div>
            </div>

            <!-- 操作按钮 -->
            <div class="border-t border-gray-200 pt-4">
                <h4 class="text-sm font-medium mb-3">订单操作</h4>
                <Space wrap>
                    <Button v-if="viewingOrder.status === 0" type="primary" size="small"
                        @click="handleStatusChange(viewingOrder, 1)">确认付款</Button>
                    <Button v-if="viewingOrder.status === 1" type="primary" size="small"
                        @click="handleStatusChange(viewingOrder, 2)">完成订单</Button>
                    <Button v-if="viewingOrder.status === 0 || viewingOrder.status === 1" status="danger" size="small"
                        @click="handleStatusChange(viewingOrder, 3)">取消订单</Button>
                </Space>
            </div>
        </div>
    </Modal>

    <!-- 编辑订单弹窗 -->
    <Modal v-model:visible="showEditModal" title="编辑订单" :on-before-ok="handleSubmitEdit" :width="500">
        <Form :model="editingOrder" layout="vertical">
            <FormItem label="订单状态">
                <Select v-model="editingOrder.status" class="w-full">
                    <Select.Option :value="1">待付款</Select.Option>
                    <Select.Option :value="2">已付款</Select.Option>
                    <Select.Option :value="3">已完成</Select.Option>
                    <Select.Option :value="4">已取消</Select.Option>
                </Select>
            </FormItem>
            <FormItem label="备注">
                <Input v-model="editingOrder.remark" placeholder="备注信息" :rows="2" />
            </FormItem>
        </Form>
    </Modal>
</template>
