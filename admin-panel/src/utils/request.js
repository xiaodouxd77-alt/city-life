/**
 * axios 请求统一封装
 * 功能：统一接口前缀、请求超时、自动携带token、全局错误拦截提示、登录过期跳转
 * 适配element-plus消息弹窗，后端约定返回格式：{success:布尔值, errorMsg:错误文案, 业务数据}
 * 支持处理401登录失效、403权限不足、各类http状态码错误、业务逻辑失败
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例，统一基础配置
const request = axios.create({
  baseURL: '/api', // 所有接口统一拼接/api前缀，方便代理转发
  timeout: 15000 // 请求超时时间15秒，超时直接报错
})

/**
 * 请求拦截器
 * 每次发起请求前自动执行，统一在请求头挂载登录token
 */
request.interceptors.request.use(config => {
  // 从本地缓存取出后台登录token
  const token = localStorage.getItem('admin_token')
  if (token) {
    // 请求头带上Bearer格式token，后端做登录鉴权
    config.headers.authorization = 'Bearer ' + token
  }
  return config
})

/**
 * 响应拦截器
 * 分为成功回调、失败回调，统一处理后端业务报错和网络HTTP报错
 */
request.interceptors.response.use(
    // 接口正常返回（http 200）
    response => {
      const data = response.data
      // 后端业务主动返回失败 success: false
      if (data.success === false) {
        // 弹出错误提示，优先使用后端返回的错误信息，兜底文案
        ElMessage.error(data.errorMsg || '操作失败')
        // 返回reject，让调用方进入catch捕获错误
        return Promise.reject(data)
      }
      // 直接返回后端原始data，调用接口不用额外写 .data
      return data
    },
    // HTTP异常：4xx/5xx、断网、超时等错误
    error => {
      // 后端返回了状态码
      if (error.response) {
        // 401 token过期/未登录：清空本地登录信息，强制跳登录页
        if (error.response.status === 401) {
          localStorage.removeItem('admin_token')
          localStorage.removeItem('admin_user')
          window.location.href = '/#/login'
        } else if (error.response.status === 403) {
          // 403 登录成功，但账号无接口访问权限
          ElMessage.error('无权限访问')
        } else {
          // 其他异常状态码 404/500/502等统一弹窗提示
          ElMessage.error('请求失败: ' + error.response.status)
        }
      }
      // 抛出错误，业务页面可自行catch做额外处理
      return Promise.reject(error)
    }
)

export default request