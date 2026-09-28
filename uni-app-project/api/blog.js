import { get, post, put } from '../utils/request.js'

export function saveBlog(data) {
	return post('/blog', data)
}

export function likeBlog(id) {
	return put('/blog/like/' + id)
}

export function getMyBlogs(current = 1) {
	return get('/blog/of/me', { current })
}

export function getHotBlogs(current = 1) {
	return get('/blog/hot', { current })
}

export function getBlogById(id) {
	return get('/blog/' + id)
}

export function getBlogLikes(id) {
	return get('/blog/likes/' + id)
}

export function getBlogsByUser(userId, current = 1) {
	return get('/blog/of/user', { id: userId, current })
}

export function getFollowBlogs(lastId, offset = 0) {
	return get('/blog/of/follow', { lastId, offset })
}

export function getBlogsByShop(shopId, current = 1) {
	return get('/blog/of/shop', { shopId, current })
}
