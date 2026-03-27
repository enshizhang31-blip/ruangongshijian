<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<template>
    <div class="flex h-screen overflow-hidden">
        <!-- 侧边栏 -->
        <Sidebar />

        <!-- 主内容区 -->
        <div class="flex-1 flex flex-col overflow-hidden">
            <!-- 顶部导航 -->
            <Header />

            <!-- 页面内容 -->
            <main class="flex-1 overflow-y-auto bg-gray-50 p-6">
                <router-view />
            </main>
        </div>
    </div>
</template>
