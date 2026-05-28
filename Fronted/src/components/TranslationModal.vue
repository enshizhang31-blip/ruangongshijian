<script setup lang="ts">
import { ref, watch } from 'vue'
import { i18nApi } from '@/api'
import { Modal, Input, Button, Message, Tag, Table } from '@arco-design/web-vue'
import type { EntityField, EntityFieldResponse } from '@/types/i18n'

const props = defineProps<{
    visible: boolean
    entityType: string
    entityId: number
    entityName?: string
}>()

const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void
    (e: 'saved'): void
}>()

const loading = ref(false)
const data = ref<EntityFieldResponse | null>(null)
const supportedLocales = ref<string[]>([])
const editValues = ref<Record<string, Record<string, string>>>({}) // unitKey -> locale -> value
const addingLocale = ref('')

const columns = [
    { title: '字段', dataIndex: 'name', width: 120 },
    { title: '字段说明', dataIndex: 'description', width: 150 },
]

watch(() => props.visible, async (v) => {
    if (v) await loadData()
})

function addLocale() {
    const code = addingLocale.value.trim()
    if (!code) { Message.warning('请输入语言代码'); return }
    if (supportedLocales.value.includes(code)) { Message.warning('该语言已存在'); return }
    supportedLocales.value.push(code)
    // 为新语言在所有字段中初始化空值
    for (const unitKey of Object.keys(editValues.value)) {
        editValues.value[unitKey][code] = ''
    }
    // 重建列
    localeColumns.value = [
        { title: '字段', dataIndex: 'name', width: 120, fixed: 'left' as const },
        { title: '说明', dataIndex: 'description', width: 140 },
        ...supportedLocales.value.map(loc => ({
            title: loc,
            slotName: loc,
            width: 200,
        })),
    ]
    addingLocale.value = ''
    Message.success(`已添加语言: ${code}`)
}

async function loadData() {
    if (!props.entityType || !props.entityId) return
    loading.value = true
    try {
        const res = await i18nApi.getEntityFields(props.entityType, props.entityId)
        data.value = res
        const locs = await i18nApi.getLocales()
        supportedLocales.value = locs

        // 初始化编辑数据
        const vals: Record<string, Record<string, string>> = {}
        for (const f of res.fields) {
            vals[f.unitKey] = {}
            for (const loc of locs) {
                vals[f.unitKey][loc] = String(f.locales[loc]?.value ?? '')
            }
        }
        editValues.value = vals

        // 动态构建列（字段+每种语言一列）
        localeColumns.value = [
            { title: '字段', dataIndex: 'name', width: 120, fixed: 'left' as const },
            { title: '说明', dataIndex: 'description', width: 140 },
            ...locs.map(loc => ({
                title: loc,
                slotName: loc,
                width: 200,
            })),
        ]
    } catch (e: any) {
        Message.error('加载翻译数据失败: ' + (e?.message || '未知错误'))
    } finally {
        loading.value = false
    }
}

const localeColumns = ref<any[]>([])

// 扁平化字段列表供表格使用
const flatFields = ref<any[]>([])
watch(data, (d) => {
    if (!d) return
    flatFields.value = d.fields.map(f => ({
        ...f,
        unitKey: f.unitKey,
    }))
}, { immediate: true })

async function handleSave() {
    if (!data.value) return false
    try {
        const units: any[] = []
        for (const f of data.value.fields) {
            const locales: Record<string, { value: string; status: string }> = {}
            for (const loc of supportedLocales.value) {
                const val = editValues.value[f.unitKey]?.[loc] ?? ''
                const oldVal = String(f.locales[loc]?.value ?? '')
                if (val !== oldVal) {
                    locales[loc] = { value: val, status: 'translated' }
                } else if (f.locales[loc]?.value !== undefined) {
                    locales[loc] = { value: val, status: f.locales[loc]?.status as string || 'approved' }
                }
            }
            if (Object.keys(locales).length > 0) {
                units.push({ unitKey: f.unitKey, locales })
            }
        }
        if (units.length > 0) {
            await i18nApi.batchSave(data.value.entityType, data.value.entityId, units)
            Message.success('翻译保存成功')
        } else {
            Message.info('没有变化')
        }
        emit('update:visible', false)
        emit('saved')
        return true
    } catch (e: any) {
        Message.error('保存失败: ' + (e?.message || '未知错误'))
        return false
    }
}
</script>

<template>
    <Modal :visible="visible" :title="`翻译管理 - ${entityName || entityType}:${entityId}`" :on-before-ok="handleSave"
        @update:visible="$emit('update:visible', $event)" :width="800" :loading="loading">
        <div v-if="loading" class="text-center py-8 text-gray-400">加载中...</div>
        <template v-else-if="data">
            <div class="flex items-center gap-2 mb-3">
                <span class="text-sm text-gray-500">支持的语言：</span>
                <Tag v-for="loc in supportedLocales" :key="loc" :color="loc === 'zh-CN' ? 'blue' : ''">{{ loc }}</Tag>
                <span class="flex-1"></span>
                <Input v-model="addingLocale" placeholder="如 ko-KR" style="width:120px" size="small"
                    @keydown.enter.prevent="addLocale" />
                <Button type="text" size="small" @click="addLocale">+ 添加语言</Button>
            </div>
            <Table :data="flatFields" :columns="localeColumns" :pagination="false" :scroll="{ x: 800 }" bordered>
                <template #name="{ record }">
                    <span class="font-medium">{{ record.name }}</span>
                </template>
                <template #description="{ record }">
                    <span class="text-gray-500 text-sm">{{ record.description }}</span>
                </template>
                <template v-for="loc in supportedLocales" :key="loc" #[loc]="{ record }">
                    <Input v-model="editValues[record.unitKey][loc]" placeholder="输入翻译..." :max-length="500"
                        allow-clear />
                </template>
            </Table>
        </template>
    </Modal>
</template>