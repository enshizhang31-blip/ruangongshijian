/**
 * Web 端生成工具 - 根据字符串生成条形码或二维码（Canvas 渲染，返回 dataURL）
 */

// ============================================================
//                         类型定义
// ============================================================

/** 生成选项 */
interface GenerateOptions {
  /** 宽度，默认 200 */
  width?: number
  /** 高度（条形码），默认 60 */
  height?: number
  /** 前景色，默认 #000 */
  foreground?: string
  /** 背景色，默认 #fff */
  background?: string
}

// ============================================================
//                条形码生成器（Code128B）
// ============================================================

/** Code128 条空模式（1=黑条, 0=白空），每个字符 11 个模块 */
const CODE128_PATTERNS: number[][] = [
  [2,1,2,2,2,2,0,0], [2,2,2,1,2,2,0,0], [2,2,2,2,2,1,0,0], [1,2,1,2,2,3,0,0],
  [1,2,1,3,2,2,0,0], [1,3,1,2,2,2,0,0], [1,2,2,2,1,3,0,0], [1,2,2,3,1,2,0,0],
  [1,3,2,2,1,2,0,0], [2,2,1,2,1,3,0,0], [2,2,1,3,1,2,0,0], [2,3,1,2,1,2,0,0],
  [1,1,2,2,3,2,0,0], [1,2,2,1,3,2,0,0], [1,2,2,2,3,1,0,0], [1,1,3,2,2,2,0,0],
  [1,2,3,1,2,2,0,0], [1,2,3,2,2,1,0,0], [2,2,3,2,1,1,0,0], [2,2,1,1,3,2,0,0],
  [2,2,1,2,3,1,0,0], [2,1,3,2,1,2,0,0], [2,2,3,1,1,2,0,0], [3,1,2,1,3,1,0,0],
  [3,1,1,2,2,2,0,0], [3,2,1,1,2,2,0,0], [3,2,1,2,2,1,0,0], [3,1,2,2,1,2,0,0],
  [3,2,2,1,1,2,0,0], [3,2,2,2,1,1,0,0], [2,1,2,1,2,3,0,0], [2,1,2,3,2,1,0,0],
  [2,3,2,1,2,1,0,0], [1,1,1,3,2,3,0,0], [1,3,1,1,2,3,0,0], [1,3,1,3,2,1,0,0],
  [1,1,2,3,1,3,0,0], [1,3,2,1,1,3,0,0], [1,3,2,3,1,1,0,0], [2,1,1,3,1,3,0,0],
  [2,3,1,1,1,3,0,0], [2,3,1,3,1,1,0,0], [1,1,2,1,3,3,0,0], [1,1,2,3,3,1,0,0],
  [1,3,2,1,3,1,0,0], [1,1,3,1,2,3,0,0], [1,1,3,3,2,1,0,0], [1,3,3,1,2,1,0,0],
  [3,1,3,1,2,1,0,0], [2,1,1,3,3,1,0,0], [2,3,1,1,3,1,0,0], [2,1,3,1,1,3,0,0],
  [2,1,3,3,1,1,0,0], [2,1,3,1,3,1,0,0], [3,1,1,1,2,3,0,0], [3,1,1,3,2,1,0,0],
  [3,3,1,1,2,1,0,0], [3,1,2,1,1,3,0,0], [3,1,2,3,1,1,0,0], [3,3,2,1,1,1,0,0],
  [3,1,4,1,1,1,0,0], [2,2,1,4,1,1,0,0], [4,3,1,1,1,1,0,0], [1,1,1,2,2,4,0,0],
  [1,1,1,4,2,2,0,0], [1,2,1,1,2,4,0,0], [1,2,1,4,2,1,0,0], [1,4,1,1,2,2,0,0],
  [1,4,1,2,2,1,0,0], [1,1,2,2,1,4,0,0], [1,1,2,4,1,2,0,0], [1,2,2,1,1,4,0,0],
  [1,2,2,4,1,1,0,0], [1,4,2,1,1,2,0,0], [1,4,2,2,1,1,0,0], [2,4,1,2,1,1,0,0],
  [2,2,1,1,1,4,0,0], [4,1,3,1,1,1,0,0], [2,4,1,1,1,2,0,0], [1,3,4,1,1,1,0,0],
  [1,1,1,2,4,2,0,0], [1,2,1,1,4,2,0,0], [1,2,1,2,4,1,0,0], [1,1,4,2,1,2,0,0],
  [1,2,4,1,1,2,0,0], [1,2,4,2,1,1,0,0], [4,1,1,2,1,2,0,0], [4,2,1,1,1,2,0,0],
  [4,2,1,2,1,1,0,0], [2,1,2,1,4,1,0,0], [2,1,4,1,2,1,0,0], [4,1,2,1,2,1,0,0],
  [1,1,1,1,4,3,0,0], [1,1,1,3,4,1,0,0], [1,3,1,1,4,1,0,0], [1,1,4,1,1,3,0,0],
  [1,1,4,3,1,1,0,0], [4,1,1,1,1,3,0,0], [4,1,1,3,1,1,0,0], [1,1,3,1,4,1,0,0],
  [1,1,4,1,3,1,0,0], [3,1,1,1,4,1,0,0], [4,1,1,1,3,1,0,0], [2,1,1,4,1,2,0,0],
  [2,1,1,2,1,4,0,0], [2,1,1,2,3,2,0,0], [2,3,3,1,1,1,2,0]
]

const CODE128_START_B = 104
const CODE128_STOP = 106

function toCode128BValue(ch: string): number {
  const code = ch.charCodeAt(0)
  if (code >= 32 && code <= 126) return code - 32
  return 0
}

/**
 * 生成条形码 (Code128B)
 * @param text 文本内容（仅支持 ASCII 32-126）
 * @param options 生成选项
 * @returns dataURL 字符串，失败返回空字符串
 */
export function generateBarcode(text: string, options: GenerateOptions = {}): string {
  const { width = 200, height = 60, foreground = '#000', background = '#fff' } = options

  if (!text) return ''

  const codes: number[] = [CODE128_START_B]
  for (let i = 0; i < text.length; i++) {
    codes.push(toCode128BValue(text[i]))
  }

  let checksum = codes[0]
  for (let i = 1; i < codes.length; i++) {
    checksum += i * codes[i]
  }
  codes.push(checksum % 103)
  codes.push(CODE128_STOP)

  let totalModules = 0
  for (const c of codes) {
    const pattern = CODE128_PATTERNS[c]
    totalModules += pattern.reduce((s, v) => s + v, 0)
  }
  totalModules += 20

  const moduleWidth = width / totalModules
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height

  const ctx = canvas.getContext('2d')
  if (!ctx) return ''

  ctx.fillStyle = background
  ctx.fillRect(0, 0, width, height)

  let x = 10 * moduleWidth
  ctx.fillStyle = foreground

  for (const c of codes) {
    const pattern = CODE128_PATTERNS[c]
    for (let i = 0; i < pattern.length; i++) {
      const w = pattern[i] * moduleWidth
      if (i % 2 === 0) {
        ctx.fillRect(x, 0, w, height)
      }
      x += w
    }
  }

  return canvas.toDataURL()
}

// ============================================================
//                  二维码生成器（QR Code）
// ============================================================

type ErrorCorrectionLevel = 'L' | 'M' | 'Q' | 'H'

const VERSION_CAPACITY: Record<number, Record<ErrorCorrectionLevel, number>> = {
  1:  { L: 19, M: 16, Q: 13, H: 9 },
  2:  { L: 34, M: 28, Q: 22, H: 16 },
  3:  { L: 55, M: 44, Q: 34, H: 26 },
  4:  { L: 80, M: 64, Q: 48, H: 36 },
  5:  { L: 108, M: 86, Q: 62, H: 46 },
  6:  { L: 136, M: 108, Q: 76, H: 60 },
  7:  { L: 156, M: 124, Q: 88, H: 66 },
  8:  { L: 194, M: 154, Q: 110, H: 86 },
  9:  { L: 232, M: 182, Q: 132, H: 100 },
  10: { L: 274, M: 216, Q: 154, H: 122 }
}

const ALIGNMENT_POSITIONS: Record<number, number[]> = {
  2: [6, 18], 3: [6, 22], 4: [6, 26], 5: [6, 30],
  6: [6, 34], 7: [6, 22, 38], 8: [6, 24, 42], 9: [6, 26, 46],
  10: [6, 28, 50]
}

function createGaloisField(): { exp: number[]; log: number[] } {
  const exp: number[] = new Array(512)
  const log: number[] = new Array(256)
  let x = 1
  for (let i = 0; i < 255; i++) {
    exp[i] = x
    log[x] = i
    x <<= 1
    if (x >= 256) x ^= 0x11d
  }
  for (let i = 255; i < 512; i++) {
    exp[i] = exp[i - 255]
  }
  return { exp, log }
}

const GF = createGaloisField()

const GENERATOR_POLYNOMIALS: Record<number, number[]> = {
  7: [], 10: [], 13: [], 17: [], 18: [], 20: [], 22: [], 24: [],
  26: [], 28: [], 30: [], 34: [], 36: [], 40: []
}

const EC_CODEWORDS_PER_BLOCK_L: Record<number, number> = {
  1: 7, 2: 10, 3: 15, 4: 20, 5: 26, 6: 18, 7: 20, 8: 24, 9: 30, 10: 18
}

const BLOCK_INFO_L: Record<number, [number, number]> = {
  1: [1, 19], 2: [1, 34], 3: [1, 55], 4: [1, 80], 5: [1, 108],
  6: [2, 68], 7: [2, 78], 8: [2, 97], 9: [2, 116], 10: [2, 68]
}

function gfMul(a: number, b: number): number {
  if (a === 0 || b === 0) return 0
  return GF.exp[(GF.log[a] + GF.log[b]) % 255]
}

function getGeneratorPolynomial(degree: number): number[] {
  if (GENERATOR_POLYNOMIALS[degree].length > 0) {
    return GENERATOR_POLYNOMIALS[degree]
  }
  let poly = [1]
  for (let i = 0; i < degree; i++) {
    const next = new Array<number>(poly.length + 1).fill(0)
    for (let j = 0; j < poly.length; j++) {
      const mul = gfMul(poly[j], GF.exp[i])
      next[j] ^= mul
      next[j + 1] ^= poly[j]
    }
    poly = next
  }
  GENERATOR_POLYNOMIALS[degree] = poly
  return poly
}

function computeEC(data: number[], ecCount: number): number[] {
  const generator = getGeneratorPolynomial(ecCount)
  let remainder = new Array<number>(data.length + ecCount).fill(0)
  for (let i = 0; i < data.length; i++) remainder[i] = data[i]

  for (let i = 0; i < data.length; i++) {
    const factor = remainder[i]
    if (factor !== 0) {
      for (let j = 0; j < generator.length; j++) {
        remainder[i + j] ^= gfMul(generator[j], factor)
      }
    }
  }

  return remainder.slice(data.length)
}

function selectVersion(text: string, ecLevel: ErrorCorrectionLevel): number {
  const len = text.length
  for (let v = 1; v <= 10; v++) {
    if (len <= VERSION_CAPACITY[v][ecLevel]) return v
  }
  return 10
}

function encodeData(text: string, version: number): number[] {
  const result: number[] = []
  const countBits = version <= 9 ? 8 : 16

  let bits = '0100' + text.length.toString(2).padStart(countBits, '0')

  for (let i = 0; i < text.length; i++) {
    bits += text.charCodeAt(i).toString(2).padStart(8, '0')
  }

  const terminator = Math.min(4, VERSION_CAPACITY[version]['L'] * 8 - bits.length)
  if (terminator > 0) bits += '0'.repeat(terminator)

  while (bits.length % 8 !== 0) bits += '0'

  const fillBytes = [0xec, 0x11]
  let fi = 0
  const totalBytes = VERSION_CAPACITY[version]['L']
  while (bits.length < totalBytes * 8) {
    bits += fillBytes[fi].toString(2).padStart(8, '0')
    fi = (fi + 1) % 2
  }

  for (let i = 0; i < bits.length; i += 8) {
    result.push(parseInt(bits.substring(i, i + 8), 2))
  }

  return result
}

function buildMatrix(version: number, dataWithEC: number[]): number[][] {
  const size = 17 + version * 4
  const matrix: number[][] = Array.from({ length: size }, () => new Array<number>(size).fill(-1))

  const drawFinder = (row: number, col: number) => {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        const val = (r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) ? 1 : 0
        matrix[row + r][col + c] = val
      }
    }
    for (let i = 0; i < 8; i++) {
      if (row + 7 < size) matrix[row + 7][col + i] = 0
      if (col + 7 < size) matrix[row + i][col + 7] = 0
    }
  }

  drawFinder(0, 0)
  drawFinder(0, size - 7)
  drawFinder(size - 7, 0)

  for (let i = 8; i < size - 8; i++) {
    matrix[6][i] = i % 2 === 0 ? 1 : 0
    matrix[i][6] = i % 2 === 0 ? 1 : 0
  }

  if (version >= 2) {
    const positions = ALIGNMENT_POSITIONS[version]
    for (const row of positions) {
      for (const col of positions) {
        if ((row < 9 && col < 9) || (row < 9 && col > size - 9) || (row > size - 9 && col < 9)) continue
        for (let r = -2; r <= 2; r++) {
          for (let c = -2; c <= 2; c++) {
            matrix[row + r][col + c] = (Math.abs(r) === 2 || Math.abs(c) === 2 || (r === 0 && c === 0)) ? 1 : 0
          }
        }
      }
    }
  }

  matrix[size - 8][8] = 1

  for (let i = 0; i < 9; i++) {
    if (matrix[i][8] === -1) matrix[i][8] = -2
    if (matrix[8][i] === -1) matrix[8][i] = -2
  }
  for (let i = 0; i < 8; i++) {
    if (matrix[size - 1 - i][8] === -1) matrix[size - 1 - i][8] = -2
    if (matrix[8][size - 1 - i] === -1) matrix[8][size - 1 - i] = -2
  }

  let bitIndex = 0
  let goingUp = true
  let col = size - 1

  while (col > 0) {
    if (col === 6) col--
    for (let row = goingUp ? size - 1 : 0; goingUp ? row >= 0 : row < size; row += goingUp ? -1 : 1) {
      for (let c = 0; c < 2; c++) {
        const cc = col - c
        if (matrix[row][cc] === -1 && bitIndex < dataWithEC.length) {
          matrix[row][cc] = dataWithEC[bitIndex++]
        } else if (matrix[row][cc] === -1) {
          matrix[row][cc] = 0
        }
      }
    }
    goingUp = !goingUp
    col -= 2
  }

  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (matrix[r][c] < 0) matrix[r][c] = 0
    }
  }

  return matrix
}

/**
 * 生成二维码
 * @param text 文本内容
 * @param options 生成选项
 * @returns dataURL 字符串，失败返回空字符串
 */
export function generateQRCode(text: string, options: GenerateOptions = {}): string {
  const { width = 200, foreground = '#000', background = '#fff' } = options

  if (!text) return ''

  const version = selectVersion(text, 'L')

  const data = encodeData(text, version)

  const [group1Count, group1Size] = BLOCK_INFO_L[version]
  const ecCount = EC_CODEWORDS_PER_BLOCK_L[version]
  const allData: number[] = []
  const allEC: number[] = []

  for (let g = 0; g < group1Count; g++) {
    const block = data.slice(g * group1Size, (g + 1) * group1Size)
    allData.push(...block)
    allEC.push(...computeEC(block, ecCount))
  }

  const dataWithEC = allData.concat(allEC)

  const matrix = buildMatrix(version, dataWithEC)
  const size = matrix.length

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = width

  const ctx = canvas.getContext('2d')
  if (!ctx) return ''

  ctx.fillStyle = background
  ctx.fillRect(0, 0, width, width)

  const quietZone = 4
  const moduleSize = width / (size + quietZone * 2)

  ctx.fillStyle = foreground
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (matrix[r][c] === 1) {
        ctx.fillRect(
          (c + quietZone) * moduleSize,
          (r + quietZone) * moduleSize,
          moduleSize,
          moduleSize
        )
      }
    }
  }

  return canvas.toDataURL()
}
