<template>
  <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="80px" @submit.prevent>
    <!--input输入-->
    <el-form-item label="群名称" prop="groupName">
      <el-input
        v-model.trim="formData.groupName"
        maxlength="150"
        clearable
        placeholder="请输入群名称"
      ></el-input>
    </el-form-item>
    <!--input输入-->
    <el-form-item label="封面" prop="avatarFile">
      <AvatarUpload
        ref="avatarUploadRef"
        v-model="formData.avatarFile"
        @cover-file="saveCover"
      ></AvatarUpload>
    </el-form-item>
    <!-- 下拉框 -->
    <!-- 单选 -->
    <el-form-item label="加入权限" prop="joinType">
      <el-radio-group v-model="formData.joinType">
        <el-radio :label="1">管理员同意后加入</el-radio>
        <el-radio :label="0">直接加入</el-radio>
      </el-radio-group>
    </el-form-item>
    <!-- 单选 -->
    <el-form-item label="公告" prop="groupNotice">
      <el-input
        v-model.trim="formData.groupNotice"
        clearable
        placeholder="请输入群公告"
        type="textarea"
        rows="5"
        maxlength="300"
        :show-word-limit="true"
        resize="none"
      ></el-input>
    </el-form-item>
    <!--input输入-->
    <el-form-item>
      <el-button type="primary" @click="submit">{{
        formData.groupId ? '修改群组' : '创建群组'
      }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import AvatarUpload from '@/components/AvatarUpload.vue'
import { getCurrentInstance, ref } from 'vue'
import { useContactStateStore } from '@/stores/ContactStateStore'
import { useAvatarInfoStore } from '@/stores/AvatarUpdateStore'

const { proxy } = getCurrentInstance()
const contactStateStore = useContactStateStore()

const avatarInfoStore = useAvatarInfoStore()

const formData = ref({})
const formDataRef = ref()
const rules = {
  groupName: [{ required: true, message: '请输入群名称' }],
  joinType: [{ required: true, message: '请选择加入权限' }],
  avatarFile: [{ required: true, message: '请选择头像' }]
}

const avatarUploadRef = ref()

const emit = defineEmits(['eidtBack'])
const submit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)
    contactStateStore.setContactReload(null)
    if (params.groupId) {
      avatarInfoStore.setFoceReload(params.groupId, false)
    }
    let result = await proxy.Request({
      url: proxy.Api.saveGroup,
      params
    })
    if (!result) {
      return
    }
    avatarUploadRef.value.clear()
    if (params.groupId) {
      proxy.Message.success('群组修改成功')
      //修改后回调处理弹窗
      emit('eidtBack')
    } else {
      proxy.Message.success('群组创建成功')
    }
    formDataRef.value.resetFields()
    //重新加载列表
    contactStateStore.setContactReload('MY')
    //重新加载头像
    if (params.groupId) {
      avatarInfoStore.setFoceReload(params.groupId, true)
    }
  })
}

//设置封面
const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.avatarFile = avatarFile
  formData.value.avatarCover = coverFile
}

const show = (data) => {
  formDataRef.value.resetFields()
  formData.value = Object.assign({}, data)
  formData.value.avatarFile = data.groupId
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped></style>
