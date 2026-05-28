<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { productApi } from '@/api'
import { Table, Button, Input, Space, Card, Modal, Message, Empty, Select, Popconfirm, Tag, Form, FormItem } from '@arco-design/web-vue'
import type { Spec, SpecValue } from '@/types'
import { PlusIcon, PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'
import TranslationModal from '@/components/TranslationModal.vue'

const loading = ref(false)
const error = ref<Error | null>(null)
const flatSpecs = ref<Spec[]>([])
const allSpecValues = ref<Record<number, SpecValue[]>>({})
const valueLoading = ref<Record<number, boolean>>({})

// 构建树形数据
const treeData = computed(() => {
  return flatSpecs.value.map(spec => ({
    ...spec,
    key: `spec-${spec.id}`,
    children: (allSpecValues.value[spec.id] || []).map(v => ({
      ...v,
      key: `value-${v.id}`,
      _specId: spec.id,
      _specName: spec.name,
    })),
  }))
})

const expandedKeys = ref<string[]>([])
const columns = [
  { title: '名称', slotName: 'name', width: 200 },
  { title: '翻译键', slotName: 'unitKey', width: 260 },
  { title: '操作', slotName: 'actions', align: 'right', width: 220 },
]

// 规格名弹窗（只创建名称）
const showSpecModal = ref(false)
const isSpecEdit = ref(false)
const editingSpecId = ref<number>()
const specForm = reactive({
  name: '',
})

// 规格值弹窗（编辑单个值）
const showValueModal = ref(false)
const isValueEdit = ref(false)
const editingValueId = ref<number>()
const editingSpecIdForValue = ref<number>()
const valueForm = ref<{ value: string; sort?: number }>({ value: '', sort: 0 })

onMounted(() => { load() })

async function load() {
  loading.value = true
  error.value = null
  try {
    flatSpecs.value = await productApi.getSpecs()
    // 加载所有规格的值
    for (const spec of flatSpecs.value) {
      if (!allSpecValues.value[spec.id]) loadSpecValues(spec.id)
    }
  } catch (e) {
    error.value = e instanceof Error ? e : new Error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadSpecValues(specId: number) {
  valueLoading.value[specId] = true
  try {
    allSpecValues.value[specId] = await productApi.getSpecValues(specId)
    // 自动展开有值的规格
    if (allSpecValues.value[specId]?.length) {
      expandedKeys.value.push(`spec-${specId}`)
    }
  } catch { Message.error('获取规格值失败') }
  finally { valueLoading.value[specId] = false }
}

// 规格名相关
function handleAddSpec() {
  isSpecEdit.value = false
  editingSpecId.value = undefined
  specForm.name = ''
  showSpecModal.value = true
}

function handleEditSpec(record: Spec) {
  isSpecEdit.value = true
  editingSpecId.value = record.id
  specForm.name = record.name
  showSpecModal.value = true
}

async function handleSubmitSpec() {
  if (!specForm.name.trim()) { Message.warning('请填写规格名称'); return false }
  try {
    if (isSpecEdit.value && editingSpecId.value) {
      await productApi.updateSpec(editingSpecId.value, { name: specForm.name })
      Message.success('更新成功')
    } else {
      await productApi.createSpec({ name: specForm.name })
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

// 规格值相关
function handleAddValue(specId: number) {
  isValueEdit.value = false
  editingValueId.value = undefined
  editingSpecIdForValue.value = specId
  valueForm.value = { value: '', sort: 0 }
  showValueModal.value = true
}

function handleEditValue(value: SpecValue) {
  isValueEdit.value = true
  editingValueId.value = value.id
  editingSpecIdForValue.value = (value as any)._specId
  valueForm.value = { value: value.value, sort: value.sort }
  showValueModal.value = true
}

async function handleSubmitValue() {
  if (!valueForm.value.value.trim()) { Message.warning('请填写规格值'); return false }
  try {
    if (isValueEdit.value && editingValueId.value) {
      await productApi.updateSpecValue(editingValueId.value, valueForm.value)
      Message.success('更新成功')
    } else if (editingSpecIdForValue.value) {
      await productApi.createSpecValue(editingSpecIdForValue.value, valueForm.value)
      Message.success('新增成功')
    }
    showValueModal.value = false
    if (editingSpecIdForValue.value) loadSpecValues(editingSpecIdForValue.value)
    return true
  } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDeleteValue(specId: number, valueId: number) {
  try { await productApi.deleteSpecValue(valueId); Message.success('删除成功'); loadSpecValues(specId) }
  catch (e: any) { Message.error(e?.message || '删除失败') }
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
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">规格管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品规格及其值，支持树形展示</p>
      </div>
      <Button type="primary" @click="handleAddSpec">
        <template #icon>
          <PlusIcon class="w-4 h-4" />
        </template>
        新增规格
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

      <Table v-else :loading="loading" :columns="columns" :data="treeData" :pagination="false" :scroll="{ x: 800 }"
        row-key="key" v-model:expandedKeys="expandedKeys" show-empty-tree>
        <template #name="{ record }">
          <span v-if="record._specName" class="text-gray-500">{{ record._specName }}：</span>
          <span :class="{ 'font-medium': !record._specName }">{{ record.value || record.name }}</span>
        </template>
        <template #unitKey="{ record }">
          <code v-if="record.unitKey" class="text-xs text-gray-400">{{ record.unitKey }}</code>
          <code v-else class="text-xs text-gray-400">
            {{ (record as any)._specId ? `spec:${(record as any)._specId}:value_${record.id}` : `spec:${record.id}:name` }}
          </code>
        </template>
        <template #actions="{ record }">
          <Space v-if="!record._specName">
            <!-- 规格名行 -->
            <Button type="text" size="small" @click="handleAddValue(record.id)">+ 添加值</Button>
            <Button type="text" size="small" @click="handleEditSpec(record)">编辑</Button>
            <Button type="text" size="small" @click="handleTranslate('spec', record.id, record.name)">翻译</Button>
            <Popconfirm title="确定删除该规格？" @ok="handleDeleteSpec(record.id)">
              <Button type="text" status="danger" size="small">删除</Button>
            </Popconfirm>
          </Space>
          <Space v-else>
            <!-- 规格值行（子节点） -->
            <Button type="text" size="small" @click="handleEditValue(record as any)">编辑</Button>
            <Popconfirm title="确定删除该规格值？" @ok="handleDeleteValue((record as any)._specId, record.id)">
              <Button type="text" status="danger" size="small">删除</Button>
            </Popconfirm>
          </Space>
        </template>
      </Table>
    </Card>
  </div>

  <!-- 规格名称弹窗 -->
  <Modal v-model:visible="showSpecModal" :title="isSpecEdit ? '编辑规格' : '新增规格'" :on-before-ok="handleSubmitSpec"
    :width="400">
    <a-form :model="specForm">
      <a-form-item field="name" label="规格名称">
        <a-input v-model="specForm.name" placeholder="如：颜色、尺寸、内存" />
      </a-form-item>
    </a-form>
  </Modal>

  <!-- 规格值弹窗 -->
  <Modal v-model:visible="showValueModal" :title="isValueEdit ? '编辑规格值' : '新增规格值'" :width="400"
    :on-before-ok="handleSubmitValue">
    <div class="flex items-center gap-4">
      <div class="w-20 text-sm text-gray-500">规格值</div>
      <Input v-model="valueForm.value" placeholder="如：黑色、白色、红色" class="flex-1" />
    </div>
  </Modal>

  <TranslationModal v-model:visible="showTranslateModal" :entity-type="translateTarget?.type || ''"
    :entity-id="translateTarget?.id || 0" :entity-name="translateTarget?.name" />
</template>