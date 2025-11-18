<template>
  <div v-if="count > 0" class="badge" :style="{ top: top + 'px', left: left + 'px' }">
    {{ count > 99 ? '99+' : count }}
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()

const props = defineProps({
  count: {
    type: Number,
    default: 0
  },
  top: {
    type: Number,
    default: 0
  },
  left: {
    type: Number,
    default: 0
  }
})
</script>

<style lang="scss" scoped>
.badge {
  position: absolute;
  padding: 1px 6px;
  border-radius: 10px;
  background: #fa5151;
  color: #fff;
  z-index: 1;
  font-size: 12px;
  zoom: 0.85;
}
</style>
