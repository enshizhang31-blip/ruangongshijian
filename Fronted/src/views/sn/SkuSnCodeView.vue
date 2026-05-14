<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, snApi } from '@/api'
import { formatDate, formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select } from '@arco-design/web-vue'
import type { SnCode, Sku } from '@/types'
import { PlusIcon, ArrowLeftIcon, TrashIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const skuId = ref(Number(route.params.skuId) || 0)

const loading = ref(false)
const sku = ref<Sku | null>(null)
const list = ref<SnCode[]>([])
const stats = ref<Record<string, number>>({})
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const hasValidSku = ref(false)

// SKU 选择器（当没有有效skuId时显示）
const allSkus = ref<Sku[]>([])
const selectedSkuId = ref<number>()

// 录入表单
const showFormModal = ref(false)
const showBatchModal = ref(false)
const snForm = ref({ sn: '' })
const batchSnText = ref('')
const remark = ref('')

onMounted(() => {
  if (skuId.value && skuId.value > 0) {
    hasValidSku.value = true
    loadAll()
  } else {
    loadAllSkus()
  }
})

async function loadAllSkus() {
  try {
    const res: any = await productApi.list({ page: 1, pageSize: 500 })
    const spuList: any[] = res.list
    const all: Sku[] = []
    for (const spu of spuList) {
      try {
        const skus = await productApi.getSkus(spu.id)
        all.push(...skus)
      } catch { /* ignore */ }
    }
    allSkus.value = all
  } catch { /* ignore */ }
}

function selectSku() {
  if (selectedSkuId.value) {
    skuId.value = selectedSkuId.value
    hasValidSku.value = true
    loadAll()
  }
}

async function loadAll() {
  loading.value = true
  try {
    const spuId = route.query.spuId
    if (spuId) {
      const skus = await productApi.getSkus(Number(spuId))
      sku.value = skus.find(s => s.id === skuId.value) || null
    }
    if (skuId.value > 0) {
      stats.value = await snApi.getStatsBySkuId(skuId.value)
      await loadList()
    }
  } catch (e: any) {
    Message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadList() {
  const res: any = await snApi.getBySkuId(skuId.value, { page: page.value, pageSize: pageSize.value })
  list.value = res.list
  total.value = res.pagination.total
}

function handlePageChange(p: number) {
  page.value = p
  loadList()
}

// 录入SN码
async function handleCreateSn() {
  if (!snForm.value.sn.trim()) { Message.warning('请输入SN码'); return }
  try {
    await snApi.create({ sn: snForm.value.sn, goodsId: sku.value?.spuId || 0, remark: remark.value })
    Message.success('录入成功')
    showFormModal.value = false
    snForm.value.sn = ''
    remark.value = ''
    loadAll()
  } catch (e: any) { Message.error(e?.message || '录入失败') }
}

// 批量录入
async function handleBatchCreate() {
  const sns = batchSnText.value.split('\n').map(s => s.trim()).filter(Boolean)
  if (sns.length === 0) { Message.warning('请输入SN码'); return }
  try {
    const res = await snApi.batchCreate({ sns, goodsId: sku.value?.spuId || 0, remark: remark.value })
    Message.success(`批量录入完成：成功 ${res.success}，失败 ${res.failed}`)
    showBatchModal.value = false
    batchSnText.value = ''
    remark.value = ''
    loadAll()
  } catch (e: any) { Message.error(e?.message || '录入失败') }
}

// 作废
async function handleVoid(record: SnCode) {
  try {
    await snApi.voidCode(record.id, remark.value)
    Message.success('作废成功')
    loadAll()
  } catch (e: any) { Message.error(e?.message || '操作失败') }
}

// 退货
async function handleReturn(record: SnCode) {
  try {
    await snApi.applyReturn(record.id, remark.value)
    Message.success('退货申请已提交')
    loadAll()
  } catch (e: any) { Message.error(e?.message || '操作失败') }
}

const statusOptions = [
  { value: 0, label: '在库', color: 'green' },
  { value: 1, label: '已售', color: 'blue' },
  { value: 2, label: '已作废', color: 'gray' },
  { value: 3, label: '退货中', color: 'orange' },
  { value: 4, label: '已退货', color: 'purple' },
]

function getStatusInfo(status: number) {
  return statusOptions.find(s => s.value === status) || { label: '未知', color: 'gray' }
}

function goBack() {
  router.back()
}

const columns = [
  { title: 'SN码', dataIndex: 'snCode', width: 220 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '来源', dataIndex: 'source', width: 80 },
  { title: '价格', dataIndex: 'price', width: 100 },
  { title: '录入时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', align: 'right', width: 200 },
]
</script>

<template>
  <div class="p-4 lg:p-6">
    <!-- SKU 选择器（无有效skuId时显示） -->
    <Card v-if="!hasValidSku" class="mb-4">
      <div class="text-center py-8">
        <p class="text-gray-500 mb-4">请选择一个 SKU 查看其 SN 码</p>
        <Space direction="horizontal" :size="12">
          <Select v-model="selectedSkuId" placeholder="选择SKU" class="w-64!" allow-search :filter-option="true">
            <Select.Option v-for="s in allSkus" :key="s.id" :value="s.id">
              {{ s.skuCode }} <span class="text-gray-400 text-xs">({{ s.specJson }})</span>
            </Select.Option>
          </Select>
          <Button type="primary" @click="selectSku" :disabled="!selectedSkuId">查看</Button>
        </Space>
      </div>
    </Card>

    <template v-else>
    <!-- 头部 -->
    <div class="flex items-center gap-4 mb-6">
      <Button type="text" @click="goBack">
        <ArrowLeftIcon class="w-5 h-5" />
      </Button>
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SN码管理</h1>
        <p v-if="sku" class="text-sm text-gray-500 mt-1">
          SKU: {{ sku.skuCode }} | 
          <span v-if="sku.specJson">
            <Tag v-for="(v, k) in JSON.parse(sku.specJson || '{}')" :key="k" size="small">{{ k }}: {{ v }}</Tag>
          </span>
        </p>
      </div>
    </div>

    <!-- 库存统计卡片 -->
    <div class="grid grid-cols-3 sm:grid-cols-5 gap-4 mb-4">
      <Card v-for="item in [
        { key: 'total', label: '总数', color: 'text-gray-700' },
        { key: 'inStock', label: '在库', color: 'text-green-600' },
        { key: 'sold', label: '已售', color: 'text-blue-600' },
        { key: 'voided', label: '作废', color: 'text-gray-400' },
        { key: 'returning', label: '退货中', color: 'text-orange-500' },
      ]" :key="item.key" class="text-center">
        <div class="text-2xl font-bold" :class="item.color">{{ stats[item.key] || 0 }}</div>
        <div class="text-xs text-gray-500 mt-1">{{ item.label }}</div>
      </Card>
    </div>

    <!-- 操作栏 -->
    <Card class="mb-4">
      <Space direction="horizontal" :size="12">
        <Button type="primary" @click="showFormModal = true">
          <template #icon><PlusIcon class="w-4 h-4" /></template>
          录入SN
        </Button>
        <Button @click="showBatchModal = true">批量录入</Button>
      </Space>
    </Card>

    <!-- SN码列表 -->
    <Card>
      <div v-if="loading" class="text-center py-8 text-gray-400">加载中...</div>
      <div v-else-if="list.length === 0" class="text-center py-8">
        <Empty description="暂无SN码" />
      </div>
      <Table v-else :columns="columns" :data="list" :pagination="false" :scroll="{ x: 900 }">
        <template #status="{ record }">
          <Tag :color="getStatusInfo(record.status).color">{{ getStatusInfo(record.status).label }}</Tag>
        </template>
        <template #source="{ record }">
          {{ record.source === 1 ? '手动' : record.source === 2 ? 'CSV' : record.source === 3 ? '自动' : '-' }}
        </template>
        <template #price="{ record }">{{ record.price ? formatMoney(record.price) : '-' }}</template>
        <template #createdAt="{ record }">{{ record.createdAt ? formatDate(record.createdAt) : '-' }}</template>
        <template #actions="{ record }">
          <Space>
            <Popconfirm v-if="record.status === 0" title="确定作废该SN码？" @ok="handleVoid(record)">
              <Button type="text" status="warning" size="small">作废</Button>
            </Popconfirm>
            <Popconfirm v-if="record.status === 1" title="确定发起退货？" @ok="handleReturn(record)">
              <Button type="text" status="warning" size="small">退货</Button>
            </Popconfirm>
            <span v-if="record.status === 2" class="text-xs text-gray-400">已作废</span>
            <span v-if="record.status === 3 || record.status === 4" class="text-xs text-gray-400">--</span>
          </Space>
        </template>
      </Table>

      <div class="flex justify-end mt-4" v-if="total > 0">
        <Space direction="horizontal">
          <span class="text-sm text-gray-500">共 {{ total }} 条</span>
          <Button :disabled="page <= 1" @click="handlePageChange(page - 1)">上一页</Button>
          <span class="text-sm py-2">{{ page }} / {{ Math.ceil(total / pageSize) || 1 }}</span>
          <Button :disabled="page >= Math.ceil(total / pageSize)" @click="handlePageChange(page + 1)">下一页</Button>
        </Space>
      </div>
    </Card>
    </template>
  </div>

  <!-- 录入弹窗 -->
  <Modal v-model:visible="showFormModal" title="录入SN码" @ok="handleCreateSn" :width="400">
    <div class="flex flex-col gap-4">
      <Input v-model="snForm.sn" placeholder="请输入SN码" />
      <Input v-model="remark" placeholder="备注（可选）" />
    </div>
  </Modal>

  <!-- 批量录入弹窗 -->
  <Modal v-model:visible="showBatchModal" title="批量录入SN码" @ok="handleBatchCreate" :width="500">
    <div class="flex flex-col gap-4">
      <p class="text-sm text-gray-500">每行一个SN码</p>
      <textarea v-model="batchSnText" rows="8" class="w-full border rounded-lg p-3 text-sm font-mono" placeholder="SN202501010001&#10;SN202501010002&#10;SN202501010003"></textarea>
      <Input v-model="remark" placeholder="备注（可选）" />
    </div>
  </Modal>
</template>
