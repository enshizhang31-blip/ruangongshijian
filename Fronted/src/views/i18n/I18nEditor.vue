<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { i18nApi } from '@/api'
import { Card, Table, Button, Input, Select, Space, Message, Pagination, Tag } from '@arco-design/web-vue'
import type { EntitySummary } from '@/types'
import TranslationModal from '@/components/TranslationModal.vue'

// ===== 实体列表 =====
const loading = ref(false)
const rows = ref<EntitySummary[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(20)
const filterEntityType = ref('')
const filterKeyword = ref('')

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

// ===== 翻译编辑弹窗 =====
const translateTarget = ref<{ type: string; id: number; name: string } | null>(null)
const showTranslateModal = ref(false)

function openEdit(entity: EntitySummary) {
  translateTarget.value = { type: entity.entityType, id: entity.entityId, name: entity.name }
  showTranslateModal.value = true
}

onMounted(() => { load() })
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

    <!-- 翻译编辑弹窗 -->
    <TranslationModal v-model:visible="showTranslateModal" :entity-type="translateTarget?.type || ''"
      :entity-id="translateTarget?.id || 0" :entity-name="translateTarget?.name" />
  </div>
</template>
