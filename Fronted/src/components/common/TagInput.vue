<script setup lang="ts">
import { ref } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'

interface Props {
    modelValue?: string[]
    placeholder?: string
}

withDefaults(defineProps<Props>(), {
    modelValue: () => [],
    placeholder: '输入标签后回车确认',
})

const emit = defineEmits<{
    'update:modelValue': [value: string[]]
}>()

const input = ref('')

function addTag() {
    const tag = input.value.trim()
    if (tag && !input.value.includes(tag)) {
        emit('update:modelValue', [...input.value, tag])
    }
    input.value = ''
}

function removeTag(index: number) {
    const newTags = [...input.value]
    newTags.splice(index, 1)
    emit('update:modelValue', newTags)
}
</script>

<template>
    <div class="flex flex-wrap gap-2 p-2 border border-gray-200 rounded-xl min-h-[42px]">
        <span v-for="(tag, index) in modelValue" :key="index"
            class="inline-flex items-center gap-1 px-2 py-1 bg-blue-50 text-blue-600 rounded-md text-sm">
            {{ tag }}
            <button type="button" @click="removeTag(index)">
                <XMarkIcon class="w-3 h-3 hover:text-blue-800" />
            </button>
        </span>
        <input v-model="input" type="text" :placeholder="modelValue.length === 0 ? placeholder : ''"
            class="flex-1 min-w-24 outline-none text-sm" @keyup.enter="addTag" />
    </div>
</template>
