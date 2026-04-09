<script setup lang="ts">
import type { Customer } from '@/types'
import { PencilIcon, TrashIcon, ArrowPathIcon, InboxIcon } from '@heroicons/vue/24/outline'
import StatusBadge from '../common/StatusBadge.vue'

interface Props {
    list: Customer[]
    loading?: boolean
}

withDefaults(defineProps<Props>(), {
    loading: false,
})

const emit = defineEmits<{
    edit: [customer: Customer]
    delete: [id: number]
}>()

const levelMap: Record<number, { label: string; class: string }> = {
    1: { label: '普通', class: 'bg-gray-100 text-gray-700' },
    2: { label: '银卡', class: 'bg-gray-200 text-gray-800' },
    3: { label: '金卡', class: 'bg-yellow-100 text-yellow-700' },
    4: { label: '钻石', class: 'bg-purple-100 text-purple-700' },
}
</script>

<template>
    <div class="overflow-x-auto">
        <table class="w-full bg-white rounded-xl text-sm min-w-max">
            <thead class="bg-gray-50">
                <tr class="text-left text-sm text-gray-600">
                    <th class="px-6 py-3 font-medium">昵称</th>
                    <th class="px-6 py-3 font-medium">手机号</th>
                    <th class="px-6 py-3 font-medium">会员等级</th>
                    <th class="px-6 py-3 font-medium">余额</th>
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
                    <td class="px-6 py-4 font-medium text-gray-900">{{ item.nickname || '-' }}</td>
                    <td class="px-6 py-4 text-gray-500">{{ item.phone || '-' }}</td>
                    <td class="px-6 py-4">
                        <span class="px-2 py-1 rounded-full text-xs" :class="levelMap[item.memberLevel || 1]?.class">
                            {{ levelMap[item.memberLevel || 1]?.label || '普通' }}
                        </span>
                    </td>
                    <td class="px-6 py-4 text-blue-600 font-medium">¥{{ item.balance?.toFixed(2) || '0.00' }}</td>
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
