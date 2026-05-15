<script setup lang="ts">
import { onMounted, ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate, formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select } from '@arco-design/web-vue'
import type { Product, Sku, ProductCategory, Spec } from '@/types'
import { PlusIcon, PencilIcon, ChevronDownIcon, ChevronRightIcon } from '@heroicons/vue/24/outline'

const router = useRouter()

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(
  (params) => productApi.list(params).then((res: any) => {
    return { list: res.list, pagination: { page: params.page || 1, pageSize: res.pagination.pageSize, total: res.pagination.total } }
  })
)

const keyword = ref('')
const searchCategoryId = ref<number>()
const searchStatus = ref<number>()

const categories = ref<ProductCategory[]>([])
const loadingCategories = ref(false)

const showFormModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const form = ref<Partial<Product>>({
  name: '', categoryId: undefined, brand: '', imageUrl: '', images: '', shortDesc: '', description: '', status: 1,
})

const expandedSkus = ref<Set<number>>(new Set())
const skuData = ref<Record<number, Sku[]>>({})
const skuLoading = ref<Record<number, boolean>>({})

const specs = ref<Spec[]>([])
const showBatchSkuModal = ref(false)
const batchSkuSpuId = ref<number>(0)
const batchSkuPrefix = ref('')
const batchSkuPrice = ref<number>(0)
const batchSkuCostPrice = ref<number>(0)
const selectedSpecIds = ref<number[]>([])

const columns = [
  { title: '', dataIndex: 'expand', width: 50 },
  { title: '商品名称', dataIndex: 'name', width: 200 },
  { title: '分类', dataIndex: 'categoryName', width: 100 },
  { title: '品牌', dataIndex: 'brand', width: 100 },
  { title: '价格区间', dataIndex: 'priceRange', width: 140 },
  { title: 'SKU数', dataIndex: 'skuCount', width: 70, align: 'center' as const },
  { title: '总库存', dataIndex: 'stockCount', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', align: 'right', width: 180 },
]

const skuColumns = [
  { title: '规格组合', dataIndex: 'specJson', width: 200 },
  { title: 'SKU编码', dataIndex: 'skuCode', width: 140 },
  { title: '价格', dataIndex: 'price', width: 100 },
  { title: '成本价', dataIndex: 'costPrice', width: 100 },
  { title: '库存', dataIndex: 'stock', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', slotName: 'skuActions', align: 'right', width: 120 },
]

onMounted(() => { load(); fetchCategories() })

async function fetchCategories() {
  loadingCategories.value = true
  try { categories.value = await productApi.categories() }
  catch { Message.error('获取分类失败') }
  finally { loadingCategories.value = false }
}

function handleSearch() {
  Object.assign(query.value, { keyword: keyword.value || undefined, categoryId: searchCategoryId.value, status: searchStatus.value, page: 1 })
  load()
}

function handleReset() {
  keyword.value = ''; searchCategoryId.value = undefined; searchStatus.value = undefined
  setKeyword('')
  query.value.keyword = undefined; query.value.categoryId = undefined; query.value.status = undefined
  load()
}

function handleAdd() {
  isEdit.value = false; editingId.value = undefined
  form.value = { name: '', categoryId: undefined, brand: '', imageUrl: '', images: '', shortDesc: '', description: '', status: 1 }
  showFormModal.value = true
}

function handleEdit(record: Product) {
  isEdit.value = true; editingId.value = record.id
  form.value = { ...record }
  showFormModal.value = true
}

async function handleSubmit() {
  if (!form.value.name?.trim()) { Message.warning('请填写商品名称'); return false }
  if (form.value.name.length > 128) { Message.warning('商品名称不能超过128个字符'); return false }
  try {
    if (isEdit.value && editingId.value) {
      await productApi.update({ ...form.value, id: editingId.value } as Product)
      Message.success('更新成功')
    } else {
      await productApi.create(form.value as Product)
      Message.success('创建成功')
    }
    showFormModal.value = false; load(); return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDelete(id: number) {
  try { await productApi.delete(id); Message.success('删除成功'); load() }
  catch (e: any) { Message.error(e?.message || '删除失败') }
}

async function handleToggleStatus(record: Product) {
  const newStatus = record.status === 1 ? 0 : 1
  try { await productApi.updateStatus(record.id, newStatus); Message.success(newStatus === 1 ? '上架成功' : '下架成功'); load() }
  catch (e: any) { Message.error(e?.message || '操作失败') }
}

async function toggleSkus(productId: number) {
  if (expandedSkus.value.has(productId)) { expandedSkus.value.delete(productId); return }
  if (skuData.value[productId]) { expandedSkus.value.add(productId); return }
  skuLoading.value[productId] = true; expandedSkus.value.add(productId)
  try { skuData.value[productId] = await productApi.getSkus(productId) }
  catch { Message.error('获取SKU失败'); expandedSkus.value.delete(productId) }
  finally { skuLoading.value[productId] = false }
}

function getPriceRange(skus: Sku[] | undefined): string {
  if (!skus || skus.length === 0) return '-'
  const prices = skus.map(s => s.price)
  const min = Math.min(...prices), max = Math.max(...prices)
  return min === max ? formatMoney(min) : `${formatMoney(min)} ~ ${formatMoney(max)}`
}

async function handleDeleteSku(sku: Sku, record: Product) {
  try { await productApi.deleteSku(sku.id); Message.success('删除成功'); toggleSkus(record.id) }
  catch (e: any) { Message.error(e?.message || '删除失败') }
}

async function handleOpenBatchSku(record: Product) {
  batchSkuSpuId.value = record.id
  batchSkuPrefix.value = ''
  batchSkuPrice.value = 0
  batchSkuCostPrice.value = 0
  selectedSpecIds.value = []
  try {
    specs.value = await productApi.getSpecs()
  } catch { specs.value = [] }
  showBatchSkuModal.value = true
}

async function handleBatchGenerateSku() {
  if (selectedSpecIds.value.length === 0) { Message.warning('请至少选择一个规格'); return false }
  try {
    await productApi.batchGenerateSkus({
      spuId: batchSkuSpuId.value,
      specIds: selectedSpecIds.value,
      codePrefix: batchSkuPrefix.value || undefined,
      defaultPrice: batchSkuPrice.value || undefined,
      defaultCostPrice: batchSkuCostPrice.value || undefined,
    } as any)
    Message.success('批量生成SKU成功')
    showBatchSkuModal.value = false
    toggleSkus(batchSkuSpuId.value)
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">商品管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品信息（SPU + SKU）</p>
      </div>
      <Button type="primary" @click="handleAdd">
        <template #icon><PlusIcon class="w-4 h-4" /></template>
        新增商品
      </Button>
    </div>

    <Card class="mb-4">
      <Space direction="horizontal" :size="12" wrap>
        <Input v-model="keyword" placeholder="搜索商品名称..." class="w-56!" @press-enter="handleSearch">
          <template #prefix><span class="text-gray-400">🔍</span></template>
        </Input>
        <Select v-model="searchCategoryId" placeholder="选择分类" class="w-44!" :loading="loadingCategories" allow-clear>
          <Select.Option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
        </Select>
        <Select v-model="searchStatus" placeholder="商品状态" class="w-32!" allow-clear>
          <Select.Option :value="1">上架</Select.Option>
          <Select.Option :value="0">下架</Select.Option>
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

      <Table
        v-else
        :loading="loading"
        :columns="columns"
        :data="list"
        :pagination="false"
        :scroll="{ x: 1200 }"
        :expandable="{ expandedRowKeys: Array.from(expandedSkus), onExpand: (record: Product, expanded: boolean) => { expanded ? toggleSkus(record.id) : expandedSkus.delete(record.id) } }"
      >
        <template #expand="{ record }">
          <div class="px-8 py-4">
            <div v-if="skuLoading[record.id]" class="text-center py-4 text-gray-400">加载中...</div>
            <template v-else-if="skuData[record.id]?.length">
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm text-gray-500">规格组合列表</span>
                <Button type="outline" size="small" @click="handleOpenBatchSku(record)">批量生成SKU</Button>
              </div>
              <Table :columns="skuColumns" :data="skuData[record.id]" :pagination="false" :scroll="{ x: 800 }">
                <template #specJson="{ record: sku }">
                  <Space>
                    <Tag v-for="(v, k) in JSON.parse(sku.specJson || '{}')" :key="k">{{ k }}: {{ v }}</Tag>
                  </Space>
                </template>
                <template #price="{ record: sku }">{{ formatMoney(sku.price) }}</template>
                <template #costPrice="{ record: sku }">{{ sku.costPrice ? formatMoney(sku.costPrice) : '-' }}</template>
                <template #status="{ record: sku }">
                  <Tag :color="sku.status === 1 ? 'green' : 'gray'">{{ sku.status === 1 ? '启用' : '禁用' }}</Tag>
                </template>
                <template #skuActions="{ record: sku }">
                  <Space>
                    <Button type="text" size="small" class="text-blue-600">编辑</Button>
                    <Button type="text" size="small" class="text-purple-600" @click="router.push(`/sn/sku/${sku.id}?spuId=${record.id}`)">SN码</Button>
                    <Popconfirm title="确定删除该SKU？" @ok="handleDeleteSku(sku, record)">
                      <Button type="text" status="danger" size="small">删除</Button>
                    </Popconfirm>
                  </Space>
                </template>
              </Table>
            </template>
            <div v-else class="text-center py-4 text-gray-400">
              暂无SKU
              <div class="mt-2"><Button type="outline" size="small" @click="handleOpenBatchSku(record)">批量生成SKU</Button></div>
            </div>
          </div>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'expand'">
            <Button type="text" size="small" @click="toggleSkus(record.id)">
              <ChevronDownIcon v-if="expandedSkus.has(record.id)" class="w-4 h-4" />
              <ChevronRightIcon v-else class="w-4 h-4" />
            </Button>
          </template>
          <template v-else-if="column.dataIndex === 'priceRange'">
            {{ getPriceRange(skuData[record.id]) }}
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '上架' : '下架' }}</Tag>
          </template>
          <template v-else-if="column.dataIndex === 'stockCount'">
            <span :class="record.stockCount ? 'text-green-600 font-medium' : 'text-red-400'">{{ record.stockCount ?? 0 }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'createdAt'">
            {{ record.createdAt ? formatDate(record.createdAt) : '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'actions'">
            <Space>
              <Button type="text" size="small" @click="handleToggleStatus(record)">
                {{ record.status === 1 ? '下架' : '上架' }}
              </Button>
              <Button type="text" size="small" @click="handleEdit(record)">
                <PencilIcon class="w-4 h-4" />
              </Button>
              <Popconfirm title="确定删除该商品？" @ok="handleDelete(record.id)">
                <Button type="text" status="danger" size="small">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>

      <div class="flex justify-end mt-4">
        <Space direction="horizontal">
          <span class="text-sm text-gray-500">共 {{ total }} 条</span>
          <Button :disabled="(query.page || 1) <= 1" @click="setPage((query.page || 1) - 1)">上一页</Button>
          <span class="text-sm py-2">第 {{ query.page || 1 }} / {{ Math.ceil(total / (query.pageSize || 20)) || 1 }} 页</span>
          <Button :disabled="(query.page || 1) >= Math.ceil(total / (query.pageSize || 20))" @click="setPage((query.page || 1) + 1)">下一页</Button>
        </Space>
      </div>
    </Card>
  </div>

  <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑商品' : '新增商品'" :on-before-ok="handleSubmit" :width="500">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">商品名称</div>
        <Input v-model="form.name" placeholder="请输入商品名称" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">商品分类</div>
        <Select v-model="form.categoryId" placeholder="选择分类" class="flex-1" allow-clear>
          <Select.Option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
        </Select>
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">品牌</div>
        <Input v-model="form.brand" placeholder="请输入品牌" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">商品图片</div>
        <Input v-model="form.imageUrl" placeholder="图片URL" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">短描述</div>
        <Input v-model="form.shortDesc" placeholder="卡片/推荐位展示(最多256字)" :max-length="256" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">商品描述</div>
        <Input v-model="form.description" placeholder="商品描述" :rows="2" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">状态</div>
        <Select v-model="form.status" class="w-32!">
          <Select.Option :value="1">上架</Select.Option>
          <Select.Option :value="0">下架</Select.Option>
        </Select>
      </div>
    </div>
  </Modal>
  <Modal v-model:visible="showBatchSkuModal" title="批量生成SKU" :on-before-ok="handleBatchGenerateSku" :width="500">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">SKU前缀</div>
        <Input v-model="batchSkuPrefix" placeholder="如 IP15" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">默认价格</div>
        <Input v-model="batchSkuPrice" type="number" placeholder="0.00" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">默认成本</div>
        <Input v-model="batchSkuCostPrice" type="number" placeholder="0.00" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">规格选择</div>
        <Select v-model="selectedSpecIds" placeholder="选择规格（系统自动做笛卡尔积）" class="flex-1" multiple>
          <Select.Option v-for="spec in specs" :key="spec.id" :value="spec.id">{{ spec.name }}</Select.Option>
        </Select>
      </div>
      <p class="text-xs text-gray-400">系统将根据所选规格的所有值做笛卡尔积，自动生成所有SKU组合。</p>
    </div>
  </Modal>
</template>
