const os = require('os')
const path = require('path')
import { insertOrIgnore, queryAll, queryOne, run, update } from './ADB'
import { startLocalServer } from '../file'
import store from '../store'

const userDir = os.homedir()
const defaultLocalFolder = path.join(userDir, '.chatapp', 'fileStorage')
const normalizeFolder = (folder) => {
  if (!folder) {
    return defaultLocalFolder
  }
  return path.normalize(folder.replace(/\\/g, path.sep))
}
const updateContactNoReadCount = ({ userId, noReadCount }) => {
  return new Promise(async (resolve, reject) => {
    let sql = null
    if (noReadCount) {
      sql = 'update user_setting set contact_no_read = contact_no_read+? where user_id = ?'
    } else {
      noReadCount = 0
      sql = 'update user_setting set contact_no_read = ? where user_id = ?'
    }
    await run(sql, [noReadCount, userId])
    resolve()
  })
}

const addUserSetting = async (userId, email) => {
  let sql = 'select max(server_port) server_port from user_setting'
  let { serverPort } = await queryOne(sql, [])
  if (serverPort == null) {
    serverPort = 10240
  } else {
    serverPort++
  }
  const sysSetting = {
    localFileFolder: defaultLocalFolder
  }
  sql = 'select * from user_setting where user_id =?'
  const userInfo = await queryOne(sql, [userId])

  let resultServerPort = null
  let localFileFolder = path.join(sysSetting.localFileFolder, userId)
  if (userInfo) {
    await update('user_setting', { email: email }, { userId: userId })
    resultServerPort = userInfo.serverPort
    const parsedSetting = JSON.parse(userInfo.sysSetting || '{}')
    const storedBaseFolder = normalizeFolder(parsedSetting.localFileFolder)
    localFileFolder = path.join(storedBaseFolder, userId)
  } else {
    await insertOrIgnore('user_setting', {
      userId: userId,
      email: email,
      sysSetting: JSON.stringify(sysSetting),
      contactNoRead: 0,
      serverPort: serverPort
    })
    resultServerPort = serverPort
  }
  //启动本地服务
  startLocalServer(resultServerPort)
  store.setUserData('localServerPort', resultServerPort)
  store.setUserData('localFileFolder', localFileFolder)
}

const selectSettingInfo = (userId) => {
  let sql = 'select  * from  user_setting where user_id = ?'
  return queryOne(sql, [userId])
}

const updateSysSetting = (sysSetting) => {
  const data = {
    sysSetting
  }
  const paramData = {
    userId: store.getUserId()
  }
  return update('user_setting', data, paramData)
}

const loadLocalUser = async () => {
  let sql = 'select email from user_setting where email is not null'
  return queryAll(sql, [])
}

export {
  updateContactNoReadCount,
  selectSettingInfo,
  addUserSetting,
  updateSysSetting,
  loadLocalUser
}
