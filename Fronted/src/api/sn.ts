import request from './request'
import type { SnCode, SnCodeLog, SnQuery, PageQuery, PageResult } from '@/types'

export const snApi = {
    // SN码列表
    list(params: PageQuery & SnQuery) {
        return request.get<PageResult<SnCode>>('/sn/code', { params }).then((res: any) => res.data.data)
    },

    // 获取SN码详情
    getById(id: number) {
        return request.get<SnCode>(`/sn/code/${id}`).then((res: any) => res.data.data)
    },

    // 录入SN码
    create(data: { sn: string; goodsId: number; remark?: string }) {
        return request.post<SnCode>('/sn/code', data).then((res: any) => res.data.data)
    },

    // 批量录入SN码
    batchCreate(data: { sns: string[]; goodsId: number; remark?: string }) {
        return request.post<{ success: number; failed: number }>('/sn/code/batch', data).then((res: any) => res.data.data)
    },

    // 绑定SN码
    bind(data: { sn: string; orderId: number }) {
        return request.post<void>('/sn/code/bind', data).then((res: any) => res.data.data)
    },

    // 解绑SN码
    unbind(id: number) {
        return request.post<void>(`/sn/code/${id}/unbind`).then((res: any) => res.data.data)
    },

    // 查询SN码
    query(sn: string) {
        return request.get<SnCode>(`/sn/code/query/${sn}`).then((res: any) => res.data.data)
    },

    // SN码操作日志
    logs(params: PageQuery & { snId?: number }) {
        return request.get<PageResult<SnCodeLog>>('/sn/log', { params }).then((res: any) => res.data.data)
    },

    // 获取商品关联的SN码列表
    getByGoodsId(goodsId: number) {
        return request.get<SnCode[]>(`/sn/code/goods/${goodsId}`).then((res: any) => res.data.data)
    },
}
