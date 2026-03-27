<script setup lang="ts">
interface Props {
    loading?: boolean
}
withDefaults(defineProps<Props>(), {
    loading: false,
})
</script>

<template>
    <div class="relative">
        <table class="w-full bg-white rounded-xl overflow-hidden text-sm">
            <thead class="bg-gray-50">
                <slot name="thead" />
            </thead>
            <tbody class="divide-y divide-gray-100">
                <slot v-if="!loading" />
                <tr v-else>
                    <td :colspan="100" class="text-center py-12 text-gray-400">
                        <i class="fas fa-spinner fa-spin mr-2" />加载中...
                    </td>
                </tr>
                <tr v-if="!loading">
                    <slot name="empty" />
                </tr>
            </tbody>
        </table>
    </div>
</template>
