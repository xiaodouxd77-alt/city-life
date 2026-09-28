<template>
	<view class="container">
		<nav-bar title="编辑资料" :back="true"></nav-bar>

		<!-- 头像 -->
		<view class="section">
			<view class="section-title">头像</view>
			<view class="avatar-row" @click="changeAvatar">
				<user-avatar :src="form.icon" :size="120" />
				<view class="avatar-tip">点击更换头像</view>
				<uni-icons type="right" size="16" color="#ccc"></uni-icons>
			</view>
		</view>

		<!-- 基本资料 -->
		<view class="section">
			<view class="section-title">基本资料</view>
			<view class="form-item">
				<text class="label">昵称</text>
				<input class="input" v-model="form.nickName" placeholder="请输入昵称" maxlength="20" />
			</view>
		</view>

		<!-- 个人信息 -->
		<view class="section">
			<view class="section-title">个人信息</view>
			<view class="form-item">
				<text class="label">城市</text>
				<input class="input" v-model="form.city" placeholder="请输入所在城市" maxlength="20" />
			</view>
			<view class="form-item form-item-textarea">
				<text class="label">简介</text>
				<textarea class="textarea" v-model="form.introduce" placeholder="介绍一下自己吧" maxlength="128" />
			</view>

			<!-- 性别选择器：picker 包裹点击区域 -->
			<picker mode="selector" :range="genderOptions" :value="genderIndex" @change="onGenderChange">
				<view class="form-item">
					<text class="label">性别</text>
					<text class="value" :class="{ placeholder: genderIndex === 0 }">{{ genderText }}</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
			</picker>

			<!-- 日期选择器：picker 包裹点击区域 -->
			<picker mode="date" :value="form.birthday" :end="maxDate" @change="onDateChange">
				<view class="form-item">
					<text class="label">生日</text>
					<text class="value" :class="{ placeholder: !form.birthday }">{{ form.birthday || '请选择生日' }}</text>
					<uni-icons type="right" size="16" color="#ccc"></uni-icons>
				</view>
			</picker>
		</view>

		<!-- 保存按钮 -->
		<view class="btn-area">
			<button class="save-btn" :loading="saving" @click="handleSave">保存</button>
		</view>
	</view>
</template>

<script>
import { getMe, getUserInfo, updateProfile, updateUserInfo, uploadAvatar } from '@/api/user.js'
import { getUserInfo as getLocalUser, setUserInfo } from '@/utils/auth.js'

export default {
	data() {
		return {
			form: {
				icon: '',
				nickName: '',
				city: '',
				introduce: '',
				gender: null,
				birthday: ''
			},
			saving: false,
			genderOptions: ['保密', '男', '女'],
			maxDate: new Date().toISOString().split('T')[0]
		}
	},
	computed: {
		genderText() {
			if (this.form.gender === null || this.form.gender === undefined) return '保密'
			return this.form.gender ? '女' : '男'
		},
		genderIndex() {
			if (this.form.gender === null || this.form.gender === undefined) return 0
			return this.form.gender ? 2 : 1
		}
	},
	async onLoad() {
		await this.loadData()
	},
	methods: {
		async loadData() {
			const local = getLocalUser()
			if (local) {
				this.form.icon = local.icon || ''
				this.form.nickName = local.nickName || ''
			}
			try {
				const res = await getMe()
				if (res.success && res.data) {
					this.form.icon = res.data.icon || this.form.icon
					this.form.nickName = res.data.nickName || this.form.nickName
				}
			} catch (e) {}
			if (local && local.id) {
				try {
					const infoRes = await getUserInfo(local.id)
					if (infoRes.success && infoRes.data) {
						const d = infoRes.data
						this.form.city = d.city || ''
						this.form.introduce = d.introduce || ''
						if (d.gender !== undefined && d.gender !== null) {
							this.form.gender = d.gender
						}
						this.form.birthday = d.birthday || ''
					}
				} catch (e) {}
			}
		},
		changeAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: async (res) => {
					const filePath = res.tempFilePaths[0]
					try {
						uni.showLoading({ title: '上传中...' })
						const uploadRes = await uploadAvatar(filePath)
						uni.hideLoading()
						if (uploadRes.success) {
							this.form.icon = uploadRes.data
							setUserInfo({ ...getLocalUser(), icon: uploadRes.data })
							uni.showToast({ title: '头像已更新', icon: 'success' })
						} else {
							uni.showToast({ title: '上传失败', icon: 'none' })
						}
					} catch (e) {
						uni.hideLoading()
						uni.showToast({ title: '上传失败', icon: 'none' })
					}
				}
			})
		},
		onGenderChange(e) {
			// index: 0=保密(null), 1=男(false), 2=女(true)
			const map = { 0: null, 1: false, 2: true }
			this.form.gender = map[e.detail.value]
		},
		onDateChange(e) {
			this.form.birthday = e.detail.value
		},
		async handleSave() {
			if (!this.form.nickName || !this.form.nickName.trim()) {
				uni.showToast({ title: '请输入昵称', icon: 'none' })
				return
			}
			this.saving = true
			try {
				const profileRes = await updateProfile({
					nickName: this.form.nickName,
					icon: this.form.icon
				})
				if (!profileRes.success) {
					uni.showToast({ title: profileRes.errorMsg || '保存失败', icon: 'none' })
					return
				}
				const infoRes = await updateUserInfo({
					city: this.form.city,
					introduce: this.form.introduce,
					gender: this.form.gender,
					birthday: this.form.birthday || null
				})
				if (!infoRes.success) {
					uni.showToast({ title: infoRes.errorMsg || '保存失败', icon: 'none' })
					return
				}
				const local = getLocalUser() || {}
				local.nickName = this.form.nickName
				local.icon = this.form.icon
				setUserInfo(local)
				uni.showToast({ title: '保存成功', icon: 'success' })
				setTimeout(() => { uni.navigateBack() }, 1000)
			} catch (e) {
				uni.showToast({ title: '保存失败', icon: 'none' })
			} finally {
				this.saving = false
			}
		}
	}
}
</script>

<style scoped>
.container { background-color: #f5f5f5; min-height: 100vh; }
.section { background-color: #ffffff; margin-bottom: 20rpx; }
.section-title { font-size: 24rpx; color: #999999; padding: 20rpx 24rpx 8rpx; }
.avatar-row { display: flex; align-items: center; padding: 20rpx 24rpx; }
.avatar-tip { flex: 1; margin-left: 24rpx; font-size: 26rpx; color: #999999; }
.form-item { display: flex; align-items: center; padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5; }
.form-item:last-child { border-bottom: none; }
.form-item-textarea { align-items: flex-start; }
.label { width: 120rpx; font-size: 28rpx; color: #333333; flex-shrink: 0; padding-top: 2rpx; }
.input { flex: 1; font-size: 28rpx; color: #333333; text-align: right; }
.textarea { flex: 1; font-size: 28rpx; color: #333333; min-height: 120rpx; padding: 10rpx 0; width: 100%; box-sizing: border-box; }
.value { flex: 1; font-size: 28rpx; color: #333333; text-align: right; margin-right: 8rpx; }
.value.placeholder { color: #cccccc; }
.btn-area { padding: 40rpx 24rpx; }
.save-btn { width: 100%; height: 88rpx; line-height: 88rpx; text-align: center; background: linear-gradient(135deg, #ff6600, #ff8833); color: #ffffff; border-radius: 44rpx; font-size: 30rpx; border: none; }
.save-btn::after { border: none; }
</style>
