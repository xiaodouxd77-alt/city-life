import { uploadFile, get } from '../utils/request.js'

export function uploadImage(filePath) {
	return uploadFile('/upload/blog', filePath, 'file')
}

export function deleteImage(name) {
	return get('/upload/blog/delete', { name })
}
