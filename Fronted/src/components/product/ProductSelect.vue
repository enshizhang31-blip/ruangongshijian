<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { productApi } from '@/api'
import type { Product } from '@/types'
import { CheckIcon } from '@heroicons/vue/24/outline'
import LoadingSpinner from '../common/LoadingSpinner.vue'
import EmptyState from '../common/EmptyState.vue'

interface Props {
    modelValue?: number
    placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
    placeholder: '选择商品',
})

const emit = defineEmits<{
    'update:modelValue': [value: number]
}>()

const loading = ref(false)
const showDropdown = ref(false)
const products = ref<Product[]>([])
const selectedProduct = ref<Product | null>(null)

async function fetchProducts() {
    loading.value = true
    try {
        const result = await productApi.list({ page: 1, pageSize: 100 }) as { list: Product[] }
        products.value = result.list.filter((p: Product) => p.status === 1)
    } finally {
        loading.value = false
    }
}

function select(product: Product) {
    selectedProduct.value = product
    emit('update:modelValue', product.id)
    showDropdown.value = false
}

onMounted(() => {
    fetchProducts()
})
</script>

<template>
    <div class="relative">
        <button type="button"
            class="w-full flex items-center justify-between px-4 py-2.5 border border-gray-300 rounded-xl bg-white hover:border-blue-400 transition-colors"
            @click="showDropdown = !showDropdown">
            <span :class="selectedProduct ? 'text-gray-900' : 'text-gray-400'">
                {{ selectedProduct?.name || placeholder }}
            </span>
            <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
        </button>

        <div v-if="showDropdown"
            class="absolute z-20 w-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg max-h-64 overflow-y-auto">
            <LoadingSpinner v-if="loading" />
            <template v-else>
                <button v-for="product in products" :key="product.id" type="button"
                    class="w-full flex items-center justify-between px-4 py-2.5 hover:bg-gray-50 transition-colors"
                    :class="selectedProduct?.id === product.id ? 'bg-blue-50' : ''" @click="select(product)">
                    <div class="text-left">
                        <p class="text-sm font-medium text-gray-900">{{ product.name }}</p>
                        <p class="text-xs text-gray-500">¥{{ product.price }} / {{ product.unit || '件' }}</p>
                    </div>
                    <CheckIcon v-if="selectedProduct?.id === product.id" class="w-4 h-4 text-blue-500" />
                </button>
                <EmptyState v-if="products.length === 0" title="暂无商品" />
            </template>
        </div>
    </div>
</template>
