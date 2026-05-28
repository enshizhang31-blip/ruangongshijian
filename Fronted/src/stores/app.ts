import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo, setUserInfo, removeUserInfo } from '@/utils/storage'

export interface UserInfo {
    userId: number
    username: string
    realName: string
}

export const useAppStore = defineStore('app', () => {
    const sidebarCollapsed = ref(false)
    const mobileSidebarVisible = ref(false)
    const userInfo = ref<UserInfo | null>(getUserInfo())

    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value
    }

    function toggleMobileSidebar() {
        mobileSidebarVisible.value = !mobileSidebarVisible.value
    }

    function closeMobileSidebar() {
        mobileSidebarVisible.value = false
    }

    function setUser(info: UserInfo) {
        userInfo.value = info
        setUserInfo(info)
    }

    function logout() {
        userInfo.value = null
        removeUserInfo()
    }

    return {
        sidebarCollapsed,
        mobileSidebarVisible,
        userInfo,
        toggleSidebar,
        toggleMobileSidebar,
        closeMobileSidebar,
        setUser,
        logout,
    }
})
