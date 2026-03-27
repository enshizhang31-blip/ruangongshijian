<script setup lang="ts">
import { ArrowPathIcon } from '@heroicons/vue/24/outline'
interface Props {
    loading?: boolean
}
withDefaults(defineProps<Props>(), {
    loading: false,
})
</script>

<template>
    <div class="overflow-x-auto">
        <table class="w-full bg-white rounded-xl text-sm min-w-max">
            <thead class="bg-gray-50">
                <slot name="thead" />
            </thead>
            <tbody class="divide-y divide-gray-100">
                <slot v-if="!loading" />
                <tr v-else>
                    <td colspan="100" class="text-center py-12 text-gray-400">
                        <ArrowPathIcon class="w-5 h-5 mr-2 animate-spin inline-block" />加载中...
                    </td>
                </tr>
                <tr v-if="!loading">
                    <slot name="empty" />
                </tr>
            </tbody>
        </table>
    </div>
</template>
