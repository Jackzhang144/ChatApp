import axios from 'axios'
import { ElLoading } from 'element-plus'
import Message from '../utils/Message'
import Api from '../utils/Api'

const CONTENT_TYPE_FORM = 'application/x-www-form-urlencoded;charset=UTF-8'
const CONTENT_TYPE_JSON = 'application/json'
const RESPONSE_TYPE_JSON = 'json'
const BINARY_RESPONSE = ['arraybuffer', 'blob']
const REQUEST_TIMEOUT = 10 * 1000

let loadingInstance = null
const instance = axios.create({
  withCredentials: true,
  baseURL: `${import.meta.env.PROD ? Api.prodDomain : ''}/api`,
  timeout: REQUEST_TIMEOUT
})

instance.interceptors.request.use(
  (config) => {
    if (config.showLoading) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中……',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    return config
  },
  (error) => {
    if (error?.config?.showLoading && loadingInstance) {
      loadingInstance.close()
    }
    Message.error('请求发送失败')
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    const { showLoading, errorCallback, showError = true, responseType } = response.config
    if (showLoading && loadingInstance) {
      loadingInstance.close()
    }
    const responseData = response.data
    if (BINARY_RESPONSE.includes(responseType)) {
      return responseData
    }

    if (responseData.code === 200) {
      return responseData
    }
    if (responseData.code === 901) {
      setTimeout(() => {
        window.ipcRenderer.send('reLogin')
      }, 2000)
      return Promise.reject({ showError: true, msg: '登录超时' })
    }

    if (typeof errorCallback === 'function') {
      errorCallback(responseData)
    }
    return Promise.reject({ showError, msg: responseData.info })
  },
  (error) => {
    if (error?.config?.showLoading && loadingInstance) {
      loadingInstance.close()
    }
    return Promise.reject({ showError: true, msg: '网络异常' })
  }
)

const buildPayload = (params = {}, useJson = false) => {
  if (useJson) {
    return params
  }
  const formData = new FormData()
  Object.entries(params).forEach(([key, value]) => {
    formData.append(key, value ?? '')
  })
  return formData
}

const request = (config = {}) => {
  const {
    url,
    params = {},
    dataType,
    showLoading = true,
    responseType = RESPONSE_TYPE_JSON,
    showError = true,
    errorCallback
  } = config
  const useJson = dataType === 'json'
  const payload = buildPayload(params, useJson)
  const token = localStorage.getItem('token')
  const headers = {
    'Content-Type': useJson ? CONTENT_TYPE_JSON : CONTENT_TYPE_FORM,
    'X-Requested-With': 'XMLHttpRequest',
    ...(token ? { token } : {})
  }
  return instance
    .post(url, payload, {
      headers,
      showLoading,
      errorCallback,
      showError,
      responseType
    })
    .catch((error) => {
      if (error.showError) {
        Message.error(error.msg)
      }
      return null
    })
}

export default request
