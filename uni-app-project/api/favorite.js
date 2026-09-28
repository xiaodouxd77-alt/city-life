import { get, post } from '../utils/request.js'

/**
 * 切换收藏状态（收藏/取消收藏）
 * @param {string} type - 'SHOP' 或 'BLOG'
 * @param {number} targetId - 目标ID
 * @returns {Promise} res.data = true(已收藏) / false(已取消)
 */
export function toggleFavorite(type, targetId) {
	return post('/favorite/' + type + '/' + targetId)
}

/**
 * 查询是否已收藏
 */
export function isFavorite(type, targetId) {
	return get('/favorite/' + type + '/' + targetId + '/is')
}

/**
 * 获取我的收藏列表
 */
export function getMyFavorites(type) {
	return get('/favorite/' + type)
}

export function getMyFavoriteShops() {
	return get('/favorite/shops')
}
