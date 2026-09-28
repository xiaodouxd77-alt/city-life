<template>
	<view class="container safe-bottom">
		<nav-bar title="积分商城" :showBack="true"></nav-bar>

		<view class="asset-panel">
			<view class="asset-top">
				<view>
					<text class="asset-label">当前积分</text>
					<text class="asset-num">{{ creditsInfo.credits || 0 }}</text>
				</view>
				<image class="asset-illustration" src="/static/credits-store.svg" mode="aspectFit" />
				<view class="asset-badge" @click="goBadges">
					<text class="asset-badge-num">{{ creditsInfo.totalBadges || 0 }}</text>
					<text class="asset-badge-label">枚勋章</text>
				</view>
			</view>
			<view class="asset-stats">
				<view class="asset-stat">
					<text class="stat-num">{{ creditsInfo.totalSignDays || 0 }}</text>
					<text class="stat-label">签到天数</text>
				</view>
				<view class="asset-line"></view>
				<view class="asset-stat">
					<text class="stat-num">{{ creditsInfo.availableCoupons || 0 }}</text>
					<text class="stat-label">可用优惠券</text>
				</view>
				<view class="asset-line"></view>
				<view class="asset-stat" @click="goCoupons">
					<text class="stat-num link">查看</text>
					<text class="stat-label">我的券包</text>
				</view>
			</view>
		</view>

		<view class="task-card">
			<view class="task-copy">
				<text class="task-title">{{ creditsInfo.todaySigned ? '今日签到已完成' : '今日签到任务' }}</text>
				<text class="task-desc">{{ creditsInfo.todaySigned ? '明天再来继续积累积分' : '签到后立即获得 2 积分' }}</text>
			</view>
			<button v-if="!creditsInfo.todaySigned" class="signin-btn" @click="handleSignIn">签到 +2</button>
			<button v-else class="signin-btn signed" disabled>已签到</button>
		</view>

		<view class="section-head">
			<view>
				<text class="section-title">积分兑换</text>
				<text class="section-sub">兑换后直接进入我的优惠券，可到店出示核销码</text>
			</view>
		</view>

		<view v-if="vouchers.length === 0" class="empty-state">
			<uni-icons type="wallet" size="48" color="#c9d2d6"></uni-icons>
			<text class="empty-text">暂无可用优惠券</text>
		</view>
		<view v-else class="voucher-list">
			<view class="voucher-card" v-for="v in vouchers" :key="v.id">
				<view class="voucher-main">
					<view class="voucher-value">
						<text class="value-num">¥{{ money(v.actualValue || v.payValue) }}</text>
						<text class="value-label">到店抵扣</text>
					</view>
					<view class="voucher-info">
						<text class="voucher-title">{{ v.title }}</text>
						<text class="voucher-desc">{{ v.subTitle || '适用于指定商家优惠券' }}</text>
						<view class="voucher-tags">
							<text class="universal-tag">通用优惠券</text>
							<text class="cost-tag">{{ v.creditsCost }} 积分</text>
							<text class="cash-tag">原价 ¥{{ money(v.payValue) }}</text>
						</view>
					</view>
				</view>
				<button class="exchange-btn" :disabled="(creditsInfo.credits || 0) < v.creditsCost" @click="handleExchange(v)">
					{{ (creditsInfo.credits || 0) >= v.creditsCost ? '立即兑换' : '积分不足' }}
				</button>
			</view>
		</view>

		<view class="section-head history-head" v-if="exchanges.length > 0">
			<view>
				<text class="section-title">兑换记录</text>
				<text class="section-sub">最近 20 条积分兑换流水</text>
			</view>
		</view>
		<view v-if="exchanges.length > 0" class="exchange-list">
			<view class="exchange-item" v-for="ex in exchanges" :key="ex.id">
				<view class="exchange-left">
					<text class="ex-name">{{ ex.voucherTitle || (ex.voucherId ? '优惠券 #' + ex.voucherId : '积分兑换') }}</text>
					<text class="ex-time">{{ ex.createTime || '' }}</text>
				</view>
				<text class="ex-cost">-{{ ex.creditsCost }} 积分</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getCreditsInfo, dailySignIn, getExchangeableVouchers, exchangeVoucher, getExchangeHistory } from "@/api/credits.js"

export default {
	data() {
		return {
			creditsInfo: {},
			vouchers: [],
			exchanges: []
		}
	},
	async onShow() {
		await Promise.all([this.loadCredits(), this.loadVouchers(), this.loadExchanges()])
	},
	methods: {
		money(value) {
			return ((Number(value) || 0) / 100).toFixed(0)
		},
		async loadCredits() {
			const res = await getCreditsInfo()
			if (res.success) this.creditsInfo = res.data || {}
		},
		async loadVouchers() {
			const res = await getExchangeableVouchers()
			if (res.success) this.vouchers = res.data || []
		},
		async loadExchanges() {
			const res = await getExchangeHistory()
			if (res.success) this.exchanges = res.data || []
		},
		async handleSignIn() {
			const res = await dailySignIn()
			if (res.success) {
				uni.showToast({ title: "签到成功 +2积分", icon: "success" })
				await this.loadCredits()
			} else {
				uni.showToast({ title: res.errorMsg || "签到失败", icon: "none" })
			}
		},
		async handleExchange(v) {
			uni.showModal({
				title: "确认兑换",
				content: "消耗 " + v.creditsCost + " 积分兑换「" + v.title + "」？",
				success: async (r) => {
					if (r.confirm) {
						const res = await exchangeVoucher(v.id)
						if (res.success) {
							uni.showToast({ title: "已放入我的优惠券", icon: "success" })
							await Promise.all([this.loadCredits(), this.loadExchanges()])
						} else {
							uni.showToast({ title: res.errorMsg || "兑换失败", icon: "none" })
						}
					}
				}
			})
		},
		goBadges() {
			uni.navigateTo({ url: "/pages/badges/badges" })
		},
		goCoupons() {
			uni.navigateTo({ url: "/pages/my-orders/my-orders?status=2" })
		}
	}
}
</script>

<style scoped>
.container { background: #f4f7f8; min-height: 100vh; }
.safe-bottom { padding-bottom: calc(24rpx + env(safe-area-inset-bottom)); }
.asset-panel {
	margin: 20rpx;
	padding: 30rpx;
	border-radius: 16rpx;
	background: linear-gradient(135deg, #173b45, #236f77 58%, #58b09c);
	color: #fff;
	box-shadow: 0 12rpx 28rpx rgba(23, 59, 69, 0.18);
}
.asset-top { display: flex; justify-content: space-between; align-items: flex-start; }
.asset-label { display: block; font-size: 24rpx; color: rgba(255,255,255,0.75); }
.asset-num { display: block; margin-top: 8rpx; font-size: 72rpx; line-height: 1; font-weight: 800; }
.asset-illustration { width: 150rpx; height: 120rpx; margin-left: auto; margin-right: 18rpx; }
.asset-badge {
	min-width: 118rpx;
	padding: 14rpx 18rpx;
	border-radius: 14rpx;
	background: rgba(255,255,255,0.14);
	display: flex;
	flex-direction: column;
	align-items: center;
}
.asset-badge-num { font-size: 34rpx; font-weight: 700; }
.asset-badge-label { font-size: 20rpx; color: rgba(255,255,255,0.78); margin-top: 2rpx; }
.asset-stats {
	margin-top: 30rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid rgba(255,255,255,0.18);
	display: flex;
	align-items: center;
}
.asset-stat { flex: 1; display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 32rpx; font-weight: 700; color: #fff; }
.stat-num.link { font-size: 26rpx; color: #ffd166; }
.stat-label { font-size: 21rpx; color: rgba(255,255,255,0.72); margin-top: 6rpx; }
.asset-line { width: 1rpx; height: 42rpx; background: rgba(255,255,255,0.18); }
.task-card {
	margin: 0 20rpx 24rpx;
	padding: 24rpx;
	border-radius: 16rpx;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 8rpx 22rpx rgba(26, 50, 58, 0.05);
}
.task-copy { display: flex; flex-direction: column; }
.task-title { font-size: 30rpx; font-weight: 700; color: #263238; }
.task-desc { font-size: 23rpx; color: #73858c; margin-top: 6rpx; }
.signin-btn {
	width: 150rpx;
	height: 62rpx;
	line-height: 62rpx;
	border-radius: 31rpx;
	background: #2364aa;
	color: #fff;
	font-size: 25rpx;
	border: none;
	padding: 0;
}
.signin-btn::after { border: none; }
.signin-btn.signed { background: #edf1f2; color: #8d9aa0; }
.section-head { padding: 4rpx 24rpx 14rpx; }
.history-head { padding-top: 26rpx; }
.section-title { display: block; font-size: 30rpx; font-weight: 700; color: #263238; }
.section-sub { display: block; font-size: 22rpx; color: #839198; margin-top: 6rpx; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 70rpx 0; }
.empty-text { font-size: 26rpx; color: #a8b3b8; margin-top: 16rpx; }
.voucher-list { padding: 0 20rpx; }
.voucher-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 22rpx;
	margin-bottom: 18rpx;
	box-shadow: 0 8rpx 24rpx rgba(26, 50, 58, 0.06);
}
.voucher-main { display: flex; align-items: stretch; }
.voucher-value {
	width: 160rpx;
	border-radius: 12rpx;
	background: #f0faf7;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #1f7a72;
}
.value-num { font-size: 44rpx; font-weight: 800; }
.value-label { font-size: 20rpx; margin-top: 6rpx; color: #5c9087; }
.voucher-info { flex: 1; min-width: 0; padding-left: 20rpx; }
.voucher-title { display: block; font-size: 30rpx; font-weight: 700; color: #263238; line-height: 1.35; }
.voucher-desc { display: block; font-size: 23rpx; color: #7b8b91; margin-top: 8rpx; line-height: 1.35; }
.voucher-tags { display: flex; align-items: center; gap: 12rpx; margin-top: 18rpx; }
.universal-tag { font-size: 22rpx; color: #167c5b; background: #e5f6ef; border-radius: 18rpx; padding: 5rpx 14rpx; font-weight: 600; }
.cost-tag { font-size: 23rpx; color: #9b5c00; background: #fff3d6; border-radius: 18rpx; padding: 5rpx 14rpx; font-weight: 600; }
.cash-tag { font-size: 22rpx; color: #8b9aa0; background: #f1f4f5; border-radius: 18rpx; padding: 5rpx 14rpx; }
.exchange-btn {
	width: 100%;
	height: 68rpx;
	line-height: 68rpx;
	margin-top: 20rpx;
	background: #1f7a72;
	color: #fff;
	border-radius: 34rpx;
	font-size: 26rpx;
	border: none;
	padding: 0;
}
.exchange-btn::after { border: none; }
.exchange-btn[disabled] { background: #edf1f2; color: #96a3a8; }
.exchange-list { padding: 0 20rpx 20rpx; }
.exchange-item {
	background: #fff;
	border-radius: 14rpx;
	padding: 20rpx 22rpx;
	margin-bottom: 12rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.exchange-left { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.ex-name { font-size: 26rpx; color: #263238; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ex-time { font-size: 20rpx; color: #9aa7ac; margin-top: 6rpx; }
.ex-cost { font-size: 24rpx; color: #c35f18; font-weight: 700; margin-left: 20rpx; }
</style>
