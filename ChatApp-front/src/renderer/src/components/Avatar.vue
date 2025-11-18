<template>
  <div>
    <AvatarBase
      v-if="userId == 'Urobot' || contactType == 1"
      :user-id="userId"
      :width="width"
      :border-radius="borderRadius"
      :show-detail="false"
    ></AvatarBase>
    <el-popover
      v-else
      ref="popoverRef"
      :width="280"
      placement="right-start"
      :show-arrow="false"
      trigger="click"
      transition="none"
      :hide-after="0"
      @show="getContactInfo"
    >
      <template #reference>
        <AvatarBase
          :user-id="userId"
          :width="width"
          :border-radius="borderRadius"
          :show-detail="false"
        ></AvatarBase>
      </template>
      <template #default>
        <div class="popover-user-panel">
          <UserBaseInfo :user-info="userInfo"></UserBaseInfo>
          <div v-if="userId !== userInfoStore.getInfo().userId" class="op-btn">
            <el-button v-if="userInfo.contactStatus == 1" type="primary" @click="sendMessage"
              >发送消息</el-button
            >
            <el-button v-else type="primary" @click="addContact">加为好友</el-button>
          </div>
        </div>
      </template>
    </el-popover>

    <!--添加好友-->
    <SearchAdd ref="searchAddRef"></SearchAdd>
  </div>
</template>

<script setup>
import SearchAdd from '@/views/contact/SearchAdd.vue'
import AvatarBase from './AvatarBase.vue'
import UserBaseInfo from './UserBaseInfo.vue'
import { getCurrentInstance, ref } from 'vue'
import { useAvatarInfoStore } from '@/stores/AvatarUpdateStore'
import { useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/UserInfoStore'

const { proxy } = getCurrentInstance()
const avatarInfoStore = useAvatarInfoStore()
const router = useRouter()

const userInfoStore = useUserInfoStore()

const props = defineProps({
  userId: {
    type: String
  },
  width: {
    type: Number,
    default: 40
  },
  borderRadius: {
    type: Number,
    default: 0
  },
  groupId: {
    type: String
  },
  contactType: {
    type: Number,
    default: 0
  }
})

const userInfo = ref({})
const getContactInfo = async () => {
  userInfo.value.userId = props.userId
  if (userInfoStore.getInfo().userId == props.userId) {
    userInfo.value = userInfoStore.getInfo()
  } else {
    let result = await proxy.Request({
      url: proxy.Api.getContactInfo,
      params: {
        contactId: props.userId
      },
      showLoading: false
    })
    if (!result) {
      return
    }
    userInfo.value = Object.assign({}, result.data)
  }
}
//添加好友
const popoverRef = ref()
const searchAddRef = ref()
const addContact = () => {
  popoverRef.value.hide()
  searchAddRef.value.show({
    contactId: props.userId,
    contactType: 'USER'
  })
}

//发送消息
const emit = defineEmits(['closeDrawer'])
const sendMessage = () => {
  popoverRef.value.hide()
  emit('closeDrawer')
  router.push({ path: '/chat', query: { chatId: props.userId, timestamp: new Date().getTime() } })
}
</script>
<style lang="scss" scoped>
.op-btn {
  text-align: center;
  border-top: 1px solid #eaeaea;
  padding-top: 10px;
}
</style>
