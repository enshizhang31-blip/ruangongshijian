<script setup lang="ts">
import { ref, computed } from 'vue'
import { snApi, productApi } from '@/api'
import { Modal, Select, Message, InputNumber } from '@arco-design/web-vue'
import type { Sku } from '@/types'

const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void
    (e: 'saved'): void
}>()

const props = defineProps<{
    visible: boolean
}>()
// eslint-disable-next-line @typescript-eslint/no-unused-vars
void props

// ============ 数据加载 ============
interface SkuWithSpu extends Sku {
    spuName?: string
    spuImage?: string
}

const skuList = ref<SkuWithSpu[]>([])
const loadingSkus = ref(false)
const selectedSkuId = ref<string>()
const selectedSku = computed<SkuWithSpu | undefined>(() =>
    skuList.value.find((s) => String(s.id) === selectedSkuId.value)
)
const generateCount = ref<number>(1)

async function loadSkus() {
    loadingSkus.value = true
    try {
        const goodsRes: any = await productApi.list({ pageSize: 200 })
        const allSkus: SkuWithSpu[] = []
        for (const g of goodsRes.list || []) {
            try {
                const skus: Sku[] = await productApi.getSkus(g.id)
                for (const sku of skus) {
                    allSkus.push({
                        ...sku,
                        spuName: g.name || g.spuName || '',
                        spuImage: g.imageUrl || g.mainImage || '',
                    })
                }
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

// 解析 specJson 为可读字符串
function parseSpec(specJson?: string): string {
    if (!specJson) return ''
    try {
        const obj = JSON.parse(specJson)
        if (typeof obj === 'object' && obj) {
            return Object.entries(obj).map(([k, v]) => `${k}:${v}`).join(' / ')
        }
        return String(obj)
    } catch {
        return specJson
    }
}

// 选中后展示信息
const previewInfo = computed(() => {
    if (!selectedSku.value) return null
    const sku = selectedSku.value
    return {
        spuName: sku.spuName || '（未命名商品）',
        skuCode: sku.skuCode,
        spec: parseSpec(sku.specJson),
        price: sku.price,
        image: sku.imageUrl || sku.spuImage,
    }
})

// 预览要生成的 SN 码（格式：SKU编码-序号），仅供前端预览
const previewSnCodes = computed<string[]>(() => {
    if (!selectedSku.value) return []
    const code = selectedSku.value.skuCode || 'SN'
    const n = Math.max(1, Math.min(100, generateCount.value || 1))
    const list: string[] = []
    for (let i = 1; i <= Math.min(n, 5); i++) {
        list.push(`${code}-${String(i).padStart(4, '0')}`)
    }
    return list
})

async function handleSave() {
    if (!selectedSkuId.value) {
        Message.warning('请选择商品 SKU')
        return false
    }
    if (!generateCount.value || generateCount.value < 1) {
        Message.warning('请输入正确的生成数量')
        return false
    }
    try {
        const skuId = Number(selectedSkuId.value)
        const result = await snApi.generate(skuId, generateCount.value)
        Message.success(
            `【${previewInfo.value?.spuName}】生成成功，共 ${result.length} 个 SN 码`
        )
        emit('update:visible', false)
        emit('saved')
        return true
    } catch { Message.error('生成失败'); return false }
}
</script>

<template>
    <Modal :visible="visible" title="新增SN码" :on-before-ok="handleSave" :width="560"
        @update:visible="$emit('update:visible', $event)" @open="reset">
        <div class="flex flex-col gap-4">
            <!-- SKU 选择 -->
            <div>
                <div class="text-sm text-gray-600 mb-1">
                    选择商品 / SKU <span class="text-red-500">*</span>
                </div>
                <Select v-model="selectedSkuId" placeholder="搜索商品名称或 SKU 编码" class="w-full"
                    :loading="loadingSkus" filterable show-search>
                    <Select.Option v-for="s in skuList" :key="s.id" :value="String(s.id)">
                        <div class="flex items-center gap-2">
                            <img v-if="s.imageUrl || s.spuImage"
                                :src="s.imageUrl || s.spuImage"
                                class="w-8 h-8 rounded object-cover border border-gray-200 flex-shrink-0"
                                @error="(e: any) => (e.target.style.display = 'none')" />
                            <div class="flex flex-col flex-1 min-w-0">
                                <div class="font-medium text-gray-800 truncate">
                                    {{ s.spuName || '（未命名商品）' }}
                                </div>
                                <div class="text-xs text-gray-500 truncate">
                                    {{ s.skuCode }}
                                    <span v-if="s.specJson" class="ml-1 text-gray-400">
                                        · {{ parseSpec(s.specJson) }}
                                    </span>
                                    <span class="ml-1 text-orange-500">
                                        ¥{{ Number(s.price || 0).toFixed(2) }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </Select.Option>
                </Select>
                <div class="text-xs text-gray-400 mt-1">
                    支持输入商品名称或 SKU 编码搜索（如「小米」或「SKU-001」）
                </div>
            </div>

            <!-- 选中商品详情卡片 -->
            <div v-if="previewInfo" class="flex items-center gap-3 p-3 bg-blue-50 rounded-lg border border-blue-100">
                <img v-if="previewInfo.image"
                    :src="previewInfo.image"
                    class="w-12 h-12 rounded object-cover border border-gray-200 flex-shrink-0"
                    @error="(e: any) => (e.target.style.display = 'none')" />
                <div v-else
                    class="w-12 h-12 rounded bg-gray-200 flex items-center justify-center text-gray-400 text-xs flex-shrink-0">
                    无图
                </div>
                <div class="flex-1 min-w-0">
                    <div class="font-semibold text-gray-800 truncate">
                        {{ previewInfo.spuName }}
                    </div>
                    <div class="text-xs text-gray-500 truncate">
                        <span class="font-mono">{{ previewInfo.skuCode }}</span>
                        <span v-if="previewInfo.spec" class="ml-2">{{ previewInfo.spec }}</span>
                    </div>
                </div>
                <div class="text-orange-500 font-bold">
                    ¥{{ Number(previewInfo.price || 0).toFixed(2) }}
                </div>
            </div>

            <!-- 生成数量 -->
            <div>
                <div class="text-sm text-gray-600 mb-1">生成数量</div>
                <InputNumber v-model="generateCount" :min="1" :max="100" class="w-full"
                    placeholder="请输入 1-100" />
                <div class="text-xs text-gray-400 mt-1">
                    一次最多生成 100 个 SN 码
                </div>
            </div>

            <!-- SN 码预览 -->
            <div v-if="selectedSkuId" class="text-xs text-gray-500 bg-gray-50 rounded-lg p-3 border border-gray-100">
                <div class="font-medium text-gray-700 mb-2">
                    将为【{{ previewInfo?.spuName }}】生成以下 SN 码预览：
                </div>
                <div class="flex flex-wrap gap-1.5">
                    <code v-for="sn in previewSnCodes" :key="sn"
                        class="px-2 py-1 bg-white border border-gray-200 rounded text-gray-700 font-mono">
                        {{ sn }}
                    </code>
                    <span v-if="generateCount > 5" class="px-2 py-1 text-gray-500">
                        ... 共 {{ generateCount }} 个
                    </span>
                </div>
                <div class="mt-2 text-gray-400">
                    * 实际 SN 码由后端按规则生成，格式可能略有差异
                </div>
            </div>
        </div>
    </Modal>
</template>