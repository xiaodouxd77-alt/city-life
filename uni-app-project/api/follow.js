import { get, put } from '../utils/request.js'

export function follow(userId, isFollow) {
	return put(`/follow/${userId}/${isFollow}`)
}

export function isFollow(userId) {
	return get('/follow/or/not/' + userId)
}

export function followCommons(userId) {
	return get('/follow/common/' + userId)
}

export function getMyFollows() {
	return get('/follow/my')
}
