import { get, put } from '../utils/request.js'

/**
 * 获取我的通知列表
 */
export function getNotifications() {
	return get('/notification/unread')
}

/**
 * 获取未读通知数量。
 */
export function getUnreadCount() {
	return get('/notification/unread-count')
}

/**
 * 标记单条已读
 */
export function markRead(id) {
	return put('/notification/' + id + '/read')
}

/**
 * 全部标记已读
 */
export function markAllRead() {
	return put('/notification/read-all')
}
