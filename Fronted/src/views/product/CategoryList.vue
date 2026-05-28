<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { productApi } from '@/api'
import { Table, Button, Input, Space, Card, Modal, Message, Empty, Select, Tag, Popconfirm } from '@arco-design/web-vue'
import type { ProductCategory } from '@/types'
import { PlusIcon, TrashIcon } from '@heroicons/vue/24/outline'
import TranslationModal from '@/components/TranslationModal.vue'

const loading = ref(false)
const error = ref<Error | null>(null)
const treeData = ref<ProductCategory[]>([])

const showFormModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const form = ref<Partial<ProductCategory>>({
  name: '',
  parentId: undefined,
  icon: '',
  sort: 0,
  status: 1,
})

const expandedKeys = ref<number[]>([])

const columns = [
  { title: '分类名称', dataIndex: 'name', width: 200 },
  { title: '状态', slotName: 'status', width: 80 },
  { title: '操作', slotName: 'actions', align: 'right', width: 200 },
]

// 翻译弹窗
const translateTarget = ref<{ type: string; id: number; name: string } | null>(null)
const showTranslateModal = ref(false)

function handleTranslate(type: string, id: number, name: string) {
  translateTarget.value = { type, id, name }
  showTranslateModal.value = true
}

onMounted(() => { load() })

async function load() {
  loading.value = true
  error.value = null
  try {
    const data = await productApi.categories()
    const { tree, parentIds } = buildTree(data)
    treeData.value = tree
    expandedKeys.value = parentIds
  } catch (e) {
    error.value = e instanceof Error ? e : new Error('加载失败')
  } finally {
    loading.value = false
  }
}

function buildTree(list: ProductCategory[]) {
  const map = new Map<number, ProductCategory>()
  for (const item of list) {
    map.set(item.id, { ...item, key: item.id, children: [] })
  }
  const tree: ProductCategory[] = []
  const parentIds: number[] = []
  for (const item of list) {
    const node = map.get(item.id)!
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId)!.children!.push(node)
      parentIds.push(item.parentId)
    } else {
      tree.push(node)
    }
  }
  return { tree, parentIds: [...new Set(parentIds)] }
}

function handleAdd(parentId?: number) {
  isEdit.value = false
  editingId.value = undefined
  form.value = { name: '', parentId, icon: '', sort: 0, status: 1 }
  showFormModal.value = true
}

function handleEdit(record: ProductCategory) {
  isEdit.value = true
  editingId.value = record.id
  form.value = { ...record }
  showFormModal.value = true
}

async function handleSubmit() {
  if (!form.value.name?.trim()) { Message.warning('请填写分类名称'); return false }
  try {
    if (isEdit.value && editingId.value) {
      await productApi.updateCategory(editingId.value, form.value)
      Message.success('更新成功')
    } else {
      await productApi.createCategory(form.value)
      Message.success('创建成功')
    }
    showFormModal.value = false
    load()
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDelete(id: number) {
  try {
    await productApi.deleteCategory(id)
    Message.success('删除成功')
    load()
  } catch (e: any) { Message.error(e?.message || '删除失败') }
}

async function handleToggleStatus(record: ProductCategory) {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await productApi.updateCategory(record.id, { status: newStatus })
    Message.success('状态更新成功')
    load()
  }
  catch (e: any) {
    Message.error(e?.message || '操作失败')
  }
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">商品分类</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品分类（支持多级）</p>
      </div>
      <Button type="primary" @click="handleAdd()">
        <template #icon>
          <PlusIcon class="w-4 h-4" />
        </template>
        新增分类
      </Button>
    </div>

    <Card>
      <div v-if="error" class="text-center py-8">
        <div class="text-red-500 mb-2">加载失败: {{ error.message }}</div>
        <Button type="primary" size="small" @click="load">重试</Button>
      </div>
      <div v-else-if="!loading && treeData.length === 0" class="text-center py-8">
        <Empty description="暂无数据" />
      </div>

      <Table v-else :loading="loading" :columns="columns" :data="treeData" :pagination="false" :scroll="{ x: 600 }"
        column-resizable row-key="id" v-model:expandedKeys="expandedKeys" show-empty-tree>
        <template #name="{ record }">
          <span>{{ record.name }}</span>
        </template>
        <template #status="{ record }">
          <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '启用' : '未启用' }}</Tag>
        </template>
        <template #actions="{ record }">
          <Space>
            <Button type="text" size="small" @click="handleAdd(record.id)">添加子分类</Button>
            <Button type="text" size="small" @click="handleEdit(record)">编辑</Button>
            <Button type="text" size="small" @click="handleTranslate('category', record.id, record.name)">翻译</Button>
            <Popconfirm :content="`确定将状态改为${record.status === 1 ? '未启用' : '启用'}吗？`" @ok="handleToggleStatus(record)">
              <Button type="text" size="small">{{ record.status === 1 ? '禁用' : '启用' }}</Button>
            </Popconfirm>
            <Popconfirm content="确定删除该分类？" @ok="handleDelete(record.id)">
              <Button type="text" status="danger" size="small">删除</Button>
            </Popconfirm>
          </Space>
        </template>
      </Table>
    </Card>
  </div>

  <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑分类' : '新增分类'" :on-before-ok="handleSubmit" :width="500">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">分类名称</div>
        <Input v-model="form.name" placeholder="请输入分类名称" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">上级分类</div>
        <Select v-model="form.parentId" placeholder="选择上级分类（不选则为顶级）" class="flex-1" allow-clear>
          <Select.Option v-for="cat in treeData" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
        </Select>
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">状态</div>
        <Select v-model="form.status" class="w-32!">
          <Select.Option :value="1">启用</Select.Option>
          <Select.Option :value="0">未启用</Select.Option>
        </Select>
      </div>
    </div>
  </Modal>

  <TranslationModal v-model:visible="showTranslateModal" :entity-type="translateTarget?.type || ''"
    :entity-id="translateTarget?.id || 0" :entity-name="translateTarget?.name" />
</template>