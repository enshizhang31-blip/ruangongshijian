<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { snApi } from '@/api'
import { Modal, Select, Message, Descriptions } from '@arco-design/web-vue'

const emit = defineEmits<{
    (e: 'update:visible', v: boolean): void
    (e: 'saved'): void
}>()

const props = defineProps<{
    visible: boolean
    snId: number
    currentStatus: number
}>()

// SN 码的完整 9 种状态 (与后端 SnCodeStatusEnum 一致)
// 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货
const ALL_STATUS_OPTIONS = [
    { label: '在库', value: 0, desc: '商品已入库, 可销售', color: 'green' },
    { label: '锁定', value: 1, desc: '已下单锁定, 待支付', color: 'orange' },
    { label: '已售', value: 2, desc: '已支付, 等待发货', color: 'arcoblue' },
    { label: '已发货', value: 3, desc: '已发出, 运输中', color: 'blue' },
    { label: '已签收', value: 4, desc: '客户已签收', color: 'green' },
    { label: '已完成', value: 5, desc: '订单完成', color: 'gray' },
    { label: '已作废', value: 6, desc: 'SN 码已作废, 不可用', color: 'red' },
    { label: '退货中', value: 7, desc: '客户退货处理中', color: 'orange' },
    { label: '已退货', value: 8, desc: '已退货入库', color: 'purple' },
]

/**
 * SN 码状态流转规则 (与后端 SnCodeServiceImpl 保持一致)
 *
 * 正向流程: 0(在库) → 1(锁定) → 2(已售) → 3(已发货) → 4(已签收) → 5(已完成)
 * 逆向/分支:
 *   - 任意非销售链 → 6(已作废) (除 2/3/4 状态需走退货)
 *   - 2/3/4 → 7(退货中) → 8(已退货) → 0(在库)
 *   - 0(在库) ↔ 1(锁定) 可互转 (订单创建/取消)
 *
 * 注: 以下手动调整场景由前端控制 + 后端校验:
 *   - 0 → 6: 管理员强制作废在库 SN 码
 *   - 1 → 0: 订单取消, 解锁回在库
 *   - 7 → 0: 仓库确认退货入库
 *   - 8 → 0: 重新入库
 *   - 其他正向流转走扫码接口, 此处不允许手动改
 */
const TRANSITION_RULES: Record<number, number[]> = {
    0: [1, 6],          // 在库 → 锁定(下单) / 已作废
    1: [0, 2, 6],       // 锁定 → 在库(取消) / 已售(支付) / 已作废
    2: [7],             // 已售 → 退货中 (发货请扫码, 此处禁止)
    3: [7],             // 已发货 → 退货中 (签收请扫码)
    4: [7],             // 已签收 → 退货中 (完成请扫码)
    5: [],              // 已完成: 终态, 不可手动调整
    6: [],              // 已作废: 终态, 不可调整
    7: [0, 8],          // 退货中 → 在库(确认收货) / 已退货(走退货入库流程)
    8: [0, 6],          // 已退货 → 在库(重新入库) / 已作废
}

const editStatus = ref<string>('')
const currentStatusInfo = ref<any>(null)

// 根据当前状态计算允许的目标状态列表
const allowedTargets = computed(() => {
    const allowed = TRANSITION_RULES[props.currentStatus] || []
    return ALL_STATUS_OPTIONS.filter(s => allowed.includes(s.value))
})

// 是否有可流转的目标状态
const hasAllowedTargets = computed(() => allowedTargets.value.length > 0)

watch(() => props.visible, (v) => {
    if (v) {
        currentStatusInfo.value = ALL_STATUS_OPTIONS.find(s => s.value === props.currentStatus)
        // 默认选中第一个允许的目标状态
        if (hasAllowedTargets.value) {
            editStatus.value = String(allowedTargets.value[0].value)
        } else {
            editStatus.value = ''
        }
    }
})

async function handleSave() {
    if (!editStatus.value) {
        Message.warning('当前状态无允许的流转目标, 请使用扫码功能')
        return false
    }
    const newStatus = Number(editStatus.value)
    if (newStatus === props.currentStatus) {
        Message.warning('目标状态与当前状态相同')
        return false
    }
    // 前端二次校验流转规则
    const allowed = TRANSITION_RULES[props.currentStatus] || []
    if (!allowed.includes(newStatus)) {
        Message.error(`不允许从「${currentStatusInfo.value?.label}」直接变更为「${ALL_STATUS_OPTIONS.find(s => s.value === newStatus)?.label}」, 请使用扫码功能`)
        return false
    }
    try {
        await snApi.updateStatus(props.snId, newStatus)
        Message.success('状态更新成功')
        emit('update:visible', false)
        emit('saved')
        return true
    } catch (e: any) {
        Message.error(e?.response?.data?.message || '操作失败')
        return false
    }
}
</script>

<template>
    <Modal :visible="visible" title="编辑 SN 码状态" :on-before-ok="handleSave" :width="520"
        @update:visible="$emit('update:visible', $event)">
        <div class="flex flex-col gap-4">
            <div class="bg-gray-50 rounded-lg p-3">
                <Descriptions :column="1" size="small" bordered>
                    <Descriptions.Item label="SN 码 ID">{{ snId }}</Descriptions.Item>
                    <Descriptions.Item v-if="currentStatusInfo" label="当前状态">
                        <Tag :color="currentStatusInfo.color" size="medium">
                            {{ currentStatusInfo.label }} (状态码: {{ props.currentStatus }})
                        </Tag>
                    </Descriptions.Item>
                </Descriptions>
            </div>

            <Alert type="warning" :show-icon="true" v-if="!hasAllowedTargets">
                当前状态为「<b>{{ currentStatusInfo?.label }}</b>」, 属于终态, 无法手动调整。
                <br />终态 SN 码只能通过数据库直接修改, 请联系系统管理员。
            </Alert>

            <Alert type="info" :show-icon="true" v-else>
                <div class="text-sm">
                    根据 SN 码状态流转规则, 当前状态「<b>{{ currentStatusInfo?.label }}</b>」<b>仅允许</b>变更为以下状态之一:
                </div>
                <div class="flex flex-wrap gap-2 mt-2">
                    <Tag v-for="s in allowedTargets" :key="s.value" :color="s.color">{{ s.label }}</Tag>
                </div>
            </Alert>

            <div v-if="hasAllowedTargets">
                <div class="text-sm text-gray-600 mb-1">修改状态为</div>
                <Select v-model="editStatus" class="w-full" placeholder="选择目标状态">
                    <Select.Option v-for="s in allowedTargets" :key="s.value" :value="String(s.value)">
                        <div class="flex items-center justify-between">
                            <Tag :color="s.color" size="small">{{ s.label }}</Tag>
                            <span class="text-xs text-gray-400 ml-2">{{ s.desc }}</span>
                        </div>
                    </Select.Option>
                </Select>
            </div>

            <Alert type="warning" :show-icon="false">
                <div class="text-xs">
                    ⚠️ <b>正常业务流转请使用扫码功能</b>:
                    <ul class="ml-4 mt-1 space-y-0.5">
                        <li>• <b>入库/发货/签收</b>: 在「扫码管理」页面扫描 SN 码二维码</li>
                        <li>• <b>退货</b>: 扫描触发退货中状态, 仓库再扫描完成退货入库</li>
                        <li>• <b>作废</b>: 仅在库/锁定状态的 SN 码可手动作废</li>
                    </ul>
                    <div class="mt-1.5">本弹窗的<b>手动修改</b>仅用于异常状态修复 (如: 订单取消解锁、强制作废、退货入库等), 系统会严格校验流转规则。</div>
                </div>
            </Alert>
        </div>
    </Modal>
</template>