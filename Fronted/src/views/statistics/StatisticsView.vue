<script setup lang="ts">
import { ChartBarIcon, ArrowTrendingUpIcon } from '@heroicons/vue/24/outline'
</script>

<template>
    <div>
        <!-- 页面标题 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold">数据统计</h1>
                <p class="text-gray-500 mt-1">查看销售数据分析</p>
            </div>
        </div>

        <!-- 统计图表区域 -->
        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <div class="bg-white rounded-xl shadow-sm p-6">
                <h3 class="text-lg font-semibold mb-4">销售趋势</h3>
                <div class="h-64 flex items-center justify-center text-gray-400">
                    <ArrowTrendingUpIcon class="w-12 h-12 mr-3" />图表区域
                </div>
            </div>

            <div class="bg-white rounded-xl shadow-sm p-6">
                <h3 class="text-lg font-semibold mb-4">商品销量排行</h3>
                <div class="h-64 flex items-center justify-center text-gray-400">
                    <ChartBarIcon class="w-12 h-12 mr-3" />图表区域
                </div>
            </div>
        </div>
    </div>
</template>
