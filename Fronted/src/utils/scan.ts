/**
 * Web 端扫码工具 - 浏览器摄像头扫码
 * 基于 BarcodeDetector API（Chrome/Edge 支持）
 */

/**
 * 扫码二维码
 * @returns 扫码结果字符串，取消或失败返回空字符串
 */
export async function scanQRCode(): Promise<string> {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })

    return new Promise((resolve) => {
      // 创建 video 元素
      const video = document.createElement('video')
      video.setAttribute('playsinline', '')
      video.setAttribute('autoplay', '')
      video.srcObject = stream
      video.style.position = 'fixed'
      video.style.top = '0'
      video.style.left = '0'
      video.style.width = '100%'
      video.style.height = '100%'
      video.style.objectFit = 'cover'
      video.style.zIndex = '9999'
      document.body.appendChild(video)

      let detector: any = null
      let timer: ReturnType<typeof setInterval> | null = null
      let resolved = false

      const cleanup = (result: string) => {
        if (resolved) return
        resolved = true
        if (timer) clearInterval(timer)
        stream.getTracks().forEach((t) => t.stop())
        document.body.removeChild(video)
        resolve(result)
      }

      video.onloadedmetadata = async () => {
        await video.play()

        // 优先使用 BarcodeDetector API
        if ('BarcodeDetector' in window) {
          try {
            detector = new (window as any).BarcodeDetector({
              formats: ['qr_code']
            })
          } catch {
            detector = null
          }
        }

        timer = setInterval(async () => {
          if (detector) {
            try {
              const barcodes = await detector.detect(video)
              if (barcodes.length > 0) {
                cleanup(barcodes[0].rawValue || '')
              }
            } catch {
              // 检测失败，继续重试
            }
          }
        }, 300)
      }

      // 点击关闭
      video.addEventListener('click', () => cleanup(''))
    })
  } catch {
    console.error('二维码扫码失败')
    return ''
  }
}

/**
 * 扫码条形码
 * @returns 扫码结果字符串，取消或失败返回空字符串
 */
export async function scanBarcode(): Promise<string> {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })

    return new Promise((resolve) => {
      const video = document.createElement('video')
      video.setAttribute('playsinline', '')
      video.setAttribute('autoplay', '')
      video.srcObject = stream
      video.style.position = 'fixed'
      video.style.top = '0'
      video.style.left = '0'
      video.style.width = '100%'
      video.style.height = '100%'
      video.style.objectFit = 'cover'
      video.style.zIndex = '9999'
      document.body.appendChild(video)

      let detector: any = null
      let timer: ReturnType<typeof setInterval> | null = null
      let resolved = false

      const cleanup = (result: string) => {
        if (resolved) return
        resolved = true
        if (timer) clearInterval(timer)
        stream.getTracks().forEach((t) => t.stop())
        document.body.removeChild(video)
        resolve(result)
      }

      video.onloadedmetadata = async () => {
        await video.play()

        if ('BarcodeDetector' in window) {
          try {
            detector = new (window as any).BarcodeDetector({
              formats: ['ean_8', 'ean_13', 'code_128', 'code_39', 'upc_a', 'upc_e']
            })
          } catch {
            detector = null
          }
        }

        timer = setInterval(async () => {
          if (detector) {
            try {
              const barcodes = await detector.detect(video)
              if (barcodes.length > 0) {
                cleanup(barcodes[0].rawValue || '')
              }
            } catch {
              // 检测失败，继续重试
            }
          }
        }, 300)
      }

      video.addEventListener('click', () => cleanup(''))
    })
  } catch {
    console.error('条形码扫码失败')
    return ''
  }
}
