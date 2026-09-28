import { BASE_URL } from './constants.js'

/**
 * 通用请求函数 - 封装了 uni.request，统一处理认证、错误提示等
 */
function request(options = {}) {
	// 从本地存储获取token，用于身份验证
	const token = uni.getStorageSync('token') || ''
	const authHeader = token ? 'Bearer ' + token : ''

	return new Promise((resolve, reject) => {
		uni.request({
			// 如果是完整URL则直接使用，否则拼接BASE_URL
			url: options.url.startsWith('http') ? options.url : BASE_URL + options.url,
			method: options.method || 'GET',
			header: {
				'Content-Type': 'application/json',
				'authorization': authHeader,
				...options.header // 允许覆盖默认请求头
			},
			data: options.data,
			success: (res) => {
				// 401未授权 - 清除登录状态并跳转登录页
				if (res.statusCode === 401) {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
					uni.showToast({ title: '请先登录', icon: 'none' })
					setTimeout(() => { uni.navigateTo({ url: '/pages/login/login' }) }, 500)
					reject(new Error('未登录'))
					return
				}

				// 2xx成功响应 - 返回数据
				if (res.statusCode >= 200 && res.statusCode < 300) {
					resolve(res.data)
				} else {
					// 其他错误码 - 显示错误信息并拒绝
					uni.showToast({ title: res.data?.errorMsg || '请求失败(' + res.statusCode + ')', icon: 'none' })
					reject(res.data)
				}
			},
			fail: (err) => {
				// 网络请求失败（如断网、超时）
				uni.showToast({ title: '网络请求失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

/**
 * GET请求 - 自动拼接查询参数
 * @param {string} url - 请求地址
 * @param {Object} [params={}] - 查询参数对象
 * @param {Object} [header={}] - 自定义请求头
 * @returns {Promise}
 */
export function get(url, params = {}, header = {}) {
	// 将参数对象转换为URL查询字符串
	let query = ''
	if (params && Object.keys(params).length > 0) {
		const arr = []
		for (const key in params) {
			if (params[key] !== undefined && params[key] !== null) {
				arr.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
			}
		}
		query = '?' + arr.join('&')
	}
	return request({ url: url + query, method: 'GET', header })
}

/**
 * POST请求
 * @param {string} url - 请求地址
 * @param {Object} [data={}] - 请求体数据
 * @param {Object} [header={}] - 自定义请求头
 * @returns {Promise}
 */
export function post(url, data = {}, header = {}) {
	return request({ url, method: 'POST', data, header })
}

/**
 * PUT请求
 * @param {string} url - 请求地址
 * @param {Object} [data={}] - 请求体数据
 * @param {Object} [header={}] - 自定义请求头
 * @returns {Promise}
 */
export function put(url, data = {}, header = {}) {
	return request({ url, method: 'PUT', data, header })
}

/**
 * DELETE请求
 * @param {string} url - 请求地址
 * @param {Object} [data={}] - 请求体数据
 * @param {Object} [header={}] - 自定义请求头
 * @returns {Promise}
 */
export function del(url, data = {}, header = {}) {
	return request({ url, method: 'DELETE', data, header })
}

/**
 * 文件上传 - 使用uni.uploadFile
 * @param {string} url - 上传地址（相对路径）
 * @param {string} filePath - 文件路径
 * @param {string} [name='file'] - 文件字段名
 * @param {Object} [formData={}] - 附加表单数据
 * @returns {Promise} - 返回解析后的JSON数据或原始数据
 */
export function uploadFile(url, filePath, name = 'file', formData = {}) {
	const token = uni.getStorageSync('token') || ''
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: BASE_URL + url,
			filePath,
			name,
			formData,
			header: { 'authorization': token ? 'Bearer ' + token : '' },
			success: (res) => {
				// 尝试将返回数据解析为JSON
				let data = res.data
				try { data = JSON.parse(res.data) } catch (e) {}

				// 与request函数相同的认证处理逻辑
				if (res.statusCode === 401) {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
					uni.showToast({ title: '请先登录', icon: 'none' })
					setTimeout(() => { uni.navigateTo({ url: '/pages/login/login' }) }, 500)
					reject(new Error('未登录'))
					return
				}

				if (res.statusCode >= 200 && res.statusCode < 300) {
					resolve(data)
				} else {
					reject(data)
				}
			},
			fail: (err) => {
				uni.showToast({ title: '上传失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

export default request
