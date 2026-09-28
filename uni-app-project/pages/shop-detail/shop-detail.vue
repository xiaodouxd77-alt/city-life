<template>
	<view class="container">
		<!-- 导航栏 -->
		<view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-bar">
				<view class="nav-back" @click="goBack">
					<view class="back-circle"><uni-icons type="left" size="18" color="#333"></uni-icons></view>
				</view>
				<text class="nav-title ellipsis">{{ shop.name || '店铺详情' }}</text>
			</view>
		</view>

		<view v-if="loaded" class="content safe-bottom">
			<!-- 店铺图片 -->
			<swiper v-if="images.length" class="image-swiper" indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#ff6600">
				<swiper-item v-for="(img, idx) in images" :key="idx">
					<image :src="img" mode="aspectFill" class="swiper-img" @click="previewImage(idx)" />
				</swiper-item>
			</swiper>

			<!-- 基本信息 -->
			<view class="info-card card">
				<view class="shop-title-row">
					<text class="shop-name">{{ shop.name }}</text>
					<view class="fav-button" :class="{ active: isFav }" @click.stop="doFavorite">
						<uni-icons :type="isFav ? 'star-filled' : 'star'" size="18" :color="isFav ? '#ffffff' : '#ff6600'" />
						<text>{{ isFav ? '已收藏' : '收藏店铺' }}</text>
					</view>
				</view>
				<view class="rating-row">
					<view class="stars">
						<uni-icons v-for="i in 5" :key="i" type="star-filled" size="16" :color="i <= starCount ? '#ff6600' : '#e0e0e0'" />
					</view>
					<text class="score-text">{{ formatScore(shop.score) }}分</text>
					<text class="comments-text">{{ shop.comments || 0 }}篇笔记</text>
				</view>
				<view class="info-row">
					<text>人均 ¥{{ shop.avgPrice || 0 }}</text>
					<text>月售 {{ shop.sold || 0 }}</text>
				</view>
				<view class="detail-row">
					<uni-icons type="location-filled" size="16" color="#999"></uni-icons>
					<text class="detail-text">{{ shop.address || '' }}</text>
				</view>
				<view v-if="shop.openHours" class="detail-row">
					<uni-icons type="calendar" size="16" color="#999"></uni-icons>
					<text class="detail-text">营业时间：{{ shop.openHours }}</text>
				</view>
				<view v-if="shop.area" class="tag-row">
					<text class="tag">{{ shop.area }}</text>
				</view>
			</view>

			<!-- 优惠券 -->
			<view class="section" @click="goVouchers">
				<view class="section-header">
					<text class="section-title">优惠券</text>
					<view class="section-more">
						<text>查看更多</text>
						<uni-icons type="right" size="14" color="#999"></uni-icons>
					</view>
				</view>
				<view v-if="vouchers.length === 0" class="section-empty">
					<text>暂无可用优惠券</text>
				</view>
				<view v-else class="voucher-list">
					<view
						v-for="v in vouchers.slice(0, 3)"
						:key="v.id"
						class="voucher-item"
						@click.stop="buyVoucher(v)"
					>
						<view class="voucher-left">
							<text class="voucher-price"><text class="symbol">¥</text>{{ v.payValue / 100 || v.payValue }}</text>
							<text class="voucher-desc">满 ¥{{ v.actualValue / 100 || v.actualValue }}可用</text>
						</view>
						<view class="voucher-right">
							<text class="voucher-title">{{ v.title }}</text>
							<text class="voucher-sub">{{ v.subTitle || '' }}</text>
							<view class="voucher-btn">{{ v.type === 1 ? '立即抢购' : '立即购买' }}</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 网友笔记 -->
			<view class="section">
				<view class="section-header">
					<text class="section-title">网友笔记</text>
				</view>
				<view v-if="blogs.length === 0" class="section-empty">
					<text>暂无相关笔记</text>
				</view>
				<blog-card v-for="blog in blogs" :key="blog.id" :blog="blog" />
			</view>
		</view>

		<!-- 选择店铺按钮 -->
		<view v-if="selectMode && loaded" class="select-bar safe-bottom">
			<button class="select-shop-btn" @click="selectShop">选择该店铺</button>
		</view>
	</view>
</template>

<script>
import { getShopById, getShopsByType } from '@/api/shop.js'
import { getVouchersByShop, seckillVoucher, buyVoucher } from '@/api/voucher.js'
import { getBlogsByShop } from '@/api/blog.js'
import { toggleFavorite, isFavorite } from '@/api/favorite.js'
import { getShopImages, formatScore, formatDistance } from '@/utils/constants.js'
import { isLogin } from '@/utils/auth.js'

export default {
	data() {
		return {
			shop: {},
			images: [],
			vouchers: [],
			blogs: [],
			isFav: false,
			loaded: false,
			statusBarHeight: 0,
			selectMode: false
		}
	},
	computed: {
		starCount() {
			return Math.round((this.shop.score || 0) / 10)
		}
	},
	onLoad(options) {
		this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
		if (options.id) {
			this.loadShop(options.id)
		}
		if (options.select === '1') {
			this.selectMode = true
		}
	},
	methods: {
		formatScore,
		formatDistance,
		async loadShop(id) {
			this.loaded = false
			try {
				const shopRes = await getShopById(id)
				if (shopRes.success) {
					this.shop = shopRes.data
					this.images = getShopImages(this.shop.images)
					this.checkFavorite()
				} else {
					uni.showToast({ title: shopRes.errorMsg || '店铺不存在', icon: 'none' })
					return
				}
				this.loaded = true
				const [voucherRes, blogRes] = await Promise.all([
					getVouchersByShop(id).catch(() => ({ success: false, data: [] })),
					getBlogsByShop(id).catch(() => ({ success: false, data: [] }))
				])
				if (voucherRes.success) {
					this.vouchers = voucherRes.data || []
				}
				if (blogRes.success) {
					this.blogs = blogRes.data || []
				}
			} catch (e) {
				console.error('loadShop error:', e)
				uni.showToast({ title: '店铺加载失败', icon: 'none' })
			}
		},
		previewImage(idx) {
			uni.previewImage({ urls: this.images, current: this.images[idx] })
		},
		goVouchers() {
			if (this.shop && this.shop.id) {
				uni.navigateTo({ url: `/pages/voucher-list/voucher-list?shopId=${this.shop.id}` })
			}
		},
		async checkFavorite() {
			if (!isLogin() || !this.shop.id) return
			try { const res = await isFavorite('SHOP', this.shop.id); if (res.success) this.isFav = !!res.data } catch (e) {}
		},
		async doFavorite() {
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			if (!this.shop || !this.shop.id) { uni.showToast({ title: '店铺信息未加载完成', icon: 'none' }); return }
			try {
				const res = await toggleFavorite('SHOP', this.shop.id)
				if (res.success) { this.isFav = res.data; uni.showToast({ title: this.isFav ? '已收藏店铺' : '已取消收藏', icon: 'none' }) }
			} catch (e) {}
		},
		async buyVoucher(v) {
			if (!isLogin()) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			const isSeckill = v.type === 1
			const title = isSeckill ? '确认抢购' : '确认购买'
			const payAmount = (v.payValue / 100).toFixed(2) || '0.01'
			uni.showModal({
				title: title,
				content: `${v.title}\n¥${payAmount}`,
				success: async (r) => {
					if (!r.confirm) return
					uni.showLoading({ title: '创建订单中...' })
					try {
						const res = isSeckill
							? await seckillVoucher(v.id)
							: await buyVoucher(v.id)
						uni.hideLoading()
						if (res.success) {
							const orderId = res.data
							// 跳转到支付宝沙箱支付页面
							uni.navigateTo({ url: `/pages/pay/pay?orderId=${orderId}&amount=${payAmount}` })
						} else {
							uni.showToast({ title: res.errorMsg || '购买失败', icon: 'none' })
						}
					} catch (e) {
						uni.hideLoading()
						uni.showToast({ title: '网络错误，请重试', icon: 'none' })
					}
				}
			})
		},
		selectShop() {
			if (!this.shop || !this.shop.id) { uni.showToast({ title: '店铺信息未加载完成', icon: 'none' }); return }
			uni.$emit('selectShop', {
				id: this.shop.id,
				name: this.shop.name
			})
			// 页面栈: 发布页 → 搜索页 → 详情页，需要返回2级直接到发布页
			uni.navigateBack({ delta: 2 })
		},
		goBack() {
			const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: "/pages/home/home" })
		}
	},
	onReachBottom() {},
	onPullDownRefresh() {
		if (this.shop.id) {
			this.loadShop(this.shop.id).then(() => uni.stopPullDownRefresh())
		}
	}
}
</script>

<style scoped>
.container {
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background-color: #ffffff;
}

.nav-bar {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}

.nav-back {
	position: absolute;
	left: 20rpx;
	top: 50%;
	transform: translateY(-50%);
	padding: 10rpx;
}

.back-circle {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background-color: rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	max-width: 60%;
}

.content {
	margin-top: 0;
}

.image-swiper {
	width: 100%;
	height: 400rpx;
}

.swiper-img {
	width: 100%;
	height: 100%;
	background-color: #f0f0f0;
}

.card {
	margin: 20rpx;
	padding: 24rpx;
	background-color: #ffffff;
	border-radius: 16rpx;
}

.shop-title-row {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 16rpx;
}

.shop-name {
	flex: 1;
	font-size: 36rpx;
	font-weight: 700;
	color: #333;
	line-height: 1.35;
	word-break: break-all;
}

.fav-button {
	flex-shrink: 0;
	min-width: 168rpx;
	height: 64rpx;
	padding: 0 22rpx;
	border: 2rpx solid #ff6600;
	border-radius: 32rpx;
	background-color: #fff7f1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.16);
}

.fav-button text {
	font-size: 26rpx;
	font-weight: 600;
	color: #ff6600;
	line-height: 1;
}

.fav-button.active {
	background: linear-gradient(135deg, #ff6600, #ff8a32);
	border-color: transparent;
	box-shadow: 0 10rpx 24rpx rgba(255, 102, 0, 0.28);
}

.fav-button.active text {
	color: #ffffff;
}

.rating-row {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.stars {
	display: flex;
	gap: 4rpx;
	margin-right: 12rpx;
}

.score-text {
	font-size: 28rpx;
	color: #ff6600;
	font-weight: 600;
	margin-right: 16rpx;
}

.comments-text {
	font-size: 24rpx;
	color: #999;
}

.info-row {
	display: flex;
	gap: 32rpx;
	margin-bottom: 20rpx;
	font-size: 26rpx;
	color: #666;
}

.detail-row {
	display: flex;
	align-items: center;
	margin-bottom: 12rpx;
	gap: 8rpx;
}

.detail-text {
	font-size: 24rpx;
	color: #666;
	flex: 1;
}

.tag-row {
	margin-top: 8rpx;
	display: flex;
	flex-wrap: wrap;
}

.tag {
	display: inline-block;
	padding: 6rpx 20rpx;
	background-color: #fff5ef;
	color: #ff6600;
	font-size: 22rpx;
	border-radius: 8rpx;
}

.section {
	margin: 20rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.section-more {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #999;
}

.section-empty {
	padding: 40rpx;
	text-align: center;
	font-size: 26rpx;
	color: #999;
}

.voucher-item {
	background: linear-gradient(135deg, #ff6600, #ff8833);
	border-radius: 16rpx;
	padding: 24rpx;
	display: flex;
	margin-bottom: 16rpx;
}

.voucher-left {
	width: 180rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-right: 24rpx;
	border-right: 2rpx dashed rgba(255, 255, 255, 0.4);
}

.voucher-price {
	font-size: 48rpx;
	font-weight: 700;
	color: #ffffff;
}

.voucher-price .symbol {
	font-size: 28rpx;
	font-weight: 400;
}

.voucher-desc {
	font-size: 22rpx;
	color: rgba(255, 255, 255, 0.8);
	margin-top: 6rpx;
}

.voucher-right {
	flex: 1;
	display: flex;
	flex-direction: column;
	padding-left: 24rpx;
}

.voucher-title {
	font-size: 28rpx;
	color: #ffffff;
	font-weight: 600;
}

.voucher-sub {
	font-size: 22rpx;
	color: rgba(255, 255, 255, 0.7);
	margin-top: 8rpx;
}

.voucher-btn {
	align-self: flex-start;
	margin-top: 16rpx;
	padding: 10rpx 24rpx;
	background-color: rgba(255, 255, 255, 0.95);
	color: #ff6600;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 600;
}

/* 选择店铺按钮 */
.select-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 20rpx 32rpx;
	background-color: #ffffff;
	box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
	z-index: 200;
}

.select-shop-btn {
	width: 100%;
	height: 88rpx;
	line-height: 88rpx;
	background: linear-gradient(135deg, #ff6600, #ff8833);
	color: #fff;
	border-radius: 44rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
	padding: 0;
}

.select-shop-btn::after {
	border: none;
}

.safe-bottom {
	padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}
</style>
