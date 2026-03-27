<script setup lang="ts">
import { InboxIcon } from '@heroicons/vue/24/outline'
import type { Component } from 'vue'

interface Props {
    icon?: Component
    title?: string
    description?: string
}

withDefaults(defineProps<Props>(), {
    icon: InboxIcon,
    title: '暂无数据',
    description: '',
})
</script>

<template>
    <div class="flex flex-col items-center justify-center py-12 px-4 text-center">
        <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
            <component :is="icon" class="w-8 h-8 text-gray-400" />
        </div>
        <h3 class="text-base font-medium text-gray-900 mb-1">{{ title }}</h3>
        <p v-if="description" class="text-sm text-gray-500 max-w-sm">{{ description }}</p>
        <slot />
    </div>
</template>
