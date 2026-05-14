<script setup lang="ts">
import { ref, computed } from 'vue'
import { i18nApi } from '@/api'
import { Card, Input, Select, Button, Space, Message, Tag, Switch } from '@arco-design/web-vue'
import type { TranslationUnit, FieldType, LocaleEntry } from '@/types'

const entityType = ref('goods')
const entityId = ref<number | null>(null)
const units = ref<TranslationUnit[]>([])
const loading = ref(false)
const saving = ref(false)

const supportedLocales = ['zh-CN', 'en-US', 'ja-JP']

async function loadUnits() {
  if (!entityId.value) return
  loading.value = true
  try {
    const data = await i18nApi.getUnits(entityType.value, entityId.value)
    units.value = data.units
  } catch (e: any) {
    Message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function getLocaleValue(unit: TranslationUnit, locale: string): string {
  const e = unit.locales[locale]
  if (!e || e.value === null || e.value === undefined) return ''
  return String(e.value)
}

function getLocaleStatus(unit: TranslationUnit, locale: string): string {
  return unit.locales[locale]?.status || 'draft'
}

const editingCell = ref<{ unitKey: string; locale: string } | null>(null)
const editValue = ref('')

function startEdit(unitKey: string, locale: string, currentValue: string) {
  editingCell.value = { unitKey, locale }
  editValue.value = currentValue
}

function cancelEdit() {
  editingCell.value = null
}

async function saveUnit(unit: TranslationUnit, locale: string) {
  saving.value = true
  try {
    let value: unknown = editValue.value
    if (unit.fieldType === 'number') value = Number(editValue.value)
    else if (unit.fieldType === 'boolean') value = editValue.value === 'true'
    else if (unit.fieldType === 'array') value = editValue.value.split(',').map(s => s.trim()).filter(Boolean)
    else if (unit.fieldType === 'object') value = JSON.parse(editValue.value)

    await i18nApi.saveUnit(unit.unitKey, locale, value)
    if (!unit.locales[locale]) {
      unit.locales[locale] = { value, status: 'translated' } as LocaleEntry
    } else {
      unit.locales[locale]!.value = value
      unit.locales[locale]!.status = 'translated'
    }
    editingCell.value = null
    Message.success('保存成功')
  } catch (e: any) {
    Message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const statusTag = computed(() => {
  const total = units.value.length
  return supportedLocales.map(loc => {
    let ok = 0
    units.value.forEach(u => {
      const e = u.locales[loc]
      if (e && e.value !== null && e.value !== undefined && String(e.value).length > 0 && e.status !== 'draft') ok++
    })
    return { loc, percent: total ? Math.round(ok / total * 100) : 0 }
  })
})

function fieldInputType(ft: FieldType): string {
  if (ft === 'number') return 'number'
  if (ft === 'boolean') return 'checkbox'
  return 'text'
}

function renderValue(unit: TranslationUnit, locale: string): string {
  const v = getLocaleValue(unit, locale)
  if (unit.fieldType === 'array') return v.replace(/,/g, ', ')
  if (unit.fieldType === 'object') return v.length > 30 ? v.slice(0, 30) + '...' : v
  if (unit.fieldType === 'rich_text') return v.replace(/<[^>]*>/g, '').slice(0, 40) + (v.length > 40 ? '...' : '')
  return v
}
</script>

<template>
  <div class="p-4 lg:p-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">翻译编辑</h1>
        <p class="text-sm text-gray-500 mt-1">统一管理商品、分类、规格的多语言翻译</p>
      </div>
    </div>

    <!-- 实体选择 -->
    <Card class="mb-4">
      <Space :size="12" wrap>
        <Select v-model="entityType" style="width:140px">
          <Select.Option value="goods">商品</Select.Option>
          <Select.Option value="category">分类</Select.Option>
          <Select.Option value="spec">规格</Select.Option>
        </Select>
        <Input v-model.number="entityId" placeholder="实体ID" style="width:120px" />
        <Button type="primary" :loading="loading" @click="loadUnits">加载</Button>
      </Space>
    </Card>

    <!-- 状态条 -->
    <Card v-if="units.length" class="mb-4">
      <Space :size="24">
        <span v-for="s in statusTag" :key="s.loc" class="text-sm">
          <Tag :color="s.percent >= 80 ? 'green' : s.percent >= 40 ? 'orange' : 'red'" size="small">
            {{ s.loc }} {{ s.percent }}%
          </Tag>
        </span>
      </Space>
    </Card>

    <!-- 表格 -->
    <Card v-if="units.length">
      <div class="overflow-x-auto">
        <table class="w-full text-sm border-collapse">
          <thead>
            <tr class="border-b bg-gray-50">
              <th class="text-left p-2 font-medium text-gray-600">字段</th>
              <th class="text-left p-2 font-medium text-gray-600 hidden md:table-cell">说明</th>
              <th class="text-left p-2 font-medium text-gray-600 hidden md:table-cell">类型</th>
              <th v-for="loc in supportedLocales" :key="loc" class="text-left p-2 font-medium text-gray-600">
                {{ loc }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="unit in units" :key="unit.unitKey" class="border-b hover:bg-gray-50">
              <td class="p-2 font-medium text-gray-700">{{ unit.name }}</td>
              <td class="p-2 text-gray-500 hidden md:table-cell">{{ unit.description }}</td>
              <td class="p-2 text-gray-400 hidden md:table-cell">
                <Tag size="small">{{ unit.fieldType }}</Tag>
              </td>
              <td v-for="loc in supportedLocales" :key="loc" class="p-2">
                <div v-if="editingCell?.unitKey === unit.unitKey && editingCell?.locale === loc" class="flex gap-1">
                  <template v-if="unit.fieldType === 'text' || unit.fieldType === 'rich_text'">
                    <Input v-model="editValue" size="small" style="width:120px" @keyup.enter="saveUnit(unit, loc)" />
                  </template>
                  <template v-else-if="unit.fieldType === 'number'">
                    <Input v-model="editValue" type="number" size="small" style="width:80px" @keyup.enter="saveUnit(unit, loc)" />
                  </template>
                  <template v-else-if="unit.fieldType === 'boolean'">
                    <Select v-model="editValue" size="small" style="width:80px">
                      <Select.Option value="true">true</Select.Option>
                      <Select.Option value="false">false</Select.Option>
                    </Select>
                  </template>
                  <template v-else>
                    <Input v-model="editValue" size="small" style="width:140px" @keyup.enter="saveUnit(unit, loc)" />
                  </template>
                  <Button type="primary" size="mini" :loading="saving" @click="saveUnit(unit, loc)">保存</Button>
                  <Button size="mini" @click="cancelEdit">取消</Button>
                </div>
                <div v-else
                  class="cursor-pointer hover:text-blue-600 py-1 px-1 -mx-1 rounded hover:bg-blue-50 min-w-[60px] min-h-[22px]"
                  @click="startEdit(unit.unitKey, loc, getLocaleValue(unit, loc))">
                  <span v-if="getLocaleValue(unit, loc)" :class="{ 'text-orange-400': getLocaleStatus(unit, loc) === 'draft' }">
                    {{ renderValue(unit, loc) }}
                  </span>
                  <span v-else class="text-gray-300 italic">未翻译</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </Card>

    <Card v-else-if="!loading && entityId" class="text-center text-gray-400 py-8">
      暂无翻译数据，请确认实体ID
    </Card>
  </div>
</template>
