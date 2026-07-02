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
  { title: '操作', slotName: 'actions', align: 'right', width: 220, fixed: 'right' },
]

// 规格名弹窗（只创建名称）
const showSpecModal = ref(false)
const isSpecEdit = ref(false)
const editingSpecId = ref<number>()
const specForm = reactive({
  name: '',
})

// 规格值弹窗（多值批量录入, 实时显示已添加列表）
const showValueModal = ref(false)
const isValueEdit = ref(false)
const editingValueId = ref<number>()
const editingSpecIdForValue = ref<number>()
const valueForm = ref<{ value: string; sort?: number }>({ value: '', sort: 0 })
// 批量添加模式: 暂存待添加的值列表
const pendingValues = ref<string[]>([])
// 当前规格的所有已存在值（用于去重判断）
const existingValues = ref<string[]>([])
const batchInput = ref('')
const batchSaving = ref(false)

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
  // 初始化批量模式: 加载已有值列表
  const existing = allSpecValues.value[specId] || []
  existingValues.value = existing.map(v => v.value)
  pendingValues.value = []
  batchInput.value = ''
  showValueModal.value = true
}

function handleEditValue(value: SpecValue) {
  isValueEdit.value = true
  editingValueId.value = value.id
  editingSpecIdForValue.value = (value as any)._specId
  valueForm.value = { value: value.value, sort: value.sort }
  showValueModal.value = true
}

// 解析批量输入: 支持逗号/换行/空格/分号分隔
function parseBatchInput(text: string): string[] {
  if (!text) return []
  return text.split(/[,\n\s;；，\t]+/)
    .map(s => s.trim())
    .filter(Boolean)
}

// 添加到待提交列表
function addToPending() {
  const items = parseBatchInput(batchInput.value)
  if (items.length === 0) {
    Message.warning('请输入规格值')
    return
  }
  let added = 0
  let skipped = 0
  for (const v of items) {
    if (pendingValues.value.includes(v) || existingValues.value.includes(v)) {
      skipped++
      continue
    }
    pendingValues.value.push(v)
    added++
  }
  batchInput.value = ''
  if (added > 0) {
    Message.success(`已添加 ${added} 个${skipped > 0 ? `, 跳过 ${skipped} 个重复` : ''}`)
  } else if (skipped > 0) {
    Message.warning(`全部 ${skipped} 个已存在或重复`)
  }
}

// 从待提交列表移除
function removeFromPending(idx: number) {
  pendingValues.value.splice(idx, 1)
}

// 清空待提交列表
function clearPending() {
  if (pendingValues.value.length === 0) return
  pendingValues.value = []
}

// 一键提交批量
async function handleSubmitBatch() {
  if (pendingValues.value.length === 0) {
    Message.warning('请先添加规格值')
    return
  }
  if (!editingSpecIdForValue.value) return
  batchSaving.value = true
  try {
    const res = await productApi.batchCreateSpecValues(
      editingSpecIdForValue.value,
      [...pendingValues.value]
    )
    Message.success(`批量添加成功, 共 ${res.length} 个`)
    showValueModal.value = false
    pendingValues.value = []
    batchInput.value = ''
    loadSpecValues(editingSpecIdForValue.value)
  } catch (e: any) {
    Message.error(e?.message || '批量添加失败')
  } finally {
    batchSaving.value = false
  }
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
  <Modal v-model:visible="showValueModal"
    :title="isValueEdit ? '编辑规格值' : `添加规格值 (${pendingValues.length} 个待提交)`"
    :width="560"
    :footer="isValueEdit ? undefined : false"
    :on-before-ok="isValueEdit ? handleSubmitValue : () => false">
    <!-- 编辑模式 -->
    <div v-if="isValueEdit" class="flex items-center gap-4">
      <div class="w-20 text-sm text-gray-500">规格值</div>
      <Input v-model="valueForm.value" placeholder="如：黑色、白色、红色" class="flex-1" />
    </div>

    <!-- 批量添加模式 -->
    <div v-else class="flex flex-col gap-4">
      <Alert type="info" :show-icon="true">
        <div class="text-sm">
          一次性输入多个规格值, 已添加的会显示在下方列表中, 确认后一键提交。
          <br />支持<b>空格</b>、<b>逗号</b>、<b>分号</b>、<b>换行</b>分隔。
        </div>
      </Alert>

      <!-- 输入区 -->
      <div>
        <div class="text-sm text-gray-600 mb-1.5">输入规格值</div>
        <div class="flex gap-2">
          <Input v-model="batchInput" placeholder="如：黑色 白色 红色 或 黑色,白色,红色" class="flex-1"
            allow-clear @press-enter="addToPending" size="large">
            <template #prefix>
              <PlusIcon class="w-4 h-4 text-gray-400" />
            </template>
          </Input>
          <Button type="primary" size="large" @click="addToPending" :disabled="!batchInput.trim()">
            <template #icon><PlusIcon class="w-4 h-4" /></template>
            添加到列表
          </Button>
        </div>
        <div class="text-xs text-gray-400 mt-1">
          💡 提示: 按回车或点击"添加到列表"按钮即可加入下方列表
        </div>
      </div>

      <!-- 待提交列表 -->
      <div v-if="pendingValues.length > 0" class="border border-blue-200 rounded-lg bg-blue-50/30">
        <div class="flex items-center justify-between px-3 py-2 border-b border-blue-200 bg-blue-50 rounded-t-lg">
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium text-blue-700">待提交列表</span>
            <Tag color="arcoblue" size="small">{{ pendingValues.length }} 个</Tag>
          </div>
          <Button type="text" size="mini" status="danger" @click="clearPending">
            <template #icon><TrashIcon class="w-3.5 h-3.5" /></template>
            清空
          </Button>
        </div>
        <div class="max-h-48 overflow-y-auto p-2">
          <div v-for="(v, idx) in pendingValues" :key="`${v}-${idx}`"
            class="flex items-center justify-between px-3 py-1.5 mb-1 bg-white rounded border border-gray-200 hover:border-blue-300 group">
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-400 w-6 text-right">{{ idx + 1 }}.</span>
              <span class="text-sm text-gray-800">{{ v }}</span>
            </div>
            <Button type="text" size="mini" status="danger"
              class="opacity-0 group-hover:opacity-100 transition-opacity"
              @click="removeFromPending(idx)">
              <template #icon>
                <TrashIcon class="w-3.5 h-3.5" />
              </template>
            </Button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-6 text-gray-400 text-sm border border-dashed rounded-lg">
        📋 暂未添加规格值
      </div>

      <!-- 已存在值提示 (折叠) -->
      <details v-if="existingValues.length > 0" class="text-xs">
        <summary class="cursor-pointer text-gray-500 hover:text-gray-700">
          已存在 {{ existingValues.length }} 个值 (点击展开)
        </summary>
        <div class="mt-2 flex flex-wrap gap-1.5">
          <Tag v-for="v in existingValues" :key="v" color="gray" size="small">{{ v }}</Tag>
        </div>
      </details>
    </div>

    <!-- 批量模式底部操作栏 -->
    <template v-if="!isValueEdit" #footer>
      <div class="flex items-center justify-between w-full">
        <div class="text-xs text-gray-500">
          共 <b class="text-blue-600">{{ pendingValues.length }}</b> 个待提交
          <span v-if="existingValues.length > 0" class="ml-2">
            (已存在 {{ existingValues.length }} 个)
          </span>
        </div>
        <Space>
          <Button @click="showValueModal = false">取消</Button>
          <Button type="primary" :loading="batchSaving" :disabled="pendingValues.length === 0"
            @click="handleSubmitBatch">
            <template #icon>
              <PlusIcon class="w-4 h-4" />
            </template>
            确认添加 {{ pendingValues.length > 0 ? `${pendingValues.length} 个` : '' }}
          </Button>
        </Space>
      </div>
    </template>
  </Modal>

  <TranslationModal v-model:visible="showTranslateModal" :entity-type="translateTarget?.type || ''"
    :entity-id="translateTarget?.id || 0" :entity-name="translateTarget?.name" />
</template>