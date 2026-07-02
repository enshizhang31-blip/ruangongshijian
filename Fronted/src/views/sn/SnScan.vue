<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { snApi } from '@/api'
import { useAppStore } from '@/stores/app'
import { authApi } from '@/api/auth'
import {
    Card, Button, Input, Space, Tag, Message, Modal,
    Empty, Descriptions, DescriptionsItem, Alert, Tabs, TabPane, Switch
} from '@arco-design/web-vue'
import {
    QrCodeIcon, BoltIcon, TruckIcon, CheckCircleIcon,
    ArrowUturnLeftIcon, XCircleIcon, PlusIcon, ArrowPathIcon,
    CameraIcon, StopIcon, IdentificationIcon
} from '@heroicons/vue/24/outline'
import { BrowserMultiFormatReader, IScannerControls } from '@zxing/browser'
import { DecodeHintType, BarcodeFormat } from '@zxing/library'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 真实登录用户 (从 Pinia store 取)
const currentUser = computed(() => {
    const u = appStore.userInfo
    if (u && u.userId) {
        return { id: u.userId, name: u.realName || u.username || '未登录', username: u.username }
    }
    return { id: 0, name: '未登录', username: '' }
})

onMounted(async () => {
    if (!currentUser.value.id) {
        try {
            const u: any = await authApi.getCurrentUser()
            if (u?.userId) {
                appStore.setUser({ userId: u.userId, username: u.username, realName: u.realName })
            } else { Message.warning('请先登录'); router.push('/login'); return }
        } catch { Message.warning('请先登录'); router.push('/login'); return }
    }
    setTimeout(() => inputRef.value?.focus?.(), 300)
})

// 6 种扫码模式
const activeTab = ref<string>('inbound')
const tabOptions = [
    { value: 'inbound', label: '扫码入库', icon: PlusIcon, color: '#0fc6c2' },
    { value: 'deliver', label: '扫码发货', icon: TruckIcon, color: '#165dff' },
    { value: 'receive', label: '扫码签收', icon: CheckCircleIcon, color: '#00b42a' },
    { value: 'return', label: '扫码退货', icon: ArrowUturnLeftIcon, color: '#ff7d00' },
    { value: 'return-complete', label: '退货入库', icon: BoltIcon, color: '#722ed1' },
    { value: 'void', label: '扫码作废', icon: XCircleIcon, color: '#86909c' },
]

const lastScanResult = ref<any>(null)
const lastError = ref<string>('')
const scanInput = ref('')
const inputRef = ref<any>(null)
const logisticsNo = ref('')
const returnReason = ref('')
const voidReason = ref('')

// 摄像头扫码
const showCameraModal = ref(false)
const cameraEnabled = ref(false)
const cameraLoading = ref(false)
const cameraError = ref('')
const videoRef = ref<HTMLVideoElement | null>(null)
const cameraScanner = ref<BrowserMultiFormatReader | null>(null)
const cameraControls = ref<IScannerControls | null>(null)
const lastCameraScan = ref('')
const cameraScanCount = ref(0)
const videoDevices = ref<MediaDeviceInfo[]>([])
const selectedDeviceId = ref<string>('')
const enableContinuous = ref(true)

async function getCameras() {
    try {
        const devices = await BrowserMultiFormatReader.listVideoInputDevices()
        videoDevices.value = devices
        if (devices.length > 0 && !selectedDeviceId.value) {
            const back = devices.find(d => /back|rear|environment/i.test(d.label))
            selectedDeviceId.value = back?.deviceId || devices[0].deviceId
        }
    } catch (e: any) { cameraError.value = '获取摄像头列表失败: ' + (e?.message || e) }
}

async function startCamera() {
    cameraError.value = ''
    if (!videoRef.value) return
    cameraLoading.value = true
    try {
        await getCameras()
        if (videoDevices.value.length === 0) throw new Error('未检测到摄像头设备')
        const hints = new Map()
        const formats: BarcodeFormat[] = [
            BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
            BarcodeFormat.CODABAR, BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
            BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX, BarcodeFormat.ITF,
        ]
        hints.set(DecodeHintType.POSSIBLE_FORMATS, formats)
        hints.set(DecodeHintType.TRY_HARDER, true)
        hints.set(DecodeHintType.CHARACTER_SET, 'UTF-8')
        cameraScanner.value = new BrowserMultiFormatReader(hints, {
            delayBetweenScanAttempts: 100,
            delayBetweenImageRestorations: 50
        })
        cameraControls.value = await cameraScanner.value.decodeFromVideoDevice(
            selectedDeviceId.value, videoRef.value,
            (result, err, controls) => {
                if (result) {
                    const text = result.getText()
                    console.log('[扫码识别成功]', { text })
                    handleCameraScan(text)
                }
            }
        )
        cameraEnabled.value = true
        Message.success('摄像头已启动, 请将条码对准扫码框')
    } catch (e: any) {
        cameraError.value = '启动摄像头失败: ' + (e?.message || e)
        Message.error(cameraError.value)
    } finally { cameraLoading.value = false }
}

function stopCamera() {
    try {
        cameraControls.value?.stop()
        cameraControls.value = null
        cameraScanner.value = null
        cameraEnabled.value = false
        if (videoRef.value?.srcObject) {
            const stream = videoRef.value.srcObject as MediaStream
            stream.getTracks().forEach(t => t.stop())
            videoRef.value.srcObject = null
        }
    } catch (e) { console.error('停止摄像头失败', e) }
}

async function handleCameraScan(text: string) {
    if (!text || text === lastCameraScan.value) return
    cameraScanCount.value++
    if (enableContinuous.value) {
        scanInput.value = text
        lastCameraScan.value = text
        await new Promise(r => setTimeout(r, 200))
        await executeScan(text)
        setTimeout(() => { lastCameraScan.value = '' }, 800)
    } else {
        scanInput.value = text
        lastCameraScan.value = text
        stopCamera(); showCameraModal.value = false
        Message.success('已识别SN码, 请确认操作')
    }
}

async function switchCamera() {
    if (cameraEnabled.value) { stopCamera(); await new Promise(r => setTimeout(r, 200)); await startCamera() }
}

async function openCameraScan() {
    showCameraModal.value = true; await nextTick(); await startCamera()
}
function closeCameraModal() { stopCamera(); showCameraModal.value = false }
onUnmounted(() => { stopCamera() })

// 测试二维码生成
const showQrTest = ref(false)
const qrTestContent = ref('')
const qrTestCanvas = ref<HTMLCanvasElement | null>(null)
async function generateQrTest() {
    if (!qrTestContent.value.trim() || !qrTestCanvas.value) return
    try {
        const QRCode = (await import('qrcode')).default
        await QRCode.toCanvas(qrTestCanvas.value, qrTestContent.value.trim(), {
            width: 240, margin: 2, errorCorrectionLevel: 'M',
            color: { dark: '#000000', light: '#ffffff' }
        })
        Message.success('二维码已生成, 可用摄像头对准扫描测试')
    } catch (e: any) { Message.error('二维码生成失败: ' + (e?.message || e)) }
}

// 扫码历史
const scanHistory = ref<Array<{ sn: string; action: string; result: string; time: string; status?: number; operator?: string }>>([])
const actionNameMap: Record<string, string> = {
    inbound: '入库', deliver: '发货', receive: '签收',
    return: '退货', 'return-complete': '退货入库', void: '作废',
}

async function executeScan(sn: string) {
    if (!sn.trim()) { Message.warning('请输入或扫描SN码'); return }
    if (!currentUser.value.id) { Message.error('操作人未登录, 无法执行扫码'); return }
    lastError.value = ''
    lastScanResult.value = null
    const action = activeTab.value
    const operator = currentUser.value.name
    try {
        let res: any
        const baseData = { sn: sn.trim(), userId: currentUser.value.id, userName: operator }
        switch (action) {
            case 'inbound': res = await snApi.scanInbound(baseData); break
            case 'deliver': res = await snApi.scanDeliver({ ...baseData, logisticsNo: logisticsNo.value || undefined }); break
            case 'receive': res = await snApi.scanReceive(baseData); break
            case 'return': res = await snApi.scanReturn({ sn: sn.trim(), reason: returnReason.value || '扫码退货' }); break
            case 'return-complete': res = await snApi.scanReturnComplete(baseData); break
            case 'void': res = await snApi.scanVoid({ sn: sn.trim(), reason: voidReason.value || '扫码作废' }); break
        }
        lastScanResult.value = res
        Message.success(`扫码${actionNameMap[action]}成功: ${sn} (操作人: ${operator})`)
        scanHistory.value.unshift({
            sn: sn.trim(), action: actionNameMap[action], result: '成功',
            time: new Date().toLocaleTimeString(), status: res?.status, operator: operator,
        })
        if (scanHistory.value.length > 50) scanHistory.value = scanHistory.value.slice(0, 50)
        scanInput.value = ''
        if (action === 'deliver') logisticsNo.value = ''
        setTimeout(() => inputRef.value?.focus?.(), 100)
    } catch (e: any) {
        const errMsg = e?.response?.data?.message || e?.message || '扫码失败'
        lastError.value = errMsg
        Message.error(errMsg)
        scanHistory.value.unshift({
            sn: sn.trim(), action: actionNameMap[action], result: '失败: ' + errMsg,
            time: new Date().toLocaleTimeString(), operator: operator,
        })
    }
}

function handleEnter() { if (scanInput.value.trim()) executeScan(scanInput.value) }

const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '在库', color: 'green' }, 1: { label: '锁定', color: 'orange' },
    2: { label: '已售', color: 'arcoblue' }, 3: { label: '已发货', color: 'blue' },
    4: { label: '已签收', color: 'green' }, 5: { label: '已完成', color: 'gray' },
    6: { label: '已作废', color: 'red' }, 7: { label: '退货中', color: 'orange' },
    8: { label: '已退货', color: 'purple' },
}

const stepHints: Record<string, string[]> = {
    inbound: ['点击"摄像头扫码"或用扫码枪扫描SN码', '扫描后自动入库, 状态变为"在库"', '操作人自动记录为当前登录账号'],
    deliver: ['扫描SN码, 状态变为"已发货"', '可选填物流单号', '当前状态需为"在库"或"锁定"'],
    receive: ['客户收到货后扫描SN码签收', '状态从"已发货"变为"已签收"', '客户手中的SN码即被确认'],
    return: ['客户发起退货时扫描SN码', '状态变为"退货中"', '填入退货原因便于追溯'],
    'return-complete': ['仓库收到退回的SN码, 扫描入库', '状态从"退货中"变为"在库"', 'SN码可重新销售'],
    void: ['SN码损坏/无效时扫码作废', '状态变为"已作废"', '已售出的SN码需先走退货'],
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800 flex items-center gap-2">
                    <QrCodeIcon class="w-6 h-6 text-blue-600" />
                    扫码管理 SN 码
                </h1>
                <p class="text-sm text-gray-500 mt-1">
                    通过摄像头扫码或扫码枪完成 SN 码入库 / 发货 / 签收 / 退货 / 作废 等全流程
                </p>
            </div>
            <Space>
                <Card class="!p-2">
                    <Space>
                        <IdentificationIcon class="w-4 h-4 text-blue-600" />
                        <span class="text-sm text-gray-600">操作人:</span>
                        <Tag color="arcoblue" size="medium">{{ currentUser.name }}</Tag>
                    </Space>
                </Card>
            </Space>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <div class="lg:col-span-2 space-y-4">
                <Card>
                    <Tabs v-model:active-key="activeTab" type="rounded" size="large">
                        <TabPane v-for="t in tabOptions" :key="t.value" :title="t.label">
                            <div class="py-2">
                                <Alert type="info" :show-icon="false">
                                    <ul class="text-sm space-y-1">
                                        <li v-for="(h, i) in stepHints[t.value]" :key="i">• {{ h }}</li>
                                    </ul>
                                </Alert>
                            </div>
                        </TabPane>
                    </Tabs>
                </Card>

                <Card>
                    <div class="flex flex-col gap-4">
                        <div class="flex items-center justify-between gap-2">
                            <div class="flex items-center gap-2 text-base font-medium text-gray-700">
                                <component :is="tabOptions.find(t => t.value === activeTab)?.icon" class="w-5 h-5" />
                                <span>扫码{{ tabOptions.find(t => t.value === activeTab)?.label }}</span>
                            </div>
                            <Space>
                                <Button type="primary" status="success" @click="openCameraScan">
                                    <template #icon><CameraIcon class="w-4 h-4" /></template>
                                    摄像头扫码
                                </Button>
                                <Button @click="showQrTest = true">
                                    <template #icon><QrCodeIcon class="w-4 h-4" /></template>
                                    生成测试二维码
                                </Button>
                            </Space>
                        </div>

                        <Input ref="inputRef" v-model="scanInput"
                            placeholder="请用扫码枪扫描, 或手动输入SN码后回车"
                            size="large" allow-clear @press-enter="handleEnter">
                            <template #prefix><QrCodeIcon class="w-4 h-4 text-gray-400" /></template>
                        </Input>

                        <Input v-if="activeTab === 'deliver'" v-model="logisticsNo"
                            placeholder="物流单号 (可选)" size="large" allow-clear>
                            <template #prefix><TruckIcon class="w-4 h-4 text-gray-400" /></template>
                        </Input>

                        <Input v-if="activeTab === 'return'" v-model="returnReason"
                            placeholder="退货原因" size="large" allow-clear />

                        <Input v-if="activeTab === 'void'" v-model="voidReason"
                            placeholder="作废原因" size="large" allow-clear />

                        <Button type="primary" size="large" long @click="handleEnter">
                            <template #icon><BoltIcon class="w-5 h-5" /></template>
                            确认{{ tabOptions.find(t => t.value === activeTab)?.label }}
                            (操作人: {{ currentUser.name }})
                        </Button>
                    </div>
                </Card>

                <Card v-if="lastScanResult || lastError">
                    <div v-if="lastError" class="text-red-600">
                        <Alert type="error">{{ lastError }}</Alert>
                    </div>
                    <div v-else-if="lastScanResult">
                        <div class="flex items-center gap-2 mb-4">
                            <CheckCircleIcon class="w-6 h-6 text-green-500" />
                            <span class="text-lg font-medium text-green-700">扫码成功</span>
                            <Tag :color="statusMap[lastScanResult.status]?.color">
                                {{ statusMap[lastScanResult.status]?.label || '未知' }}
                            </Tag>
                        </div>
                        <Descriptions :data="lastScanResult" :column="2" size="small" bordered>
                            <DescriptionsItem label="SN码">{{ lastScanResult.snCode }}</DescriptionsItem>
                            <DescriptionsItem label="状态">
                                <Tag :color="statusMap[lastScanResult.status]?.color">
                                    {{ statusMap[lastScanResult.status]?.label }}
                                </Tag>
                            </DescriptionsItem>
                            <DescriptionsItem label="商品名称">{{ lastScanResult.spuName || '-' }}</DescriptionsItem>
                            <DescriptionsItem label="SKU编码">{{ lastScanResult.skuCode || '-' }}</DescriptionsItem>
                            <DescriptionsItem label="操作人">{{ lastScanResult.inboundUserName || currentUser.name }}</DescriptionsItem>
                            <DescriptionsItem label="当前持有者">{{ lastScanResult.currentHolder || '-' }}</DescriptionsItem>
                            <DescriptionsItem label="当前位置">{{ lastScanResult.currentLocation || '-' }}</DescriptionsItem>
                            <DescriptionsItem v-if="lastScanResult.inboundAt" label="入库时间">{{ lastScanResult.inboundAt }}</DescriptionsItem>
                            <DescriptionsItem v-if="lastScanResult.deliveredAt" label="发货时间">{{ lastScanResult.deliveredAt }}</DescriptionsItem>
                            <DescriptionsItem v-if="lastScanResult.receivedAt" label="签收时间">{{ lastScanResult.receivedAt }}</DescriptionsItem>
                            <DescriptionsItem v-if="lastScanResult.logisticsNo" label="物流单号">{{ lastScanResult.logisticsNo }}</DescriptionsItem>
                        </Descriptions>
                    </div>
                </Card>
            </div>

            <div>
                <Card title="扫码历史">
                    <template #extra>
                        <Button type="text" size="mini" @click="scanHistory = []" :disabled="scanHistory.length === 0">
                            <template #icon><ArrowPathIcon class="w-4 h-4" /></template>
                            清空
                        </Button>
                    </template>
                    <Empty v-if="scanHistory.length === 0" description="暂无扫码记录" />
                    <div v-else class="space-y-2 max-h-[600px] overflow-y-auto">
                        <div v-for="(h, idx) in scanHistory" :key="idx"
                            class="p-3 rounded border text-sm"
                            :class="h.result.startsWith('成功') ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'">
                            <div class="flex items-center justify-between mb-1">
                                <Tag :color="h.result.startsWith('成功') ? 'green' : 'red'" size="small">{{ h.action }}</Tag>
                                <span class="text-xs text-gray-500">{{ h.time }}</span>
                            </div>
                            <div class="font-mono text-gray-700 text-xs break-all">{{ h.sn }}</div>
                            <div v-if="h.operator" class="text-xs text-gray-500 mt-1">操作人: {{ h.operator }}</div>
                            <div class="text-xs mt-1" :class="h.result.startsWith('成功') ? 'text-green-700' : 'text-red-700'">
                                {{ h.result }}
                            </div>
                        </div>
                    </div>
                </Card>
            </div>
        </div>
    </div>

    <Modal v-model:visible="showCameraModal" title="摄像头扫码" :footer="false"
        :width="640" :mask-closable="false" :closable="true"
        @cancel="closeCameraModal" @before-close="closeCameraModal">
        <div class="flex flex-col gap-3">
            <div class="relative bg-black rounded-lg overflow-hidden" style="aspect-ratio: 4/3;">
                <video ref="videoRef" class="w-full h-full object-cover"
                    style="transform: scaleX(-1);" autoplay muted playsinline></video>
                <div v-if="cameraEnabled" class="absolute inset-0 pointer-events-none flex items-center justify-center">
                    <div class="relative w-3/5 h-1/2 border-2 border-green-400 rounded-lg">
                        <div class="absolute top-0 left-0 w-6 h-6 border-t-4 border-l-4 border-green-500 -mt-1 -ml-1"></div>
                        <div class="absolute top-0 right-0 w-6 h-6 border-t-4 border-r-4 border-green-500 -mt-1 -mr-1"></div>
                        <div class="absolute bottom-0 left-0 w-6 h-6 border-b-4 border-l-4 border-green-500 -mb-1 -ml-1"></div>
                        <div class="absolute bottom-0 right-0 w-6 h-6 border-b-4 border-r-4 border-green-500 -mb-1 -mr-1"></div>
                        <div class="absolute inset-x-0 top-1/2 h-0.5 bg-green-400 animate-pulse"></div>
                    </div>
                </div>
                <div v-if="cameraLoading" class="absolute inset-0 bg-black bg-opacity-70 flex items-center justify-center">
                    <span class="text-white">正在启动摄像头...</span>
                </div>
                <div v-if="cameraError" class="absolute inset-0 bg-black bg-opacity-70 flex items-center justify-center p-4">
                    <Alert type="error" :show-icon="true" class="w-full">{{ cameraError }}</Alert>
                </div>
            </div>

            <div class="flex flex-wrap items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <Space>
                    <span class="text-sm text-gray-600">摄像头:</span>
                    <select v-if="videoDevices.length > 1" v-model="selectedDeviceId" @change="switchCamera"
                        class="px-2 py-1 border rounded text-sm">
                        <option v-for="d in videoDevices" :key="d.deviceId" :value="d.deviceId">
                            {{ d.label || `摄像头 ${d.deviceId.slice(0, 8)}` }}
                        </option>
                    </select>
                    <span v-else-if="videoDevices.length === 1" class="text-sm">
                        {{ videoDevices[0].label || '默认摄像头' }}
                    </span>
                </Space>
                <Space>
                    <span class="text-sm text-gray-600">连续扫码:</span>
                    <Switch v-model="enableContinuous" />
                </Space>
                <div class="flex-1"></div>
                <Space><Tag color="arcoblue">已扫描: {{ cameraScanCount }} 次</Tag></Space>
            </div>

            <Alert type="info" :show-icon="false">
                <ul class="text-xs space-y-1">
                    <li>• 请将SN码条码对准扫码框, 距离 10-30cm</li>
                    <li>• 支持格式: CODE128 / CODE39 / EAN13 / QR码 等</li>
                    <li>• 连续扫码模式: 扫描后自动执行, 无需手动确认</li>
                    <li>• 单次模式: 扫描后填入输入框, 需手动点击确认</li>
                </ul>
            </Alert>

            <div class="flex justify-end gap-2">
                <Button @click="closeCameraModal">关闭</Button>
                <Button v-if="!cameraEnabled && !cameraLoading" type="primary" @click="startCamera">
                    <template #icon><CameraIcon class="w-4 h-4" /></template>
                    重新启动
                </Button>
                <Button v-if="cameraEnabled" status="warning" @click="stopCamera">
                    <template #icon><StopIcon class="w-4 h-4" /></template>
                    停止
                </Button>
            </div>
        </div>
    </Modal>

    <Modal v-model:visible="showQrTest" title="生成测试二维码" :footer="false" :width="480">
        <div class="flex flex-col gap-4">
            <Alert type="info" :show-icon="true">
                <div class="text-sm">输入要测试的 SN 码字符串, 生成二维码后, 用"摄像头扫码"对准屏幕即可扫描测试</div>
            </Alert>
            <div>
                <div class="text-sm text-gray-600 mb-1">SN 码内容</div>
                <Input v-model="qrTestContent" placeholder="例如: SKU-SPU-1662-SPU1-0003" size="large" allow-clear @press-enter="generateQrTest">
                    <template #prefix><QrCodeIcon class="w-4 h-4 text-gray-400" /></template>
                </Input>
            </div>
            <Button type="primary" size="large" long @click="generateQrTest">
                <template #icon><QrCodeIcon class="w-4 h-4" /></template>
                生成二维码
            </Button>
            <div class="flex justify-center p-4 bg-gray-50 rounded-lg">
                <canvas ref="qrTestCanvas"></canvas>
            </div>
            <Alert type="warning" :show-icon="true">
                <div class="text-xs">
                    <div>📌 测试步骤:</div>
                    <ol class="ml-4 mt-1 space-y-1">
                        <li>1. 复制一个真实的 SN 码 (如 <code>SKU-SPU-1662-SPU1-0003</code>)</li>
                        <li>2. 粘贴到上面输入框 → 点击"生成二维码"</li>
                        <li>3. 点击"摄像头扫码" → 对准屏幕上的二维码</li>
                        <li>4. 浏览器控制台会输出识别结果, 包含识别的字符串和格式</li>
                    </ol>
                </div>
            </Alert>
        </div>
    </Modal>
</template>