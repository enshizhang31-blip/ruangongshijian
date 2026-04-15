<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Card, Row, Col, Statistic, Space } from '@arco-design/web-vue'
import * as echarts from 'echarts'
import type { DashboardStats } from '@/types'
import {
    CurrencyDollarIcon,
    ShoppingCartIcon,
    UsersIcon,
    CubeIcon,
    ChartBarIcon,
} from '@heroicons/vue/24/outline'

const stats = ref<DashboardStats>({
    todaySales: 0,
    monthSales: 0,
    totalSales: 0,
    todayOrders: 0,
    monthOrders: 0,
    totalOrders: 0,
    totalCustomers: 0,
    totalProducts: 0,
    totalSnCodes: 0,
    todayCustomers: 0,
    lowStockProducts: 0,
})

const loading = ref(false)
const salesChartRef = ref<HTMLDivElement>()
let salesChart: echarts.ECharts | null = null

async function fetchStats() {
    loading.value = true
    try {
        stats.value = await dashboardApi.stats()
        updateSalesChart()
    } finally {
        loading.value = false
    }
}

function initSalesChart() {
    if (!salesChartRef.value) return

    salesChart = echarts.init(salesChartRef.value)

    const option: echarts.EChartsOption = {
        tooltip: {
            trigger: 'axis',
        },
        legend: {
            data: ['销售额', '订单量'],
            bottom: 0,
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '10%',
            containLabel: true,
        },
        xAxis: {
            type: 'category',
            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        },
        yAxis: [
            {
                type: 'value',
                name: '销售额',
            },
            {
                type: 'value',
                name: '订单量',
            },
        ],
        series: [
            {
                name: '销售额',
                type: 'bar',
                data: [8200, 9320, 9010, 12340, 12920, 15380, 12500],
                itemStyle: { color: '#1650D0' },
            },
            {
                name: '订单量',
                type: 'line',
                yAxisIndex: 1,
                data: [12, 25, 18, 35, 42, 38, 28],
                itemStyle: { color: '#00B42A' },
            },
        ],
    }

    salesChart.setOption(option)
}

function updateSalesChart() {
    if (!salesChart) {
        initSalesChart()
        return
    }
    // 模拟数据更新
    const option: echarts.EChartsOption = {
        series: [
            {
                name: '销售额',
                data: [
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                    Math.random() * 10000 + 5000,
                ],
            },
            {
                name: '订单量',
                data: [
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                    Math.floor(Math.random() * 50 + 10),
                ],
            },
        ],
    }
    salesChart.setOption(option)
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
                        <Statistic title="今日销售额">
                            <template #prefix>¥</template>
                            {{ formatMoney(stats.todaySales) }}
                        </Statistic>
                        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                            <CurrencyDollarIcon class="w-5 h-5 text-blue-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="本月销售额">
                            <template #prefix>¥</template>
                            {{ formatMoney(stats.monthSales) }}
                        </Statistic>
                        <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                            <CurrencyDollarIcon class="w-5 h-5 text-green-600" />
                        </div>
                    </div>
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all cursor-pointer">
                    <div class="flex items-center justify-between">
                        <Statistic title="总销售额">
                            <template #prefix>¥</template>
                            {{ formatMoney(stats.totalSales) }}
                        </Statistic>
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

        <!-- 销售趋势图表 -->
        <h3 class="text-lg font-medium text-gray-800 mb-4 mt-6">
            <Space>
                <ChartBarIcon class="w-5 h-5 text-blue-600" />
                销售趋势
            </Space>
        </h3>
        <Card>
            <div ref="salesChartRef" class="w-full h-80"></div>
        </Card>
    </div>
</template>
