import { get, post, put } from '../utils/request.js'

export function getShopById(id) {
	return get('/shop/' + id)
}

export function saveShop(data) {
	return post('/shop', data)
}

export function updateShop(data) {
	return put('/shop', data)
}

export function getShopsByType(typeId, current = 1, x, y) {
	return get('/shop/of/type', { typeId, current, x, y })
}

export function searchShops(name, current = 1) {
	return get('/shop/of/name', { name, current })
}

export function getShopTypes() {
	return get('/shop-type/list')
}
