<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Card, Row, Col, Statistic, Skeleton } from '@arco-design/web-vue'
import {
    CurrencyDollarIcon,
    ShoppingCartIcon,
    UserPlusIcon,
    ExclamationTriangleIcon,
} from '@heroicons/vue/24/outline'

const stats = ref({
    todaySales: 0,
    todayOrders: 0,
    todayCustomers: 0,
    totalProducts: 0,
    lowStockProducts: 0,
})

const loading = ref(false)

async function fetchStats() {
    loading.value = true
    try {
        stats.value = await dashboardApi.stats()
    } catch {
        // handle error
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchStats()
})
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="mb-6">
            <h1 class="text-xl lg:text-2xl font-bold text-gray-800">仪表盘</h1>
            <p class="text-sm text-gray-500 mt-1">欢迎回来，查看今日数据概览</p>
        </div>

        <Row :gutter="[16, 16]" class="mb-6">
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="今日销售额" :value="stats.todaySales" :precision="2" prefix="¥" />
                        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                            <CurrencyDollarIcon class="w-5 h-5 text-blue-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="今日订单" :value="stats.todayOrders" />
                        <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                            <ShoppingCartIcon class="w-5 h-5 text-green-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="新增客户" :value="stats.todayCustomers" />
                        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                            <UserPlusIcon class="w-5 h-5 text-purple-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="低库存商品" :value="stats.lowStockProducts"
                            :value-color="stats.lowStockProducts > 0 ? '#f53f2c' : '#4e05f5'" />
                        <div class="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                            <ExclamationTriangleIcon class="w-5 h-5 text-red-600" />
                        </div>
                    </div>
                </Card>
            </Col>
        </Row>
    </div>
</template>
