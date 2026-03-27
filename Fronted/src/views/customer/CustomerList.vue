<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { customerApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Input, Space, Tag, Popconfirm, Card } from '@arco-design/web-vue'
import type { Customer } from '@/types'
import { PlusIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(customerApi.list)
const keyword = ref('')

onMounted(() => {
    load()
})

function handleSearch() {
    setKeyword(keyword.value)
}

function handleEdit(record: Customer) {
    console.log('edit', record)
}

async function handleDelete(id: number) {
    await customerApi.delete(id)
    load()
}

const typeMap: Record<number, string> = { 1: '个人', 2: '企业' }

const columns = [
    { title: '客户名称', dataIndex: 'name' },
    { title: '联系方式', dataIndex: 'phone' },
    {
        title: '客户类型', dataIndex: 'customerType', render: (type: number) =>
            Tag.color(type === 1 ? 'arcoblue' : 'purple')(typeMap[type] || '未知')
    },
    { title: '等级', dataIndex: 'level' },
    { title: '余额', dataIndex: 'balance', render: (balance: number) => `¥${balance || 0}` },
    {
        title: '状态', dataIndex: 'status', render: (status: number) =>
            status === 1 ? Tag.color('green')('正常') : Tag.color('gray')('禁用')
    },
    { title: '操作', slotName: 'actions', align: 'right' },
]
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">客户管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理客户资料</p>
            </div>
            <Button type="primary" @click="() => { }">
                <template #icon>
                    <PlusIcon class="w-4 h-4" />
                </template>
                新增客户
            </Button>
        </div>

        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索客户名称..." class="!w-64" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Button type="primary" @click="handleSearch">搜索</Button>
                <Button @click="keyword = ''; setKeyword('')">重置</Button>
            </Space>
        </Card>

        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 800 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleEdit(record)">
                            <PencilIcon class="w-4 h-4" />
                        </Button>
                        <Popconfirm title="确定删除该客户？" @ok="handleDelete(record.id)">
                            <Button type="text" status="danger" size="small">
                                删除
                            </Button>
                        </Popconfirm>
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
