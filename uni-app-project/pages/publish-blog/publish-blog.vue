<template>
	<view class="container">
		<view class="header" :style="{ paddingTop: statusBarH + 'px' }">
			<view class="nav-bar">
				<view class="nav-back" @click="goBack">
					<view class="back-circle"><uni-icons type="left" size="18" color="#333"></uni-icons></view>
				</view>
				<text class="nav-title">发布笔记</text>
				<button class="pub-btn" :disabled="!valid || submitting" @click="submit">发布</button>
			</view>
		</view>

		<view class="content safe-bottom">
			<!-- 标题 -->
			<input
				v-model="title"
				placeholder="写个标题吧"
				placeholder-class="ph"
				class="title-input"
				maxlength="100"
			/>

			<!-- 内容 -->
			<textarea
				v-model="content"
				placeholder="分享你的探店体验..."
				placeholder-class="ph"
				class="content-input"
				:maxlength="500"
			/>

			<!-- 图片 -->
			<view class="img-section">
				<view class="img-header">
					<text>添加图片（最多9张）</text>
				</view>
				<view class="img-list">
					<view v-for="(img, idx) in images" :key="idx" class="img-item">
						<image :src="img.url" mode="aspectFill" class="preview-img" />
						<view class="img-del" @click="removeImage(idx)">
							<uni-icons type="closeempty" size="18" color="#fff"></uni-icons>
						</view>
					</view>
					<view v-if="images.length < 9" class="img-add" @click="chooseImage">
						<uni-icons type="plus" size="36" color="#ccc"></uni-icons>
					</view>
				</view>
			</view>

			<!-- 关联店铺 -->
			<view class="shop-section" @click="goSelectShop">
				<view class="shop-label">
					<uni-icons type="shop-filled" size="18" color="#ff6600"></uni-icons>
					<text>{{ selectedShop ? selectedShop.name : '关联店铺（可选）' }}</text>
				</view>
				<uni-icons type="right" size="16" color="#ccc"></uni-icons>
			</view>

		<!-- 评分（关联店铺后显示） -->
		<view v-if="selectedShop" class="rating-section">
			<text class="rating-label">店铺评分</text>
			<view class="star-picker">
				<uni-icons v-for="i in 5" :key="i" :type="i <= rating ? 'star-filled' : 'star'" size="36" :color="i <= rating ? '#ff6600' : '#ddd'" @click="rating = i" />
			</view>
			<text class="rating-hint" v-if="rating > 0">{{ rating }}分</text>
		</view>
		</view>
	</view>
</template>

<script>
import { saveBlog } from '@/api/blog.js'
import { uploadImage, deleteImage } from '@/api/upload.js'

export default {
	data() {
		return {
			title: '',
			content: '',
			images: [], // [{ url, path, uploaded }]
			selectedShop: null,
			rating: 0,       // 评分 1-5
			statusBarH: 0,
			submitting: false
		}
	},
	computed: {
		valid() {
			return this.title.trim() && this.content.trim()
		}
	},
	onLoad() {
		this.statusBarH = uni.getSystemInfoSync().statusBarHeight || 0
		this._onShopSelected = (shop) => { this.selectedShop = shop }
		uni.$on('selectShop', this._onShopSelected)
	},
	onUnload() {
		uni.$off('selectShop', this._onShopSelected)
	},
	methods: {
		chooseImage() {
			uni.chooseImage({
				count: 9 - this.images.length,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					res.tempFilePaths.forEach((path) => {
						this.images.push({ url: path, path, uploaded: false })
					})
				}
			})
		},
		removeImage(idx) {
			this.images.splice(idx, 1)
		},
		goSelectShop() {
			uni.navigateTo({ url: '/pages/search/search?select=1' })
		},
		async submit() {
			if (!this.valid) return
			this.submitting = true
			uni.showLoading({ title: '发布中...' })

			try {
				// 上传图片
				const uploadedPaths = []
				for (const img of this.images) {
					if (!img.uploaded) {
						try {
							const res = await uploadImage(img.path)
							console.log('upload result:', JSON.stringify(res))
							if (res.success) {
								uploadedPaths.push(res.data)
							} else {
								console.error('upload failed:', res.errorMsg)
								uni.showToast({ title: '图片上传失败: ' + (res.errorMsg || '未知错误'), icon: 'none' })
								uni.hideLoading()
								this.submitting = false
								return
							}
						} catch (e) {
							console.error('upload error:', e)
							uni.showToast({ title: '图片上传失败，请检查网络', icon: 'none' })
							uni.hideLoading()
							this.submitting = false
							return
						}
					} else {
						uploadedPaths.push(img.path)
					}
				}

				const blogData = {
					title: this.title.trim(),
					content: this.content.trim(),
					images: uploadedPaths.join(','),
					shopId: this.selectedShop ? this.selectedShop.id : null,
					score: this.selectedShop ? this.rating : null
				}
				console.log('saveBlog data:', JSON.stringify(blogData))

				const res = await saveBlog(blogData)
				console.log('saveBlog result:', JSON.stringify(res))
				if (res.success) {
					uni.showToast({ title: '发布成功', icon: 'success' })
					setTimeout(() => {
						const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: "/pages/home/home" })
					}, 1000)
				} else {
					uni.showToast({ title: res.errorMsg || '发布失败', icon: 'none', duration: 3000 })
				}
			} catch (e) {
				console.error('submit error:', e)
				uni.showToast({ title: '发布失败: ' + (e.message || '网络错误'), icon: 'none', duration: 3000 })
			} finally {
				uni.hideLoading()
				this.submitting = false
			}
		},
		goBack() {
			if (this.title || this.content || this.images.length > 0) {
				uni.showModal({
					title: '提示',
					content: '确定放弃编辑的内容吗？',
					success: (r) => {
						if (r.confirm) { const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: '/pages/home/home' }) }
					}
				})
			} else {
				const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: '/pages/home/home' })
			}
		}
	}
}
</script>

<style scoped>
.container {
	background-color: #f5f5f5;
	min-height: 100vh;
}

.header {
	background-color: #ffffff;
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-bar {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}

.nav-back {
	position: absolute;
	left: 20rpx;
	top: 50%;
	transform: translateY(-50%);
	padding: 10rpx;
}

.back-circle {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background-color: rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 600;
}

.pub-btn {
	position: absolute;
	right: 24rpx;
	top: 50%;
	transform: translateY(-50%);
	width: 120rpx;
	height: 60rpx;
	line-height: 60rpx;
	background: linear-gradient(135deg, #ff6600, #ff8833);
	color: #fff;
	border-radius: 30rpx;
	font-size: 26rpx;
	padding: 0;
	border: none;
}

.pub-btn[disabled] {
	background: #f0f0f0;
	color: #ccc;
}

.pub-btn::after { border: none; }

.content {
	padding: 20rpx;
}

.title-input {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 16rpx;
}

.content-input {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	font-size: 28rpx;
	color: #333;
	min-height: 300rpx;
	margin-bottom: 16rpx;
	width: 100%;
	box-sizing: border-box;
}

.ph {
	color: #ccc;
	font-size: 28rpx;
}

.img-section {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 16rpx;
}

.img-header text {
	font-size: 26rpx;
	color: #666;
}

.img-list {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-top: 16rpx;
}

.img-item {
	position: relative;
	width: 200rpx;
	height: 200rpx;
}

.preview-img {
	width: 100%;
	height: 100%;
	border-radius: 12rpx;
	background-color: #f0f0f0;
}

.img-del {
	position: absolute;
	top: -8rpx;
	right: -8rpx;
	width: 40rpx;
	height: 40rpx;
	background-color: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.img-add {
	width: 200rpx;
	height: 200rpx;
	background-color: #f5f5f5;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx dashed #e0e0e0;
}

.shop-section {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.shop-label {
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-size: 26rpx;
	color: #333;
}

.rating-section {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-top: 16rpx;
	display: flex;
	align-items: center;
	gap: 16rpx;
}
.rating-label {
	font-size: 26rpx;
	color: #666;
}
.star-picker {
	display: flex;
	gap: 8rpx;
}
.rating-hint {
	font-size: 24rpx;
	color: #ff6600;
	font-weight: 600;
}

.safe-bottom {
	padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
}
</style>
