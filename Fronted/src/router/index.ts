import { createRouter, createWebHistory } from 'vue-router'
import { BackendLayout } from '@/layouts'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
    },
    {
      path: '/',
      component: BackendLayout,
      redirect: '/dashboard',
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        {
          path: '/product',
          name: 'Product',
          component: () => import('@/views/product/ProductList.vue'),
        },
        {
          path: '/sn',
          name: 'Sn',
          component: () => import('@/views/sn/SnList.vue'),
        },
        {
          path: '/customer',
          name: 'Customer',
          component: () => import('@/views/customer/CustomerList.vue'),
        },
        {
          path: '/sale',
          name: 'Sale',
          component: () => import('@/views/sale/SaleList.vue'),
        },
        {
          path: '/statistics',
          name: 'Statistics',
          component: () => import('@/views/statistics/StatisticsView.vue'),
        },
        {
          path: '/admin/user',
          name: 'AdminUser',
          component: () => import('@/views/admin/AdminUserList.vue'),
        },
        {
          path: '/settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

export default router
