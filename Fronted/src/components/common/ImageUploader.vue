<script setup lang="ts">
import { PhotoIcon } from '@heroicons/vue/24/outline'

interface Props {
    modelValue?: string
    preview?: boolean
}

withDefaults(defineProps<Props>(), {
    preview: true,
})

const emit = defineEmits<{
    'update:modelValue': [value: string]
}>()

function handleFileChange(e: Event) {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
            emit('update:modelValue', e.target?.result as string)
        }
        reader.readAsDataURL(file)
    }
}
</script>

<template>
    <div class="flex items-center gap-4">
        <label
            class="flex flex-col items-center justify-center w-24 h-24 border-2 border-dashed border-gray-300 rounded-xl cursor-pointer hover:border-blue-400 transition-colors overflow-hidden">
            <PhotoIcon v-if="!modelValue" class="w-8 h-8 text-gray-400" />
            <img v-else-if="preview" :src="modelValue" class="w-full h-full object-cover" />
            <input type="file" accept="image/*" class="hidden" @change="handleFileChange" />
        </label>
        <div v-if="!modelValue" class="text-xs text-gray-500">
            <p>点击上传图片</p>
            <p>支持 JPG、PNG</p>
        </div>
    </div>
</template>
