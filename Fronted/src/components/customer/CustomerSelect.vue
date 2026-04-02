<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { customerApi } from '@/api'
import type { Customer } from '@/types'
import { CheckIcon } from '@heroicons/vue/24/outline'
import LoadingSpinner from '../common/LoadingSpinner.vue'
import EmptyState from '../common/EmptyState.vue'

interface Props {
    modelValue?: number
    placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
    placeholder: '选择客户',
})

const emit = defineEmits<{
    'update:modelValue': [value: number]
}>()

const loading = ref(false)
const showDropdown = ref(false)
const customers = ref<Customer[]>([])
const selectedCustomer = ref<Customer | null>(null)

async function fetchCustomers() {
    loading.value = true
    try {
        const result = await customerApi.list({ page: 1, pageSize: 100 }) as { list: Customer[] }
        customers.value = result.list.filter((c: Customer) => c.status === 1)
    } finally {
        loading.value = false
    }
}

function select(customer: Customer) {
    selectedCustomer.value = customer
    emit('update:modelValue', customer.id)
    showDropdown.value = false
}

onMounted(() => {
    fetchCustomers()
})
</script>

<template>
    <div class="relative">
        <button type="button"
            class="w-full flex items-center justify-between px-4 py-2.5 border border-gray-300 rounded-xl bg-white hover:border-blue-400 transition-colors"
            @click="showDropdown = !showDropdown">
            <span :class="selectedCustomer ? 'text-gray-900' : 'text-gray-400'">
                {{ selectedCustomer?.name || placeholder }}
            </span>
            <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
        </button>

        <div v-if="showDropdown"
            class="absolute z-20 w-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg max-h-64 overflow-y-auto">
            <LoadingSpinner v-if="loading" />
            <template v-else>
                <button v-for="customer in customers" :key="customer.id" type="button"
                    class="w-full flex items-center justify-between px-4 py-2.5 hover:bg-gray-50 transition-colors"
                    :class="selectedCustomer?.id === customer.id ? 'bg-blue-50' : ''" @click="select(customer)">
                    <div class="text-left">
                        <p class="text-sm font-medium text-gray-900">{{ customer.name }}</p>
                        <p class="text-xs text-gray-500">{{ customer.phone || '无电话' }}</p>
                    </div>
                    <CheckIcon v-if="selectedCustomer?.id === customer.id" class="w-4 h-4 text-blue-500" />
                </button>
                <EmptyState v-if="customers.length === 0" title="暂无客户" />
            </template>
        </div>
    </div>
</template>
