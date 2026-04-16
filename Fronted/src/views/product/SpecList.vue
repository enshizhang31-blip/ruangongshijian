<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { productApi } from '@/api'
import { Table, Button, Input, Space, Card, Modal, Message, Empty, Select, Popconfirm } from '@arco-design/web-vue'
import type { Spec, SpecValue } from '@/types'
import { PlusIcon, PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'

const loading = ref(false)
const error = ref<Error | null>(null)
const list = ref<Spec[]>([])
const specValues = ref<Record<number, SpecValue[]>>({})
const valueLoading = ref<Record<number, boolean>>({})

const showSpecModal = ref(false)
const isSpecEdit = ref(false)
const editingSpecId = ref<number>()
const specForm = ref<{ name: string }>({ name: '' })

const showValueModal = ref(false)
const currentSpecId = ref<number>()
const isValueEdit = ref(false)
const editingValueId = ref<number>()
const valueForm = ref<{ value: string; sort?: number }>({ value: '', sort: 0 })

const columns = [
  { title: '规格名称', dataIndex: 'name', width: 150 },
  { title: '规格值', dataIndex: 'values', width: 400 },
  { title: '操作', slotName: 'actions', align: 'right', width: 180 },
]

const valueColumns = [
  { title: '规格值', dataIndex: 'value', width: 150 },
  { title: '排序', dataIndex: 'sort', width: 80, align: 'center' as const },
  { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]

onMounted(() => { load() })

async function load() {
  loading.value = true
  error.value = null
  try {
    list.value = await productApi.getSpecs()
  } catch (e) {
    error.value = e instanceof Error ? e : new Error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadSpecValues(specId: number) {
  if (specValues.value[specId]) return
  valueLoading.value[specId] = true
  try {
    specValues.value[specId] = await productApi.getSpecValues(specId)
  } catch { Message.error('获取规格值失败') }
  finally { valueLoading.value[specId] = false }
}

function handleAddSpec() {
  isSpecEdit.value = false
  editingSpecId.value = undefined
  specForm.value = { name: '' }
  showSpecModal.value = true
}

function handleEditSpec(record: Spec) {
  isSpecEdit.value = true
  editingSpecId.value = record.id
  specForm.value = { name: record.name }
  showSpecModal.value = true
}

async function handleSubmitSpec() {
  if (!specForm.value.name.trim()) { Message.warning('请填写规格名称'); return false }
  try {
    if (isSpecEdit.value && editingSpecId.value) {
      await productApi.updateSpec(editingSpecId.value, specForm.value)
      Message.success('更新成功')
    } else {
      await productApi.createSpec(specForm.value)
      Message.success('创建成功')
    }
    showSpecModal.value = false
    load()
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDeleteSpec(id: number) {
  try { await productApi.deleteSpec(id); Message.success('删除成功'); load() }
  catch (e: any) { Message.error(e?.message || '删除失败') }
}

function handleAddValue(specId: number) {
  currentSpecId.value = specId
  isValueEdit.value = false
  editingValueId.value = undefined
  valueForm.value = { value: '', sort: 0 }
  loadSpecValues(specId)
  showValueModal.value = true
}

function handleEditValue(specId: number, value: SpecValue) {
  currentSpecId.value = specId
  isValueEdit.value = true
  editingValueId.value = value.id
  valueForm.value = { value: value.value, sort: value.sort }
  loadSpecValues(specId)
  showValueModal.value = true
}

async function handleSubmitValue() {
  if (!valueForm.value.value.trim()) { Message.warning('请填写规格值'); return false }
  try {
    if (isValueEdit.value && editingValueId.value) {
      await productApi.updateSpecValue(editingValueId.value, valueForm.value)
      Message.success('更新成功')
    } else if (currentSpecId.value) {
      await productApi.createSpecValue(currentSpecId.value, valueForm.value)
      Message.success('创建成功')
    }
    showValueModal.value = false
    if (currentSpecId.value) loadSpecValues(currentSpecId.value)
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDeleteValue(specId: number, valueId: number) {
  try { await productApi.deleteSpecValue(valueId); Message.success('删除成功'); loadSpecValues(specId) }
  catch (e: any) { Message.error(e?.message || '删除失败') }
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">规格管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品规格名称和规格值</p>
      </div>
      <Button type="primary" @click="handleAddSpec">
        <template #icon><PlusIcon class="w-4 h-4" /></template>
        新增规格
      </Button>
    </div>

    <Card>
      <div v-if="error" class="text-center py-8">
        <div class="text-red-500 mb-2">加载失败: {{ error.message }}</div>
        <Button type="primary" size="small" @click="load">重试</Button>
      </div>
      <div v-else-if="!loading && list.length === 0" class="text-center py-8">
        <Empty description="暂无数据" />
      </div>

      <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 800 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <span class="font-medium">{{ record.name }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'values'">
            <div v-if="valueLoading[record.id]" class="text-gray-400">加载中...</div>
            <Space v-else wrap>
              <Tag v-for="v in (specValues[record.id] || [])" :key="v.id" closable @close="handleDeleteValue(record.id, v.id)">
                {{ v.value }}
              </Tag>
              <Button type="text" size="small" @click="handleAddValue(record.id)">+ 添加</Button>
            </Space>
          </template>
          <template v-else-if="column.dataIndex === 'actions'">
            <Space>
              <Button type="text" size="small" @click="handleEditSpec(record)">
                <PencilIcon class="w-4 h-4" />
              </Button>
              <Popconfirm title="确定删除该规格？" @ok="handleDeleteSpec(record.id)">
                <Button type="text" status="danger" size="small">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </Card>
  </div>

  <!-- 规格名称弹窗 -->
  <Modal v-model:visible="showSpecModal" :title="isSpecEdit ? '编辑规格' : '新增规格'" :on-before-ok="handleSubmitSpec" :width="400">
    <div class="flex items-center gap-4">
      <div class="w-20 text-sm text-gray-500">规格名称</div>
      <Input v-model="specForm.name" placeholder="如：颜色、尺寸、内存" class="flex-1" />
    </div>
  </Modal>

  <!-- 规格值弹窗 -->
  <Modal v-model:visible="showValueModal" :title="isValueEdit ? '编辑规格值' : '新增规格值'" :on-before-ok="handleSubmitValue" :width="400">
    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">规格值</div>
        <Input v-model="valueForm.value" placeholder="如：黑色、白色、红色" class="flex-1" />
      </div>
      <div class="flex items-center gap-4">
        <div class="w-20 text-sm text-gray-500">排序</div>
        <Input v-model="valueForm.sort" type="number" placeholder="0" class="w-24!" />
      </div>
    </div>
  </Modal>
</template>