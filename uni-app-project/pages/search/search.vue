<template>
	<view class="container safe-bottom">
		<view class="header" :style="{ paddingTop: statusBarH + 'px' }">
			<view class="nav-bar">
				<view class="nav-back" @click.stop="goBack">
					<uni-icons type="left" size="22" color="#333"></uni-icons>
				</view>
				<view class="search-box">
					<view class="search-icon"><uni-icons type="search" size="18" color="#999"></uni-icons></view>
					<input
						v-model="keyword"
						placeholder="搜索店铺名称"
						placeholder-class="placeholder"
						confirm-type="search"
						@input="handleSearchInput"
						@confirm="doSearch"
					/>
					<view v-if="keyword" class="clear-btn" @click="clearSearch">
						<uni-icons type="clear" size="18" color="#bbb"></uni-icons>
					</view>
				</view>
			</view>
		</view>

		<view class="content">
			<block v-if="!searched">
				<view v-if="selectMode" class="recommend-section">
					<scroll-view v-if="types.length" scroll-x="true" class="type-scroll" :show-scrollbar="false">
						<view class="type-list">
							<view
								v-for="item in types"
								:key="item.id"
								:class="['type-chip', currentTypeId === item.id ? 'active' : '']"
								@click="selectType(item)"
							>
								<image v-if="item.icon" :src="item.icon" mode="aspectFit" class="type-icon" />
								<text>{{ item.name }}</text>
							</view>
						</view>
					</scroll-view>
					<view class="shop-list">
						<shop-card
							v-for="shop in recommendedShops"
							:key="shop.id"
							:shop="shop"
							:selectMode="selectMode"
							@select="onShopSelect"
						/>
						<view v-if="loading && recommendedShops.length === 0" class="loading-tip">
							<uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
							<text>加载店铺中...</text>
						</view>
						<view v-if="!loading && recommendedShops.length === 0" class="empty-state">
							<uni-icons type="shop" size="64" color="#ddd"></uni-icons>
							<text>暂无可选店铺</text>
						</view>
						<view v-if="!loading && noMore && recommendedShops.length > 0" class="loading-tip no-more">
							<text>已经到底了</text>
						</view>
					</view>
				</view>

				<view v-else class="hot-card">
					<text class="hot-title">热门搜索</text>
					<view class="hot-tags">
						<text
							v-for="(item, idx) in hotTags"
							:key="idx"
							class="hot-tag"
							@click="quickSearch(item)"
						>{{ item }}</text>
					</view>
				</view>
			</block>

			<view v-else>
				<view class="section-head">
					<text class="section-title">搜索结果</text>
				</view>
				<view v-if="results.length === 0 && !loading" class="empty-state">
					<uni-icons type="search" size="64" color="#ddd"></uni-icons>
					<text>暂无相关店铺</text>
				</view>
				<shop-card
					v-for="shop in results"
					:key="shop.id"
					:shop="shop"
					:selectMode="selectMode"
					@select="onShopSelect"
				/>
				<view v-if="loading" class="loading-tip">
					<uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
					<text>搜索中...</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getShopTypes, getShopsByType, searchShops } from '@/api/shop.js'

export default {
	data() {
		return {
			keyword: '',
			searched: false,
			results: [],
			recommendedShops: [],
			types: [],
			currentTypeId: 0,
			current: 1,
			noMore: false,
			loading: false,
			statusBarH: 0,
			selectMode: false,
			lng: null,
			lat: null,
			searchTimer: null,
			searchRequestId: 0,
			hotTags: ['火锅', '烧烤', '日料', '咖啡', '奶茶', '川菜', '粤菜', '小吃', '西餐', '甜品']
		}
	},
	onLoad(options) {
		this.statusBarH = uni.getSystemInfoSync().statusBarHeight || 0
		if (options.select === '1') {
			this.selectMode = true
			this.initRecommendations()
		}
	},
	methods: {
		async initRecommendations() {
			await this.loadTypes()
			await this.getLocation()
			if (this.currentTypeId) this.loadRecommendedShops(true)
		},
		async loadTypes() {
			try {
				const res = await getShopTypes()
				if (res.success && res.data) {
					this.types = res.data.sort((a, b) => a.sort - b.sort)
					if (this.types.length > 0 && !this.currentTypeId) {
						this.currentTypeId = this.types[0].id
					}
				}
			} catch (e) {}
		},
		async getLocation() {
			try {
				const loc = await uni.getLocation({ type: 'gcj02' })
				this.lng = loc.longitude
				this.lat = loc.latitude
			} catch (e) {
				this.lng = null
				this.lat = null
			}
		},
		selectType(item) {
			if (this.currentTypeId === item.id) return
			this.currentTypeId = item.id
			this.loadRecommendedShops(true)
		},
		async loadRecommendedShops(refresh) {
			if (this.loading || !this.currentTypeId) return
			if (refresh) {
				this.current = 1
				this.noMore = false
			}
			if (this.noMore && !refresh) return
			this.loading = true
			try {
				const res = await getShopsByType(this.currentTypeId, this.current, this.lng, this.lat)
				if (res.success) {
					const list = res.data || []
					this.recommendedShops = refresh ? list : [...this.recommendedShops, ...list]
					if (list.length < 6) this.noMore = true
				}
			} catch (e) {} finally {
				this.loading = false
			}
		},
		quickSearch(tag) {
			this.keyword = tag
			this.doSearch()
		},
		clearSearch() {
			clearTimeout(this.searchTimer)
			this.searchRequestId++
			this.keyword = ''
			this.searched = false
			this.results = []
			this.loading = false
		},
		handleSearchInput() {
			clearTimeout(this.searchTimer)
			if (!this.keyword.trim()) {
				this.searched = false
				this.results = []
				this.loading = false
				return
			}
			this.searchTimer = setTimeout(() => this.doSearch(), 300)
		},
		async doSearch() {
			const keyword = this.keyword.trim()
			if (!keyword) {
				this.clearSearch()
				return
			}
			clearTimeout(this.searchTimer)
			const requestId = ++this.searchRequestId
			this.searched = true
			this.loading = true
			this.results = []
			try {
				const res = await searchShops(keyword)
				if (requestId === this.searchRequestId && res.success) {
					this.results = res.data || []
				}
			} catch (e) {} finally {
				if (requestId === this.searchRequestId) this.loading = false
			}
		},
		onShopSelect(shop) {
			uni.$emit('selectShop', shop)
			uni.navigateBack()
		},
		goBack() {
			uni.hideKeyboard()
			uni.navigateBack({
				delta: 1,
				fail: () => uni.showToast({ title: '无法返回上一页', icon: 'none' })
			})
		}
	},
	onReachBottom() {
		if (this.selectMode && !this.searched && !this.loading && !this.noMore) {
			this.current++
			this.loadRecommendedShops(false)
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
	position: relative;
	display: flex;
	align-items: center;
	/* 左侧留出返回键的空间(返回键绝对定位),右侧正常留白 */
	padding: 0 20rpx 0 84rpx;
}

.nav-back {
	position: absolute;
	left: 12rpx;
	top: 50%;
	transform: translateY(-50%);
	width: 56rpx;
	height: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 2;
}

.nav-back .uni-icons {
	line-height: 1;
}

.search-box {
	flex: 1;
	min-width: 0;
	height: 72rpx;
	display: flex;
	align-items: center;
	background-color: #f5f5f5;
	border-radius: 40rpx;
	padding: 0 20rpx;
}

.search-box input {
	flex: 1;
	min-width: 0;
	font-size: 28rpx;
	color: #333;
}

.clear-btn {
	width: 44rpx;
	height: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.search-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40rpx;
	height: 40rpx;
	margin-right: 12rpx;
	flex-shrink: 0;
}

.placeholder {
	color: #ccc;
	font-size: 26rpx;
}

.content {
	padding: 20rpx;
}

.section-head {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	margin: 4rpx 4rpx 18rpx;
}

.section-title,
.hot-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #333;
}

.type-scroll {
	width: 100%;
	height: 68rpx;
	white-space: nowrap;
	margin-bottom: 20rpx;
}

.type-list {
	display: inline-flex;
	gap: 12rpx;
	padding: 0 4rpx 4rpx;
}

.type-chip {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	min-width: 112rpx;
	height: 60rpx;
	padding: 0 24rpx;
	border-radius: 32rpx;
	background: #ffffff;
	font-size: 26rpx;
	color: #666;
	flex-shrink: 0;
}

.type-icon {
	width: 32rpx;
	height: 32rpx;
	flex-shrink: 0;
}

.type-chip.active {
	background: linear-gradient(135deg, #ff6600, #ff8833);
	color: #ffffff;
	font-weight: 600;
	box-shadow: 0 6rpx 16rpx rgba(255, 102, 0, 0.22);
}

.hot-card {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 24rpx;
}

.hot-title {
	margin-bottom: 20rpx;
	display: block;
}

.hot-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.hot-tag {
	padding: 16rpx 32rpx;
	background-color: #f7f7f7;
	border-radius: 32rpx;
	font-size: 26rpx;
	color: #666;
}

.loading-tip {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 30rpx;
	font-size: 24rpx;
	color: #999;
	gap: 12rpx;
}

.loading-tip.no-more {
	color: #ccc;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 20rpx;
	font-size: 26rpx;
	color: #999;
	gap: 20rpx;
}
</style>
