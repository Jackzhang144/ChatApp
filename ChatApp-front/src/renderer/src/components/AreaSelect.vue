<template>
  <div>
    <el-cascader
      :options="AreaData"
      v-model="modelValue.areaCode"
      @change="change"
      ref="areaSelectRef"
      clearable
    />
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import AreaData from './AreaData'

const { proxy } = getCurrentInstance()

const props = defineProps({
  modelValue: {
    type: Object,
    default: {}
  }
})

const emit = defineEmits(['update:modelValue'])
const areaSelectRef = ref()
const change = (e) => {
  const areaData = {
    areaName: [],
    areaCode: []
  }
  const checkedNodes = areaSelectRef.value.getCheckedNodes()[0]
  if (!checkedNodes) {
    emit('update:modelValue', areaData)
    return
  }
  const pathValues = checkedNodes.pathValues
  const pathLabels = checkedNodes.pathLabels
  areaData.areaName = pathLabels
  areaData.areaCode = pathValues
  emit('update:modelValue', areaData)
}
</script>

<style lang="scss" scoped></style>
