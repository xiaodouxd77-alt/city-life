<template>
	<view class="nav-bar-container" :style="{ paddingTop: statusBarHeight + 'px' }">
		<view class="nav-bar" :style="{ backgroundColor: bgColor }">
			<view v-if="showBack" class="nav-back" @click.stop="goBack">
				<uni-icons type="left" size="22" color="#333"></uni-icons>
			</view>
			<text class="nav-title" :style="{ color: titleColor }">{{ title }}</text>
			<view class="nav-right"><slot name="right"></slot></view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'NavBar',
	props: {
		title: { type: String, default: '' },
		showBack: { type: Boolean, default: true },
		bgColor: { type: String, default: '#ffffff' },
		titleColor: { type: String, default: '#333333' }
	},
	data() {
		return { statusBarHeight: 0 }
	},
	created() {
		this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
	},
	methods: {
		goBack() {
			const pages = getCurrentPages()
			if (pages.length > 1) {
				uni.navigateBack()
			} else {
				uni.switchTab({ url: '/pages/home/home' })
			}
		}
	}
}
</script>

<style scoped>
.nav-bar-container { position: sticky; top: 0; z-index: 100; }
.nav-bar { height: 88rpx; display: flex; align-items: center; justify-content: center; position: relative; border-bottom: 1rpx solid #f0f0f0; }
.nav-title { font-size: 34rpx; font-weight: 600; }
.nav-back { position: absolute; left: 10rpx; top: 50%; transform: translateY(-50%); padding: 16rpx; z-index: 10; }
.nav-right { position: absolute; right: 20rpx; top: 50%; transform: translateY(-50%); }
</style>
