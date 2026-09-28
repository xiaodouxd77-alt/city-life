<template>
	<view class="container">
		<view class="header" :style="{ paddingTop: statusBarH + 'px' }">
			<view class="nav-bar">
				<view class="nav-back" @click="goBack"><view class="back-circle"><uni-icons type="left" size="18" color="#333"></uni-icons></view></view>
				<text class="nav-title">笔记详情</text>
			</view>
		</view>

		<view v-if="loaded" class="content safe-bottom">
			<!-- 作者 -->
			<view class="author-card" @click="goUser(blog.userId)">
				<user-avatar :src="blog.icon" :size="80" />
				<view class="author-info">
					<text class="author-name">{{ blog.name || '用户' + blog.userId }}</text>
					<text class="pub-time">{{ formatTime(blog.createTime) }}</text>
				</view>
				<button :class="['follow-btn', { followed: isFollowed }]" @click.stop="toggleFollow">{{ isFollowed ? '已关注' : '关注' }}</button>
			</view>
			<!-- 内容 -->
			<view class="blog-body">
				<text class="blog-title">{{ blog.title }}</text>
				<text class="blog-text">{{ blog.content }}</text>
			</view>
			<!-- 图片 -->
			<view v-if="images.length" class="blog-images">
				<image v-for="(img, idx) in images" :key="idx" :src="img" mode="aspectFill" class="detail-img" @click="previewImage(idx)" />
			</view>
			<!-- 操作栏 -->
			<view class="action-bar">
				<view class="action-item" @click="doLike">
					<uni-icons :type="blog.isLike ? 'heart-filled' : 'heart'" size="22" :color="blog.isLike ? '#ff6600' : '#666'" />
					<text :style="{ color: blog.isLike ? '#ff6600' : '#666' }">{{ blog.liked || 0 }}</text>
				</view>
				<view class="action-item" @click="doFavorite">
					<uni-icons :type="isFav ? 'star-filled' : 'star'" size="22" :color="isFav ? '#ffaa00' : '#666'" />
					<text :style="{ color: isFav ? '#ffaa00' : '#666' }">收藏</text>
				</view>
				<view class="action-item">
					<uni-icons type="chatbubble" size="22" color="#666"></uni-icons>
					<text>{{ blog.comments || 0 }}</text>
				</view>
			</view>
			<!-- 点赞用户 -->
			<view v-if="likes.length" class="likes-section">
				<view class="likes-header"><uni-icons type="heart-filled" size="16" color="#ff6600"></uni-icons><text class="likes-title">{{ likes.length }}人觉得很赞</text></view>
				<view class="likes-list"><view v-for="u in likes" :key="u.id" class="like-user" @click="goUser(u.id)"><user-avatar :src="u.icon" :size="64" /><text class="like-name">{{ u.nickName }}</text></view></view>
			</view>

			<!-- 评论区 -->
			<view class="comments-section">
				<text class="section-title">评论 ({{ comments.length }})</text>
				<view v-if="comments.length === 0" class="no-comment">
					<text>暂无评论，来说两句吧</text>
				</view>
				<view v-for="c in comments" :key="c.id" class="comment-item">
					<user-avatar :src="c.userIcon" :size="64" @click="goUser(c.userId)" />
					<view class="comment-body">
						<view class="comment-top">
							<text class="comment-name">{{ c.userName }}</text>
							<text class="comment-time">{{ formatTime(c.createTime) }}</text>
						</view>
						<text class="comment-content">{{ c.content }}</text>
					</view>
					<view v-if="c.userId === myId" class="comment-del" @click="doDeleteComment(c.id)"><uni-icons type="trash" size="18" color="#ccc" /></view>
				</view>
			</view>
		</view>

		<!-- 底部评论输入 -->
		<view class="comment-bar">
			<input v-model="inputComment" placeholder="写评论..." placeholder-class="ph" confirm-type="send" @confirm="doComment" />
			<button class="send-btn" :disabled="!inputComment.trim()" @click="doComment">发送</button>
		</view>
	</view>
</template>

<script>
import { getBlogById, getBlogLikes, likeBlog } from '@/api/blog.js'
import { getComments, addComment, deleteComment } from '@/api/comment.js'
import { isFollow, follow } from '@/api/follow.js'
import { toggleFavorite, isFavorite } from '@/api/favorite.js'
import { getBlogImages, formatTime } from '@/utils/constants.js'
import { isLogin } from '@/utils/auth.js'

export default {
	data() {
		return {
			blog: {}, images: [], likes: [], comments: [],
			isFollowed: false, isFav: false, loaded: false, statusBarH: 0,
			inputComment: '', myId: 0
		}
	},
	onLoad(options) {
		this.statusBarH = uni.getSystemInfoSync().statusBarHeight || 0
		const info = uni.getStorageSync('userInfo')
		if (info) this.myId = info.id
		if (options.id) this.loadData(options.id)
	},
	methods: {
		formatTime,
		async loadData(id) {
			try {
				const [blogRes, commentRes] = await Promise.all([getBlogById(id), getComments(id)])
				if (blogRes.success) { this.blog = blogRes.data; this.images = getBlogImages(this.blog.images); this.loadLikes(id); this.checkFollow(); this.checkFavorite() }
				if (commentRes.success) this.comments = commentRes.data || []
				this.loaded = true
			} catch (e) {}
		},
		async loadLikes(id) {
			const res = await getBlogLikes(id)
			if (res.success && res.data) this.likes = res.data
		},
		async checkFollow() {
			if (!isLogin() || !this.blog.userId) return
			const res = await isFollow(this.blog.userId)
			if (res.success) this.isFollowed = res.data
		},
		async doLike() {
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			try {
				const res = await likeBlog(this.blog.id)
				if (res.success) { this.blog.isLike = !this.blog.isLike; this.blog.liked = (this.blog.liked || 0) + (this.blog.isLike ? 1 : -1) }
			} catch (e) {}
		},
		async toggleFollow() {
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			const res = await follow(this.blog.userId, !this.isFollowed)
			if (res.success) {
				this.isFollowed = !this.isFollowed
				uni.$emit('followChanged')
			}
		},
		async doComment() {
			if (!this.inputComment.trim()) return
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			try {
				const res = await addComment({ blogId: this.blog.id, content: this.inputComment.trim(), parentId: 0, answerId: 0 })
				if (res.success) {
					uni.showToast({ title: '评论成功', icon: 'success' })
					this.inputComment = ''
					this.blog.comments = (this.blog.comments || 0) + 1
					// 刷新评论列表
					const cRes = await getComments(this.blog.id)
					if (cRes.success) this.comments = cRes.data || []
				} else {
					uni.showToast({ title: res.errorMsg || '评论失败', icon: 'none' })
				}
			} catch (e) { uni.showToast({ title: '评论失败', icon: 'none' }) }
		},
		async doDeleteComment(id) {
			uni.showModal({
				title: '删除评论',
				content: '确定删除这条评论吗？',
				success: async (r) => {
					if (!r.confirm) return
					const res = await deleteComment(id)
					if (res.success) {
						this.comments = this.comments.filter(c => c.id !== id)
						this.blog.comments = Math.max(0, (this.blog.comments || 1) - 1)
						uni.showToast({ title: '已删除', icon: 'success' })
					} else {
						uni.showToast({ title: res.errorMsg || '删除失败', icon: 'none' })
					}
				}
			})
		},
		previewImage(idx) { uni.previewImage({ urls: this.images, current: this.images[idx] }) },
		goUser(id) { if (id) uni.navigateTo({ url: '/pages/user-profile/user-profile?id=' + id }) },
		checkFavorite() {
			if (!isLogin() || !this.blog.id) return
			isFavorite('BLOG', this.blog.id).then(res => { if (res.success) this.isFav = !!res.data }).catch(() => {})
		},
		async doFavorite() {
			if (!isLogin()) { uni.navigateTo({ url: '/pages/login/login' }); return }
			try {
				const res = await toggleFavorite('BLOG', this.blog.id)
				if (res.success) { this.isFav = res.data; uni.showToast({ title: this.isFav ? '已收藏' : '已取消收藏', icon: 'none' }) }
			} catch (e) {}
		},

		goBack() { const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: '/pages/home/home' }) }
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; }
.header { position: sticky; top: 0; z-index: 100; background: #fff; }
.nav-bar { height: 88rpx; display: flex; align-items: center; justify-content: center; position: relative; }
.nav-back { position: absolute; left: 20rpx; top: 50%; transform: translateY(-50%); padding: 10rpx; }
.back-circle { width: 60rpx; height: 60rpx; border-radius: 50%; background: rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 32rpx; font-weight: 600; color: #333; }
.content { padding: 0 20rpx; }
.author-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; display: flex; align-items: center; }
.author-info { flex: 1; margin-left: 20rpx; display: flex; flex-direction: column; }
.author-name { font-size: 28rpx; font-weight: 600; color: #333; }
.pub-time { font-size: 22rpx; color: #999; margin-top: 4rpx; }
.follow-btn { width: 120rpx; height: 56rpx; line-height: 56rpx; font-size: 24rpx; background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; border-radius: 28rpx; padding: 0; border: none; }
.follow-btn::after { border: none; }
.follow-btn.followed { background: #f0f0f0; color: #999; }
.blog-body { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.blog-title { font-size: 34rpx; font-weight: 600; color: #333; margin-bottom: 16rpx; display: block; }
.blog-text { font-size: 28rpx; color: #666; line-height: 1.8; white-space: pre-wrap; }
.blog-images { display: flex; flex-direction: column; gap: 10rpx; margin-bottom: 16rpx; }
.detail-img { width: 100%; border-radius: 12rpx; background: #f0f0f0; height: 400rpx; }
.action-bar { background: #fff; border-radius: 16rpx; padding: 20rpx 24rpx; margin-bottom: 16rpx; display: flex; justify-content: space-around; }
.action-item { display: flex; align-items: center; font-size: 26rpx; color: #666; gap: 8rpx; }
.likes-section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.likes-header { display: flex; align-items: center; margin-bottom: 16rpx; gap: 8rpx; }
.likes-title { font-size: 26rpx; font-weight: 600; color: #333; }
.likes-list { display: flex; flex-wrap: wrap; gap: 20rpx; }
.like-user { display: flex; flex-direction: column; align-items: center; }
.like-name { font-size: 22rpx; color: #666; margin-top: 8rpx; }
.comments-section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.section-title { font-size: 28rpx; font-weight: 600; color: #333; margin-bottom: 20rpx; display: block; }
.no-comment { text-align: center; padding: 40rpx; font-size: 26rpx; color: #999; }
.comment-item { display: flex; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.comment-item:last-child { border-bottom: none; }
.comment-body { flex: 1; margin-left: 16rpx; }
.comment-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.comment-name { font-size: 26rpx; font-weight: 600; color: #333; }
.comment-time { font-size: 20rpx; color: #999; }
.comment-content { font-size: 26rpx; color: #444; line-height: 1.5; }
.comment-del { padding: 8rpx 16rpx; font-size: 22rpx; color: #ccc; }
.comment-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; display: flex; align-items: center; padding: 16rpx 20rpx calc(16rpx + env(safe-area-inset-bottom)); border-top: 1rpx solid #eee; z-index: 100; gap: 16rpx; }
.comment-bar input { flex: 1; height: 72rpx; background: #f5f5f5; border-radius: 36rpx; padding: 0 28rpx; font-size: 26rpx; }
.ph { color: #ccc; font-size: 26rpx; }
.send-btn { width: 120rpx; height: 72rpx; line-height: 72rpx; background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; border-radius: 36rpx; font-size: 26rpx; padding: 0; border: none; }
.send-btn::after { border: none; }
.send-btn[disabled] { background: #f0f0f0; color: #ccc; }
.safe-bottom { padding-bottom: calc(140rpx + env(safe-area-inset-bottom)); }
</style>
