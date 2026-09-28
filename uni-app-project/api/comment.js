import { get, post, del } from '../utils/request.js'

export function getComments(blogId, current = 1) {
	return get('/blog-comments/of/' + blogId, { current })
}

export function addComment(data) {
	return post('/blog-comments', data)
}

export function deleteComment(id) {
	return del('/blog-comments/' + id)
}

export function getCommentsOnMyBlogs(current = 1) {
	return get('/blog-comments/my-blog', { current })
}
