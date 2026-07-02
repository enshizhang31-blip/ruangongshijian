<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { snApi } from '@/api'
import {
    Card, Button, Space, Tag, Table, Modal, Input, Message, Tabs, TabPane, Empty
} from '@arco-design/web-vue'
import {
    QrCodeIcon, PlayIcon, ArrowPathIcon, TrashIcon
} from '@heroicons/vue/24/outline'

// ============ 扫码历史 ============
interface ScanRecord {
    sn: string
    action: string
    result: string
    time: string
    status?: number
    operator?: string
}
const scanHistory = ref<ScanRecord[]>([])

// ============ 操作描述映射 ============
const actionNameMap: Record<string, string> = {
    inbound: '入库',
    deliver: '发货',
    receive: '签收',
    return: '退货申请',
    'return-complete': '退货入库',
    void: '作废',
}

// ============ 状态映射 ============
const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '在库', color: 'orange' },
    1: { label: '已售', color: 'arcoblue' },
    2: { label: '已作废', color: 'gray' },
    3: { label: '退货中', color: 'purple' },
    4: { label: '已退货', color: 'red' },
}

// ============ 操作步骤提示映射 ============
const stepHints: Record<string, string[]> = {
    inbound: [
        '点击"摄像头扫码"或用扫码枪扫描SN码',
        '扫描后自动入库, 状态变为"在库"',
        '操作人自动记录为当前登录账号',
    ],
    deliver: [
        '扫描SN码, 状态变为"已发货"',
        '可选填物流单号',
        '当前状态需为"在库"或"锁定"',
    ],
    receive: [
        '客户收到货后扫描SN码签收',
        '状态从"已发货"变为"已签收"',
        '客户手中的SN码即被确认',
    ],
    return: [
        '客户发起退货时扫描SN码',
        '状态变为"退货中"',
        '填入退货原因便于追溯',
    ],
    'return-complete': [
        '仓库收到退回的SN码, 扫描入库',
        '状态从"退货中"变为"在库"',
        'SN码可重新销售',
    ],
    void: [
        'SN码损坏/无效时扫码作废',
        '状态变为"已作废"',
        '已售出的SN码需先走退货',
    ],
}

// ============ 当前操作 ============
const currentAction = ref<string>('inbound')
const trackingNo = ref<string>('')
const remark = ref<string>('')
const manualSn = ref<string>('')
const executing = ref(false)

function switchAction(action: string) {
    currentAction.value = action
    trackingNo.value = ''
    remark.value = ''
    manualSn.value = ''
}

async function executeScan(snCode?: string) {
    const code = (snCode || manualSn.value).trim()
    if (!code) {
        Message.warning('请输入或扫描 SN 码')
        return
    }
    executing.value = true
    try {
        const payload: any = { snCode: code, action: currentAction.value }
        if (trackingNo.value) payload.trackingNo = trackingNo.value
        if (remark.value) payload.remark = remark.value
        await snApi.scan(payload)
        const now = new Date().toLocaleString('zh-CN')
        const resultMsg = `${actionNameMap[currentAction.value]}成功`
        scanHistory.value.unshift({
            sn: code,
            action: actionNameMap[currentAction.value] || currentAction.value,
            result: resultMsg,
            time: now,
            status: 0,
            operator: '当前用户',
        })
        Message.success(`${code} ${resultMsg}`)
        manualSn.value = ''
        trackingNo.value = ''
        remark.value = ''
    } catch (e: any) {
        const now = new Date().toLocaleString('zh-CN')
        scanHistory.value.unshift({
            sn: code,
            action: actionNameMap[currentAction.value] || currentAction.value,
            result: e?.message || '操作失败',
            time: now,
            status: 2,
            operator: '当前用户',
        })
        Message.error(e?.message || '操作失败')
    } finally {
        executing.value = false
    }
}

function clearHistory() {
    Modal.confirm({
        title: '确认清空',
        content: '确定要清空当前会话的扫码历史吗？(此操作不影响后端记录)',
        onOk: () => {
            scanHistory.value = []
            Message.success('已清空')
        },
    })
}

// 历史表格列
const historyColumns = [
    { title: '时间', dataIndex: 'time', width: 170 },
    { title: 'SN 码', dataIndex: 'sn', width: 200 },
    { title: '操作', dataIndex: 'action', width: 100 },
    { title: '结果', slotName: 'resultCol' },
]
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="mb-6">
            <h1 class="text-xl lg:text-2xl font-bold text-gray-800">扫码管理</h1>
            <p class="text-sm text-gray-500 mt-1">扫码完成 SN 码的入库 / 发货 / 签收 / 退货 / 作废</p>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <!-- 左侧：操作选择 -->
            <div class="lg:col-span-1">
                <Card title="选择操作">
                    <Space direction="vertical" :size="8" class="w-full">
                        <Button long :type="currentAction === 'inbound' ? 'primary' : 'outline'"
                            @click="switchAction('inbound')">
                            📥 入库扫描
                        </Button>
                        <Button long :type="currentAction === 'deliver' ? 'primary' : 'outline'"
                            @click="switchAction('deliver')">
                            🚚 发货扫描
                        </Button>
                        <Button long :type="currentAction === 'receive' ? 'primary' : 'outline'"
                            @click="switchAction('receive')">
                            ✅ 签收扫描
                        </Button>
                        <Button long :type="currentAction === 'return' ? 'primary' : 'outline'"
                            @click="switchAction('return')">
                            ↩️ 退货申请
                        </Button>
                        <Button long :type="currentAction === 'return-complete' ? 'primary' : 'outline'"
                            @click="switchAction('return-complete')">
                            📦 退货入库
                        </Button>
                        <Button long :type="currentAction === 'void' ? 'primary' : 'outline'"
                            status="danger" @click="switchAction('void')">
                            🗑️ 作废扫描
                        </Button>
                    </Space>
                </Card>

                <!-- 操作提示 -->
                <Card class="mt-4" title="操作说明">
                    <ul class="text-sm text-gray-600 list-disc pl-5 space-y-1">
                        <li v-for="(hint, idx) in stepHints[currentAction] || []" :key="idx">
                            {{ hint }}
                        </li>
                    </ul>
                </Card>
            </div>

            <!-- 中间：扫码输入区 -->
            <div class="lg:col-span-2">
                <Card :title="`当前操作: ${actionNameMap[currentAction] || ''}`">
                    <Tabs default-active-key="manual">
                        <TabPane key="manual" title="手动输入 / 扫码枪">
                            <Space direction="vertical" :size="12" class="w-full">
                                <Input v-model="manualSn" placeholder="将光标定位到此处，使用扫码枪扫描，或手动输入 SN 码后回车"
                                    size="large" allow-clear @keyup.enter="executeScan()"
                                    :disabled="executing">
                                    <template #prefix>
                                        <QrCodeIcon class="w-5 h-5 text-gray-400" />
                                    </template>
                                </Input>
                                <Input v-model="trackingNo" placeholder="物流单号（可选，仅发货时使用）"
                                    allow-clear />
                                <Input v-model="remark" placeholder="备注 / 退货原因（可选）" allow-clear
                                    type="textarea" :rows="2" />
                                <Button type="primary" size="large" long :loading="executing"
                                    @click="executeScan()">
                                    <template #icon>
                                        <PlayIcon class="w-4 h-4" />
                                    </template>
                                    执行 {{ actionNameMap[currentAction] }}
                                </Button>
                            </Space>
                        </TabPane>
                        <TabPane key="camera" title="摄像头扫码">
                            <Empty description="摄像头扫码组件开发中，请使用扫码枪或手动输入" />
                        </TabPane>
                    </Tabs>
                </Card>

                <!-- 扫码历史 -->
                <Card class="mt-4" title="本次会话扫码历史">
                    <template #extra>
                        <Button v-if="scanHistory.length > 0" size="small" @click="clearHistory">
                            <template #icon>
                                <TrashIcon class="w-4 h-4" />
                            </template>
                            清空
                        </Button>
                    </template>
                    <Table :data="scanHistory" :columns="historyColumns" :pagination="false" size="small">
                        <template #resultCol="{ record }">
                            <Tag :color="record.status === 2 ? 'red' : 'green'">
                                {{ record.result }}
                            </Tag>
                        </template>
                    </Table>
                    <Empty v-if="scanHistory.length === 0" description="暂无扫码记录" />
                </Card>
            </div>
        </div>
    </div>
</template>