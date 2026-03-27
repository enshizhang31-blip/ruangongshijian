import { ref, computed, type Ref } from 'vue'
import type { PageQuery, PageResult } from '@/types'

export function usePageQuery<T>(
  fetchFn: (params: PageQuery) => Promise<PageResult<T>>,
) {
  const loading = ref(false)
  const list: Ref<T[]> = ref([]) as Ref<T[]>
  const total = ref(0)
  const query = ref<PageQuery>({
    page: 1,
    pageSize: 20,
    keyword: '',
  })

  const totalPages = computed(() => Math.ceil(total.value / query.value.pageSize))

  async function load() {
    loading.value = true
    try {
      const result = await fetchFn(query.value)
      list.value = result.list
      total.value = result.total
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

  return {
    loading,
    list,
    total,
    query,
    totalPages,
    load,
    setPage,
    setKeyword,
  }
}
