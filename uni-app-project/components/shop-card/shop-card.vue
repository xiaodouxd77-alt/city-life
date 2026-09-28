<template>
	<view class="shop-card">
		<image class="shop-image" :src="firstImage" mode="aspectFill" @click="goDetail" />
		<view class="shop-info" @click="goDetail">
			<view class="shop-header">
				<text class="shop-name ellipsis">{{ shop.name }}</text>
				<text v-if="shop.distance !== undefined" class="shop-distance">{{ formatDistance(shop.distance) }}</text>
			</view>
			<view class="shop-meta">
				<text class="shop-score">{{ formatScore(shop.score) }}分</text>
				<text class="shop-sold">月售{{ shop.sold || 0 }}</text>
				<text class="shop-avg">人均 ¥{{ shop.avgPrice || 0 }}</text>
			</view>
			<view class="shop-area">
				<text class="ellipsis">{{ shop.area || shop.address || '' }}</text>
			</view>
			<view v-if="tags.length" class="shop-tags">
				<text v-for="(tag, idx) in tags" :key="idx" class="tag">{{ tag }}</text>
			</view>
		</view>
		<view v-if="selectMode" class="select-btn" @click.stop="selectThis">
			<text>选择</text>
		</view>
	</view>
</template>

<script>
import { getShopImages, formatScore, formatDistance } from '@/utils/constants.js'

export default {
	name: 'ShopCard',
	props: {
		shop: {
			type: Object,
			default: () => ({})
		},
		selectMode: {
			type: Boolean,
			default: false
		}
	},
	computed: {
		firstImage() {
			const imgs = getShopImages(this.shop.images)
			return imgs[0] || '/static/shop-default.svg'
		},
		tags() {
			if (!this.shop.openHours) return []
			return [this.shop.openHours]
		}
	},
	methods: {
		formatScore,
		formatDistance,
		goDetail() {
			if (this.selectMode) {
				uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + this.shop.id + '&select=1' })
			} else {
				uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + this.shop.id })
			}
		},
		selectThis() {
			this.$emit('select', this.shop)
		}
	}
}
</script>

<style scoped>
.shop-card {
	display: flex;
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
	position: relative;
}

.shop-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 12rpx;
	margin-right: 20rpx;
	background-color: #f0f0f0;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	overflow: hidden;
}

.shop-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.shop-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333333;
	max-width: 70%;
}

.shop-distance {
	font-size: 24rpx;
	color: #999999;
}

.shop-meta {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #666666;
	margin-top: 10rpx;
}

.shop-score {
	color: #ff6600;
	font-weight: 600;
	margin-right: 16rpx;
}

.shop-sold {
	margin-right: 16rpx;
}

.shop-area {
	font-size: 24rpx;
	color: #999999;
	margin-top: 10rpx;
}

.shop-tags {
	margin-top: 10rpx;
}

.select-btn {
	position: absolute;
	right: 20rpx;
	bottom: 20rpx;
	background: linear-gradient(135deg, #ff6600, #ff8833);
	color: #fff;
	font-size: 24rpx;
	padding: 10rpx 28rpx;
	border-radius: 20rpx;
}
</style>
