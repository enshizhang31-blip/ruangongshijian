<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { i18nApi, productApi } from '@/api'
import { Table, Card, Tag, Button, Message } from '@arco-design/web-vue'

interface StatusRow {
  entityType: string
  entityId: number
  name: string
  totalUnits: number
  locales: { locale: string; completeness: number; outdated: number }[]
}

const loading = ref(false)
const rows = ref<StatusRow[]>([])

async function load() {
  loading.value = true
  try {
    // Load goods list first
    const products = await productApi.getList({ page: 1, size: 50 })
    const list: StatusRow[] = []

    for (const p of (products as any).list || []) {
      try {
        const status = await i18nApi.getStatus('goods', p.id)
        list.push({
          entityType: 'goods',
          entityId: p.id,
          name: p.name,
          totalUnits: status.totalUnits,
          locales: Object.entries(status.localesStatus).map(([locale, s]) => ({
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

onMounted(() => load())

const columns = [
  { title: '实体', dataIndex: 'name', width: 200 },
  { title: '总字段', dataIndex: 'totalUnits', width: 80, align: 'center' as const },
  { title: '中文', slotName: 'zh-CN', width: 120 },
  { title: 'English', slotName: 'en-US', width: 120 },
  { title: '日本語', slotName: 'ja-JP', width: 120 },
]

function progressColor(pct: number) {
  if (pct >= 80) return '#00b42a'
  if (pct >= 40) return '#ff7d00'
  return '#f53f3c'
}
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
        <template #zh-CN="{ record }">
          <template v-for="l in record.locales" :key="l.locale">
            <div v-if="l.locale === 'zh-CN'" class="flex items-center gap-2">
              <div class="w-16 h-2 bg-gray-100 rounded overflow-hidden">
                <div class="h-full rounded transition-all" :style="{ width: l.completeness + '%', backgroundColor: progressColor(l.completeness) }" />
              </div>
              <span class="text-xs text-gray-500">{{ l.completeness }}%</span>
            </div>
          </template>
        </template>
        <template #en-US="{ record }">
          <template v-for="l in record.locales" :key="l.locale">
            <div v-if="l.locale === 'en-US'" class="flex items-center gap-2">
              <div class="w-16 h-2 bg-gray-100 rounded overflow-hidden">
                <div class="h-full rounded transition-all" :style="{ width: l.completeness + '%', backgroundColor: progressColor(l.completeness) }" />
              </div>
              <span class="text-xs text-gray-500">{{ l.completeness }}%</span>
              <Tag v-if="l.outdated > 0" size="small" color="orange">{{ l.outdated }}过时</Tag>
            </div>
          </template>
        </template>
        <template #ja-JP="{ record }">
          <template v-for="l in record.locales" :key="l.locale">
            <div v-if="l.locale === 'ja-JP'" class="flex items-center gap-2">
              <div class="w-16 h-2 bg-gray-100 rounded overflow-hidden">
                <div class="h-full rounded transition-all" :style="{ width: l.completeness + '%', backgroundColor: progressColor(l.completeness) }" />
              </div>
              <span class="text-xs text-gray-500">{{ l.completeness }}%</span>
            </div>
          </template>
        </template>
      </Table>
    </Card>
  </div>
</template>
