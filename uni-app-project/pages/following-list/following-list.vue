<template>
	<view class="container safe-bottom">
		<nav-bar title="我的关注" :showBack="true"></nav-bar>
		<view class="content">
			<empty v-if="!loading && users.length === 0" text="暂无关注用户"></empty>
			<view v-for="user in users" :key="user.id" class="user-item">
				<view class="user-main" @click="goUser(user.id)">
					<user-avatar :src="user.icon" :size="88" />
					<view class="user-info">
						<text class="uname">{{ user.nickName || '用户' }}</text>
						<text class="hint">已关注</text>
					</view>
				</view>
				<button class="unfollow-btn" @click.stop="unfollow(user)">取消关注</button>
			</view>
		</view>
	</view>
</template>

<script>
import { getMyFollows, follow } from '@/api/follow.js'

export default {
	data() {
		return { users: [], loading: false }
	},
	onShow() { this.loadUsers() },
	methods: {
		async loadUsers() {
			this.loading = true
			try {
				const res = await getMyFollows()
				if (res.success) this.users = res.data || []
			} finally { this.loading = false }
		},
		goUser(id) {
			uni.navigateTo({ url: '/pages/user-profile/user-profile?id=' + id })
		},
		unfollow(user) {
			uni.showModal({
				title: '取消关注',
				content: '确定不再关注该用户吗？',
				success: async ({ confirm }) => {
					if (!confirm) return
					const res = await follow(user.id, false)
					if (res.success) {
						this.users = this.users.filter(item => item.id !== user.id)
						uni.$emit('followChanged')
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
.user-item { min-height: 128rpx; margin-bottom: 16rpx; padding: 20rpx 22rpx; border-radius: 12rpx; background: #fff; display: flex; align-items: center; justify-content: space-between; }
.user-main { min-width: 0; flex: 1; display: flex; align-items: center; }
.user-info { min-width: 0; margin-left: 20rpx; display: flex; flex-direction: column; }
.uname { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 29rpx; font-weight: 600; color: #263238; }
.hint { margin-top: 7rpx; font-size: 22rpx; color: #829096; }
.unfollow-btn { width: 142rpx; height: 58rpx; line-height: 58rpx; padding: 0; border: 1rpx solid #c9d2d6; border-radius: 29rpx; background: #fff; color: #58686e; font-size: 22rpx; }
.unfollow-btn::after { border: none; }
.safe-bottom { padding-bottom: calc(24rpx + env(safe-area-inset-bottom)); }
</style>
