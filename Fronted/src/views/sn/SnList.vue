<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { snApi, productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Space, Tag, Card, Select, Message, Pagination } from '@arco-design/web-vue'
import { PlusIcon } from '@heroicons/vue/24/outline'
import SnAddModal from '@/components/SnAddModal.vue'
import SnEditModal from '@/components/SnEditModal.vue'
import type { Sku } from '@/types'

const { loading, list, total, query, load, setPage, setKeyword, setPageSize } = usePageQuery(snApi.list)
const showAddModal = ref(false)
const showEditModal = ref(false)
const editTarget = ref<{ id: number; status: number }>({ id: 0, status: 0 })

const skuList = ref<Sku[]>([])
const loadingSkus = ref(false)
const selectedSkuCode = ref<string>('')

const searchForm = reactive({
    status: undefined as number | undefined,
})

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

onMounted(() => { load(); loadSkus() })

async function loadSkus() {
    loadingSkus.value = true
    try {
        const goodsRes: any = await productApi.list({ pageSize: 100 })
        const all: Sku[] = []
        for (const g of goodsRes.list || []) {
            try { all.push(...await productApi.getSkus(g.id)) } catch { /* ignore */ }
        }
        skuList.value = all
    } catch { /* silent */ }
    finally { loadingSkus.value = false }
}

function handleSearch() { setKeyword(selectedSkuCode.value || undefined) }
function handleAdvancedSearch() {
    query.value = { page: 1, pageSize: query.value.pageSize || 20, keyword: selectedSkuCode.value || undefined, status: searchForm.status }
    load()
}
function handleReset() { selectedSkuCode.value = ''; searchForm.status = undefined; query.value = { page: 1, pageSize: 20 }; load() }
function handlePageChange(p: number) { setPage(p) }
function handlePageSizeChange(size: number) { setPageSize(size) }

function handleOpenEdit(record: any) {
    editTarget.value = { id: record.id, status: record.status }
    showEditModal.value = true
}

const columns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: 'SN码', dataIndex: 'snCode', width: 180 },
    { title: '商品名称', dataIndex: 'spuName' },
    { title: '状态', slotName: 'statusCol', width: 100 },
    { title: '销售时间', dataIndex: 'soldAt' },
    { title: '创建时间', dataIndex: 'createdAt' },
    { title: '操作', slotName: 'actions', align: 'right', width: 120, fixed: 'right' },
]
</script>

<template>
    <div class="p-4 lg:p-6">
        <!-- 页面标题 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SN码管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理商品SN码</p>
            </div>
            <Button type="primary" @click="handleOpenAdd">
                <template #icon>
                    <PlusIcon class="w-4 h-4"></PlusIcon>
                </template>
                新增SN码
            </Button>
        </div>

        <!-- 搜索区域 -->
        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Select v-model="selectedSkuCode" placeholder="按SKU编码筛选" style="width:260px" allow-clear
                    :loading="loadingSkus" filterable @change="handleAdvancedSearch">
                    <Select.Option v-for="s in skuList" :key="s.id" :value="s.skuCode">
                        {{ s.skuCode }}
                    </Select.Option>
                </Select>
                <Select v-model="searchForm.status" :options="statusOptions" placeholder="状态筛选" style="width:140px"
                    @change="handleAdvancedSearch"></Select>
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <!-- 数据表格 -->
        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 900 }">
                <template #statusCol="{ record }">
                    <Tag :color="(statusMap[record.status] || {}).color || 'gray'">
                        {{ (statusMap[record.status] || {}).label || '未知' }}
                    </Tag>
                </template>
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleOpenEdit(record)">编辑</Button>
                    </Space>
                </template>
            </Table>

            <!-- 分页 -->
            <div class="flex justify-end mt-4">
                <Pagination :current="query.page || 1" :total="total" :page-size="query.pageSize || 20"
                    :page-size-options="[10, 20, 50, 100]" show-total @change="handlePageChange"
                    @page-size-change="handlePageSizeChange"></Pagination>
            </div>
        </Card>
    </div>

    <SnAddModal v-model:visible="showAddModal" @saved="load" />
    <SnEditModal v-model:visible="showEditModal" :sn-id="editTarget.id" :current-status="editTarget.status"
        @saved="load" />
</template>
