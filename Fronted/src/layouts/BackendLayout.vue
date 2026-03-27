<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import BottomNav from './BottomNav.vue'
</script>

<template>
    <div class="flex h-dvh overflow-hidden bg-gray-50">
        <!-- 桌面端侧边栏 (md及以上) -->
        <Sidebar class="hidden md:flex" />

        <!-- 主内容区 -->
        <div class="flex-1 flex flex-col overflow-hidden min-w-0">
            <!-- 桌面端顶部栏 (md及以上) -->
            <Header class="hidden md:flex" />

            <!-- 页面内容 -->
            <main class="flex-1 overflow-y-auto pb-20 md:p-6">
                <router-view />
            </main>
        </div>

        <!-- 移动端底部导航 (小于md) -->
        <BottomNav class="md:hidden" />
    </div>
</template>
