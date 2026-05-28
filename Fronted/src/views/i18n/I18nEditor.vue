<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { i18nApi } from '@/api'
import { Card, Table, Button, Input, Select, Space, Message, Modal, Pagination, Tag } from '@arco-design/web-vue'
import type { EntitySummary, EntityField } from '@/types'

// ===== 实体列表 =====
const loading = ref(false)
const saving = ref(false)
const rows = ref<EntitySummary[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(20)
const filterEntityType = ref('')
const filterKeyword = ref('')
const supportedLocales = ref<string[]>([])

const entityTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'goods', label: '商品' },
  { value: 'category', label: '分类' },
  { value: 'spec', label: '规格' },
]

const entityTypeLabel: Record<string, string> = {
  goods: '商品', category: '分类', spec: '规格',
}

async function load() {
  loading.value = true
  try {
    const res = await i18nApi.searchEntities({
      entityType: filterEntityType.value || undefined,
      keyword: filterKeyword.value || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    rows.value = res.items || []
    total.value = res.total || 0
  } catch (e: any) {
    Message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadLocales() {
  try {
    supportedLocales.value = await i18nApi.getLocales()
  } catch {
    supportedLocales.value = ['zh-CN', 'en-US', 'ja-JP']
  }
}

function handleSearch() { page.value = 0; load() }
function handleReset() { filterEntityType.value = ''; filterKeyword.value = ''; page.value = 0; load() }
function handlePageChange(p: number) { page.value = p - 1; load() }
function handlePageSizeChange(size: number) { pageSize.value = size; page.value = 0; load() }

const columns = [
  { title: '名称', dataIndex: 'name', ellipsis: true },
  { title: '类型', slotName: 'entityType', width: 80 },
  { title: 'ID', dataIndex: 'entityId', width: 80, align: 'center' as const },
  { title: '操作', slotName: 'actions', width: 80, fixed: 'right' as const },
]

// ===== 编辑弹窗 =====
const editVisible = ref(false)
const fieldsLoading = ref(false)
const editingEntity = ref<EntitySummary | null>(null)
const fields = ref<EntityField[]>([])
const addingLocale = ref('')

function addLocale() {
  const code = addingLocale.value.trim()
  if (!code) { Message.warning('请输入语言代码'); return }
  if (supportedLocales.value.includes(code)) { Message.warning('该语言已存在'); return }
  supportedLocales.value.push(code)
  for (const unitKey of Object.keys(editValues.value)) {
    editValues.value[unitKey][code] = ''
  }
  addingLocale.value = ''
  Message.success(`已添加语言: ${code}`)
}

function buildEmptyEditValues(): Record<string, string> {
  const map: Record<string, string> = {}
  for (const loc of supportedLocales.value) map[loc] = ''
  return map
}
const editValues = ref<Record<string, Record<string, string>>>({})

async function openEdit(entity: EntitySummary) {
  editingEntity.value = entity
  editVisible.value = true
  fieldsLoading.value = true
  try {
    const res = await i18nApi.getEntityFields(entity.entityType, entity.entityId)
    fields.value = res.fields || []
    const ev: Record<string, Record<string, string>> = {}
    for (const f of fields.value) {
      const m: Record<string, string> = {}
      for (const loc of supportedLocales.value) {
        const entry = f.locales?.[loc]
        let v = ''
        if (entry && entry.value !== null && entry.value !== undefined) {
          v = typeof entry.value === 'string' ? entry.value : JSON.stringify(entry.value)
        }
        m[loc] = v
      }
      ev[f.unitKey] = m
    }
    editValues.value = ev
  } catch (e: any) {
    Message.error(e?.message || '加载字段失败')
  } finally {
    fieldsLoading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    for (const f of fields.value) {
      const vals = editValues.value[f.unitKey]
      if (!vals) continue
      for (const loc of supportedLocales.value) {
        const rawValue = vals[loc]
        let value: unknown = rawValue
        if (f.fieldType === 'number') value = Number(rawValue)
        else if (f.fieldType === 'boolean') value = rawValue === 'true'
        else if (f.fieldType === 'array') value = rawValue ? rawValue.split(',').map(s => s.trim()).filter(Boolean) : []
        else if (f.fieldType === 'object') {
          try { value = rawValue ? JSON.parse(rawValue) : '' } catch { value = rawValue }
        }
        await i18nApi.saveUnit(f.unitKey, loc, value, true)
      }
    }
    Message.success('保存成功')
    editVisible.value = false
  } catch (e: any) {
    Message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadLocales()
  load()
})
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">翻译编辑</h1>
        <p class="text-sm text-gray-500 mt-1">选择实体编辑多语言翻译，未翻译的语言将使用基准语言展示</p>
      </div>
    </div>

    <!-- 搜索区 -->
    <Card class="mb-4">
      <Space :size="12" wrap>
        <Select v-model="filterEntityType" :options="entityTypeOptions" style="width:140px" />
        <Input v-model="filterKeyword" placeholder="搜索名称..." style="width:220px" @keyup.enter="handleSearch"
          allow-clear />
        <Button type="primary" @click="handleSearch">搜索</Button>
        <Button @click="handleReset">重置</Button>
      </Space>
    </Card>

    <!-- 实体列表 -->
    <Card>
      <Table :columns="columns" :data="rows" :loading="loading" :pagination="false" row-key="entityId">
        <template #entityType="{ record }">
          <Tag size="small"
            :color="record.entityType === 'goods' ? 'blue' : record.entityType === 'category' ? 'green' : 'orange'">
            {{ entityTypeLabel[record.entityType] || record.entityType }}
          </Tag>
        </template>
        <template #actions="{ record }">
          <Button type="text" size="small" @click="openEdit(record)">编辑翻译</Button>
        </template>
      </Table>
      <div class="flex justify-end mt-4">
        <Pagination :current="page + 1" :total="total" :page-size="pageSize" :page-size-options="[10, 20, 50, 100]"
          show-total @change="handlePageChange" @page-size-change="handlePageSizeChange" />
      </div>
    </Card>

    <!-- 编辑弹窗 -->
    <Modal v-model:visible="editVisible" title="编辑翻译"
      :width="Math.min(780, typeof window !== 'undefined' ? window.innerWidth - 40 : 780)"
      :ok-text="saving ? '保存中...' : '保存全部'" :ok-loading="saving" @ok="handleSave">
      <template v-if="editingEntity">
        <div class="mb-4 p-3 bg-gray-50 rounded-lg text-sm">
          <span class="text-gray-400">{{ entityTypeLabel[editingEntity.entityType] }} #{{ editingEntity.entityId
          }}：</span>
          <span class="text-gray-800 font-medium">{{ editingEntity.name }}</span>
        </div>
      </template>
      <div v-if="fieldsLoading" class="text-center text-gray-400 py-8">加载字段中...</div>
      <div v-else-if="!fields.length" class="text-center text-gray-400 py-8">该实体暂无需要翻译的字段</div>
      <div v-else>
        <div class="flex items-center gap-2 mb-3 px-1">
          <span class="text-sm text-gray-500">支持的语言：</span>
          <Tag v-for="loc in supportedLocales" :key="loc" :color="loc === 'zh-CN' ? 'red' : ''" size="small">{{ loc }}
          </Tag>
          <span class="flex-1"></span>
          <Input v-model="addingLocale" placeholder="如 ko-KR" style="width:120px" size="small"
            @keydown.enter.prevent="addLocale" />
          <Button type="text" size="small" @click="addLocale">+ 添加语言</Button>
        </div>
        <div class="space-y-5 max-h-[60vh] overflow-y-auto pr-2">
          <div v-for="f in fields" :key="f.unitKey" class="border rounded-lg p-3">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-sm font-medium text-gray-700">{{ f.name }}</span>
              <Tag size="small" color="arcoblue">{{ f.fieldType }}</Tag>
              <span v-if="f.description" class="text-xs text-gray-400">{{ f.description }}</span>
            </div>
            <div class="space-y-2">
              <div v-for="loc in supportedLocales" :key="loc" class="flex items-start gap-2">
                <Tag class="shrink-0" :color="loc === 'zh-CN' ? 'red' : 'arcoblue'" size="small">{{ loc }}</Tag>
                <Input v-if="f.fieldType === 'text' || f.fieldType === 'rich_text'" v-model="editValues[f.unitKey][loc]"
                  :placeholder="loc === 'zh-CN' ? '基准语言值' : `输入${loc}翻译...`" size="small" class="flex-1" />
                <Input v-else-if="f.fieldType === 'number'" v-model="editValues[f.unitKey][loc]" type="number"
                  size="small" placeholder="输入数字..." class="flex-1" />
                <Select v-else-if="f.fieldType === 'boolean'" v-model="editValues[f.unitKey][loc]" size="small"
                  placeholder="选择..." class="flex-1">
                  <Select.Option value="true">true</Select.Option>
                  <Select.Option value="false">false</Select.Option>
                </Select>
                <Input v-else v-model="editValues[f.unitKey][loc]" size="small"
                  :placeholder="f.fieldType === 'array' ? '逗号分隔' : 'JSON'" class="flex-1" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>
