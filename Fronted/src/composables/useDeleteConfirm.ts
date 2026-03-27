import { ref } from 'vue'

export function useDeleteConfirm() {
  const deleting = ref(false)

  async function confirmDelete(id: number, deleteFn: (id: number) => Promise<void>): Promise<boolean> {
    if (!confirm('确定删除该数据吗？此操作不可恢复。')) {
      return false
    }
    deleting.value = true
    try {
      await deleteFn(id)
      return true
    } catch {
      return false
    } finally {
      deleting.value = false
    }
  }

  return { deleting, confirmDelete }
}
