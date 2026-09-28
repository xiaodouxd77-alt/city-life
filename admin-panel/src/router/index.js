import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'shops', name: 'Shops', component: () => import('../views/Shops.vue'), meta: { title: '商户管理' } },
      { path: 'blogs', name: 'Blogs', component: () => import('../views/Blogs.vue'), meta: { title: '点评管理' } },
      { path: 'comments', name: 'Comments', component: () => import('../views/Comments.vue'), meta: { title: '评论管理' } },
      { path: 'vouchers', name: 'Vouchers', component: () => import('../views/Vouchers.vue'), meta: { title: '优惠券管理' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { title: '订单管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
