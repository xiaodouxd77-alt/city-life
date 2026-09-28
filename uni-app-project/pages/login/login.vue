<template>
	<view class="container">
		<view class="status-bar"></view>
		<view class="login-box">
			<view class="logo-area">
				<text class="app-icon"></text>
				<text class="app-name">优质生活</text>
				<text class="app-slogan">发现身边好去处</text>
			</view>

			<!-- 登录方式切换 -->
			<view class="login-tabs">
				<view :class="['tab', mode === 'code' ? 'active' : '']" @click="mode = 'code'">验证码登录</view>
				<view :class="['tab', mode === 'pwd' ? 'active' : '']" @click="mode = 'pwd'">密码登录</view>
			</view>

			<view class="form">
				<view class="input-item">
					<text class="label">手机号</text>
					<input v-model="phone" type="text" maxlength="11" placeholder="请输入11位手机号" placeholder-class="ph" />
				</view>

				<!-- 验证码登录 -->
				<view v-if="mode === 'code'" class="input-item code-item">
					<text class="label">验证码</text>
					<input v-model="code" type="text" maxlength="6" placeholder="输入6位验证码" placeholder-class="ph" />
					<button class="code-btn" :disabled="codeDisabled" @click="sendCode">{{ codeText }}</button>
				</view>

				<!-- 密码登录 -->
				<view v-if="mode === 'pwd'" class="input-item">
					<text class="label">密码</text>
					<input v-model="password" type="password" placeholder="请输入密码" placeholder-class="ph" />
				</view>
			</view>

			<button class="login-btn btn-primary" :loading="loading" @click="doLogin">登录</button>

			<view class="bottom-row">
				<text class="link" @click="goRegister">还没有账号？立即注册</text>
			</view>

			<view class="tips">
				<text>验证码登录：验证码将发送到后端控制台日志</text>
			</view>
		</view>
	</view>
</template>

<script>
import { sendCode, login, getMe } from '@/api/user.js'
import { setToken, setUserInfo } from '@/utils/auth.js'
import { connectNotificationSocket } from '@/utils/notification-socket.js'

export default {
	data() {
		return {
			mode: 'code',
			phone: '',
			code: '',
			password: '',
			codeText: '获取验证码',
			codeDisabled: false,
			countdown: 60,
			loading: false,
			backUrl: ''
		}
	},
	onLoad(options) {
		if (options.backUrl) this.backUrl = decodeURIComponent(options.backUrl)
	},
	methods: {
		async sendCode() {
			const phoneStr = String(this.phone || '').trim()
			if (!phoneStr || phoneStr.length !== 11) {
				uni.showToast({ title: '请输入11位手机号', icon: 'none' })
				return
			}
			try {
				const res = await sendCode(phoneStr)
				if (res.success) {
					uni.showToast({ title: '验证码已发送，查看后端控制台', icon: 'none', duration: 2500 })
					this.startCountdown()
				} else {
					uni.showToast({ title: res.errorMsg || '发送失败', icon: 'none' })
				}
			} catch (e) { uni.showToast({ title: '发送失败', icon: 'none' }) }
		},
		startCountdown() {
			this.codeDisabled = true; this.countdown = 60; this.codeText = '60s'
			const timer = setInterval(() => {
				this.countdown--
				if (this.countdown <= 0) { clearInterval(timer); this.codeDisabled = false; this.codeText = '获取验证码' }
				else { this.codeText = this.countdown + 's' }
			}, 1000)
		},
		async doLogin() {
			const phoneStr = String(this.phone || '').trim()
			if (!phoneStr || phoneStr.length !== 11) {
				uni.showToast({ title: '请输入11位手机号', icon: 'none' })
				return
			}
			const loginData = { phone: phoneStr }
			if (this.mode === 'code') {
				if (!String(this.code || '').trim()) { uni.showToast({ title: '请输入验证码', icon: 'none' }); return }
				loginData.code = String(this.code).trim()
			} else {
				if (!this.password) { uni.showToast({ title: '请输入密码', icon: 'none' }); return }
				loginData.password = this.password
			}
			this.loading = true
			try {
				const res = await login(loginData)
				if (res.success) {
					setToken(res.data)
					connectNotificationSocket()
					try {
						const meRes = await getMe()
						if (meRes.success && meRes.data) setUserInfo(meRes.data)
					} catch (e) {}
					uni.showToast({ title: '登录成功', icon: 'success' })
					setTimeout(() => {
						if (this.backUrl) { uni.navigateBack({ delta: 1 }) }
						else { uni.switchTab({ url: '/pages/me/me' }) }
					}, 500)
				} else {
					uni.showToast({ title: res.errorMsg || '登录失败', icon: 'none', duration: 2500 })
				}
			} catch (e) { uni.showToast({ title: '登录失败，网络错误', icon: 'none' }) }
			finally { this.loading = false }
		},
		goRegister() { uni.navigateTo({ url: '/pages/register/register' }) }
	}
}
</script>

<style scoped>
.container { min-height: 100vh; background: #fff; }
.status-bar { height: var(--status-bar-height); }
.login-box { padding: 60rpx 60rpx; }
.logo-area { display: flex; flex-direction: column; align-items: center; margin-bottom: 50rpx; }
.app-icon { font-size: 80rpx; }
.app-name { font-size: 48rpx; font-weight: 700; color: #333; margin-top: 16rpx; }
.app-slogan { font-size: 28rpx; color: #999; margin-top: 10rpx; }
.login-tabs { display: flex; justify-content: center; gap: 60rpx; margin-bottom: 50rpx; }
.tab { font-size: 30rpx; color: #999; padding-bottom: 12rpx; border-bottom: 4rpx solid transparent; transition: all 0.2s; }
.tab.active { color: #ff6600; font-weight: 700; border-bottom-color: #ff6600; }
.form { margin-bottom: 50rpx; }
.input-item { display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #eee; }
.label { width: 140rpx; font-size: 30rpx; color: #333; font-weight: 500; }
.input-item input { flex: 1; font-size: 30rpx; color: #333; }
.ph { color: #ccc; }
.code-item { justify-content: space-between; }
.code-btn { width: 180rpx; height: 64rpx; line-height: 64rpx; font-size: 24rpx; color: #ff6600; background: #fff5ef; border-radius: 32rpx; padding: 0; margin: 0; }
.code-btn[disabled] { color: #999; background: #f5f5f5; }
.login-btn { margin-top: 30rpx; }
.bottom-row { text-align: center; margin-top: 30rpx; }
.link { font-size: 26rpx; color: #ff6600; }
.tips { text-align: center; margin-top: 40rpx; font-size: 22rpx; color: #ccc; }
</style>
