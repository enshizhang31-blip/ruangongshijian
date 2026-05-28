/**
 * 格式化后端返回的时间，兼容字符串和 LocalDateTime 对象两种格式
 * 示例输入：
 *   "2026-05-29 01:44:45"
 *   { year: 2026, month: 5, day: 29, hour: 1, minute: 44, second: 45 }
 *   { year: 2026, monthValue: 5, dayOfMonth: 29, hour: 1, minute: 44, second: 45 }
 */
export function formatBackendTime(value: unknown, def = '-'): string {
  if (!value) return def

  // 字符串格式
  if (typeof value === 'string') {
    const trimmed = value.trim()
    return trimmed || def
  }

  // 对象格式（LocalDateTime / Date）
  if (typeof value === 'object') {
    const obj = value as Record<string, number>
    const y = obj.year
    const m = obj.monthValue || obj.month
    const d = obj.dayOfMonth || obj.day
    const h = obj.hour
    const min = obj.minute
    const s = obj.second
    if (y && m && d) {
      const pad = (n: number) => String(n).padStart(2, '0')
      const time = h != null ? ` ${pad(h)}:${pad(min ?? 0)}` : ''
      return `${y}-${pad(m)}-${pad(d)}${time}`
    }
  }

  return def
}
