<template>
	<view class="blog-card" @click="goDetail">
		<view class="blog-header">
			<user-avatar :src="blog.icon" :size="72" />
			<view class="blog-user">
				<text class="user-name">{{ blog.name || '用户' + blog.userId }}</text>
				<text class="blog-time">{{ formatTime(blog.createTime) }}</text>
			</view>
		</view>
		<view class="blog-content">
			<text class="blog-title">{{ blog.title }}</text>
			<text class="blog-desc ellipsis-2">{{ blog.content }}</text>
		</view>
		<view v-if="images.length" class="blog-images">
			<image
				v-for="(img, idx) in images"
				:key="idx"
				:src="img"
				mode="aspectFill"
				:class="['blog-img', images.length === 1 ? 'single' : '']"
				@click.stop="previewImage(idx)"
			/>
		</view>
		<view class="blog-footer">
			<view class="action-item">
				<uni-icons :type="blog.isLike ? 'heart-filled' : 'heart'" size="18" :color="blog.isLike ? '#ff6600' : '#999'" />
				<text :style="{ color: blog.isLike ? '#ff6600' : '#999' }">{{ blog.liked || 0 }}</text>
			</view>
			<view class="action-item">
				<uni-icons type="chatbubble" size="18" color="#999" />
				<text>{{ blog.comments || 0 }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getBlogImages, formatTime } from '@/utils/constants.js'

export default {
	name: 'BlogCard',
	props: {
		blog: {
			type: Object,
			default: () => ({})
		}
	},
	computed: {
		images() {
			return getBlogImages(this.blog.images).slice(0, 3)
		}
	},
	methods: {
		formatTime,
		goDetail() {
			uni.navigateTo({ url: '/pages/blog-detail/blog-detail?id=' + this.blog.id })
		},
		previewImage(idx) {
			uni.previewImage({
				urls: this.images,
				current: this.images[idx]
			})
		}
	}
}
</script>

<style scoped>
.blog-card {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 20rpx;
}

.blog-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.blog-user {
	margin-left: 16rpx;
	display: flex;
	flex-direction: column;
}

.user-name {
	font-size: 28rpx;
	font-weight: 600;
	color: #333333;
}

.blog-time {
	font-size: 22rpx;
	color: #999999;
	margin-top: 4rpx;
}

.blog-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333333;
	margin-bottom: 8rpx;
}

.blog-desc {
	font-size: 26rpx;
	color: #666666;
	line-height: 1.6;
}

.blog-images {
	display: flex;
	flex-wrap: wrap;
	margin-top: 16rpx;
	gap: 8rpx;
}

.blog-img {
	width: 220rpx;
	height: 220rpx;
	border-radius: 12rpx;
	background-color: #f0f0f0;
}

.blog-img.single {
	width: 460rpx;
	height: 320rpx;
}

.blog-footer {
	display: flex;
	margin-top: 20rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f5f5f5;
}

.action-item {
	display: flex;
	align-items: center;
	margin-right: 40rpx;
	font-size: 24rpx;
	color: #999999;
}

.action-item text {
	margin-left: 8rpx;
}
</style>
