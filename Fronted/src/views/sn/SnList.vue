<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { snApi, productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate } from '@/utils/format'
import { Table, Button, Input, InputNumber, Space, Tag, Card, Modal, Select, DatePicker, Message, Empty, Upload } from '@arco-design/web-vue'
import type { SnCode, Product } from '@/types'
import { PlusIcon, MagnifyingGlassIcon, ArrowPathIcon, ArrowUpTrayIcon } from '@heroicons/vue/24/outline'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(snApi.list)
const keyword = ref('')
const showAddModal = ref(false)
const showBatchModal = ref(false)
const showQueryModal = ref(false)
const querySn = ref('')
const queryResult = ref<SnCode | null>(null)

// 商品列表
const goodsList = ref<Product[]>([])
const loadingGoods = ref(false)

// 批量导入
const batchSns = ref('')

const searchForm = reactive({
    goodsId: undefined as number | undefined,
    status: undefined as number | undefined,
    startDate: '',
    endDate: '',
})

// SN码状态 - 与数据库 sn_code.status 一致
// 0:在库 1:已售 2:已作废 3:退货中 4:已退货
const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '在库', color: 'orange' },
    1: { label: '已售', color: 'arcoblue' },
    2: { label: '已作废', color: 'gray' },
    3: { label: '退货中', color: 'purple' },
    4: { label: '已退货', color: 'red' },
}

const statusOptions = [
    { label: '全部状态', value: undefined },
    { label: '在库', value: 0 },
    { label: '已售', value: 1 },
    { label: '已作废', value: 2 },
    { label: '退货中', value: 3 },
    { label: '已退货', value: 4 },
]

const newSn = reactive({
    sn: '',
    goodsId: undefined as number | undefined,
    remark: '',
})

const batchGoodsId = ref<number>()
const batchRemark = ref('')

onMounted(() => {
    load()
    fetchGoods()
})

async function fetchGoods() {
    loadingGoods.value = true
    try {
        const res = await productApi.list({ pageSize: 100 })
        goodsList.value = res.list
    } catch {
        Message.error('获取商品列表失败')
    } finally {
        loadingGoods.value = false
    }
}

function getGoodsName(goodsId?: number): string {
    if (!goodsId) return '-'
    const goods = goodsList.value.find(g => g.id === goodsId)
    return goods?.name || '-'
}

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
        return false
    }
    try {
        await snApi.create({ sn: newSn.sn, goodsId: newSn.goodsId, remark: newSn.remark })
        Message.success('录入成功')
        showAddModal.value = false
        Object.assign(newSn, { sn: '', goodsId: undefined, remark: '' })
        load()
    } catch (e: any) {
        Message.error('录入失败')
        return false
    }
}

async function handleBatchImport() {
    if (!batchGoodsId.value) {
        Message.warning('请选择商品')
        return false
    }
    if (!batchSns.value || batchSns.value.trim() === '') {
        Message.warning('请输入SN码')
        return false
    }
    const sns = batchSns.value.split('\n').map(s => s.trim()).filter(s => s.length > 0)
    if (sns.length === 0) {
        Message.warning('请输入有效的SN码')
        return false
    }
    try {
        const result = await snApi.batchCreate({ sns, goodsId: batchGoodsId.value, remark: batchRemark.value })
        Message.success(`批量导入成功: ${result.success}个，失败: ${result.failed}个`)
        showBatchModal.value = false
        batchSns.value = ''
        batchGoodsId.value = undefined
        batchRemark.value = ''
        load()
    } catch (e: any) {
        Message.error('批量导入失败')
        return false
    }
}

function handleOpenBatchModal() {
    batchGoodsId.value = undefined
    batchSns.value = ''
    batchRemark.value = ''
    showBatchModal.value = true
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
    { title: 'SN码', dataIndex: 'snCode', width: 180 },
    { title: '商品名称', dataIndex: 'spuName' },
    {
        title: '状态', dataIndex: 'status', render: (status: number) => {
            const item = statusMap[status] || { label: '未知', color: 'gray' }
            return h(Tag, { color: item.color }, () => item.label)
        }
    },
    { title: '销售时间', dataIndex: 'soldAt', render: (t: string) => t ? formatDate(t) : '-' },
    { title: '创建时间', dataIndex: 'createdAt', render: (t: string) => formatDate(t) },
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
                <Button @click="handleOpenBatchModal">
                    <template #icon>
                        <ArrowUpTrayIcon class="w-4 h-4" />
                    </template>
                    批量导入
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
                <Input v-model="keyword" placeholder="搜索SN码或商品名称..." class="w-64!" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Select v-model="searchForm.status" :options="statusOptions" placeholder="状态筛选" class="w-32!" />
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

    <!-- 录入SN码弹窗 -->
    <Modal v-model:visible="showAddModal" title="录入SN码" :on-before-ok="handleAddSn" :width="500">
        <Space direction="vertical" :size="16" class="w-full">
            <div>
                <div class="text-sm text-gray-600 mb-1">商品 *</div>
                <Select v-model="newSn.goodsId" placeholder="请选择商品" class="w-full" :loading="loadingGoods" filterable>
                    <Select.Option v-for="g in goodsList" :key="g.id" :value="g.id">
                        {{ g.name }} ({{ g.brand || '无品牌' }})
                    </Select.Option>
                </Select>
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">SN码 *</div>
                <Input v-model="newSn.sn" placeholder="请输入SN码" class="w-full" />
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">备注</div>
                <Input v-model="newSn.remark" placeholder="可选" class="w-full" />
            </div>
        </Space>
    </Modal>

    <!-- 批量导入弹窗 -->
    <Modal v-model:visible="showBatchModal" title="批量导入SN码" :on-before-ok="handleBatchImport" :width="600">
        <Space direction="vertical" :size="16" class="w-full">
            <div>
                <div class="text-sm text-gray-600 mb-1">商品 *</div>
                <Select v-model="batchGoodsId" placeholder="请选择商品" class="w-full" :loading="loadingGoods" filterable>
                    <Select.Option v-for="g in goodsList" :key="g.id" :value="g.id">
                        {{ g.name }} ({{ g.brand || '无品牌' }})
                    </Select.Option>
                </Select>
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">SN码列表（每行一个） *</div>
                <Input v-model="batchSns" placeholder="请输入SN码，每行一个" :rows="10" type="textarea" class="w-full" />
            </div>
            <div class="text-xs text-gray-500">
                提示：每行输入一个SN码，批量导入将自动识别换行符分割
            </div>
        </Space>
    </Modal>

    <!-- SN码查询弹窗 -->
    <Modal v-model:visible="showQueryModal" title="SN码查询" :width="500">
        <Space direction="horizontal" :size="12" class="w-full mb-4">
            <Input v-model="querySn" placeholder="请输入SN码" class="w-64!" @press-enter="handleQuerySn" />
            <Button type="primary" @click="handleQuerySn">查询</Button>
        </Space>

        <div v-if="queryResult" class="border-t border-gray-200 pt-4">
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <div class="text-xs text-gray-500">SN码</div>
                    <div class="text-sm font-medium">{{ queryResult.snCode }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">商品</div>
                    <div class="text-sm font-medium">{{ queryResult.spuName || '-' }}</div>
                </div>
                <div>
                    <div class="text-xs text-gray-500">状态</div>
                    <Tag :color="statusMap[queryResult.status]?.color">
                        {{ statusMap[queryResult.status]?.label }}
                    </Tag>
                </div>
                <div>
                    <div class="text-xs text-gray-500">销售时间</div>
                    <div class="text-sm">{{ queryResult.soldAt ? formatDate(queryResult.soldAt) : '-' }}</div>
                </div>
            </div>
        </div>
    </Modal>
</template>
