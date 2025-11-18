const isDev = process.env.NODE_ENV === 'development'

const appConfig = {
  /**
   * 是否在窗口创建后自动打开开发者工具
   * 可通过环境变量 AUTO_OPEN_DEVTOOLS=true/false 进行覆盖
   */
  autoOpenDevTools: process.env.AUTO_OPEN_DEVTOOLS
    ? process.env.AUTO_OPEN_DEVTOOLS === 'true'
    : isDev
}

export default appConfig
