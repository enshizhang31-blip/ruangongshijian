<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Card, Row, Col, Statistic } from '@arco-design/web-vue'
import {
    CurrencyDollarIcon,
    ShoppingCartIcon,
    UsersIcon,
    CubeIcon,
} from '@heroicons/vue/24/outline'

const stats = ref({
    todaySales: 0,
    monthSales: 0,
    totalSales: 0,
    todayOrders: 0,
    monthOrders: 0,
    totalOrders: 0,
    totalCustomers: 0,
    totalProducts: 0,
    totalSnCodes: 0,
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
            <p class="text-sm text-gray-500 mt-1">欢迎回来，查看数据概览</p>
        </div>

        <!-- 销售统计 -->
        <h3 class="text-lg font-medium text-gray-800 mb-4">销售统计</h3>
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
                        <Statistic title="本月销售额" :value="stats.monthSales" :precision="2" prefix="¥" />
                        <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                            <CurrencyDollarIcon class="w-5 h-5 text-green-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="总销售额" :value="stats.totalSales" :precision="2" prefix="¥" />
                        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                            <CurrencyDollarIcon class="w-5 h-5 text-purple-600" />
                        </div>
                    </div>
                </Card>
            </Col>
        </Row>

        <!-- 订单统计 -->
        <h3 class="text-lg font-medium text-gray-800 mb-4">订单统计</h3>
        <Row :gutter="[16, 16]" class="mb-6">
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="今日订单" :value="stats.todayOrders" />
                        <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                            <ShoppingCartIcon class="w-5 h-5 text-green-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="本月订单" :value="stats.monthOrders" />
                        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                            <ShoppingCartIcon class="w-5 h-5 text-blue-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="总订单" :value="stats.totalOrders" />
                        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                            <ShoppingCartIcon class="w-5 h-5 text-purple-600" />
                        </div>
                    </div>
                </Card>
            </Col>
        </Row>

        <!-- 资产统计 -->
        <h3 class="text-lg font-medium text-gray-800 mb-4">资产统计</h3>
        <Row :gutter="[16, 16]">
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="客户总数" :value="stats.totalCustomers" />
                        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                            <UsersIcon class="w-5 h-5 text-purple-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="商品总数" :value="stats.totalProducts" />
                        <div class="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                            <CubeIcon class="w-5 h-5 text-orange-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="8" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="SN码总数" :value="stats.totalSnCodes" />
                        <div class="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                            <CubeIcon class="w-5 h-5 text-red-600" />
                        </div>
                    </div>
                </Card>
            </Col>
        </Row>
    </div>
</template>
