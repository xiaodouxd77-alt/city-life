<template>
	<image
		class="user-avatar"
		:src="avatarUrl"
		:style="{ width: size + 'rpx', height: size + 'rpx', borderRadius: radius }"
		mode="aspectFill"
		@click="onClick"
	/>
</template>

<script>
import { getAvatarUrl } from '@/utils/constants.js'

export default {
	name: 'UserAvatar',
	props: {
		src: { type: String, default: '' },
		size: { type: Number, default: 80 },
		radius: { type: String, default: '50%' }
	},
	computed: {
		avatarUrl() {
			return getAvatarUrl(this.src)
		}
	},
	methods: {
		onClick() { this.$emit('click') }
	}
}
</script>

<style scoped>
.user-avatar {
	background-color: #f0f0f0;
	flex-shrink: 0;
}
</style>
