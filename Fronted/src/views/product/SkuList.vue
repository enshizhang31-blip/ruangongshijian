<script setup lang="ts">
import { onMounted, ref, computed, h } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select } from '@arco-design/web-vue'
import type { Sku, Product } from '@/types'
import { PlusIcon, PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const spuId = computed(() => Number(route.query.spuId) || 0)
const spuName = ref(route.query.spuName as string || '')

const loading = ref(false)
const error = ref<Error | null>(null)
const list = ref<Sku[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

// 所属 SPU 列表（用于筛选）
const spuList = ref<Product[]>([])
const searchSpuId = ref<number | undefined>(spuId.value || undefined)

// 弹窗
const showFormModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const form = ref<Partial<Sku>>({
  spuId: spuId.value || undefined,
  skuCode: '',
  specJson: '',
  price: 0,
  costPrice: undefined,
  unit: '件',
  imageUrl: '',
  status: 1,
})

const columns = [
  { title: '规格组合', dataIndex: 'specJson', width: 220 },
  { title: 'SKU编码', dataIndex: 'skuCode', width: 150 },
  { title: 'SPU', dataIndex: 'spuName', width: 150 },
  { title: '价格', dataIndex: 'price', width: 100 },
  { title: '成本价', dataIndex: 'costPrice', width: 100 },
  { title: '库存', dataIndex: 'stock', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', align: 'right', width: 150 },
]

onMounted(() => {
  if (spuId.value) form.value.spuId = spuId.value
  load()
  fetchSpuList()
})

async function fetchSpuList() {
  try {
    const res: any = await productApi.list({ page: 1, pageSize: 100 })
    spuList.value = res.list
  } catch { /* ignore */ }
}

async function load() {
  loading.value = true
  error.value = null
  try {
    if (searchSpuId.value) {
      const res = await productApi.getSkus(searchSpuId.value)
      list.value = res
      total.value = res.length
    } else {
      // 获取所有 SPU 的 SKU
      const allSkus: Sku[] = []
      for (const spu of spuList.value.slice(0, 10)) {
        try {
          const skus = await productApi.getSkus(spu.id)
          allSkus.push(...skus)
        } catch { /* ignore */ }
      }
      list.value = allSkus.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
      total.value = allSkus.length
    }
  } catch (e) {
    error.value = e instanceof Error ? e : new Error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  load()
}

function handleReset() {
  searchSpuId.value = spuId.value || undefined
  page.value = 1
  load()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = undefined
  form.value = {
    spuId: searchSpuId.value || spuId.value || undefined,
    skuCode: '',
    specJson: '',
    price: 0,
    costPrice: undefined,
    unit: '件',
    imageUrl: '',
    status: 1,
  }
  showFormModal.value = true
}

function handleEdit(record: Sku) {
  isEdit.value = true
  editingId.value = record.id
  form.value = { ...record }
  showFormModal.value = true
}

async function handleSubmit() {
  if (!form.value.spuId) { Message.warning('请选择所属商品'); return false }
  if (!form.value.price || form.value.price <= 0) { Message.warning('请填写有效的价格'); return false }
  try {
    if (isEdit.value && editingId.value) {
      await productApi.updateSku({ ...form.value, id: editingId.value } as Sku)
      Message.success('更新成功')
    } else {
      await productApi.createSku(form.value as Sku)
      Message.success('创建成功')
    }
    showFormModal.value = false
    load()
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDelete(id: number) {
  try { await productApi.deleteSku(id); Message.success('删除成功'); load() }
  catch (e: any) { Message.error(e?.message || '删除失败') }
}

function renderSpecJson(specJson: string | undefined) {
  if (!specJson) return '-'
  try {
    const obj = JSON.parse(specJson)
    return Object.entries(obj).map(([k, v]) => `${k}: ${v}`).join(', ')
  } catch { return specJson }
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SKU 管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品规格组合</p>
      </div>
      <Button type="primary" @click="handleAdd">
        <template #icon><PlusIcon class="w-4 h-4" /></template>
        新增SKU
      </Button>
    </div>

    <Card class="mb-4">
      <Space direction="horizontal" :size="12" wrap>
        <Select v-model="searchSpuId" placeholder="选择商品" class="w-56!" allow-clear>
          <Select.Option v-for="spu in spuList" :key="spu.id" :value="spu.id">{{ spu.name }}</Select.Option>
        </Select>
        <Button type="primary" @click="handleSearch">搜索</Button>
        <Button @click="handleReset">重置</Button>
      </Space>
    </Card>

    <Card>
      <div v-if="error" class="text-center py-8">
        <div class="text-red-500 mb-2">加载失败: {{ error.message }}</div>
        <Button type="primary" size="small" @click="load">重试</Button>
      </div>
      <div v-else-if="!loading && list.length === 0" class="text-center py-8">
        <Empty description="暂无数据" />
      </div>

      <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1100 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'specJson'">
            <Space>
              <Tag v-for="(v, k) in JSON.parse(record.specJson || '{}')" :key="k">{{ k }}: {{ v }}</Tag>
            </Space>
          </template>
          <template v-else-if="column.dataIndex === 'price'">{{ formatMoney(record.price) }}</template>
          <template v-else-if="column.dataIndex === 'costPrice'">{{ record.costPrice ? formatMoney(record.costPrice) : '-' }}</template>
          <template v-else-if="column.dataIndex === 'status'">
            <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '启用' : '禁用' }}</Tag>
          </template>
          <template v-else-if="column.dataIndex === 'createdAt'">
            {{ record.createdAt ? record.createdAt : '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'actions'">
            <Space>
              <Button type="text" size="small" @click="handleEdit(record)">
                <PencilIcon class="w-4 h-4" />
              </Button>
              <Popconfirm title="确定删除该SKU？" @ok="handleDelete(record.id)">
                <Button type="text" status="danger" size="small">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>

      <div class="flex justify-end mt-4">
        <Space direction="horizontal">
          <span class="text-sm text-gray-500">共 {{ total }} 条</span>
          <Button :disabled="page <= 1" @click="page--; load()">上一页</Button>
          <span class="text-sm py-2">第 {{ page }} / {{ Math.ceil(total / pageSize) || 1 }} 页</span>
          <Button :disabled="page >= Math.ceil(total / pageSize)" @click="page++; load()">下一页</Button>
        </Space>
      </div>
    </Card>
  </div>

  <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑SKU' : '新增SKU'" :on-before-ok="handleSubmit" :width="500">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">所属商品</div>
        <Select v-model="form.spuId" placeholder="选择商品" class="flex-1" :disabled="!!spuId">
          <Select.Option v-for="spu in spuList" :key="spu.id" :value="spu.id">{{ spu.name }}</Select.Option>
        </Select>
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">SKU编码</div>
        <Input v-model="form.skuCode" placeholder="请输入SKU编码" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">规格组合</div>
        <Input v-model="form.specJson" placeholder='JSON格式，如: {"颜色":"黑色","内存":"256G"}' class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">销售价格</div>
        <Input v-model="form.price" type="number" placeholder="0.00" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">成本价</div>
        <Input v-model="form.costPrice" type="number" placeholder="0.00" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">单位</div>
        <Input v-model="form.unit" placeholder="件" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">状态</div>
        <Select v-model="form.status" class="w-32!">
          <Select.Option :value="1">启用</Select.Option>
          <Select.Option :value="0">禁用</Select.Option>
        </Select>
      </div>
    </div>
  </Modal>
</template>