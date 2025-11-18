<template>
  <div class="image-panel">
    <ShowLocalImage
      :file-id="data.messageId"
      :show-play="true"
      part-type="chat"
      :file-type="data.fileType"
    >
    </ShowLocalImage>
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue'
import { useGlobalInfoStore } from '@/stores/GlobalInfoStore'

const { proxy } = getCurrentInstance()
const globalInfoStore = useGlobalInfoStore()

const props = defineProps({
  data: {
    type: Object,
    default: {}
  }
})
</script>

<style lang="scss" scoped></style>
