<script setup lang="ts">
import { ref, watch } from 'vue'
import { snApi } from '@/api'
import { Modal, Select, Message } from '@arco-design/web-vue'

const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void
    (e: 'saved'): void
}>()

const props = defineProps<{
    visible: boolean
    snId: number
    currentStatus: number
}>()

const editStatus = ref(0)

const statusOptions = [
    { label: '在库', value: 0 },
    { label: '已售', value: 1 },
    { label: '已作废', value: 2 },
    { label: '退货中', value: 3 },
    { label: '已退货', value: 4 },
]

watch(() => props.visible, (v) => {
    if (v) editStatus.value = props.currentStatus
})

async function handleSave() {
    try {
        await snApi.updateStatus(props.snId, editStatus.value)
        Message.success('状态更新成功')
        emit('update:visible', false)
        emit('saved')
        return true
    } catch { Message.error('操作失败'); return false }
}
</script>

<template>
    <Modal :visible="visible" title="编辑SN码" :on-before-ok="handleSave" :width="400"
        @update:visible="$emit('update:visible', $event)">
        <div class="flex flex-col gap-4">
            <div>
                <div class="text-sm text-gray-600 mb-1">SN码 ID</div>
                <div class="text-sm font-medium">{{ snId }}</div>
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">状态</div>
                <Select v-model="editStatus" class="w-full">
                    <Select.Option v-for="s in statusOptions" :key="s.value" :value="s.value">
                        {{ s.label }}
                    </Select.Option>
                </Select>
            </div>
        </div>
    </Modal>
</template>
