<script setup lang="ts">
import type { DashboardStats } from '@/types'
import { formatMoney } from '@/utils/format'
import {
    CurrencyDollarIcon,
    ShoppingCartIcon,
    UserPlusIcon,
    ExclamationTriangleIcon,
} from '@heroicons/vue/24/outline'
import StatCard from '../common/StatCard.vue'

interface Props {
    stats: DashboardStats
}

defineProps<Props>()
</script>

<template>
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 md:gap-6">
        <StatCard label="今日销售额" :value="'¥' + formatMoney(stats.todaySales)" :icon="CurrencyDollarIcon"
            icon-bg="bg-blue-100" icon-color="text-blue-600" value-color="text-blue-600" />
        <StatCard label="今日订单" :value="stats.todayOrders" :icon="ShoppingCartIcon" icon-bg="bg-green-100"
            icon-color="text-green-600" value-color="text-gray-900" />
        <StatCard label="新增客户" :value="stats.todayCustomers" :icon="UserPlusIcon" icon-bg="bg-purple-100"
            icon-color="text-purple-600" value-color="text-gray-900" />
        <StatCard label="低库存商品" :value="stats.lowStockProducts" :icon="ExclamationTriangleIcon" icon-bg="bg-red-100"
            icon-color="text-red-600" :value-color="stats.lowStockProducts > 0 ? 'text-red-600' : 'text-gray-900'" />
    </div>
</template>
