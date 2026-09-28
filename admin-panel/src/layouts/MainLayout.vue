<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span>城市点评后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#1f2937"
        text-color="#c7d0dc"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/shops"><el-icon><Shop /></el-icon><span>商户管理</span></el-menu-item>
        <el-menu-item index="/blogs"><el-icon><Document /></el-icon><span>点评管理</span></el-menu-item>
        <el-menu-item index="/comments"><el-icon><ChatDotRound /></el-icon><span>评论管理</span></el-menu-item>
        <el-menu-item index="/vouchers"><el-icon><Ticket /></el-icon><span>优惠券管理</span></el-menu-item>
        <el-menu-item index="/orders"><el-icon><List /></el-icon><span>订单管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ currentTitle }}</div>
        <el-dropdown @command="onCommand">
          <span class="user-drop">
            <el-icon><UserFilled /></el-icon>
            <span class="user-name">{{ adminUser }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  computed: {
    activeMenu() { return this.$route.path },
    adminUser() { return localStorage.getItem('admin_user') || '管理员' },
    currentTitle() { return this.$route.meta.title || '' }
  },
  methods: {
    onCommand(cmd) {
      if (cmd === 'logout') this.logout()
    },
    logout() {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.aside {
  background: #1f2937;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background: #111827;
  letter-spacing: 1px;
}
.logo-icon { font-size: 22px; color: #409eff; }
.aside .el-menu { border-right: none; }
.aside .el-menu-item { height: 50px; }

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.header-title { font-size: 17px; font-weight: 600; color: #1f2d3d; }
.user-drop {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  outline: none;
}
.user-name { font-size: 14px; }
.main { background: #f0f2f5; padding: 20px; }
</style>
