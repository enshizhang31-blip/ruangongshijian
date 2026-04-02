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
    const userInfo = ref<UserInfo | null>(getUserInfo())

    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value
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
        userInfo,
        toggleSidebar,
        setUser,
        logout,
    }
})
