/** 手机号校验 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/** 邮箱校验 */
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/** 金额校验（正数，最多2位小数） */
export function isValidMoney(amount: string | number): boolean {
  return /^\d+(\.\d{1,2})?$/.test(String(amount))
}
