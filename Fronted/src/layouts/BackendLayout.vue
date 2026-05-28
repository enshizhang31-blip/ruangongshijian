<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<template>
    <div class="flex h-dvh overflow-hidden bg-gray-50">
        <!-- 侧边栏（桌面端固定显示，移动端抽屉式弹出） -->
        <Sidebar />

        <!-- 主内容区 -->
        <div class="flex-1 flex flex-col overflow-hidden min-w-0">
            <!-- 顶部栏（桌面端固定显示，移动端显示） -->
            <Header />

            <!-- 页面内容 -->
            <main class="flex-1 overflow-y-auto p-6">
                <router-view />
            </main>
        </div>
    </div>
</template>
