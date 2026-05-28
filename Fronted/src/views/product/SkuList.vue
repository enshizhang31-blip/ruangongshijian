<script setup lang="ts">
import { onMounted, ref, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select, Form, FormItem } from '@arco-design/web-vue'
import type { Sku, Product, Spec, SpecValue } from '@/types'
import { PlusIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const spuId = computed(() => Number(route.query.spuId) || 0)
const spuName = ref(route.query.spuName as string || '')

const loading = ref(false)
const error = ref<Error | null>(null)
const list = ref<Sku[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const spuList = ref<Product[]>([])
const searchSpuId = ref<number | undefined>(spuId.value || undefined)

// 规格缓存
const allSpecs = ref<Spec[]>([])
const specValueCache = ref<Record<number, SpecValue[]>>({})
const valueMap = ref<Record<number, SpecValue>>({})

function buildSpecMaps(specs: Spec[]) {
  const vm: Record<number, SpecValue> = {}
  for (const s of specs) {
    for (const v of s.values || []) vm[v.id] = v
  }
  valueMap.value = vm
}

async function getSpecValues(specId: number): Promise<SpecValue[]> {
  if (specValueCache.value[specId]) return specValueCache.value[specId]
  try { const vals = await productApi.getSpecValues(specId); specValueCache.value[specId] = vals; for (const v of vals) valueMap.value[v.id] = v; return vals }
  catch { return [] }
}

function onSpecSelected(row: any) {
  if (row.specId) getSpecValues(row.specId)
  row.valueId = undefined
}

// 解析 specJson
function renderSpecTags(specJson?: string): { name: string; value: string }[] {
  if (!specJson) return []
  try {
    const obj = JSON.parse(specJson)
    return Object.entries(obj).map(([k, v]) => {
      const valId = Number(v)
      if (valId && valueMap.value[valId]) {
        const spec = allSpecs.value.find(s => s.values?.some(sv => sv.id === valId))
        return { name: spec?.name || k, value: valueMap.value[valId].value }
      }
      return { name: k, value: String(v) }
    })
  } catch { return [] }
}

const columns = [
  { title: '规格组合', slotName: 'specJson', width: 220 },
  { title: 'SKU编码', dataIndex: 'skuCode', width: 150 },
  { title: 'SPU', dataIndex: 'spuName', width: 150 },
  { title: '价格', dataIndex: 'price', width: 100 },
  { title: '成本价', dataIndex: 'costPrice', width: 100 },
  { title: '库存', dataIndex: 'stock', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', align: 'right', width: 150 },
]

// 弹窗（动态规格表单）
const showFormModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()

interface SpecRow { specId: number | undefined; valueId: number | undefined }
const formModel = reactive({
  spuId: spuId.value || undefined as number | undefined,
  skuCode: '',
  specs: [{ specId: undefined, valueId: undefined }] as SpecRow[],
  price: 0,
  costPrice: undefined as number | undefined,
  unit: '件',
  imageUrl: '',
  status: 1,
})

const availableSpecs = computed(() => {
  const used = new Set(formModel.specs.map(r => r.specId).filter((id): id is number => !!id))
  return allSpecs.value.filter(s => !used.has(s.id))
})

function addSpecRow() {
  formModel.specs.push({ specId: undefined, valueId: undefined })
}

function removeSpecRow(index: number) {
  formModel.specs.splice(index, 1)
}

watch(() => formModel.specs.map(r => `${r.specId}-${r.valueId}`).join(','), () => {
  const obj: Record<string, string> = {}
  for (const row of formModel.specs) {
    if (row.specId && row.valueId) obj[String(row.specId)] = String(row.valueId)
  }
  if (!isEdit.value && !formModel.skuCode) {
    formModel.skuCode = autoGenerateSkuCode(obj)
  }
})

function autoGenerateSkuCode(_specIdObj: Record<string, string>): string {
  const spu = spuList.value.find(s => s.id === formModel.spuId)
  const abbr = spu?.name
    ? spu.name.replace(/[\u4e00-\u9fa5]/g, '').replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 6) || 'SPU'
    : 'SPU'
  const id = editingId.value || Date.now() % 10000
  return `SKU-${abbr}-${id}-SPU${formModel.spuId || ''}`
}

onMounted(() => {
  if (spuId.value) formModel.spuId = spuId.value
  load()
  fetchSpuList()
  productApi.getSpecs().then(specs => { allSpecs.value = specs; buildSpecMaps(specs) }).catch(() => { })
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
  isEdit.value = false; editingId.value = undefined
  formModel.spuId = searchSpuId.value || spuId.value || undefined
  formModel.skuCode = ''
  formModel.specs = [{ specId: undefined, valueId: undefined }]
  formModel.price = 0
  formModel.costPrice = undefined
  formModel.unit = '件'
  formModel.imageUrl = ''
  formModel.status = 1
  showFormModal.value = true
}

function handleEdit(record: Sku) {
  isEdit.value = true; editingId.value = record.id
  formModel.spuId = record.spuId
  formModel.skuCode = record.skuCode
  formModel.specs = []
  formModel.price = record.price
  formModel.costPrice = record.costPrice
  formModel.unit = record.unit || '件'
  formModel.imageUrl = record.imageUrl || ''
  formModel.status = record.status
  try {
    const obj = JSON.parse(record.specJson || '{}')
    for (const [k, v] of Object.entries(obj)) {
      const specId = Number(k); const valId = Number(v)
      if (specId && valId) formModel.specs.push({ specId, valueId: valId })
    }
  } catch { /* ignore */ }
  if (formModel.specs.length === 0) formModel.specs.push({ specId: undefined, valueId: undefined })
  showFormModal.value = true
}

async function handleSubmit() {
  if (!formModel.spuId) { Message.warning('请选择所属商品'); return false }
  if (!formModel.price || formModel.price <= 0) { Message.warning('请填写有效的价格'); return false }
  try {
    const obj: Record<string, string> = {}
    for (const row of formModel.specs) {
      if (row.specId && row.valueId) obj[String(row.specId)] = String(row.valueId)
    }
    const data: Partial<Sku> = {
      spuId: formModel.spuId,
      skuCode: formModel.skuCode,
      specJson: JSON.stringify(obj),
      price: formModel.price,
      costPrice: formModel.costPrice,
      unit: formModel.unit,
      imageUrl: formModel.imageUrl,
      status: formModel.status,
    }
    if (isEdit.value && editingId.value) {
      await productApi.updateSku({ ...data, id: editingId.value } as Sku)
      Message.success('更新成功')
    } else {
      await productApi.createSku(data as Sku)
      Message.success('创建成功')
    }
    showFormModal.value = false; load(); return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDelete(id: number) {
  try { await productApi.deleteSku(id); Message.success('删除成功'); load() }
  catch (e: any) { Message.error(e?.message || '删除失败') }
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
        <template #icon>
          <PlusIcon class="w-4 h-4" />
        </template>
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
        <template #specJson="{ record }">
          <Space>
            <Tag v-for="tag in renderSpecTags(record.specJson)" :key="tag.name">{{ tag.name }}: {{ tag.value }}</Tag>
          </Space>
        </template>
        <template #price="{ record }">{{ formatMoney(record.price) }}</template>
        <template #costPrice="{ record }">{{ record.costPrice ? formatMoney(record.costPrice) : '-' }}</template>
        <template #status="{ record }">
          <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '启用' : '禁用' }}</Tag>
        </template>
        <template #createdAt="{ record }">
          {{ record.createdAt ? record.createdAt : '-' }}
        </template>
        <template #actions="{ record }">
          <Space>
            <Button type="text" size="small" @click="handleEdit(record)">编辑</Button>
            <Button type="text" size="small" class="text-purple-600"
              @click="router.push(`/sn/sku/${record.id}?spuId=${record.spuId}`)">SN码</Button>
            <Popconfirm title="确定删除该SKU？" @ok="handleDelete(record.id)">
              <Button type="text" status="danger" size="small">删除</Button>
            </Popconfirm>
          </Space>
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

  <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑SKU' : '新增SKU'" :on-before-ok="handleSubmit" :width="580">
    <a-form :model="formModel">
      <a-form-item field="spuId" label="所属商品">
        <Select v-model="formModel.spuId" placeholder="选择商品" :disabled="!!spuId">
          <Select.Option v-for="spu in spuList" :key="spu.id" :value="spu.id">{{ spu.name }}</Select.Option>
        </Select>
      </a-form-item>
      <a-form-item field="skuCode" label="SKU编码">
        <a-input v-model="formModel.skuCode" placeholder="选择规格后自动生成" />
      </a-form-item>

      <a-form-item label="规格组合" :content-flex="false" :merge-props="false">
        <a-space direction="vertical" fill>
          <a-form-item v-for="(spec, index) in formModel.specs" :key="index" :field="`specs[${index}].valueId`"
            no-style>
            <a-space>
              <Select v-model="formModel.specs[index].specId" placeholder="选择规格名" style="width:120px"
                @change="onSpecSelected(formModel.specs[index])">
                <Select.Option v-for="s in availableSpecs" :key="s.id" :value="s.id">{{ s.name }}</Select.Option>
              </Select>
              <Select v-model="formModel.specs[index].valueId" placeholder="选择规格值" style="width:200px"
                :disabled="!formModel.specs[index].specId">
                <Select.Option v-for="v in (specValueCache[formModel.specs[index].specId || 0] || [])" :key="v.id"
                  :value="v.id">{{ v.value }}</Select.Option>
              </Select>
              <Button type="text" status="danger" size="small" @click="removeSpecRow(index)">删除</Button>
            </a-space>
          </a-form-item>
          <Button type="text" size="small" @click="addSpecRow">+ 添加规格</Button>
        </a-space>
      </a-form-item>

      <a-form-item field="price" label="销售价格">
        <a-input v-model="formModel.price" type="number" placeholder="0.00" />
      </a-form-item>
      <a-form-item field="costPrice" label="成本价">
        <a-input v-model="formModel.costPrice" type="number" placeholder="0.00" />
      </a-form-item>
      <a-form-item field="unit" label="单位">
        <a-input v-model="formModel.unit" placeholder="件" />
      </a-form-item>
      <a-form-item field="status" label="状态">
        <Select v-model="formModel.status" style="width:140px">
          <Select.Option :value="1">启用</Select.Option>
          <Select.Option :value="0">禁用</Select.Option>
        </Select>
      </a-form-item>
    </a-form>
  </Modal>
</template>