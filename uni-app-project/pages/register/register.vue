<template>
	<view class="container">
		<nav-bar title="注册账号" :showBack="true"></nav-bar>

		<view class="form-wrap">
			<!-- 头像 -->
			<view class="avatar-section" @click="chooseAvatar">
				<image :src="avatarUrl" mode="aspectFill" class="avatar-img" />
				<view class="avatar-overlay">
					<text>点击上传头像</text>
				</view>
			</view>

			<view class="input-item">
				<text class="label">昵称</text>
				<input v-model="nickName" placeholder="给自己起个昵称" placeholder-class="ph" maxlength="20" />
			</view>
			<view class="input-item">
				<text class="label">手机号</text>
				<input v-model="phone" type="text" maxlength="11" placeholder="请输入11位手机号" placeholder-class="ph" />
			</view>
			<view class="input-item">
				<text class="label">密码</text>
				<input v-model="password" type="password" placeholder="至少6位密码" placeholder-class="ph" />
			</view>
			<view class="input-item">
				<text class="label">确认密码</text>
				<input v-model="password2" type="password" placeholder="再次输入密码" placeholder-class="ph" />
			</view>

			<button class="reg-btn btn-primary" :loading="loading" @click="doRegister">注 册</button>

			<view class="bottom-row">
				<text class="link" @click="goLogin">已有账号？立即登录</text>
			</view>
		</view>
	</view>
</template>

<script>
import { register } from '@/api/user.js'
import { uploadImage } from '@/api/upload.js'
import { setToken, setUserInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			nickName: '',
			phone: '',
			password: '',
			password2: '',
			avatarUrl: '/static/avatar-default.svg',
			avatarFile: '',
			loading: false
		}
	},
	methods: {
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.avatarFile = res.tempFilePaths[0]
					this.avatarUrl = res.tempFilePaths[0]
				}
			})
		},
		async doRegister() {
			const nick = this.nickName.trim()
			const phoneStr = String(this.phone || '').trim()
			if (!nick) { uni.showToast({ title: '请输入昵称', icon: 'none' }); return }
			if (!phoneStr || phoneStr.length !== 11) { uni.showToast({ title: '请输入11位手机号', icon: 'none' }); return }
			if (!this.password || this.password.length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return }
			if (this.password !== this.password2) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }

			this.loading = true
			uni.showLoading({ title: '注册中...' })

			try {
				// 上传头像
				let iconUrl = ''
				if (this.avatarFile) {
					try {
						const uploadRes = await uploadImage(this.avatarFile)
						if (uploadRes.success) iconUrl = uploadRes.data
					} catch (e) { /* 头像上传失败继续注册 */ }
				}

				const res = await register({
					nickName: nick,
					phone: phoneStr,
					password: this.password,
					icon: iconUrl
				})
				if (res.success) {
					setToken(res.data)
					uni.showToast({ title: '注册成功', icon: 'success' })
					setTimeout(() => { uni.switchTab({ url: '/pages/me/me' }) }, 500)
				} else {
					uni.showToast({ title: res.errorMsg || '注册失败', icon: 'none', duration: 2500 })
				}
			} catch (e) {
				uni.showToast({ title: '注册失败，网络错误', icon: 'none' })
			} finally {
				uni.hideLoading()
				this.loading = false
			}
		},
		goLogin() { const p = getCurrentPages(); p.length > 1 ? uni.navigateBack() : uni.switchTab({ url: '/pages/me/me' }) }
	}
}
</script>

<style scoped>
.container { min-height: 100vh; background: #fff; }
.form-wrap { padding: 40rpx 60rpx; }
.avatar-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 60rpx; position: relative; }
.avatar-img { width: 160rpx; height: 160rpx; border-radius: 50%; background: #f0f0f0; }
.avatar-overlay { margin-top: 16rpx; }
.avatar-overlay text { font-size: 26rpx; color: #ff6600; }
.input-item { display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #eee; margin-bottom: 8rpx; }
.label { width: 160rpx; font-size: 28rpx; color: #333; font-weight: 500; }
.input-item input { flex: 1; font-size: 28rpx; color: #333; }
.ph { color: #ccc; font-size: 28rpx; }
.reg-btn { margin-top: 50rpx; }
.bottom-row { text-align: center; margin-top: 30rpx; }
.link { font-size: 26rpx; color: #ff6600; }
</style>
