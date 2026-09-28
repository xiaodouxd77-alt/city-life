<template>
	<view class="container">
		<nav-bar title="我的优惠券" :back="true"></nav-bar>

		<view class="tabs">
			<view class="tab" :class="{ active: activeTab === 2 }" @click="switchTab(2)">可使用</view>
			<view class="tab" :class="{ active: activeTab === 0 }" @click="switchTab(0)">全部</view>
			<view class="tab" :class="{ active: activeTab === 1 }" @click="switchTab(1)">待支付</view>
			<view class="tab" :class="{ active: activeTab === 3 }" @click="switchTab(3)">已使用</view>
			<view class="tab" :class="{ active: activeTab === 6 }" @click="switchTab(6)">已退款</view>
		</view>

		<view class="coupon-list" v-if="orders.length > 0">
			<view class="coupon-card" v-for="order in orders" :key="order.id" @click="goDetail(order)">
				<view class="coupon-main">
					<view class="coupon-value">
						<text class="money">¥{{ money(order.voucherValue) }}</text>
						<text class="threshold">{{ order.voucherSubTitle || '到店核销使用' }}</text>
					</view>
					<view class="coupon-info">
						<view class="coupon-title-row">
							<text class="coupon-title">{{ order.voucherTitle || '优惠券' }}</text>
							<text class="coupon-status" :class="statusClass(order.status)">{{ statusText(order.status) }}</text>
						</view>
						<text class="coupon-rule">{{ order.voucherRules || '请在商家核销前出示优惠券核销码' }}</text>
						<view class="coupon-meta">
							<text class="scope-tag" :class="{ universal: order.universal }">{{ order.useScope || '适用范围待确认' }}</text>
							<text class="source-tag" :class="{ credits: order.fromCredits }">{{ order.fromCredits ? '积分兑换' : '商家购买' }}</text>
							<text class="pay-text">{{ order.fromCredits ? '0元兑换' : '实付 ¥' + money(order.voucherPayValue) }}</text>
						</view>
					</view>
				</view>

				<view v-if="order.status === 2 && order.verifyCode" class="verify-strip">
					<text class="verify-label">核销码</text>
					<text class="verify-code">{{ order.verifyCode }}</text>
					<text class="verify-tip">到店出示</text>
				</view>

				<view class="coupon-footer">
					<text class="order-time">{{ order.createTime || '' }}</text>
					<view class="order-actions" v-if="order.status === 1">
						<button class="btn btn-cancel" @click.stop="handleCancel(order)">取消</button>
						<button class="btn btn-pay" @click.stop="handlePay(order)">支付</button>
					</view>
					<view class="order-actions" v-if="order.status === 2">
						<button class="btn btn-detail" @click.stop="goDetail(order)">详情</button>
						<button v-if="!order.fromCredits" class="btn btn-refund" @click.stop="handleRefund(order)">退款</button>
					</view>
				</view>
			</view>
		</view>

		<empty v-else text="暂无优惠券"></empty>
	</view>
</template>

<script>
import { getMyOrders, payOrder, cancelOrder, refundOrder } from '@/api/voucher.js'

export default {
	data() {
		return {
			activeTab: 2,
			orders: [],
			loading: false
		}
	},
	onLoad(options) {
		if (options.status !== undefined) this.activeTab = Number(options.status)
	},
	onShow() {
		this.loadOrders()
	},
	methods: {
		switchTab(tab) {
			this.activeTab = tab
			this.loadOrders()
		},
		async loadOrders() {
			this.loading = true
			try {
				const status = this.activeTab === 0 ? null : this.activeTab
				const res = await getMyOrders(status)
				if (res.success) this.orders = res.data || []
			} catch (e) {
			} finally {
				this.loading = false
			}
		},
		money(value) {
			return ((Number(value) || 0) / 100).toFixed(0)
		},
		statusText(status) {
			const map = { 1: '待支付', 2: '可使用', 3: '已使用', 4: '已取消', 5: '退款中', 6: '已退款' }
			return map[status] || '未知'
		},
		statusClass(status) {
			if (status === 1) return 'warning'
			if (status === 2) return 'success'
			if (status === 3) return 'info'
			return 'default'
		},
		goDetail(order) {
			uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + order.id })
		},
		async handlePay(order) {
			uni.showActionSheet({
				itemList: ['余额支付', '支付宝', '微信支付'],
				success: async (r) => {
					const payType = r.tapIndex + 1
					uni.showLoading({ title: '支付中...' })
					try {
						const res = await payOrder(order.id, payType)
						uni.hideLoading()
						if (res.success) {
							uni.showToast({ title: '支付成功', icon: 'success', duration: 2000 })
							setTimeout(() => this.loadOrders(), 800)
						} else {
							uni.showToast({ title: res.errorMsg || '支付失败', icon: 'none' })
						}
					} catch (e) {
						uni.hideLoading()
					}
				}
			})
		},
		async handleCancel(order) {
			uni.showModal({
				title: '提示',
				content: '确定取消该订单吗？',
				success: async (r) => {
					if (r.confirm) {
						try {
							const res = await cancelOrder(order.id)
							if (res.success) {
								uni.showToast({ title: '已取消', icon: 'success' })
								this.loadOrders()
							} else {
								uni.showToast({ title: res.errorMsg || '取消失败', icon: 'none' })
							}
						} catch (e) {}
					}
				}
			})
		},
		async handleRefund(order) {
			uni.showModal({
				title: '提示',
				content: '确定申请退款吗？',
				success: async (r) => {
					if (r.confirm) {
						try {
							const res = await refundOrder(order.id)
							if (res.success) {
								uni.showToast({ title: '退款成功', icon: 'success' })
								this.loadOrders()
							} else {
								uni.showToast({ title: res.errorMsg || '退款失败', icon: 'none' })
							}
						} catch (e) {}
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.container { background-color: #f4f7f8; min-height: 100vh; }
.tabs {
	display: flex;
	background-color: #fff;
	padding: 18rpx 16rpx;
	overflow-x: auto;
	white-space: nowrap;
	border-bottom: 1rpx solid #eef1f2;
}
.tab {
	flex-shrink: 0;
	padding: 12rpx 28rpx;
	font-size: 26rpx;
	color: #65747b;
	margin-right: 12rpx;
	border-radius: 30rpx;
	background-color: #f2f5f6;
}
.tab.active { background-color: #1f7a72; color: #fff; }
.coupon-list { padding: 22rpx 20rpx; }
.coupon-card {
	background-color: #fff;
	border-radius: 16rpx;
	margin-bottom: 18rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 24rpx rgba(26, 50, 58, .06);
}
.coupon-main { display: flex; min-height: 174rpx; }
.coupon-value {
	width: 190rpx;
	background: linear-gradient(135deg, #1f7a72, #58b09c);
	color: #fff;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 20rpx 12rpx;
}
.money { font-size: 54rpx; font-weight: 800; line-height: 1; }
.threshold { font-size: 20rpx; color: rgba(255,255,255,.82); margin-top: 12rpx; text-align: center; line-height: 1.35; }
.coupon-info { flex: 1; padding: 22rpx 22rpx 16rpx; min-width: 0; }
.coupon-title-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 12rpx; }
.coupon-title { flex: 1; font-size: 30rpx; font-weight: 700; color: #263238; line-height: 1.35; }
.coupon-status { flex-shrink: 0; font-size: 22rpx; padding: 4rpx 14rpx; border-radius: 20rpx; }
.coupon-status.warning { color: #b56b00; background-color: #fff4dc; }
.coupon-status.success { color: #167c5b; background-color: #e5f6ef; }
.coupon-status.info { color: #2364aa; background-color: #e7f0ff; }
.coupon-status.default { color: #889096; background-color: #f1f3f4; }
.coupon-rule { display: block; height: 60rpx; margin-top: 10rpx; font-size: 23rpx; color: #7b8b91; line-height: 1.35; overflow: hidden; }
.coupon-meta { display: flex; align-items: center; gap: 12rpx; margin-top: 12rpx; }
.scope-tag { max-width: 310rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 20rpx; color: #9b5c00; background: #fff3d6; border-radius: 18rpx; padding: 4rpx 12rpx; }
.scope-tag.universal { color: #167c5b; background: #e5f6ef; }
.source-tag { font-size: 20rpx; color: #2364aa; background: #e9f1ff; border-radius: 18rpx; padding: 4rpx 12rpx; }
.source-tag.credits { color: #9b5c00; background: #fff3d6; }
.pay-text { font-size: 22rpx; color: #6f7c82; }
.verify-strip {
	margin: 0 20rpx 16rpx;
	padding: 16rpx 18rpx;
	border-radius: 12rpx;
	background: #f7fbfa;
	display: flex;
	align-items: center;
	border: 1rpx dashed #b9d8d2;
}
.verify-label { font-size: 22rpx; color: #6f8580; margin-right: 16rpx; }
.verify-code { flex: 1; font-size: 36rpx; font-weight: 800; color: #1f7a72; letter-spacing: 6rpx; }
.verify-tip { font-size: 22rpx; color: #9aa7ac; }
.coupon-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1rpx solid #eef1f2; padding: 16rpx 20rpx; }
.order-time { font-size: 22rpx; color: #9aa7ac; }
.order-actions { display: flex; gap: 14rpx; }
.btn { width: 116rpx; height: 54rpx; line-height: 54rpx; text-align: center; font-size: 23rpx; border-radius: 27rpx; border: none; padding: 0; }
.btn::after { border: none; }
.btn-pay { background-color: #1f7a72; color: #fff; }
.btn-detail { background-color: #e8f6f3; color: #1f7a72; }
.btn-cancel { background-color: #f1f3f4; color: #889096; }
.btn-refund { background-color: #fff1e6; color: #c35f18; }
</style>
