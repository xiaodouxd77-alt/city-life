import { get, post, put, uploadFile } from '../utils/request.js'

export function sendCode(phone) {
	return post('/user/code?phone=' + encodeURIComponent(phone), {})
}

export function login(data) {
	return post('/user/login', data)
}

export function register(data) {
	return post('/user/register', data)
}

export function logout() {
	return post('/user/logout')
}

export function getMe() {
	return get('/user/me')
}

export function getUserInfo(id) {
	return get('/user/info/' + id)
}

export function getUserById(id) {
	return get('/user/' + id)
}

export function updateProfile(data) {
	return put('/user/profile', data)
}

export function updateUserInfo(data) {
	return put('/user/info', data)
}

export function uploadAvatar(filePath) {
	return uploadFile('/user/avatar', filePath, 'file')
}
