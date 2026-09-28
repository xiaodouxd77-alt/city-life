<template>
	<view class="container safe-bottom">
		<nav-bar title="共同关注" :showBack="true"></nav-bar>

		<view class="content">
			<view v-if="users.length === 0 && !loading" class="empty-state">
				<uni-icons type="person" size="64" color="#ddd"></uni-icons>
				<text>暂无共同关注</text>
			</view>
			<view v-for="user in users" :key="user.id" class="user-item" @click="goUser(user.id)">
				<user-avatar :src="user.icon" :size="88" />
				<view class="user-info">
					<text class="uname">{{ user.nickName }}</text>
				</view>
				<uni-icons type="right" size="16" color="#ccc"></uni-icons>
			</view>
		</view>
	</view>
</template>

<script>
import { followCommons } from '@/api/follow.js'

export default {
	data() {
		return {
			users: [],
			loading: false
		}
	},
	onLoad(options) {
		if (options.id) {
			this.loadData(options.id)
		}
	},
	methods: {
		async loadData(id) {
			this.loading = true
			try {
				const res = await followCommons(id)
				if (res.success) {
					this.users = res.data || []
				}
			} catch (e) {} finally {
				this.loading = false
			}
		},
		goUser(id) {
			uni.navigateTo({ url: '/pages/user-profile/user-profile?id=' + id })
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

.user-item {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 16rpx;
	display: flex;
	align-items: center;
}

.user-info {
	flex: 1;
	margin-left: 24rpx;
}

.uname {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.safe-bottom {
	padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
}
</style>
