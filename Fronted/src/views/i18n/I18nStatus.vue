<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { i18nApi, productApi } from '@/api'
import { Table, Card, Tag, Button, Message, TableColumn } from '@arco-design/web-vue'

interface StatusRow {
  entityType: string
  entityId: number
  name: string
  totalUnits: number
  locales: { locale: string; completeness: number; outdated: number }[]
}

const loading = ref(false)
const rows = ref<StatusRow[]>([])
const supportedLocales = ref<string[]>([])

const localeLabels: Record<string, string> = {
  'zh-CN': '中文', 'en-US': 'English', 'ja-JP': '日本語',
}

const columns = computed<TableColumn[]>(() => {
  const base: TableColumn[] = [
    { title: '实体', dataIndex: 'name', width: 200 },
    { title: '总字段', dataIndex: 'totalUnits', width: 80, align: 'center' },
  ]
  for (const loc of supportedLocales.value) {
    base.push({
      title: localeLabels[loc] || loc,
      slotName: loc,
      width: 160,
    })
  }
  return base
})

async function loadLocales() {
  try {
    supportedLocales.value = await i18nApi.getLocales()
  } catch {
    supportedLocales.value = ['zh-CN', 'en-US', 'ja-JP']
  }
}

async function load() {
  loading.value = true
  try {
    const products = await productApi.list({ page: 1, pageSize: 100 })
    const list: StatusRow[] = []

    for (const p of (products as any).list || []) {
      try {
        const status = await i18nApi.getStatus('goods', p.id)
        list.push({
          entityType: 'goods',
          entityId: p.id,
          name: p.name,
          totalUnits: status.totalUnits,
          locales: (Object.entries(status.localesStatus) as [string, any][]).map(([locale, s]) => ({
            locale,
            completeness: s.completeness,
            outdated: s.outdated,
          })),
        })
      } catch {
        // skip errors silently
      }
    }
    rows.value = list
  } catch (e: any) {
    Message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function findLocale(row: StatusRow, locale: string) {
  return row.locales.find(l => l.locale === locale)
}

function progressColor(pct: number) {
  if (pct >= 80) return '#00b42a'
  if (pct >= 40) return '#ff7d00'
  return '#f53f3c'
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
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">翻译状态总览</h1>
        <p class="text-sm text-gray-500 mt-1">查看各实体多语言翻译完成度</p>
      </div>
      <Button type="primary" :loading="loading" @click="load">刷新</Button>
    </div>

    <Card>
      <Table :columns="columns" :data="rows" :loading="loading" :pagination="false" row-key="entityId">
        <template v-for="loc in supportedLocales" :key="loc" #[loc]="{ record }">
          <template v-if="findLocale(record, loc)">
            <div class="flex items-center gap-2">
              <div class="w-16 h-2 bg-gray-100 rounded overflow-hidden">
                <div class="h-full rounded transition-all" :style="{ width: findLocale(record, loc)!.completeness + '%', backgroundColor: progressColor(findLocale(record, loc)!.completeness) }" />
              </div>
              <span class="text-xs text-gray-500">{{ findLocale(record, loc)!.completeness }}%</span>
              <Tag v-if="findLocale(record, loc)!.outdated > 0" size="small" color="orange">{{ findLocale(record, loc)!.outdated }}过时</Tag>
            </div>
          </template>
          <span v-else class="text-gray-300 text-xs">-</span>
        </template>
      </Table>
    </Card>
  </div>
</template>
