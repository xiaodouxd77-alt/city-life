<template>
	<view class="tb-wrap">
		<view class="tb-bar">
			<view class="tb-item" @click="goTab('/pages/home/home')">
				<uni-icons type="home" size="24" :color="current === '/pages/home/home' ? '#ff6600' : '#999'" />
				<text class="tb-text" :class="{ on: current === '/pages/home/home' }">首页</text>
			</view>
			<view class="tb-item" @click="goTab('/pages/nearby/nearby')">
				<uni-icons type="location" size="24" :color="current === '/pages/nearby/nearby' ? '#ff6600' : '#999'" />
				<text class="tb-text" :class="{ on: current === '/pages/nearby/nearby' }">附近</text>
			</view>
			<view class="tb-item tb-center" @click="goPublish">
				<view class="tb-pub">
					<image
						src="https://my-project3.oss-cn-beijing.aliyuncs.com/fabu.png"
						mode="aspectFit"
						class="pub-img"
					/>
				</view>
				<text class="tb-text pub-label">发布</text>
			</view>
			<view class="tb-item" @click="goTab('/pages/messages/messages')">
				<view class="message-icon-wrap">
					<uni-icons type="chat" size="24" :color="current === '/pages/messages/messages' ? '#ff6600' : '#999'" />
					<view v-if="unreadCount > 0" class="notification-dot"></view>
				</view>
				<text class="tb-text" :class="{ on: current === '/pages/messages/messages' }">消息</text>
			</view>
			<view class="tb-item" @click="goTab('/pages/me/me')">
				<uni-icons type="person" size="24" :color="current === '/pages/me/me' ? '#ff6600' : '#999'" />
				<text class="tb-text" :class="{ on: current === '/pages/me/me' }">我的</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { isLogin } from '@/utils/auth.js'
	import { getUnreadCount } from '@/api/notification.js'

export default {
	name: 'TabBar',
	props: {
		current: { type: String, default: '/pages/home/home' }
	},
	data() {
		return { unreadCount: 0 }
	},
	mounted() {
		uni.$on('notificationReceived', this.refreshUnreadCount)
		uni.$on('notificationChanged', this.refreshUnreadCount)
		this.refreshUnreadCount()
	},
	beforeDestroy() {
		uni.$off('notificationReceived', this.refreshUnreadCount)
		uni.$off('notificationChanged', this.refreshUnreadCount)
	},
	beforeUnmount() {
		uni.$off('notificationReceived', this.refreshUnreadCount)
		uni.$off('notificationChanged', this.refreshUnreadCount)
	},
	methods: {
		async refreshUnreadCount() {
			if (!isLogin()) {
				this.unreadCount = 0
				return
			}
			try {
				const res = await getUnreadCount()
				if (res.success) this.unreadCount = Number(res.data || 0)
			} catch (e) {}
		},
		goTab(url) {
			uni.switchTab({ url })
		},
		goPublish() {
			if (!uni.getStorageSync('token')) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			uni.navigateTo({ url: '/pages/publish-blog/publish-blog' })
		}
	}
}
</script>

<style>
.tb-wrap {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	z-index: 999;
}
.tb-bar {
	display: flex;
	align-items: center;
	justify-content: space-around;
	height: calc(110rpx + env(safe-area-inset-bottom));
	padding-bottom: env(safe-area-inset-bottom);
	background: #fff;
	border-top: 1rpx solid #eee;
	box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.05);
}
.tb-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 110rpx;
}
.tb-item .uni-icons {
	line-height: 1;
	margin-bottom: 4rpx;
}
.message-icon-wrap { position: relative; height: 28px; display: flex; align-items: center; }
.notification-dot {
	position: absolute;
	top: 0;
	right: -5px;
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: #ef476f;
	border: 2rpx solid #fff;
}
.tb-text {
	font-size: 24rpx;
	color: #999;
	font-weight: 500;
}
.tb-text.on {
	color: #ff6600;
	font-weight: 700;
}
.tb-center {
	justify-content: flex-end;
	padding-bottom: 8rpx;
}
.tb-pub {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #ff6600, #ff8833);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 6rpx 20rpx rgba(255,102,0,0.35);
	margin-bottom: 6rpx;
}
.pub-img {
	width: 44rpx;
	height: 44rpx;
}
.pub-label {
	font-size: 22rpx;
	color: #ff6600;
	margin-top: 0;
	font-weight: 600;
}
</style>
