<template>
	<view class="container safe-bottom">
		<nav-bar title="勋章墙" :back="true"></nav-bar>

		<!-- 勋章统计 -->
		<view class="stats-card">
			<text class="stats-title">{{ isMy ? "我的勋章" : "TA的勋章" }}</text>
			<text class="stats-count">{{ badges.length }} / {{ allBadges.length }} 枚</text>
		</view>

		<!-- 已获得勋章 -->
		<view class="section-title">已获得</view>
		<view v-if="badges.length === 0" class="empty-state">
			<uni-icons type="star" size="48" color="#ddd"></uni-icons>
			<text class="empty-text">还没有获得勋章，继续加油！</text>
		</view>
		<view v-else class="badge-grid">
			<view class="badge-item" v-for="b in badges" :key="b.id">
				<view class="badge-icon earned">
					<text class="badge-emoji">{{ b.iconText || "\u2764\ufe0f" }}</text>
				</view>
				<text class="badge-name">{{ b.name }}</text>
				<text class="badge-desc">{{ b.description }}</text>
			</view>
		</view>

		<!-- 未获得勋章 -->
		<view class="section-title" v-if="lockedBadges.length > 0">未解锁</view>
		<view v-if="lockedBadges.length > 0" class="badge-grid">
			<view class="badge-item" v-for="b in lockedBadges" :key="b.id">
				<view class="badge-icon locked">
					<text class="badge-emoji dim">?</text>
				</view>
				<text class="badge-name dim">{{ b.name }}</text>
				<text class="badge-desc">{{ b.description }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getMyBadges, getUserBadges, getAllBadges } from "@/api/credits.js"
import { isLogin as checkLogin, getUserInfo } from "@/utils/auth.js"

export default {
	data() {
		return {
			badges: [],
			allBadges: [],
			isMy: true
		}
	},
	async onLoad(options) {
		if (options.userId) {
			this.isMy = checkLogin() && Number(getUserInfo()?.id) === Number(options.userId)
			const res = await getUserBadges(options.userId)
			if (res.success) this.badges = res.data || []
		} else {
			const res = await getMyBadges()
			if (res.success) this.badges = res.data || []
		}
		const allRes = await getAllBadges()
		if (allRes.success) this.allBadges = allRes.data || []

		// 如果是自己的页面，也加载所有勋章
		if (options.userId && !this.isMy) {
			const allRes2 = await getAllBadges()
			if (allRes2.success) this.allBadges = allRes2.data || []
		}
	},
	computed: {
		lockedBadges() {
			const ownedIds = new Set(this.badges.map(b => b.id))
			return this.allBadges.filter(b => !ownedIds.has(b.id))
		}
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; }
.safe-bottom { padding-bottom: env(safe-area-inset-bottom); }

.stats-card {
	background: linear-gradient(135deg, #667eea, #764ba2);
	margin: 20rpx;
	border-radius: 20rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.stats-title { font-size: 32rpx; font-weight: 700; color: #fff; }
.stats-count { font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 10rpx; }

.section-title { font-size: 28rpx; font-weight: 600; color: #333; padding: 20rpx 24rpx 12rpx; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 60rpx 0; }
.empty-text { font-size: 26rpx; color: #ccc; margin-top: 16rpx; }

.badge-grid {
	padding: 0 20rpx 20rpx;
	display: grid;
	grid-template-columns: 1fr 1fr 1fr;
	gap: 16rpx;
}
.badge-item {
	background: #fff;
	border-radius: 16rpx;
	padding: 24rpx 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
}
.badge-icon {
	width: 96rpx;
	height: 96rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 12rpx;
}
.badge-icon.earned { background: linear-gradient(135deg, #ffd700, #ffaa00); }
.badge-icon.locked { background: #f0f0f0; }
.badge-emoji { font-size: 40rpx; }
.badge-emoji.dim { font-size: 36rpx; color: #ccc; }
.badge-name { font-size: 24rpx; font-weight: 600; color: #333; }
.badge-name.dim { color: #bbb; }
.badge-desc { font-size: 20rpx; color: #999; margin-top: 4rpx; line-height: 1.4; }
</style>
