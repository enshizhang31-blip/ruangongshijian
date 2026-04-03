<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { saleApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatMoney, formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card, Modal, Form, FormItem, Select, InputNumber, DatePicker, Message } from '@arco-design/web-vue'
import type { SaleOrder, SaleOrderItem } from '@/types'
import { PlusIcon, EyeIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(saleApi.list)
const keyword = ref('')
const showDetailModal = ref(false)
const showEditModal = ref(false)
const viewingOrder = ref<SaleOrder | null>(null)
const editingOrder = ref<Partial<SaleOrder>>({})

const orderStatusMap: Record<number, string> = {
    1: '待付款',
    2: '已付款',
    3: '已完成',
    4: '已取消',
}

const paymentStatusMap: Record<number, string> = {
    1: '未支付',
    2: '已支付',
}

const statusColorMap: Record<number, string> = {
    1: 'orange',
    2: 'green',
    3: 'arcoblue',
    4: 'gray',
}

const columns = [
    { title: '订单号', dataIndex: 'orderNo', width: 150 },
    { title: '客户', dataIndex: 'customerName' },
    { title: '销售人员', dataIndex: 'salesUserName' },
    { title: '订单金额', dataIndex: 'totalAmount', render: (v: number) => `¥${formatMoney(v)}` },
    { title: '实付金额', dataIndex: 'payableAmount', render: (v: number) => `¥${formatMoney(v)}` },
    {
        title: '订单状态',
        dataIndex: 'orderStatus',
        render: (status: number) => h(Tag, { color: statusColorMap[status] || 'gray' }, () => orderStatusMap[status] || '未知')
    },
    {
        title: '支付状态',
        dataIndex: 'paymentStatus',
        render: (status: number) => h(Tag, { color: status === 2 ? 'green' : 'orange' }, () => paymentStatusMap[status] || '未知')
    },
    { title: '下单时间', dataIndex: 'createTime', render: (t: string) => formatDate(t) },
    { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]

onMounted(() => {
    load()
})

function handleSearch() {
    setKeyword(keyword.value)
}

function handleReset() {
    keyword.value = ''
    setKeyword('')
}

async function handleView(record: SaleOrder) {
    viewingOrder.value = record
    showDetailModal.value = true
}

function handleEdit(record: SaleOrder) {
    editingOrder.value = { ...record }
    showEditModal.value = true
}

async function handleStatusChange(order: SaleOrder, newStatus: number) {
    try {
        await saleApi.update({ ...order, orderStatus: newStatus })
        Message.success('状态更新成功')
        load()
    } catch {
        Message.error('更新失败')
    }
}

async function handleSubmitEdit() {
    if (!editingOrder.value.id) return
    try {
        await saleApi.update(editingOrder.value as SaleOrder)
        Message.success('更新成功')
        showEditModal.value = false
        load()
    } catch {
        Message.error('更新失败')
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
                <Input v-model="keyword" placeholder="搜索订单号或客户名称..." class="!w-64" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
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
                    <Button :disabled="query.page <= 1" @click="setPage(query.page - 1)">上一页</Button>
                    <span class="text-sm py-2">第 {{ query.page }} / {{ Math.ceil(total / (query.pageSize || 20)) || 1 }}
                        页</span>
                    <Button :disabled="query.page >= Math.ceil(total / (query.pageSize || 20))"
                        @click="setPage(query.page + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <!-- 订单详情弹窗 -->
    <Modal v-model:visible="showDetailModal" title="订单详情" :width="600">
        <div v-if="viewingOrder">
            <div class="grid grid-cols-2 gap-4 mb-6">
                <div>
                    <div class="text-xs text-gray-500">订单号</div>
                    <div class="text-sm font-medium">{{ viewingOrder.orderNo }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">订单状态</div>
                    <Tag :color="statusColorMap[viewingOrder.orderStatus]">
                        {{ orderStatusMap[viewingOrder.orderStatus] }}
                    </Tag>
                </div>
                <div>
                    <div class="text-xs text-gray-500">客户</div>
                    <div class="text-sm">{{ viewingOrder.customerName || '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">销售人员</div>
                    <div class="text-sm">{{ viewingOrder.salesUserName || '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">订单金额</div>
                    <div class="text-sm font-medium">¥{{ formatMoney(viewingOrder.totalAmount) }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">实付金额</div>
                    <div class="text-sm font-medium text-green-600">¥{{ formatMoney(viewingOrder.payableAmount) }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">下单时间</div>
                    <div class="text-sm">{{ viewingOrder.createTime ? formatDate(viewingOrder.createTime) : '-' }}</div>
                </div>
                <div v-if="viewingOrder.remark">
                    <div class="text-xs text-gray-500">备注</div>
                    <div class="text-sm">{{ viewingOrder.remark }}</div>
                </div>
            </div>

            <div class="border-t border-gray-200 pt-4">
                <h4 class="text-sm font-medium mb-3">订单操作</h4>
                <Space wrap>
                    <Button v-if="viewingOrder.orderStatus === 1" type="primary" size="small"
                        @click="handleStatusChange(viewingOrder, 2)">确认付款</Button>
                    <Button v-if="viewingOrder.orderStatus === 2" type="primary" size="small"
                        @click="handleStatusChange(viewingOrder, 3)">完成订单</Button>
                    <Button v-if="viewingOrder.orderStatus === 1" status="danger" size="small"
                        @click="handleStatusChange(viewingOrder, 4)">取消订单</Button>
                </Space>
            </div>
        </div>
    </Modal>

    <!-- 编辑订单弹窗 -->
    <Modal v-model:visible="showEditModal" title="编辑订单" @ok="handleSubmitEdit" :width="500">
        <Form :model="editingOrder" layout="vertical">
            <FormItem label="订单状态">
                <Select v-model="editingOrder.orderStatus" class="w-full">
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
