<script setup lang="ts">
interface Props {
    status: number | string
    map?: Record<string | number, { label: string; class: string }>
    activeText?: string
    inactiveText?: string
}

const props = withDefaults(defineProps<Props>(), {
    map: () => ({
        1: { label: '启用', class: 'bg-green-100 text-green-700' },
        0: { label: '禁用', class: 'bg-gray-100 text-gray-500' },
    }),
})

function getStatusInfo(status: number | string) {
    return props.map[status] || { label: String(status), class: 'bg-gray-100 text-gray-600' }
}
</script>

<template>
    <span class="px-2 py-1 rounded-full text-xs font-medium" :class="getStatusInfo(status).class">
        {{ getStatusInfo(status).label }}
    </span>
</template>
