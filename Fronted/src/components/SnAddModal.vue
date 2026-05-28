<script setup lang="ts">
import { ref } from 'vue'
import { snApi, productApi } from '@/api'
import { Modal, Select, Input, Message } from '@arco-design/web-vue'
import type { Sku } from '@/types'

const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void
    (e: 'saved'): void
}>()

const props = defineProps<{
    visible: boolean
}>()

const skuList = ref<Sku[]>([])
const loadingSkus = ref(false)
const selectedSkuId = ref<number>()
const generateCount = ref(1)

async function loadSkus() {
    loadingSkus.value = true
    try {
        const goodsRes: any = await productApi.list({ pageSize: 100 })
        const allSkus: Sku[] = []
        for (const g of goodsRes.list || []) {
            try {
                const skus = await productApi.getSkus(g.id)
                allSkus.push(...skus)
            } catch { /* ignore */ }
        }
        skuList.value = allSkus
    } catch { /* silent */ }
    finally { loadingSkus.value = false }
}

function reset() {
    selectedSkuId.value = undefined
    generateCount.value = 1
    loadSkus()
}

async function handleSave() {
    if (!selectedSkuId.value) {
        Message.warning('请选择SKU')
        return false
    }
    try {
        const result = await snApi.generate(selectedSkuId.value, generateCount.value || 1)
        Message.success(`生成成功，共 ${result.length} 个`)
        emit('update:visible', false)
        emit('saved')
        return true
    } catch { Message.error('生成失败'); return false }
}
</script>

<template>
    <Modal :visible="visible" title="新增SN码" :on-before-ok="handleSave" :width="480"
        @update:visible="$emit('update:visible', $event)" @open="reset">
        <div class="flex flex-col gap-4">
            <div>
                <div class="text-sm text-gray-600 mb-1">SKU *</div>
                <Select v-model="selectedSkuId" placeholder="选择SKU" class="w-full" :loading="loadingSkus" filterable>
                    <Select.Option v-for="s in skuList" :key="s.id" :value="s.id">
                        {{ s.skuCode }} - ¥{{ s.price }}
                    </Select.Option>
                </Select>
            </div>
            <div>
                <div class="text-sm text-gray-600 mb-1">生成数量</div>
                <Input v-model.number="generateCount" type="number" :min="1" :max="100" class="w-full" />
            </div>
            <div class="text-xs text-gray-400 bg-gray-50 rounded-lg p-3">
                确认后系统将根据SKU编码自动生成一个SN码（格式：SKU编码-序号）
            </div>
        </div>
    </Modal>
</template>
