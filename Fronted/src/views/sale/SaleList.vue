<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { saleApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatMoney, formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card } from '@arco-design/web-vue'
import type { SaleOrder } from '@/types'
import { PlusIcon, EyeIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(saleApi.list)
const keyword = ref('')

onMounted(() => {
    load()
})

function handleSearch() {
    setKeyword(keyword.value)
}

function handleView(record: SaleOrder) {
    console.log('view', record)
}

function handleEdit(record: SaleOrder) {
    console.log('edit', record)
}

const orderStatusMap: Record<number, string> = {
    1: '待付款',
    2: '已付款',
    3: '已完成',
    4: '已取消',
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
        render: (status: number) => Tag.color(statusColorMap[status] || 'gray')(orderStatusMap[status] || '未知')
    },
    { title: '下单时间', dataIndex: 'createTime', render: (t: string) => formatDate(t) },
    { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]
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
                <Button @click="keyword = ''; setKeyword('')">重置</Button>
            </Space>
        </Card>

        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1000 }">
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
                    <span class="text-sm py-2">第 {{ query.page }} / {{ Math.ceil(total / query.pageSize) || 1 }}
                        页</span>
                    <Button :disabled="query.page >= Math.ceil(total / query.pageSize)"
                        @click="setPage(query.page + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>
</template>
