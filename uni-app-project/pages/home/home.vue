<template>
	<view class="container">
		<view class="header-fixed">
			<view class="status-bar"></view>
			<view class="nav-row"><text class="nav-title">优质生活</text></view>
		</view>

		<view class="tabs-fixed">
			<view class="tabs-row">
				<view :class="['tabs-item', currentTab === 'hot' ? 'active' : '']" @click="switchTab('hot')"><text>热门</text></view>
				<view :class="['tabs-item', currentTab === 'follow' ? 'active' : '']" @click="switchTab('follow')"><text>关注</text></view>
			</view>
		</view>

		<!-- 热门 -->
		<block v-if="currentTab === 'hot'">
			<view class="feed-list">
				<blog-card v-for="blog in hotBlogs" :key="blog.id" :blog="blog" />
				<view v-if="hotLoading && hotBlogs.length === 0" class="load-tip"><text>加载中...</text></view>
				<view v-if="!hotLoading && hotBlogs.length === 0" class="empty-wrap"><uni-icons class="empty-icon" type="fire" size="64" color="#ddd" /><text class="empty-text">暂无热门笔记</text></view>
				<view v-if="!hotLoading && hotNoMore && hotBlogs.length > 0" class="load-tip no-more"><text>— 已经到底了 —</text></view>
			</view>
		</block>

		<!-- 关注 -->
		<block v-if="currentTab === 'follow'">
			<view v-if="!isLogin" class="empty-wrap" style="padding-top:200rpx">
				<uni-icons class="empty-icon" type="person" size="64" color="#ddd" /><text class="empty-text">登录后查看关注动态</text>
				<view class="login-btn" @click="goLogin">去登录</view>
			</view>
			<view v-else class="feed-list">
				<blog-card v-for="blog in followBlogs" :key="blog.id" :blog="blog" />
				<view v-if="followLoading && followBlogs.length === 0" class="load-tip"><text>加载中...</text></view>
				<view v-if="!followLoading && followBlogs.length === 0" class="empty-wrap"><uni-icons class="empty-icon" type="notification" size="64" color="#ddd" /><text class="empty-text">关注一些用户，发现更多精彩</text></view>
				<view v-if="!followLoading && followNoMore && followBlogs.length > 0" class="load-tip no-more"><text>— 已经到底了 —</text></view>
			</view>
		</block>

		<tab-bar current="/pages/home/home" />
	</view>
</template>

<script>
import { getHotBlogs, getFollowBlogs } from '@/api/blog.js'
import { isLogin } from '@/utils/auth.js'

// 页面级缓存，tab 切换不丢失
const cache = { hotBlogs: [], followBlogs: [], followLastId: 0, followOffset: 0 }

export default {
	data() {
		return {
			currentTab: 'hot',
			isLogin: false,
			hotBlogs: cache.hotBlogs,
			hotCurrent: 1,
			hotLoading: false,
			hotNoMore: false,
			followBlogs: cache.followBlogs,
			followLastId: cache.followLastId || Date.now(),
			followOffset: cache.followOffset || 0,
			followLoading: false,
			followNoMore: false
		}
	},
	onLoad() {
		// 详情页/用户主页关注状态变化后会触发该事件，首页收到后刷新关注流。
		uni.$on('followChanged', this.handleFollowChanged)
	},
	onUnload() {
		// 页面销毁时解绑，避免重复监听导致一次关注触发多次刷新。
		uni.$off('followChanged', this.handleFollowChanged)
	},
	onShow() {
		this.isLogin = isLogin()
		// 有缓存直接展示，后台静默刷新
		if (this.hotBlogs.length === 0) this.loadHotBlogs(true)
		if (this.isLogin) this.loadFollowBlogs(true)
	},
	mounted() {
		// 首次进入预加载两个 tab 的数据
		this.loadHotBlogs(true)
		if (this.isLogin) this.loadFollowBlogs(true)
	},
	methods: {
		handleFollowChanged() {
			if (!this.isLogin) return
			this.loadFollowBlogs(true)
		},
		switchTab(tab) {
			this.currentTab = tab
			if (tab === 'follow' && this.isLogin && this.followBlogs.length === 0) this.loadFollowBlogs(true)
		},
		async loadHotBlogs(refresh) {
			if (this.hotLoading) return
			if (refresh) { this.hotCurrent = 1; this.hotNoMore = false }
			if (this.hotNoMore && !refresh) return
			this.hotLoading = true
			try {
				const res = await getHotBlogs(this.hotCurrent)
				if (res.success) {
					const list = res.data || []
					this.hotBlogs = refresh ? list : [...this.hotBlogs, ...list]
					cache.hotBlogs = this.hotBlogs
					if (list.length < 10) this.hotNoMore = true
				}
			} catch (e) {} finally { this.hotLoading = false }
		},
		async loadFollowBlogs(refresh) {
			if (!this.isLogin || this.followLoading) return
			// refresh 表示重新拉第一页；非 refresh 表示继续用 lastId/offset 加载下一页。
			if (refresh) { this.followLastId = Date.now(); this.followOffset = 0; this.followNoMore = false }
			if (this.followNoMore && !refresh) return
			this.followLoading = true
			try {
				const res = await getFollowBlogs(this.followLastId, this.followOffset)
				if (res.success && res.data && res.data.list && res.data.list.length > 0) {
					this.followBlogs = refresh ? res.data.list : [...this.followBlogs, ...res.data.list]
					cache.followBlogs = this.followBlogs
					this.followLastId = res.data.minTime || 0
					this.followOffset = res.data.offset || 0
					cache.followLastId = this.followLastId
					cache.followOffset = this.followOffset
				} else if (refresh) {
					// 刷新结果为空时也要同步清空页面缓存，否则旧关注流会残留。
					this.followBlogs = []
					cache.followBlogs = []
					cache.followLastId = this.followLastId
					cache.followOffset = this.followOffset
				}
				if (!res.data || !res.data.list || res.data.list.length < 5) this.followNoMore = true
			} catch (e) {} finally { this.followLoading = false }
		},
		goLogin() { uni.navigateTo({ url: '/pages/login/login' }) }
	},
	onReachBottom() {
		if (this.currentTab === 'hot') { this.hotCurrent++; this.loadHotBlogs(false) }
		else { this.loadFollowBlogs(false) }
	},
	onPullDownRefresh() {
		if (this.currentTab === 'hot') this.loadHotBlogs(true)
		else this.loadFollowBlogs(true)
		setTimeout(() => uni.stopPullDownRefresh(), 500)
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; padding-bottom: calc(120rpx + env(safe-area-inset-bottom)); }
.header-fixed { position: sticky; top: 0; z-index: 200; background: #fff; }
.status-bar { height: var(--status-bar-height); }
.nav-row { height: 88rpx; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 34rpx; font-weight: 700; color: #333; }
.tabs-fixed { position: sticky; top: calc(var(--status-bar-height) + 88rpx); z-index: 199; background: #fff; border-bottom: 1rpx solid #f0f0f0; }
.tabs-row { display: flex; justify-content: center; }
.tabs-item { padding: 20rpx 48rpx; position: relative; }
.tabs-item text { font-size: 30rpx; color: #999; font-weight: 500; }
.tabs-item.active text { color: #333; font-weight: 700; }
.tabs-item.active::after { content: ''; position: absolute; bottom: 10rpx; left: 50%; transform: translateX(-50%); width: 40rpx; height: 6rpx; background: #ff6600; border-radius: 3rpx; }
.feed-list { padding: 20rpx 20rpx 0; }
.load-tip { display: flex; justify-content: center; padding: 30rpx; font-size: 24rpx; color: #999; }
.load-tip.no-more { color: #ccc; }
.empty-wrap { display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; }
.empty-icon { display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.login-btn { margin-top: 32rpx; padding: 16rpx 60rpx; background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; border-radius: 40rpx; font-size: 28rpx; }
</style>
