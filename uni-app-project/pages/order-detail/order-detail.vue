<template>
	<view class="container">
		<nav-bar title="优惠券详情" :back="true"></nav-bar>

		<view class="card" v-if="order">
			<view class="status-area">
				<view class="status-icon" :class="statusClass(order.status)">
					<uni-icons class="icon-text" :type="statusIcon(order.status)" size="36" :color="statusColor(order.status)" />
				</view>
				<text class="status-text">{{ statusText(order.status) }}</text>
			</view>

			<view class="verify-section" v-if="order.status === 2 && order.verifyCode">
				<text class="verify-label">核销码</text>
				<text class="verify-code">{{ order.verifyCode }}</text>
				<text class="verify-tip">到店出示此核销码即可使用</text>
			</view>

			<view class="info-section">
				<view class="info-row"><text class="label">适用商家</text><text class="value">{{ order.useScope || '适用范围待确认' }}</text></view>
				<view class="info-title">订单信息</view>
				<view class="info-row"><text class="label">优惠券</text><text class="value">{{ order.voucherTitle || '-' }}</text></view>
				<view class="info-row" v-if="order.voucherRules"><text class="label">使用规则</text><text class="value">{{ order.voucherRules }}</text></view>
				<view class="info-row"><text class="label">面值</text><text class="value price">¥{{ money(order.voucherValue) }}</text></view>
				<view class="info-row"><text class="label">来源</text><text class="value">{{ order.fromCredits ? '积分兑换' : '商家购买' }}</text></view>
				<view class="info-row"><text class="label">实付</text><text class="value price">{{ order.fromCredits ? '积分兑换' : '¥' + money(order.voucherPayValue) }}</text></view>
				<view class="info-row"><text class="label">订单编号</text><text class="value small">{{ order.id }}</text></view>
				<view class="info-row"><text class="label">创建时间</text><text class="value">{{ order.createTime || '-' }}</text></view>
				<view class="info-row" v-if="order.payTime"><text class="label">支付时间</text><text class="value">{{ order.payTime }}</text></view>
				<view class="info-row" v-if="order.useTime"><text class="label">核销时间</text><text class="value">{{ order.useTime }}</text></view>
				<view class="info-row" v-if="order.refundTime"><text class="label">退款时间</text><text class="value">{{ order.refundTime }}</text></view>
			</view>
		</view>

		<view class="btn-area" v-if="order">
			<view v-if="order.status === 1">
				<button class="action-btn cancel-btn" @click="handleCancel">取消订单</button>
				<button class="action-btn pay-btn" @click="handlePay">立即支付</button>
			</view>
			<view v-if="order.status === 2 && !order.fromCredits">
				<button class="action-btn refund-btn" @click="handleRefund">申请退款</button>
			</view>
		</view>
	</view>
</template>

<script>
import { getOrderDetail, payOrder, cancelOrder, refundOrder } from '@/api/voucher.js'

export default {
	data() { return { order: null, orderId: '' } },
	onLoad(options) { this.orderId = options.id; this.loadDetail() },
	methods: {
		async loadDetail() {
			try { const res = await getOrderDetail(this.orderId); if (res.success) this.order = res.data } catch (e) {}
		},
		money(value) { return ((Number(value) || 0) / 100).toFixed(0) },
		statusText(s) { return {1:'待支付',2:'可使用',3:'已核销',4:'已取消',5:'退款中',6:'已退款'}[s]||'未知' },
		statusClass(s) { return s===1?'warning':s===2?'success':s===3?'info':'default' },
		statusIcon(s) { return {1:'spinner-cycle',2:'checkmarkempty',3:'checkbox-filled',4:'closeempty',5:'spinner-cycle',6:'closeempty'}[s]||'more' },
		statusColor(s) { return {1:'#ff8800',2:'#00b578',3:'#3b7cff',4:'#999',5:'#ff8800',6:'#999'}[s]||'#999' },
		async handlePay() {
			uni.showActionSheet({
				itemList: ['余额支付', '支付宝', '微信支付'],
				success: async (r) => {
					const payType = r.tapIndex + 1
					uni.showLoading({ title: '支付中...' })
					try {
						const res = await payOrder(this.orderId, payType)
						uni.hideLoading()
						if (res.success) {
							uni.showToast({ title: res.data || '支付成功', icon: 'success', duration: 2000 })
							setTimeout(() => this.loadDetail(), 800)
						} else {
							uni.showToast({ title: res.errorMsg || '支付失败', icon: 'none' })
						}
					} catch (e) { uni.hideLoading() }
				}
			})
		},
		async handleCancel() {
			const r = await new Promise(r => uni.showModal({ title:'提示',content:'确定取消该订单吗？',success:e=>r(e.confirm) }))
			if (!r) return
			try { const res = await cancelOrder(this.orderId); if (res.success) { uni.showToast({title:'已取消',icon:'success'}); this.loadDetail() } else uni.showToast({title:res.errorMsg||'失败',icon:'none'}) } catch (e) {}
		},
		async handleRefund() {
			const r = await new Promise(r => uni.showModal({ title:'提示',content:'确定申请退款吗？',success:e=>r(e.confirm) }))
			if (!r) return
			try { const res = await refundOrder(this.orderId); if (res.success) { uni.showToast({title:'退款成功',icon:'success'}); this.loadDetail() } else uni.showToast({title:res.errorMsg||'失败',icon:'none'}) } catch (e) {}
		}
	}
}
</script>

<style scoped>
.container { background-color: #f5f5f5; min-height: 100vh; }
.card { background-color: #fff; margin: 20rpx; border-radius: 12rpx; overflow: hidden; }
.status-area { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0 30rpx; background: linear-gradient(135deg, #fff5ef, #fff); }
.status-icon { width: 80rpx; height: 80rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 16rpx; }
.status-icon.warning { background-color: #fff0e0; }
.status-icon.success { background-color: #e6f9f0; }
.status-icon.info { background-color: #e6f0ff; }
.status-icon.default { background-color: #f5f5f5; }
.icon-text { font-size: 36rpx; }
.status-text { font-size: 30rpx; font-weight: 600; color: #333; }
.verify-section { margin: 0 24rpx 24rpx; padding: 24rpx; background-color: #fff8f0; border-radius: 8rpx; text-align: center; }
.verify-label { font-size: 24rpx; color: #999; display: block; }
.verify-code { font-size: 48rpx; font-weight: 700; color: #ff6600; letter-spacing: 8rpx; margin: 12rpx 0; display: block; }
.verify-tip { font-size: 22rpx; color: #999; }
.info-section { padding: 24rpx; }
.info-title { font-size: 28rpx; font-weight: 600; color: #333; margin-bottom: 16rpx; }
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.label { font-size: 26rpx; color: #999; }
.value { font-size: 26rpx; color: #333; }
.value.price { color: #ff6600; font-weight: 600; }
.value.small { font-size: 22rpx; color: #999; word-break: break-all; }
.btn-area { padding: 30rpx 20rpx; }
.action-btn { width: 100%; height: 88rpx; line-height: 88rpx; text-align: center; border-radius: 44rpx; font-size: 30rpx; border: none; margin-bottom: 20rpx; }
.action-btn::after { border: none; }
.pay-btn { background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; }
.cancel-btn { background-color: #f5f5f5; color: #999; }
.refund-btn { background-color: #f5f5f5; color: #ff6600; }
</style>
