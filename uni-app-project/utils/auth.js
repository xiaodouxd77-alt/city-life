// 登录状态管理
export function isLogin() {
	return !!uni.getStorageSync('token')
}

export function getToken() {
	return uni.getStorageSync('token') || ''
}

export function setToken(token) {
	uni.setStorageSync('token', token)
}

export function removeToken() {
	uni.removeStorageSync('token')
}

export function getUserInfo() {
	try {
		return uni.getStorageSync('userInfo') || null
	} catch (e) {
		return null
	}
}

export function setUserInfo(userInfo) {
	uni.setStorageSync('userInfo', userInfo)
}

export function removeUserInfo() {
	uni.removeStorageSync('userInfo')
}

export async function logout() {
	// 主动关闭通知连接，防止退出后仍收到当前账号的消息。
	try {
		const { disconnectNotificationSocket } = await import('./notification-socket.js')
		disconnectNotificationSocket()
	} catch (e) {}
	// 调用后端登出接口（将token加入黑名单）
	try {
		const { logout } = await import('../api/user.js')
		await logout()
	} catch (e) {
		// 即使后端调用失败，也清除本地状态
	}
	removeToken()
	removeUserInfo()
}

// 需要登录的页面守卫
export function requireLogin(backUrl = '') {
	if (!isLogin()) {
		uni.navigateTo({
			url: '/pages/login/login' + (backUrl ? '?backUrl=' + encodeURIComponent(backUrl) : '')
		})
		return false
	}
	return true
}
