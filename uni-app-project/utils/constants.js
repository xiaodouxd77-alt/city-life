// API 基础配置
export const BASE_URL = 'http://localhost:8081'

// OSS 图片域名
export const OSS_DOMAIN = 'https://my-project3.oss-cn-beijing.aliyuncs.com'

// 图片地址处理（OSS 返回完整 URL，直接使用即可）
export function getImageUrl(path) {
	if (!path) return ''
	// OSS 返回的是完整 HTTPS URL，直接使用
	if (path.startsWith('http://') || path.startsWith('https://')) return path
	// 兼容旧格式（相对路径）
	if (path.startsWith('/')) {
		return OSS_DOMAIN + path
	}
	return OSS_DOMAIN + '/' + path
}

// 头像默认图
export function getAvatarUrl(icon) {
	if (!icon) return '/static/avatar-default.svg'
	return getImageUrl(icon)
}

// 店铺图片处理，返回数组
export function getShopImages(images) {
	if (!images) return []
	return images.split(',').filter(Boolean).map(getImageUrl)
}

// 笔记图片处理，返回数组
export function getBlogImages(images) {
	if (!images) return []
	return images.split(',').filter(Boolean).map(getImageUrl)
}

// 评分格式化（后端存储为 1~50，显示为 1~5）
export function formatScore(score) {
	if (!score && score !== 0) return '0.0'
	return (score / 10).toFixed(1)
}

// 距离格式化
export function formatDistance(distance) {
	if (distance === undefined || distance === null) return ''
	if (distance < 1) {
		return Math.round(distance * 1000) + 'm'
	}
	return distance.toFixed(1) + 'km'
}

// 数字格式化
export function formatNumber(num) {
	if (!num && num !== 0) return '0'
	if (num >= 10000) {
		return (num / 10000).toFixed(1) + 'w'
	}
	return String(num)
}

// 日期格式化
export function formatTime(timeStr) {
	if (!timeStr) return ''
	const date = new Date(timeStr)
	const now = new Date()
	const diff = now - date
	const minute = 60 * 1000
	const hour = 60 * minute
	const day = 24 * hour
	if (diff < minute) return '刚刚'
	if (diff < hour) return Math.floor(diff / minute) + '分钟前'
	if (diff < day) return Math.floor(diff / hour) + '小时前'
	if (diff < 7 * day) return Math.floor(diff / day) + '天前'
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function pad(n) {
	return n < 10 ? '0' + n : n
}
