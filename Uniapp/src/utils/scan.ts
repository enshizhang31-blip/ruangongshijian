/**
 * 扫码工具 - 封装 uni.scanCode API
 * 支持二维码和条形码扫码，返回扫码结果字符串
 */

/** 扫码结果 */
interface ScanResult {
  /** 扫码结果字符串 */
  result: string
  /** 扫码类型 */
  scanType: string
}

/**
 * 扫码二维码
 * @returns 扫码结果字符串，扫码取消或失败返回空字符串
 *
 * @example
 * const code = await scanQRCode()
 * if (code) {
 *   console.log('扫码结果:', code)
 * }
 */
export async function scanQRCode(): Promise<string> {
  try {
    const res = await uni.scanCode({
      scanType: ['qrCode'],
      success: undefined
    })
    return res.result || ''
  } catch (err) {
    console.error('二维码扫码失败：', err)
    return ''
  }
}

/**
 * 扫码条形码
 * @returns 扫码结果字符串，扫码取消或失败返回空字符串
 *
 * @example
 * const code = await scanBarcode()
 * if (code) {
 *   console.log('条形码结果:', code)
 * }
 */
export async function scanBarcode(): Promise<string> {
  try {
    const res = await uni.scanCode({
      scanType: ['barCode'],
      success: undefined
    })
    return res.result || ''
  } catch (err) {
    console.error('条形码扫码失败：', err)
    return ''
  }
}

/**
 * 扫码（同时支持二维码和条形码）
 * @param types 扫码类型，默认同时支持二维码和条形码
 * @returns 完整的扫码结果，扫码取消或失败返回 null
 *
 * @example
 * const result = await scanCode()
 * if (result) {
 *   console.log('内容:', result.result)
 *   console.log('类型:', result.scanType)
 * }
 */
export async function scanCode(
  types: Array<'qrCode' | 'barCode' | 'datamatrix' | 'pdf417'> = ['qrCode', 'barCode']
): Promise<ScanResult | null> {
  try {
    const res = await uni.scanCode({
      scanType: types,
      success: undefined
    })
    return {
      result: res.result || '',
      scanType: res.scanType || ''
    }
  } catch (err) {
    console.error('扫码失败：', err)
    return null
  }
}
