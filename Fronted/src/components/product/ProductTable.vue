<script setup lang="ts">
import type { Product } from '@/types'
import { formatMoney } from '@/utils/format'
import { PencilIcon, TrashIcon, ArrowPathIcon, InboxIcon } from '@heroicons/vue/24/outline'
import StatusBadge from '../common/StatusBadge.vue'

interface Props {
    list: Product[]
    loading?: boolean
}

withDefaults(defineProps<Props>(), {
    loading: false,
})

const emit = defineEmits<{
    edit: [product: Product]
    delete: [id: number]
}>()
</script>

<template>
    <div class="overflow-x-auto">
        <table class="w-full bg-white rounded-xl text-sm min-w-max">
            <thead class="bg-gray-50">
                <tr class="text-left text-sm text-gray-600">
                    <th class="px-6 py-3 font-medium">商品名称</th>
                    <th class="px-6 py-3 font-medium">品牌</th>
                    <th class="px-6 py-3 font-medium">状态</th>
                    <th class="px-6 py-3 font-medium text-right">操作</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
                <tr v-if="loading">
                    <td colspan="7" class="text-center py-12 text-gray-400">
                        <ArrowPathIcon class="w-5 h-5 mr-2 animate-spin inline-block" />加载中...
                    </td>
                </tr>
                <template v-else-if="list.length === 0">
                    <tr>
                        <td colspan="7" class="text-center py-12">
                            <div class="flex flex-col items-center justify-center">
                                <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-2">
                                    <InboxIcon class="w-6 h-6 text-gray-400" />
                                </div>
                                <p class="text-sm text-gray-500">暂无数据</p>
                            </div>
                        </td>
                    </tr>
                </template>
                <tr v-for="item in list" :key="item.id"
                    class="border-t border-gray-100 hover:bg-gray-50 transition-colors">
                    <td class="px-6 py-4 font-medium text-gray-900">{{ item.name }}</td>
                    <td class="px-6 py-4 text-gray-500">{{ item.brand || '-' }}</td>
                    <td class="px-6 py-4">
                        <StatusBadge :status="item.status" />
                    </td>
                    <td class="px-6 py-4 text-right">
                        <button class="text-blue-500 hover:text-blue-600 mr-3" @click="emit('edit', item)">
                            <PencilIcon class="w-4 h-4" />
                        </button>
                        <button class="text-red-500 hover:text-red-600" @click="emit('delete', item.id)">
                            <TrashIcon class="w-4 h-4" />
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
