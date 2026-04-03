<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { snApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card, Modal, Select, DatePicker, Message } from '@arco-design/web-vue'
import type { SnCode } from '@/types'
import { PlusIcon, MagnifyingGlassIcon, ArrowPathIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(snApi.list)
const keyword = ref('')
const showAddModal = ref(false)
const showQueryModal = ref(false)
const querySn = ref('')
const queryResult = ref<SnCode | null>(null)

const searchForm = reactive({
    goodsId: undefined as number | undefined,
    status: undefined as number | undefined,
    startDate: '',
    endDate: '',
})

// SN码状态
const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '未绑定', color: 'orange' },
    1: { label: '已绑定', color: 'arcoblue' },
    2: { label: '已使用', color: 'green' },
    3: { label: '已退货', color: 'red' },
}

const statusOptions = [
    { label: '全部状态', value: undefined },
    { label: '未绑定', value: 0 },
    { label: '已绑定', value: 1 },
    { label: '已使用', value: 2 },
    { label: '已退货', value: 3 },
]

const newSn = reactive({
    sn: '',
    goodsId: undefined as number | undefined,
    remark: '',
})

onMounted(() => {
    load()
})

function handleSearch() {
    setKeyword(keyword.value)
}

function handleAdvancedSearch() {
    Object.assign(query.value, {
        ...searchForm,
        page: 1,
    })
    load()
}

function handleReset() {
    keyword.value = ''
    searchForm.goodsId = undefined
    searchForm.status = undefined
    searchForm.startDate = ''
    searchForm.endDate = ''
    setKeyword('')
}

async function handleAddSn() {
    if (!newSn.sn || newSn.goodsId === undefined) {
        Message.warning('请填写完整信息')
        return
    }
    try {
        await snApi.create({ sn: newSn.sn, goodsId: newSn.goodsId, remark: newSn.remark })
        Message.success('录入成功')
        showAddModal.value = false
        Object.assign(newSn, { sn: '', goodsId: undefined, remark: '' })
        load()
    } catch {
        Message.error('录入失败')
    }
}

async function handleQuerySn() {
    if (!querySn.value) {
        Message.warning('请输入SN码')
        return
    }
    try {
        queryResult.value = await snApi.query(querySn.value)
    } catch {
        Message.warning('未找到该SN码')
        queryResult.value = null
    }
}

const columns = [
    { title: 'SN码', dataIndex: 'sn', width: 180 },
    { title: '商品名称', dataIndex: 'goodsName' },
    {
        title: '状态', dataIndex: 'status', render: (status: number) => {
            const item = statusMap[status] || { label: '未知', color: 'gray' }
            return h(Tag, { color: item.color }, () => item.label)
        }
    },
    { title: '绑定时间', dataIndex: 'bindTime', render: (t: string) => t ? formatDate(t) : '-' },
    { title: '使用时间', dataIndex: 'usedTime', render: (t: string) => t ? formatDate(t) : '-' },
    { title: '创建时间', dataIndex: 'createTime', render: (t: string) => formatDate(t) },
    { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]
</script>

<template>
    <div class="p-4 lg:p-6">
        <!-- 页面标题 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SN码管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理商品SN码录入与查询</p>
            </div>
            <Space>
                <Button type="primary" @click="showQueryModal = true">
                    <template #icon>
                        <MagnifyingGlassIcon class="w-4 h-4" />
                    </template>
                    SN码查询
                </Button>
                <Button type="primary" @click="showAddModal = true">
                    <template #icon>
                        <PlusIcon class="w-4 h-4" />
                    </template>
                    录入SN码
                </Button>
            </Space>
        </div>

        <!-- 搜索区域 -->
        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索SN码或商品名称..." class="!w-64" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Select v-model="searchForm.status" :options="statusOptions" placeholder="状态筛选" class="!w-32" />
                <Button type="primary" @click="handleAdvancedSearch">搜索</Button>
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <!-- 数据表格 -->
        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 900 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="querySn = record.sn; showQueryModal = true">
                            <ArrowPathIcon class="w-4 h-4" />
                        </Button>
                    </Space>
                </template>
            </Table>

            <!-- 分页 -->
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

    <!-- 录入SN码弹窗 -->
    <Modal v-model:visible="showAddModal" title="录入SN码" @ok="handleAddSn" :width="400">
        <Space direction="vertical" :size="16" class="w-full">
            <div>
                <div class="text-sm text-gray-600 mb-1">SN码 *</div>
                <Input v-model="newSn.sn" placeholder="请输入SN码" class="w-full" />
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">商品 *</div>
                <Input v-model="newSn.sn" placeholder="请输入商品ID" class="w-full" />
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">备注</div>
                <Input v-model="newSn.remark" placeholder="可选" class="w-full" />
            </div>
        </Space>
    </Modal>

    <!-- SN码查询弹窗 -->
    <Modal v-model:visible="showQueryModal" title="SN码查询" :width="500">
        <Space direction="horizontal" :size="12" class="w-full mb-4">
            <Input v-model="querySn" placeholder="请输入SN码" class="!w-64" @press-enter="handleQuerySn" />
            <Button type="primary" @click="handleQuerySn">查询</Button>
        </Space>

        <div v-if="queryResult" class="border-t border-gray-200 pt-4">
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <div class="text-xs text-gray-500">SN码</div>
                    <div class="text-sm font-medium">{{ queryResult.sn }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">商品</div>
                    <div class="text-sm font-medium">{{ queryResult.goodsName || '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">状态</div>
                    <Tag :color="statusMap[queryResult.status]?.color">
                        {{ statusMap[queryResult.status]?.label }}
                    </Tag>
                </div>
                <div>
                    <div class="text-xs text-gray-500">绑定时间</div>
                    <div class="text-sm">{{ queryResult.bindTime ? formatDate(queryResult.bindTime) : '-' }}</div>
                </div>
            </div>
        </div>
    </Modal>
</template>
