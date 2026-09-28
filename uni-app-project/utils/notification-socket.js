import { BASE_URL } from './constants.js'
import { getToken } from './auth.js'

// WebSocket 连接状态变量
let socketOpen = false          // 是否已建立连接
let socketConnecting = false    // 是否正在连接中
let manualClose = false         // 是否为手动关闭（手动关闭不触发重连）
let reconnectTimer = null       // 重连定时器
let reconnectAttempts = 0       // 当前重连尝试次数（用于指数退避）
let listenersRegistered = false // 是否已注册全局事件监听器（只注册一次）

/**
 * 构建 WebSocket 连接 URL
 * 将 HTTP 协议转为 WS 协议，并拼接通知路径和 JWT token
 * @returns {string} WebSocket 完整地址
 */
function getSocketUrl() {
	const protocolUrl = BASE_URL.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:')
	return protocolUrl + '/ws/notification?token=' + encodeURIComponent(getToken())
}

/**
 * 注册 uni-app 的 WebSocket 全局事件监听器
 * 包括 onSocketOpen / onSocketMessage / onSocketClose / onSocketError
 * 这些监听器在整个应用生命周期内只需注册一次
 */
function registerSocketListeners() {
	if (listenersRegistered) return
	listenersRegistered = true

	// 连接成功打开时触发
	uni.onSocketOpen(() => {
		socketOpen = true
		socketConnecting = false
		reconnectAttempts = 0          // 重置重连次数
		uni.$emit('notificationSocketOpened')  // 通知其他模块连接已建立
	})

	// 收到服务端消息时触发
	uni.onSocketMessage((res) => {
		try {
			// 尝试将消息解析为 JSON 格式的通知
			const notification = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
			uni.$emit('notificationReceived', notification)  // 广播通知消息
		} catch (e) {
			// 非 JSON 格式的消息（如心跳包），忽略即可
		}
	})

	// 连接关闭时触发（可能是正常关闭或异常断开）
	uni.onSocketClose(() => {
		socketOpen = false
		socketConnecting = false
		scheduleReconnect()  // 尝试自动重连
	})

	// 连接发生错误时触发
	uni.onSocketError(() => {
		socketOpen = false
		socketConnecting = false
		scheduleReconnect()  // 尝试自动重连
	})
}

/**
 * 安排重连任务
 * 使用指数退避策略：1s → 2s → 4s → 8s ... 最大 30s
 * 手动关闭或无 token 时不重连
 */
function scheduleReconnect() {
	// 手动关闭、无 token、已有重连定时器时跳过
	if (manualClose || !getToken() || reconnectTimer) return

	// 计算延迟时间：2^(attempts) 秒，上限 30 秒
	const delay = Math.min(1000 * Math.pow(2, reconnectAttempts++), 30000)

	reconnectTimer = setTimeout(() => {
		reconnectTimer = null
		connectNotificationSocket()
	}, delay)
}

/**
 * 建立通知 WebSocket 连接
 * 使用当前登录用户的 JWT token 进行身份验证
 * 连接异常关闭后会自动重连
 */
export function connectNotificationSocket() {
	// 无 token、已连接或正在连接中时跳过
	if (!getToken() || socketOpen || socketConnecting) return

	manualClose = false               // 标记为非手动关闭
	registerSocketListeners()         // 确保监听器已注册
	socketConnecting = true           // 标记为连接中

	try {
		uni.connectSocket({ url: getSocketUrl() })
	} catch (e) {
		// 连接创建失败，恢复状态并尝试重连
		socketConnecting = false
		scheduleReconnect()
	}
}

/**
 * 断开通知 WebSocket 连接
 * 通常在用户退出登录时调用
 * 会取消自动重连机制
 */
export function disconnectNotificationSocket() {
	manualClose = true                // 标记为手动关闭（阻止自动重连）
	socketOpen = false
	socketConnecting = false

	// 清除重连定时器
	if (reconnectTimer) {
		clearTimeout(reconnectTimer)
		reconnectTimer = null
	}

	// 关闭当前 WebSocket 连接
	try {
		uni.closeSocket()
	} catch (e) {
		// 没有活动连接时忽略异常
	}
}
