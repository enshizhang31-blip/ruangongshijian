<script setup lang="ts">
import { ArrowPathIcon } from '@heroicons/vue/24/outline'

interface Props {
    size?: 'sm' | 'md' | 'lg'
    text?: string
}

withDefaults(defineProps<Props>(), {
    size: 'md',
    text: '加载中...',
})

const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-5 h-5',
    lg: 'w-6 h-6',
}
</script>

<template>
    <div class="flex items-center justify-center gap-2 py-8">
        <ArrowPathIcon :class="[sizeClasses[size], 'animate-spin text-blue-500']" />
        <span v-if="text" class="text-sm text-gray-500">{{ text }}</span>
    </div>
</template>
