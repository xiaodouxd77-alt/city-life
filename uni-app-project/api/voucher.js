import { get, post } from '../utils/request.js'

export function getVouchersByShop(shopId) {
	return get('/voucher/list/' + shopId)
}

export function seckillVoucher(voucherId) {
	return post('/voucher-order/seckill/' + voucherId)
}

export function buyVoucher(voucherId) {
	return post('/voucher-order/buy/' + voucherId)
}

export function addVoucher(data) {
	return post('/voucher', data)
}

export function addSeckillVoucher(data) {
	return post('/voucher/seckill', data)
}

// 璁㈠崟鐩稿叧
export function payOrder(orderId, payType) {
	return post('/voucher-order/' + orderId + '/pay', { payType })
}

export function createAlipayPay(orderId) {
	return post('/alipay/pay/' + orderId)
}

export function cancelOrder(orderId) {
	return post('/voucher-order/' + orderId + '/cancel')
}

export function useOrder(orderId, verifyCode, shopId) {
	return post('/voucher-order/' + orderId + '/use', { verifyCode, shopId })
}

export function refundOrder(orderId) {
	return post('/voucher-order/' + orderId + '/refund')
}

export function mockPayApi(orderId) {
	return post('/voucher-order/' + orderId + '/mock-pay')
}

export function getMyOrders(status) {
	return get('/voucher-order/my', status ? { status } : {})
}

export function getOrderDetail(orderId) {
	return get('/voucher-order/' + orderId)
}

