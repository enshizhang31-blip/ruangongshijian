<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select } from '@arco-design/web-vue'
import type { Product, ProductCategory } from '@/types'
import { PlusIcon, PencilIcon } from '@heroicons/vue/24/outline'
import TranslationModal from '@/components/TranslationModal.vue'

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

const columns = [
  { title: 'SPU名称', dataIndex: 'name', width: 200 },
  { title: '分类', dataIndex: 'categoryName', width: 100 },
  { title: '品牌', dataIndex: 'brand', width: 100 },
  { title: 'SKU数', dataIndex: 'skuCount', width: 70, align: 'center' as const },
  { title: '总库存', dataIndex: 'stockCount', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', align: 'right', width: 200 },
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

// 翻译弹窗
const translateTarget = ref<{ type: string; id: number; name: string } | null>(null)
const showTranslateModal = ref(false)

function handleTranslate(type: string, id: number, name: string) {
  translateTarget.value = { type, id, name }
  showTranslateModal.value = true
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SPU 管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品SPU，点击「SKU管理」进入SKU层级</p>
      </div>
      <Button type="primary" @click="handleAdd">
        <template #icon>
          <PlusIcon class="w-4 h-4" />
        </template>
        新增SPU
      </Button>
    </div>

    <Card class="mb-4">
      <Space direction="horizontal" :size="12" wrap>
        <Input v-model="keyword" placeholder="搜索SPU名称..." class="w-56!" @press-enter="handleSearch">
          <template #prefix><span class="text-gray-400">🔍</span></template>
        </Input>
        <Select v-model="searchCategoryId" placeholder="选择分类" class="w-44!" :loading="loadingCategories" allow-clear>
          <Select.Option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
        </Select>
        <Select v-model="searchStatus" placeholder="SPU状态" class="w-32!" allow-clear>
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

      <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1000 }">
        <template #status="{ record }">
          <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '上架' : '下架' }}</Tag>
        </template>
        <template #createdAt="{ record }">
          {{ record.createdAt ? formatDate(record.createdAt) : '-' }}
        </template>
        <template #actions="{ record }">
          <Space>
            <Button type="text" size="small" @click="router.push(`/product/${record.id}`)">SKU管理</Button>
            <Button type="text" size="small" @click="handleTranslate('goods', record.id, record.name)">翻译</Button>
            <Button type="text" size="small" @click="handleToggleStatus(record)">
              {{ record.status === 1 ? '下架' : '上架' }}
            </Button>
            <Button type="text" size="small" @click="handleEdit(record)">
              <PencilIcon class="w-4 h-4" />
            </Button>
            <Popconfirm title="确定删除该SPU？（将级联删除所有SKU和SN码）" @ok="handleDelete(record.id)">
              <Button type="text" status="danger" size="small">删除</Button>
            </Popconfirm>
          </Space>
        </template>
      </Table>

      <div class="flex justify-end mt-4">
        <Space direction="horizontal">
          <span class="text-sm text-gray-500">共 {{ total }} 条</span>
          <Button :disabled="(query.page || 1) <= 1" @click="setPage((query.page || 1) - 1)">上一页</Button>
          <span class="text-sm py-2">第 {{ query.page || 1 }} / {{ Math.ceil(total / (query.pageSize || 20)) || 1 }}
            页</span>
          <Button :disabled="(query.page || 1) >= Math.ceil(total / (query.pageSize || 20))"
            @click="setPage((query.page || 1) + 1)">下一页</Button>
        </Space>
      </div>
    </Card>
  </div>

  <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑SPU' : '新增SPU'" :on-before-ok="handleSubmit" :width="500">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">SPU名称</div>
        <Input v-model="form.name" placeholder="请输入SPU名称" class="flex-1" />
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

  <TranslationModal v-model:visible="showTranslateModal" :entity-type="translateTarget?.type || ''"
    :entity-id="translateTarget?.id || 0" :entity-name="translateTarget?.name" />
</template>
