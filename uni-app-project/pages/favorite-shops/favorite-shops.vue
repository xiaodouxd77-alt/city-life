<template>
	<view class="container safe-bottom">
		<nav-bar title="收藏店铺" :showBack="true"></nav-bar>
		<view class="content">
			<empty v-if="!loading && shops.length === 0" text="暂无收藏店铺"></empty>
			<view v-for="shop in shops" :key="shop.id" class="shop-wrap">
				<shop-card :shop="shop" />
				<view class="shop-actions">
					<button class="cancel-btn" @click="cancelFavorite(shop)">取消收藏</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getMyFavoriteShops, toggleFavorite } from '@/api/favorite.js'

export default {
	data() {
		return { shops: [], loading: false }
	},
	onShow() { this.loadShops() },
	methods: {
		async loadShops() {
			this.loading = true
			try {
				const res = await getMyFavoriteShops()
				if (res.success) this.shops = res.data || []
			} finally { this.loading = false }
		},
		cancelFavorite(shop) {
			uni.showModal({
				title: '取消收藏',
				content: '确定取消收藏该店铺吗？',
				success: async ({ confirm }) => {
					if (!confirm) return
					const res = await toggleFavorite('SHOP', shop.id)
					if (res.success && res.data === false) {
						this.shops = this.shops.filter(item => item.id !== shop.id)
					} else {
						uni.showToast({ title: res.errorMsg || '操作失败', icon: 'none' })
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.container { min-height: 100vh; background: #f4f7f8; }
.content { padding: 20rpx; }
.shop-wrap { overflow: hidden; margin-bottom: 20rpx; border-radius: 12rpx; background: #fff; box-shadow: 0 8rpx 24rpx rgba(26, 50, 58, .05); }
.shop-wrap :deep(.shop-card) { margin-bottom: 0; border-radius: 0; }
.shop-actions { display: flex; justify-content: flex-end; padding: 0 20rpx 16rpx; border-top: 1rpx solid #eef1f2; }
.cancel-btn { width: 142rpx; height: 58rpx; line-height: 58rpx; padding: 0; border: 1rpx solid #c9d2d6; border-radius: 29rpx; background: #fff; color: #58686e; font-size: 22rpx; }
.cancel-btn::after { border: none; }
.safe-bottom { padding-bottom: calc(24rpx + env(safe-area-inset-bottom)); }
</style>
