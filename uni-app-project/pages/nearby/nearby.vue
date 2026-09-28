<template>
	<view class="container">
		<view class="header-fixed">
			<view class="status-bar"></view>
			<view class="search-row">
				<view class="search-box">
					<view class="search-icon"><uni-icons type="search" size="18" color="#999"></uni-icons></view>
					<input
						v-model="keyword"
						class="search-input"
						placeholder="搜索店铺名称"
						placeholder-class="search-placeholder"
						confirm-type="search"
						@input="handleSearchInput"
						@confirm="searchCurrentShops"
					/>
					<view v-if="keyword" class="clear-search" @tap="clearSearch">
						<uni-icons type="clear" size="17" color="#bbb"></uni-icons>
					</view>
				</view>
			</view>
		</view>

		<view class="filter-fixed">
			<scroll-view scroll-x="true" class="filter-scroll" :show-scrollbar="false">
				<view class="filter-list">
					<view v-for="item in types" :key="item.id" :class="['filter-btn', currentTypeId === item.id ? 'active' : '']" @click="selectType(item)">
						<image :src="item.icon" mode="aspectFit" class="filter-icon" /><text>{{ item.name }}</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<view class="shop-list">
			<shop-card v-for="shop in displayedShops" :key="shop.id" :shop="shop" />
			<view v-if="isSearching && !searchLoading && searchResults.length === 0" class="empty-wrap"><text class="empty-text">暂无相关店铺</text></view>
			<view v-if="visibleLoading && displayedShops.length === 0" class="load-tip"><text>加载中...</text></view>
			<view v-if="!loading && shops.length === 0" class="empty-wrap"><uni-icons class="empty-icon" type="location" size="64" color="#ddd" /><text class="empty-text">附近暂无该类型店铺</text></view>
			<view v-if="!loading && noMore && shops.length > 0" class="load-tip no-more"><text>— 已经到底了 —</text></view>
		</view>
		<tab-bar current="/pages/nearby/nearby" />
	</view>
</template>

<script>
import { getShopTypes, getShopsByType, searchShops } from '@/api/shop.js'

// 页面级缓存
const cache = { types: null, shops: [], currentTypeId: 0, lng: null, lat: null }

export default {
	data() {
		return {
			types: cache.types || [],
			currentTypeId: cache.currentTypeId || 0,
			shops: cache.shops,
			current: 1,
			loading: false,
			keyword: '',
			searchResults: [],
			searchLoading: false,
			searchTimer: null,
			searchRequestId: 0,
			noMore: false,
			lng: cache.lng,
			lat: cache.lat
		}
	},
	onShow() {
		// 有缓存数据直接展示，避免空白
		if (this.types.length > 0 && this.shops.length > 0) return
		this.initData()
	},
	mounted() {
		this.initData()
	},
	computed: {
		isSearching() {
			return Boolean(this.keyword.trim())
		},
		displayedShops() {
			return this.isSearching ? this.searchResults : this.shops
		},
		visibleLoading() {
			return this.isSearching ? this.searchLoading : this.loading
		}
	},
	methods: {
		async initData() {
			if (this.types.length === 0) await this.loadTypes()
			if (this.lng === null) await this.getLocation()
			if (this.currentTypeId && this.shops.length === 0) this.loadShops(true)
		},
		async loadTypes() {
			if (cache.types) { this.types = cache.types; if (this.currentTypeId === 0 && this.types.length > 0) this.currentTypeId = cache.currentTypeId || this.types[0].id; return }
			try {
				const res = await getShopTypes()
				if (res.success && res.data) {
					this.types = res.data.sort((a, b) => a.sort - b.sort)
					cache.types = this.types
					if (this.currentTypeId === 0 && this.types.length > 0) this.currentTypeId = this.types[0].id
				}
			} catch (e) {}
		},
		async getLocation() {
			if (cache.lng !== null) { this.lng = cache.lng; this.lat = cache.lat; return }
			try {
				const loc = await uni.getLocation({ type: 'gcj02' })
				this.lng = loc.longitude; this.lat = loc.latitude
				cache.lng = this.lng; cache.lat = this.lat
			} catch (e) { this.lng = null; this.lat = null }
		},
		selectType(item) {
			if (this.currentTypeId === item.id) return
			this.currentTypeId = item.id; cache.currentTypeId = item.id
			this.current = 1; this.noMore = false
			// 不清空旧数据，loading 期间展示上一分类的数据
			this.loadShops(true)
		},
		async loadShops(refresh) {
			if (this.loading || !this.currentTypeId) return
			if (refresh) { this.current = 1; this.noMore = false }
			if (this.noMore) return
			this.loading = true
			try {
				const res = await getShopsByType(this.currentTypeId, this.current, this.lng, this.lat)
				if (res.success) {
					const list = res.data || []
					this.shops = refresh ? list : [...this.shops, ...list]
					cache.shops = this.shops
					if (list.length < 6) this.noMore = true
				}
			} catch (e) {} finally { this.loading = false }
		},
		handleSearchInput() {
			clearTimeout(this.searchTimer)
			if (!this.keyword.trim()) {
				this.searchResults = []
				this.searchLoading = false
				return
			}
			this.searchTimer = setTimeout(() => this.searchCurrentShops(), 300)
		},
		async searchCurrentShops() {
			const keyword = this.keyword.trim()
			if (!keyword) return
			clearTimeout(this.searchTimer)
			const requestId = ++this.searchRequestId
			this.searchLoading = true
			try {
				const res = await searchShops(keyword)
				if (requestId === this.searchRequestId && res.success) {
					this.searchResults = res.data || []
				}
			} catch (e) {} finally {
				if (requestId === this.searchRequestId) this.searchLoading = false
			}
		},
		clearSearch() {
			clearTimeout(this.searchTimer)
			this.searchRequestId++
			this.keyword = ''
			this.searchResults = []
			this.searchLoading = false
		}
	},
	onReachBottom() { if (!this.isSearching && !this.loading && !this.noMore) { this.current++; this.loadShops(false) } },
	onPullDownRefresh() {
		this.getLocation().then(() => { this.current = 1; this.noMore = false; this.loadShops(true); setTimeout(() => uni.stopPullDownRefresh(), 500) })
	}
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; padding-bottom: calc(120rpx + env(safe-area-inset-bottom)); }
.header-fixed { position: sticky; top: 0; z-index: 200; background: #fff; }
.status-bar { height: var(--status-bar-height); }
.search-row { padding: 12rpx 24rpx 16rpx; }
.search-box { display: flex; align-items: center; height: 72rpx; background: #f5f5f5; border-radius: 40rpx; padding: 0 20rpx; }
.search-icon { display: flex; align-items: center; justify-content: center; width: 40rpx; height: 40rpx; margin-right: 12rpx; flex-shrink: 0; }
.search-input { flex: 1; min-width: 0; height: 72rpx; font-size: 28rpx; color: #333; }
.search-placeholder { color: #999; font-size: 28rpx; }
.clear-search { width: 48rpx; height: 48rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.filter-fixed { position: sticky; top: calc(var(--status-bar-height) + 100rpx); z-index: 199; background: #fff; padding: 0 0 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.filter-scroll { width: 100%; white-space: nowrap; }
.filter-list { display: inline-flex; padding: 8rpx 20rpx 0; gap: 12rpx; }
.filter-btn { display: inline-flex; align-items: center; gap: 10rpx; padding: 14rpx 28rpx; border-radius: 32rpx; background: #f5f5f5; font-size: 26rpx; color: #666; flex-shrink: 0; transition: all .2s; }
.filter-btn.active { background: linear-gradient(135deg, #ff6600, #ff8833); color: #fff; font-weight: 600; box-shadow: 0 4rpx 12rpx rgba(255,102,0,.25); }
.filter-icon { width: 36rpx; height: 36rpx; }
.shop-list { padding: 20rpx 20rpx 0; }
.load-tip { display: flex; align-items: center; justify-content: center; padding: 30rpx; font-size: 24rpx; color: #999; gap: 12rpx; }
.load-tip.no-more { color: #ccc; }
.empty-wrap { display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; }
.empty-icon { display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }
</style>
