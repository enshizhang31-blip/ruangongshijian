<script setup lang="ts">
import type { SaleOrder } from '@/types'
import { formatMoney, formatDate } from '@/utils/format'
import { EyeIcon, PencilIcon, ArrowPathIcon, InboxIcon } from '@heroicons/vue/24/outline'
import StatusBadge from '../common/StatusBadge.vue'

interface Props {
    list: SaleOrder[]
    loading?: boolean
}

withDefaults(defineProps<Props>(), {
    loading: false,
})

const emit = defineEmits<{
    view: [order: SaleOrder]
    edit: [order: SaleOrder]
}>()

const orderStatusMap: Record<number, { label: string; class: string }> = {
    1: { label: '待付款', class: 'bg-yellow-100 text-yellow-700' },
    2: { label: '已付款', class: 'bg-green-100 text-green-700' },
    3: { label: '已完成', class: 'bg-blue-100 text-blue-700' },
    4: { label: '已取消', class: 'bg-gray-100 text-gray-500' },
}

const paymentStatusMap: Record<number, { label: string; class: string }> = {
    1: { label: '未支付', class: 'bg-red-100 text-red-700' },
    2: { label: '已支付', class: 'bg-green-100 text-green-700' },
}
</script>

<template>
    <div class="overflow-x-auto">
        <table class="w-full bg-white rounded-xl text-sm min-w-max">
            <thead class="bg-gray-50">
                <tr class="text-left text-sm text-gray-600">
                    <th class="px-6 py-3 font-medium">订单号</th>
                    <th class="px-6 py-3 font-medium">客户</th>
                    <th class="px-6 py-3 font-medium">订单金额</th>
                    <th class="px-6 py-3 font-medium">实付金额</th>
                    <th class="px-6 py-3 font-medium">订单状态</th>
                    <th class="px-6 py-3 font-medium">下单时间</th>
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
                    <td class="px-6 py-4 font-mono text-sm">{{ item.orderNo }}</td>
                    <td class="px-6 py-4">{{ item.customerName || '-' }}</td>
                    <td class="px-6 py-4 text-gray-400 line-through">¥{{ formatMoney(item.totalAmount) }}</td>
                    <td class="px-6 py-4 text-blue-600 font-bold">¥{{ formatMoney(item.payAmount) }}</td>
                    <td class="px-6 py-4">
                        <StatusBadge :status="item.status || 0" :map="orderStatusMap" />
                    </td>
                    <td class="px-6 py-4 text-gray-500 text-sm">{{ formatDate(item.createdAt || '') }}</td>
                    <td class="px-6 py-4 text-right">
                        <button class="text-blue-500 hover:text-blue-600 mr-3" @click="emit('view', item)">
                            <EyeIcon class="w-4 h-4" />
                        </button>
                        <button class="text-blue-500 hover:text-blue-600" @click="emit('edit', item)">
                            <PencilIcon class="w-4 h-4" />
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
