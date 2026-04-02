<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import * as echarts from 'echarts'
import { Card, Row, Col, Statistic, DatePicker, Select, Space, Button } from '@arco-design/web-vue'
import { dashboardApi } from '@/api'
import type { DashboardStats } from '@/types'

const stats = ref<DashboardStats>({
    todaySales: 0,
    todayOrders: 0,
    todayCustomers: 0,
    totalProducts: 0,
    lowStockProducts: 0,
})

const loading = ref(false)
const dateRange = ref([])
const salesChartRef = ref<HTMLDivElement>()
const rankingChartRef = ref<HTMLDivElement>()

let salesChart: echarts.ECharts | null = null
let rankingChart: echarts.ECharts | null = null

onMounted(async () => {
    await fetchStats()
    initSalesChart()
    initRankingChart()
})

async function fetchStats() {
    loading.value = true
    try {
        stats.value = await dashboardApi.stats()
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
                data: [8200, 9320, 12901, 15320, 22900, 28650, 32500],
                itemStyle: { color: '#0f62fe' },
            },
            {
                name: '订单量',
                type: 'line',
                yAxisIndex: 1,
                data: [120, 200, 150, 300, 250, 400, 380],
                itemStyle: { color: '#00b42a' },
            },
        ],
    }

    salesChart.setOption(option)

    // 响应窗口大小变化
    window.addEventListener('resize', () => {
        salesChart?.resize()
    })
}

function initRankingChart() {
    if (!rankingChartRef.value) return

    rankingChart = echarts.init(rankingChartRef.value)

    const option: echarts.EChartsOption = {
        tooltip: {
            trigger: 'item',
        },
        legend: {
            orient: 'vertical',
            left: 'left',
        },
        series: [
            {
                name: '商品分类',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2,
                },
                label: {
                    show: false,
                    position: 'center',
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 16,
                        fontWeight: 'bold',
                    },
                },
                data: [
                    { value: 1048, name: '数码产品' },
                    { value: 735, name: '服装' },
                    { value: 580, name: '食品' },
                    { value: 484, name: '家居' },
                    { value: 300, name: '其他' },
                ],
            },
        ],
    }

    rankingChart.setOption(option)

    window.addEventListener('resize', () => {
        rankingChart?.resize()
    })
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <!-- 页面标题 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">数据统计</h1>
                <p class="text-sm text-gray-500 mt-1">查看销售数据分析</p>
            </div>
            <Space>
                <DatePicker.RangePicker v-model="dateRange" />
                <Select placeholder="全部商品" class="!w-32">
                    <Select.Option value="">全部商品</Select.Option>
                </Select>
                <Button type="primary">查询</Button>
            </Space>
        </div>

        <!-- 统计卡片 -->
        <Row :gutter="[16, 16]" class="mb-6">
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all">
                    <Statistic title="今日销售额" :value="stats.todaySales" :precision="2" prefix="¥" />
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all">
                    <Statistic title="今日订单" :value="stats.todayOrders" />
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all">
                    <Statistic title="今日新增客户" :value="stats.todayCustomers" />
                </Card>
            </Col>
            <Col :xs="24" :sm="12" :xl="6">
                <Card class="hover:shadow-lg transition-all">
                    <Statistic title="低库存商品" :value="stats.lowStockProducts" suffix="件" />
                </Card>
            </Col>
        </Row>

        <!-- 图表区域 -->
        <Row :gutter="[16, 16]">
            <Col :xs="24" :xl="14">
                <Card>
                    <template #title>销售趋势</template>
                    <div ref="salesChartRef" class="h-80"></div>
                </Card>
            </Col>
            <Col :xs="24" :xl="10">
                <Card>
                    <template #title>商品分类占比</template>
                    <div ref="rankingChartRef" class="h-80"></div>
                </Card>
            </Col>
        </Row>
    </div>
</template>
