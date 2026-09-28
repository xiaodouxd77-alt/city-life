<template>
	<view class="empty">
		<uni-icons type="info-filled" size="64" color="#cccccc"></uni-icons>
		<text class="empty-text">{{ text }}</text>
		<slot></slot>
	</view>
</template>

<script>
export default {
	name: 'Empty',
	props: {
		text: {
			type: String,
			default: '暂无数据'
		}
	}
}
</script>

<style scoped>
.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 40rpx;
}

.empty-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #999999;
}
</style>
