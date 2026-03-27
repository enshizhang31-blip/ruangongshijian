<script setup lang="ts">
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import Modal from '../Modal.vue'

interface Props {
    modelValue: boolean
    title?: string
    message?: string
    confirmText?: string
    cancelText?: string
    danger?: boolean
}

withDefaults(defineProps<Props>(), {
    title: '确认操作',
    message: '确定要执行此操作吗？',
    confirmText: '确定',
    cancelText: '取消',
    danger: false,
})

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    confirm: []
    cancel: []
}>()

function handleConfirm() {
    emit('confirm')
    emit('update:modelValue', false)
}

function handleCancel() {
    emit('cancel')
    emit('update:modelValue', false)
}
</script>

<template>
    <Modal :model-value="modelValue" :title="title" width="max-w-sm" @close="handleCancel">
        <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0"
                :class="danger ? 'bg-red-100' : 'bg-blue-100'">
                <ExclamationTriangleIcon class="w-5 h-5" :class="danger ? 'text-red-600' : 'text-blue-600'" />
            </div>
            <p class="text-gray-600">{{ message }}</p>
        </div>
        <template #footer>
            <div class="flex justify-end gap-3">
                <button
                    class="px-4 py-2 rounded-lg text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 transition-colors"
                    @click="handleCancel">
                    {{ cancelText }}
                </button>
                <button class="px-4 py-2 rounded-lg text-sm font-medium text-white transition-colors"
                    :class="danger ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'"
                    @click="handleConfirm">
                    {{ confirmText }}
                </button>
            </div>
        </template>
    </Modal>
</template>
