<template>
	<view class="container safe-bottom">
		<nav-bar title="优惠券" :showBack="true"></nav-bar>

		<view class="content">
			<view v-if="vouchers.length === 0" class="empty-state">
				<uni-icons type="gift" size="64" color="#ddd"></uni-icons>
				<text>暂无可用优惠券</text>
			</view>
			<view v-for="v in vouchers" :key="v.id" class="voucher-card" :class="{ seckill: v.stock !== undefined }">
				<view class="voucher-body">
					<view class="voucher-left">
						<text class="price"><text class="symbol">¥</text>{{ v.payValue / 100 || v.payValue }}</text>
						<text class="condition">抵 ¥{{ v.actualValue / 100 || v.actualValue }}</text>
					</view>
					<view class="voucher-right">
						<text class="v-title">{{ v.title }}</text>
						<text class="v-sub">{{ v.subTitle || '' }}</text>
						<view v-if="v.stock !== undefined" class="seckill-info">
							<text class="stock">库存 {{ v.stock }}</text>
							<text v-if="v.beginTime" class="time">{{ v.beginTime }} ~ {{ v.endTime }}</text>
						</view>
					</view>
				</view>
				<view v-if="v.rules" class="voucher-rules">
					<uni-icons type="info-filled" size="14" color="#999"></uni-icons>
					<text>{{ v.rules }}</text>
				</view>
				<view class="voucher-footer">
					<view v-if="v.type === 1 && v.stock !== undefined" class="buy-btn" @click="doSeckill(v)">
						立即抢购
					</view>
					<view v-else class="got-btn" @click="doBuy(v)">领取</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getVouchersByShop, seckillVoucher, buyVoucher } from '@/api/voucher.js'
import { isLogin } from '@/utils/auth.js'

export default {
	data() {
		return {
			vouchers: [],
			shopId: 0,
			shopName: ''
		}
	},
	onLoad(options) {
		if (options.shopId) {
			this.shopId = options.shopId
			this.loadVouchers()
		}
	},
	methods: {
		async loadVouchers() {
			const res = await getVouchersByShop(this.shopId)
			if (res.success) {
				this.vouchers = res.data || []
			}
		},
		async doSeckill(v) {
			if (!isLogin()) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			const payAmount = (v.payValue / 100).toFixed(2) || '0.01'
			uni.showModal({
				title: '确认抢购',
				content: `${v.title} ¥${payAmount}`,
				success: async (r) => {
					if (!r.confirm) return
					uni.showLoading({ title: '创建订单中...' })
					try {
						const res = await seckillVoucher(v.id)
						uni.hideLoading()
						if (res.success) {
							const orderId = res.data
							uni.navigateTo({ url: `/pages/pay/pay?orderId=${orderId}&amount=${payAmount}` })
						} else {
							uni.showToast({ title: res.errorMsg || '抢购失败', icon: 'none' })
						}
					} catch (e) {
						uni.hideLoading()
						uni.showToast({ title: '网络错误，请重试', icon: 'none' })
					}
				}
			})
		},
		async doBuy(v) {
			if (!isLogin()) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			const payAmount = (v.payValue / 100).toFixed(2) || '0.01'
			uni.showModal({
				title: '确认领取',
				content: `${v.title}\n¥${payAmount}`,
				success: async (r) => {
					if (!r.confirm) return
					uni.showLoading({ title: '创建订单中...' })
					try {
						const res = await buyVoucher(v.id)
						uni.hideLoading()
						if (res.success) {
							const orderId = res.data
							uni.navigateTo({ url: `/pages/pay/pay?orderId=${orderId}&amount=${payAmount}` })
						} else {
							uni.showToast({ title: res.errorMsg || '领取失败', icon: 'none' })
						}
					} catch (e) {
						uni.hideLoading()
						uni.showToast({ title: '网络错误，请重试', icon: 'none' })
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.container {
	background-color: #f5f5f5;
	min-height: 100vh;
}

.content {
	padding: 20rpx;
}

.voucher-card {
	background-color: #ffffff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
}

.voucher-card.seckill {
	border-left: 6rpx solid #ff6600;
}

.voucher-body {
	display: flex;
	padding: 28rpx;
}

.voucher-left {
	width: 180rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	border-right: 2rpx dashed #f0f0f0;
	padding-right: 24rpx;
}

.price {
	font-size: 48rpx;
	font-weight: 700;
	color: #ff6600;
}

.price .symbol {
	font-size: 28rpx;
	font-weight: 400;
}

.condition {
	font-size: 24rpx;
	color: #999;
	margin-top: 6rpx;
}

.voucher-right {
	flex: 1;
	padding-left: 24rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.v-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.v-sub {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.seckill-info {
	display: flex;
	flex-direction: column;
	margin-top: 12rpx;
}

.stock {
	font-size: 22rpx;
	color: #ff6600;
}

.time {
	font-size: 20rpx;
	color: #999;
	margin-top: 4rpx;
}

.voucher-rules {
	padding: 16rpx 28rpx;
	background-color: #fffbf7;
	font-size: 22rpx;
	color: #999;
	display: flex;
	align-items: flex-start;
	gap: 8rpx;
}

.voucher-footer {
	padding: 16rpx 28rpx;
	display: flex;
	justify-content: flex-end;
}

.buy-btn {
	padding: 14rpx 40rpx;
	background: linear-gradient(135deg, #ff6600, #ff8833);
	color: #ffffff;
	border-radius: 32rpx;
	font-size: 26rpx;
}

.got-btn {
	padding: 14rpx 40rpx;
	background-color: #f5f5f5;
	color: #999;
	border-radius: 32rpx;
	font-size: 26rpx;
}
</style>
