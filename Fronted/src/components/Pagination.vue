<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/vue/24/outline'
interface Props {
    modelValue: number
    pageSize: number
    total: number
}
const props = defineProps<Props>()
const emit = defineEmits<{
    'update:modelValue': [page: number]
}>()

const totalPages = () => Math.ceil(props.total / props.pageSize)
</script>

<template>
    <div class="flex items-center justify-end gap-2 mt-4">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <button class="px-3 py-1 rounded-lg border border-gray-300 text-sm
             disabled:opacity-50 disabled:cursor-not-allowed
             hover:bg-gray-50 transition-colors" :disabled="modelValue <= 1"
            @click="emit('update:modelValue', modelValue - 1)">
            <ChevronLeftIcon class="w-4 h-4" />
        </button>
        <span class="text-sm">{{ modelValue }} / {{ totalPages() }}</span>
        <button class="px-3 py-1 rounded-lg border border-gray-300 text-sm
             disabled:opacity-50 disabled:cursor-not-allowed
             hover:bg-gray-50 transition-colors" :disabled="modelValue >= totalPages()"
            @click="emit('update:modelValue', modelValue + 1)">
            <ChevronRightIcon class="w-4 h-4" />
        </button>
    </div>
</template>
