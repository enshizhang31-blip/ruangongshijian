<script setup lang="ts">
import { ArrowTrendingUpIcon, ArrowTrendingDownIcon } from '@heroicons/vue/24/outline'

interface Props {
    label: string
    value: string | number
    trend?: number
    icon?: object
    iconBg?: string
    iconColor?: string
    valueColor?: string
}

withDefaults(defineProps<Props>(), {
    iconBg: 'bg-blue-100',
    iconColor: 'text-blue-600',
    valueColor: 'text-gray-900',
})
</script>

<template>
    <div class="bg-white rounded-xl shadow-sm p-6 hover:-translate-y-0.5 hover:shadow-lg transition-all">
        <div class="flex items-center justify-between">
            <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-500 mb-1 truncate">{{ label }}</p>
                <p class="text-2xl font-bold" :class="valueColor">{{ value }}</p>
                <div v-if="trend !== undefined" class="flex items-center gap-1 mt-1">
                    <ArrowTrendingUpIcon v-if="trend > 0" class="w-4 h-4 text-green-500" />
                    <ArrowTrendingDownIcon v-else-if="trend < 0" class="w-4 h-4 text-red-500" />
                    <span class="text-xs font-medium"
                        :class="trend > 0 ? 'text-green-500' : trend < 0 ? 'text-red-500' : 'text-gray-400'">
                        {{ trend > 0 ? '+' : '' }}{{ trend }}%
                    </span>
                </div>
            </div>
            <div v-if="icon" class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0" :class="iconBg">
                <component :is="icon" class="w-6 h-6" :class="iconColor" />
            </div>
        </div>
    </div>
</template>
