<template>
	<view class="container safe-bottom">
		<view class="header-area">
			<view class="status-bar"></view>
			<view class="nav-bar"><text class="nav-title">我的</text></view>
		</view>

		<view class="user-card" @click="handleUserCard">
			<view v-if="isLogin" class="user-info">
				<user-avatar :src="(userInfo && userInfo.icon) || ''" :size="100" />
				<view class="user-text">
					<text class="nickname">{{ (userInfo && userInfo.nickName) || '用户' }}</text>
					<text class="desc">查看主页</text>
				</view>
				<uni-icons type="right" size="18" color="#999"></uni-icons>
			</view>
			<view v-else class="user-info">
				<user-avatar :size="100" />
				<view class="user-text">
					<text class="nickname">点击登录</text>
					<text class="desc">登录享受更多精彩</text>
				</view>
				<uni-icons type="right" size="18" color="#999"></uni-icons>
			</view>
		</view>

		<view v-if="isLogin" class="badge-showcase" @click="goBadges">
			<view class="badge-head">
				<view>
					<text class="badge-title">我的勋章</text>
					<text class="badge-sub">{{ badges.length > 0 ? '已点亮 ' + badges.length + ' 枚城市徽章' : '完成任务点亮第一枚徽章' }}</text>
				</view>
				<view class="badge-more">
					<text>全部</text>
					<uni-icons type="right" size="14" color="#49616f"></uni-icons>
				</view>
			</view>
			<view v-if="badges.length > 0" class="badge-row">
				<view class="badge-token" v-for="badge in badgePreview" :key="badge.id">
					<view class="badge-icon"><text>{{ badge.iconText || '★' }}</text></view>
					<text class="badge-name">{{ badge.name }}</text>
				</view>
			</view>
			<view v-else class="badge-empty">
				<text class="badge-empty-icon">★</text>
				<text class="badge-empty-text">签到、发布笔记、收获点赞都可以解锁勋章</text>
			</view>
		</view>

		<view class="menu-section">
			<view class="section-title">我的内容</view>
			<view class="menu-list">
				<view class="menu-item" @click="goEditProfile">
					<uni-icons type="person" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">编辑资料</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
				<view class="menu-item" @click="goMyBlogs">
					<uni-icons type="compose" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">我的笔记</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
				<view class="menu-item" @click="goMyFollows">
					<uni-icons type="personadd" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">我的关注</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
				<view class="menu-item" @click="goFavoriteShops">
					<uni-icons type="star" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">收藏店铺</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
				<view class="menu-item" @click="goMyOrders">
					<uni-icons type="wallet" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">我的优惠券</text>
					<text v-if="creditsInfo.availableCoupons" class="menu-count">{{ creditsInfo.availableCoupons }}张可用</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
			</view>
		</view>

		<!-- 积分卡片 -->
		<view class="credits-section" v-if="isLogin" @click="goCreditsShop">
			<view class="credits-left">
				<text class="credits-tip">积分商城</text>
				<text class="credits-amount">{{ creditsInfo.credits || 0 }} 积分</text>
				<view class="credits-signin">
					<text class="credits-signin-text">{{ creditsInfo.todaySigned ? '今日已签到' : '每日签到 +2' }}</text>
					<view class="credits-badge-count">{{ creditsInfo.totalBadges || 0 }} 勋章</view>
					<view class="credits-coupon-count">{{ creditsInfo.availableCoupons || 0 }} 可用券</view>
				</view>
			</view>
			<uni-icons type="right" size="18" color="#ccc"></uni-icons>
		</view>

		<view class="menu-section">
			<view class="section-title">其他</view>
			<view class="menu-list">
				<view class="menu-item" @click="goPublish">
					<uni-icons type="plus-filled" size="22" color="#ff6600"></uni-icons>
					<text class="menu-text">发布笔记</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
				<view v-if="isLogin" class="menu-item" @click="doLogout">
					<uni-icons type="closeempty" size="22" color="#999"></uni-icons>
					<text class="menu-text muted">退出登录</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
			</view>
		</view>
	</view>
	<tab-bar current="/pages/me/me" />
</template>

<script>
import { isLogin, getUserInfo, logout, setUserInfo } from '@/utils/auth.js'
import { getMe } from '@/api/user.js'
import { getCreditsInfo, getMyBadges } from '@/api/credits.js'

export default {
	data() {
		return {
			isLogin: false,
			userInfo: null,
			creditsInfo: {},
			badges: []
		}
	},
	computed: {
		badgePreview() {
			return this.badges.slice(0, 5)
		}
	},
	async onShow() {
		this.isLogin = isLogin()
		if (this.isLogin) {
			this.userInfo = getUserInfo()
			try {
				const res = await getMe()
				if (res.success && res.data) {
					setUserInfo(res.data)
					this.userInfo = res.data
				}
			} catch (e) {}
			try {
				const cRes = await getCreditsInfo()
				if (cRes.success) this.creditsInfo = cRes.data || {}
			} catch (e) {}
			try {
				const bRes = await getMyBadges()
				if (bRes.success) this.badges = bRes.data || []
			} catch (e) {}
		} else {
			this.userInfo = null
			this.creditsInfo = {}
			this.badges = []
		}
	},
	methods: {
		doLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (r) => {
					if (r.confirm) {
						logout()
						this.isLogin = false
						this.userInfo = null
						uni.switchTab({ url: '/pages/home/home' })
					}
				}
			})
		},
		goLogin() { uni.navigateTo({ url: '/pages/login/login' }) },
		handleUserCard() { if (this.isLogin) { this.goProfile() } else { this.goLogin() } },
		goProfile() {
			const info = getUserInfo()
			if (info && info.id) { uni.navigateTo({ url: '/pages/user-profile/user-profile?id=' + info.id }) }
			else { uni.showToast({ title: '请先登录', icon: 'none' }) }
		},
		goEditProfile() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/edit-profile/edit-profile' })
		},
		goMyBlogs() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/user-profile/user-profile?id=' + this.userInfo.id + '&tab=mine' })
		},
		goMyFollows() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/following-list/following-list' })
		},
		goFavoriteShops() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/favorite-shops/favorite-shops' })
		},
		goMyOrders() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/my-orders/my-orders?status=2' })
		},
		goPublish() {
			if (!this.isLogin) { uni.navigateTo({ url: '/pages/login/login' }); return }
			uni.navigateTo({ url: '/pages/publish-blog/publish-blog' })
		},
		goCreditsShop() {
			uni.navigateTo({ url: '/pages/credits-shop/credits-shop' })
		},
		goBadges() {
			uni.navigateTo({ url: '/pages/badges/badges' })
		}
	}
}
</script>

<style scoped>
.container { background-color: #f5f5f5; min-height: 100vh; }
.header-area { background-color: #ffffff; }
.status-bar { height: var(--status-bar-height); }
.nav-bar { height: 88rpx; display: flex; align-items: center; justify-content: center; border-bottom: 1rpx solid #f0f0f0; }
.nav-title { font-size: 34rpx; font-weight: 600; color: #333333; }
.user-card { background-color: #ffffff; padding: 30rpx 24rpx; margin-bottom: 20rpx; }
.user-info { display: flex; align-items: center; }
.user-text { flex: 1; margin-left: 24rpx; display: flex; flex-direction: column; }
.nickname { font-size: 32rpx; font-weight: 600; color: #333333; }
.desc { font-size: 24rpx; color: #999999; margin-top: 8rpx; }
.menu-section { margin-bottom: 20rpx; }
.section-title { font-size: 24rpx; color: #999999; padding: 16rpx 24rpx 8rpx; }
.menu-list { background-color: #ffffff; border-radius: 16rpx; margin: 0 20rpx; overflow: hidden; }
.menu-item { display: flex; align-items: center; padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-text { flex: 1; margin-left: 24rpx; font-size: 28rpx; color: #333333; }
.menu-text.muted { color: #999999; }
.menu-count { font-size: 22rpx; color: #2d7d74; background: #e8f6f3; border-radius: 20rpx; padding: 4rpx 14rpx; margin-right: 10rpx; }
.badge-showcase {
	background: linear-gradient(135deg, #f9fbf9 0%, #eef7f4 50%, #fff6ea 100%);
	margin: 0 20rpx 20rpx;
	border-radius: 16rpx;
	padding: 24rpx;
	border: 1rpx solid #e7eee9;
}
.badge-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 22rpx; }
.badge-title { display: block; font-size: 32rpx; font-weight: 700; color: #263238; }
.badge-sub { display: block; font-size: 22rpx; color: #6f8580; margin-top: 6rpx; }
.badge-more { display: flex; align-items: center; font-size: 22rpx; color: #49616f; gap: 2rpx; }
.badge-row { display: flex; gap: 14rpx; }
.badge-token { width: 110rpx; display: flex; flex-direction: column; align-items: center; }
.badge-icon {
	width: 76rpx;
	height: 76rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #ffd166, #ef476f);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 18rpx rgba(239, 71, 111, .18);
}
.badge-icon text { font-size: 34rpx; color: #fff; }
.badge-name { width: 100%; font-size: 20rpx; color: #34444a; margin-top: 8rpx; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.badge-empty { display: flex; align-items: center; gap: 16rpx; background: rgba(255,255,255,.65); border-radius: 12rpx; padding: 20rpx; }
.badge-empty-icon { width: 52rpx; height: 52rpx; line-height: 52rpx; text-align: center; border-radius: 50%; background: #ffd166; color: #fff; font-size: 28rpx; }
.badge-empty-text { flex: 1; font-size: 24rpx; color: #6f8580; line-height: 1.5; }
.credits-section {
	background: #fff;
	margin: 0 20rpx 20rpx;
	border-radius: 16rpx;
	padding: 24rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.credits-left { display: flex; flex-direction: column; gap: 6rpx; }
.credits-tip { font-size: 22rpx; color: #ff6600; font-weight: 600; }
.credits-amount { font-size: 40rpx; font-weight: 700; color: #333; }
.credits-signin { display: flex; align-items: center; gap: 16rpx; margin-top: 4rpx; }
.credits-signin-text { font-size: 24rpx; color: #999; }
.credits-badge-count { font-size: 20rpx; color: #fff; background: #ff6600; border-radius: 20rpx; padding: 4rpx 16rpx; }
.credits-coupon-count { font-size: 20rpx; color: #2d7d74; background: #e8f6f3; border-radius: 20rpx; padding: 4rpx 16rpx; }

</style>
