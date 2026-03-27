<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/24/outline'
interface Props {
    title?: string
    width?: string
}
withDefaults(defineProps<Props>(), {
    title: '',
    width: 'max-w-lg',
})

const emit = defineEmits<{
    close: []
}>()
</script>

<template>
    <Teleport to="body">
        <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <!-- 遮罩 -->
            <div class="absolute inset-0 bg-black/40" />
            <!-- 弹窗 -->
            <div class="relative bg-white rounded-2xl shadow-xl w-full overflow-hidden" :class="width">
                <!-- 头部 -->
                <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
                    <h3 class="text-lg font-semibold">{{ title }}</h3>
                    <button class="w-8 h-8 flex items-center justify-center rounded-full
                   hover:bg-gray-100 transition-colors" @click="emit('close')">
                        <XMarkIcon class="w-5 h-5" />
                    </button>
                </div>
                <!-- 内容 -->
                <div class="px-6 py-4 max-h-[70vh] overflow-y-auto">
                    <slot />
                </div>
                <!-- 底部 -->
                <div v-if="$slots.footer" class="px-6 py-4 border-t border-gray-100 bg-gray-50">
                    <slot name="footer" />
                </div>
            </div>
        </div>
    </Teleport>
</template>
