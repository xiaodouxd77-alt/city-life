<script>
	import { isLogin } from './utils/auth.js'
	import { connectNotificationSocket } from './utils/notification-socket.js'

	export default {
		onLaunch: function() {
			console.log('App Launch')
			if (isLogin()) connectNotificationSocket()
		},
		onShow: function() {
			console.log('App Show')
			if (isLogin()) connectNotificationSocket()
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/* 隐藏默认 tabbar（用自定义组件替代） */
	.uni-tabbar { display: none !important; }
	.uni-tabbar__item { display: none !important; }

	/* 全局公共样式 */
	page {
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
		background-color: #f5f5f5;
		color: #333333;
	}

	.container {
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	/* 安全区域适配 */
	.safe-bottom {
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}

	/* 通用卡片样式 */
	.card {
		background-color: #ffffff;
		border-radius: 16rpx;
		margin: 20rpx;
		padding: 24rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	}

	/* 按钮 */
	.btn-primary {
		background: linear-gradient(135deg, #ff6600, #ff8833);
		color: #ffffff;
		border-radius: 40rpx;
		height: 80rpx;
		line-height: 80rpx;
		font-size: 30rpx;
		text-align: center;
		border: none;
	}

	.btn-primary::after {
		border: none;
	}

	.btn-plain {
		background-color: #ffffff;
		color: #ff6600;
		border: 2rpx solid #ff6600;
		border-radius: 40rpx;
		height: 64rpx;
		line-height: 64rpx;
		font-size: 26rpx;
		padding: 0 32rpx;
	}

	.btn-plain::after {
		border: none;
	}

	/* 文本省略 */
	.ellipsis {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.ellipsis-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	/* 标签 */
	.tag {
		display: inline-block;
		padding: 4rpx 16rpx;
		background-color: #fff5ef;
		color: #ff6600;
		font-size: 22rpx;
		border-radius: 8rpx;
		margin-right: 12rpx;
	}

	/* 评分 */
	.score {
		color: #ff6600;
		font-weight: 600;
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 40rpx;
		color: #999999;
	}

	.empty-state text {
		margin-top: 20rpx;
		font-size: 28rpx;
	}

	/* 导航栏占位 */
	.nav-bar {
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #ffffff;
		position: relative;
	}

	.nav-title {
		font-size: 34rpx;
		font-weight: 600;
		color: #333333;
	}

	.nav-back {
		position: absolute;
		left: 20rpx;
		top: 50%;
		transform: translateY(-50%);
		padding: 16rpx;
	}

	/* 状态栏占位 */
	.status-bar {
		height: var(--status-bar-height);
		background-color: #ffffff;
	}
</style>
