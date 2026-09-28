import { get, post } from "../utils/request.js"

export function dailySignIn() {
	return post("/credits/sign-in")
}

export function getCreditsInfo() {
	return get("/credits/info")
}

export function getCreditsLogs() {
	return get("/credits/logs")
}

export function getExchangeableVouchers() {
	return get("/credits/vouchers")
}

export function exchangeVoucher(voucherId) {
	return post("/credits/exchange/" + voucherId)
}

export function getExchangeHistory() {
	return get("/credits/exchanges")
}

export function getMyBadges() {
	return get("/credits/badges")
}

export function getUserBadges(userId) {
	return get("/credits/badges/" + userId)
}

export function getAllBadges() {
	return get("/credits/badges/all")
}
