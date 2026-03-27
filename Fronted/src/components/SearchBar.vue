<script setup lang="ts">
import { ref } from 'vue'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/outline'

interface Props {
    placeholder?: string
}
withDefaults(defineProps<Props>(), {
    placeholder: '搜索...',
})

const emit = defineEmits<{
    search: [keyword: string]
}>()

const keyword = ref('')

function handleSearch() {
    emit('search', keyword.value.trim())
}
</script>

<template>
    <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
        <div class="flex gap-4">
            <input v-model="keyword" type="text" :placeholder="placeholder" class="flex-1 rounded-xl border border-gray-300 px-4 py-2
               focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15 transition-all" @keyup.enter="handleSearch" />
            <button class="bg-blue-500 text-white px-6 rounded-xl
               hover:shadow-lg transition-shadow" @click="handleSearch">
                <MagnifyingGlassIcon class="w-5 h-5 mr-1 inline-block" />搜索
            </button>
            <slot name="extra" />
        </div>
    </div>
</template>
