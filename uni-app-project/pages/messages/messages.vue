<template>
	<view class="container safe-bottom">
		<view class="header-fixed">
			<view class="status-bar"></view>
			<view class="nav-row">
				<text class="nav-title">消息</text>
				<text v-if="unreadCount > 0" class="read-all" @click="markAllAsRead">全部已读</text>
			</view>
		</view>

		<view v-if="!isLogin" class="empty-wrap">
			<uni-icons class="empty-icon" type="chat" size="64" color="#ddd" />
			<text class="empty-text">登录后查看消息</text>
			<view class="login-btn" @click="goLogin">去登录</view>
		</view>

		<view v-else-if="loading" class="empty-wrap"><text class="empty-text">加载中...</text></view>
		<view v-else-if="notifications.length === 0" class="empty-wrap">
			<uni-icons class="empty-icon" type="notification" size="64" color="#ddd" />
			<text class="empty-text">暂无消息通知</text>
		</view>

		<view v-else class="message-list">
			<view v-for="item in notifications" :key="item.id" class="msg-item" :class="{ unread: item.isRead === 0 }" @click="openNotification(item)">
				<view class="type-icon" :class="typeClass(item.type)">
					<uni-icons :type="notificationIcon(item.type)" size="28" color="#fff" />
				</view>
				<view class="msg-body">
					<view class="msg-title-row">
						<text class="msg-type">{{ notificationType(item.type) }}</text>
						<view v-if="item.isRead === 0" class="item-dot"></view>
					</view>
					<text class="msg-content">{{ item.content }}</text>
					<text class="msg-time">{{ formatTime(item.createTime) }}</text>
				</view>
				<uni-icons v-if="item.targetId" type="right" size="16" color="#c7cdd0" />
			</view>
		</view>
	</view>
	<tab-bar current="/pages/messages/messages" />
</template>

<script>
import { getNotifications, markRead, markAllRead } from '@/api/notification.js'
import { isLogin as checkLogin } from '@/utils/auth.js'
import { formatTime } from '@/utils/constants.js'

export default {
	data() {
		return { isLogin: false, loading: false, notifications: [] }
	},
	computed: {
		unreadCount() {
			return this.notifications.filter(item => item.isRead === 0).length
		}
	},
	onLoad() {
		uni.$on('notificationReceived', this.handleNotificationReceived)
	},
	onUnload() {
		uni.$off('notificationReceived', this.handleNotificationReceived)
	},
	onShow() {
		this.isLogin = checkLogin()
		if (this.isLogin) this.loadNotifications()
		else this.notifications = []
	},
	methods: {
		formatTime,
		async loadNotifications() {
			this.loading = true
			try {
				const res = await getNotifications()
				if (res.success) this.notifications = res.data || []
			} catch (e) {} finally { this.loading = false }
		},
		handleNotificationReceived() {
			if (this.isLogin) this.loadNotifications()
		},
		notificationType(type) {
			return { LIKE: '点赞通知', COMMENT: '评论通知', REPLY: '回复通知', FOLLOW: '关注通知', SYSTEM: '系统通知' }[type] || '消息通知'
		},
		notificationIcon(type) {
			return { LIKE: 'heart-filled', COMMENT: 'chat-filled', REPLY: 'chat-filled', FOLLOW: 'personadd', SYSTEM: 'sound-filled' }[type] || 'notification-filled'
		},
		typeClass(type) {
			return 'type-' + String(type || 'SYSTEM').toLowerCase()
		},
		async markOneAsRead(item) {
			if (item.isRead !== 0) return
			try {
				const res = await markRead(item.id)
				if (res.success) {
					item.isRead = 1
					uni.$emit('notificationChanged')
				}
			} catch (e) {}
		},
		async markAllAsRead() {
			try {
				const res = await markAllRead()
				if (res.success) {
					this.notifications.forEach(item => { item.isRead = 1 })
					uni.$emit('notificationChanged')
				}
			} catch (e) {}
		},
		async openNotification(item) {
			await this.markOneAsRead(item)
			if (item.targetId) uni.navigateTo({ url: '/pages/blog-detail/blog-detail?id=' + item.targetId })
		},
		goLogin() { uni.navigateTo({ url: '/pages/login/login' }) }
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; }
.header-fixed { position: sticky; top: 0; z-index: 200; background: #fff; }
.nav-row { height: 88rpx; display: flex; align-items: center; justify-content: center; position: relative; border-bottom: 1rpx solid #f0f0f0; }
.nav-title { font-size: 34rpx; font-weight: 700; color: #333; }
.read-all { position: absolute; right: 24rpx; color: #ff6600; font-size: 26rpx; }
.message-list { padding: 20rpx; }
.msg-item { min-height: 106rpx; display: flex; align-items: center; background: #fff; padding: 24rpx; margin-bottom: 12rpx; border-radius: 12rpx; gap: 18rpx; }
.msg-item.unread { background: #fffaf6; }
.type-icon { width: 72rpx; height: 72rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: #839198; }
.type-like { background: #ef476f; }
.type-comment, .type-reply { background: #2364aa; }
.type-follow { background: #2d7d74; }
.type-system { background: #e08a24; }
.msg-body { min-width: 0; flex: 1; display: flex; flex-direction: column; }
.msg-title-row { display: flex; align-items: center; gap: 10rpx; }
.msg-type { font-size: 27rpx; color: #263238; font-weight: 700; }
.item-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #ef476f; }
.msg-content { font-size: 25rpx; color: #5b6a70; margin-top: 6rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 21rpx; color: #9aa7ab; margin-top: 8rpx; }
.empty-wrap { display: flex; flex-direction: column; align-items: center; padding: 150rpx 0; }
.empty-icon { display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.login-btn { margin-top: 32rpx; padding: 16rpx 60rpx; background: #ff6600; color: #fff; border-radius: 40rpx; font-size: 28rpx; }
</style>
