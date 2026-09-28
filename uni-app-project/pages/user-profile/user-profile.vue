<template>
	<view class="container safe-bottom">
		<view class="header" :style="{ paddingTop: statusBarH + 'px' }">
			<view class="nav-bar">
				<view class="nav-back" @click="goBack"><view class="back-circle"><uni-icons type="left" size="18" color="#333"></uni-icons></view></view>
				<text class="nav-title">{{ isMy ? '我的' : '个人主页' }}</text>
			</view>
		</view>

		<view v-if="loaded" class="content">
			<view class="profile-card">
				<user-avatar :src="info.icon || ''" :size="120" />
				<text class="profile-name">{{ info.nickName || ('用户' + userId) }}</text>
				<view class="profile-stats">
				<view class="stat" :class="{ tappable: isMy }" @click="goMyFollows"><text class="stat-num">{{ userDetail.followee || 0 }}</text><text class="stat-label">关注</text></view>
					<view class="stat"><text class="stat-num">{{ userDetail.fans || 0 }}</text><text class="stat-label">粉丝</text></view>
				</view>
				<view v-if="!isMy" class="profile-actions">
					<button :class="['profile-follow', followed ? 'followed' : '']" @click="doFollow">{{ followed ? '已关注' : '+ 关注' }}</button>
					<button class="profile-common" @click="goFollowCommons">共同关注</button>
				</view>
			</view>

			<view v-if="userDetail.introduce || userDetail.city" class="detail-card">
				<view v-if="userDetail.introduce" class="detail-row"><uni-icons type="chatbubble" size="16" color="#999"></uni-icons><text>{{ userDetail.introduce }}</text></view>
				<view v-if="userDetail.city" class="detail-row"><uni-icons type="location" size="16" color="#999"></uni-icons><text>{{ userDetail.city }}</text></view>
			</view>

			<view class="section">
				<text class="section-title">{{ isMy ? '我的笔记' : 'TA的笔记' }}</text>
				<view v-if="blogs.length === 0 && !blogLoading" class="empty-state"><uni-icons type="compose" size="48" color="#ddd"></uni-icons><text>暂无笔记</text></view>
				<blog-card v-for="blog in blogs" :key="blog.id" :blog="blog" />
				<view v-if="blogLoading" class="loading-tip"><uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons><text>加载中...</text></view>
			</view>
		</view>
	</view>
</template>

<script>
import { getUserById, getUserInfo } from '@/api/user.js'
import { getBlogsByUser } from '@/api/blog.js'
import { isFollow, follow } from '@/api/follow.js'
import { getUserBadges } from '@/api/credits.js'
import { isLogin, getUserInfo as getMyInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			userId: 0, info: {}, userDetail: {}, blogs: [],
			blogLoading: false, followed: false, loaded: false,
			isMy: false, statusBarH: 0,
			creditsInfo: null
		}
	},
	onLoad(options) {
		this.statusBarH = uni.getSystemInfoSync().statusBarHeight || 0
		if (options.id) {
			this.userId = Number(options.id)
			const myInfo = getMyInfo()
			this.isMy = myInfo && Number(myInfo.id) === this.userId
			this.loadData()
		}
	},
	onShow() {
		if (this.loaded && this.userId) this.loadData()
	},
	methods: {
		async loadData() {
			try {
				const [userRes, detailRes, blogRes] = await Promise.all([
					getUserById(this.userId), getUserInfo(this.userId), getBlogsByUser(this.userId)
				])
				if (userRes.success && userRes.data) {
					this.info = userRes.data
				}
				if (detailRes.success && detailRes.data) this.userDetail = detailRes.data
				if (blogRes.success) this.blogs = blogRes.data || []
				const br = await getUserBadges(this.userId)
				if (br.success) {
					const earned = br.data || []
					this.creditsInfo = { totalBadges: earned.length, credits: detailRes?.data?.credits, totalSignDays: detailRes?.data?.totalSignDays }
				}
				if (!this.isMy && isLogin()) this.checkFollow()
				this.loaded = true
			} catch (e) { this.loaded = true }
		},
		async checkFollow() {
			const res = await isFollow(this.userId)
			if (res.success) this.followed = res.data
		},
		async doFollow() {
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			const res = await follow(this.userId, !this.followed)
			if (res.success) {
				this.followed = !this.followed
				uni.$emit('followChanged')
			}
		},
		goFollowCommons() {
			uni.navigateTo({ url: '/pages/follow-list/follow-list?id=' + this.userId })
		},
		goMyFollows() {
			if (this.isMy) uni.navigateTo({ url: '/pages/following-list/following-list' })
		},
		goBadges() {
			uni.navigateTo({ url: '/pages/badges/badges?userId=' + this.userId })
		},
		goBack() { const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: '/pages/me/me' }) }
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; }
.header { position: sticky; top: 0; z-index: 100; background: #fff; }
.nav-bar { height: 88rpx; display: flex; align-items: center; justify-content: center; position: relative; }
.nav-back { position: absolute; left: 20rpx; top: 50%; transform: translateY(-50%); padding: 10rpx; }
.back-circle { width: 60rpx; height: 60rpx; border-radius: 50%; background: rgba(0,0,0,.05); display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 32rpx; font-weight: 600; }
.profile-card { background: #fff; padding: 40rpx; display: flex; flex-direction: column; align-items: center; }
.profile-name { font-size: 34rpx; font-weight: 600; color: #333; margin-top: 20rpx; }
.profile-stats { display: flex; gap: 60rpx; margin-top: 24rpx; }
.stat { display: flex; flex-direction: column; align-items: center; }
.stat.tappable { padding: 12rpx 18rpx; margin: -12rpx -18rpx; }
.stat-num { font-size: 32rpx; font-weight: 700; color: #333; }
.stat-label { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.profile-actions { display: flex; gap: 20rpx; margin-top: 24rpx; }
.profile-follow { width: 180rpx; height: 64rpx; line-height: 64rpx; background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; border-radius: 32rpx; font-size: 26rpx; padding: 0; border: none; }
.profile-follow::after { border: none; }
.profile-follow.followed { background: #f0f0f0; color: #999; }
.profile-common { width: 180rpx; height: 64rpx; line-height: 64rpx; background: #fff; color: #ff6600; border: 2rpx solid #ff6600; border-radius: 32rpx; font-size: 26rpx; padding: 0; }
.profile-common::after { border: none; }
.detail-card { background: #fff; margin: 20rpx; border-radius: 16rpx; padding: 24rpx; }
.detail-row { display: flex; align-items: center; gap: 10rpx; font-size: 26rpx; color: #666; padding: 12rpx 0; }
.section { padding: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 600; color: #333; margin-bottom: 16rpx; display: block; }
.loading-tip { display: flex; align-items: center; justify-content: center; padding: 30rpx; font-size: 24rpx; color: #999; gap: 12rpx; }
.safe-bottom { padding-bottom: calc(120rpx + env(safe-area-inset-bottom)); }
.profile-credits {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 0;
	margin-top: 20rpx;
	width: 100%;
}
.pc-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	flex: 1;
	padding: 8rpx 0;
}
.pc-num { font-size: 30rpx; font-weight: 700; color: #ff6600; }
.pc-label { font-size: 22rpx; color: #999; margin-top: 4rpx; }
.pc-divider { width: 2rpx; height: 32rpx; background: #eee; }

</style>
