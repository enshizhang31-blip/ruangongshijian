import { ref, computed, type Ref } from 'vue'
import type { PageQuery } from '@/types'

interface PageResult<T> {
  list: T[]
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export function usePageQuery<T>(
  fetchFn: (params: PageQuery) => Promise<PageResult<T>>,
) {
  const loading = ref(false)
  const error = ref<Error | null>(null)
  const list: Ref<T[]> = ref([]) as Ref<T[]>
  const total = ref(0)
  const query = ref<PageQuery>({
    page: 1,
    pageSize: 20,
  })

  const totalPages = computed(() => Math.ceil(total.value / (query.value.pageSize || 20)))

  async function load() {
    loading.value = true
    error.value = null
    try {
      const params = { ...query.value } as PageQuery
      const result = await fetchFn(params)
      list.value = result.list || (result as any).records || []
      total.value = result.pagination?.total ?? (result as any).total ?? 0
    } catch (e) {
      error.value = e instanceof Error ? e : new Error('请求失败')
      console.error('usePageQuery error:', e)
    } finally {
      loading.value = false
    }
  }

  function setPage(page: number) {
    query.value.page = page
    load()
  }

  function setKeyword(keyword: string) {
    query.value.keyword = keyword
    query.value.page = 1
    load()
  }

  function setPageSize(pageSize: number) {
    query.value.pageSize = pageSize
    query.value.page = 1
    load()
  }

  function setStatus(status: number | undefined) {
    query.value.status = status
    query.value.page = 1
    load()
  }

  return {
    loading,
    error,
    list,
    total,
    query,
    totalPages,
    load,
    setPage,
    setKeyword,
    setPageSize,
    setStatus,
  }
}
