<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">仪表盘</h2>
        <p class="page-subtitle">平台核心数据概览</p>
      </div>
    </div>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="c in cards" :key="c.key">
        <el-card class="page-card stat-card">
          <div class="stat-icon" :style="{ background: c.bg }">
            <el-icon :size="26"><component :is="c.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats[c.key] || 0 }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12"><el-card class="page-card"><div ref="trendChart" style="height:320px"></div></el-card></el-col>
      <el-col :span="12"><el-card class="page-card"><div ref="overviewChart" style="height:320px"></div></el-card></el-col>
    </el-row>
  </div>
</template>

<script>
import request from '../utils/request'
import * as echarts from 'echarts'

export default {
  data() {
    return {
      stats: { users: 0, shops: 0, blogs: 0, orders: 0 },
      trendData: {},
      overviewData: {},
      cards: [
        { key: 'users', label: '总用户数', icon: 'User', bg: 'linear-gradient(135deg,#409eff,#66b1ff)' },
        { key: 'shops', label: '总商户数', icon: 'Shop', bg: 'linear-gradient(135deg,#67c23a,#85ce61)' },
        { key: 'blogs', label: '总点评数', icon: 'Document', bg: 'linear-gradient(135deg,#e6a23c,#ebb563)' },
        { key: 'orders', label: '总订单数', icon: 'List', bg: 'linear-gradient(135deg,#f56c6c,#f78989)' }
      ]
    }
  },
  async mounted() {
    try {
      const res = await request.get('/admin/stats/dashboard')
      if (res.success !== false) this.stats = res.data || res
    } catch (e) {}
    try {
      const trendRes = await request.get('/admin/stats/trend')
      if (trendRes.success !== false) this.trendData = trendRes.data || {}
    } catch (e) {}
    try {
      const overviewRes = await request.get('/admin/stats/overview')
      if (overviewRes.success !== false) this.overviewData = overviewRes.data || {}
    } catch (e) {}
    this.initTrendChart()
    this.initOverviewChart()
  },
  methods: {
    initTrendChart() {
      const chart = echarts.init(this.$refs.trendChart)
      const d = this.trendData || {}
      chart.setOption({
        title: { text: '7日新增趋势', left: 'center', textStyle: { fontSize: 14 } },
        tooltip: { trigger: 'axis' },
        legend: { bottom: 0 },
        grid: { top: 50, left: 40, right: 20, bottom: 40 },
        xAxis: { type: 'category', data: d.days || ['7天前','6天前','5天前','4天前','3天前','2天前','昨天'] },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          { name: '新增用户', type: 'line', data: d.newUsers || [0,0,0,0,0,0,0], smooth: true, areaStyle: { opacity: 0.15 } },
          { name: '新增笔记', type: 'line', data: d.newBlogs || [0,0,0,0,0,0,0], smooth: true, areaStyle: { opacity: 0.15 } }
        ]
      })
    },
    initOverviewChart() {
      const chart = echarts.init(this.$refs.overviewChart)
      const d = this.overviewData || {}
      chart.setOption({
        title: { text: '本月运营概览', left: 'center', textStyle: { fontSize: 14 } },
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['40%','65%'], center: ['50%','50%'],
          data: [
            { value: d.activeUsers || 0, name: '活跃用户' },
            { value: d.orderUsers || 0, name: '下单用户' }
          ]
        }]
      })
    }
  }
}
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-num { font-size: 26px; font-weight: 700; color: #1f2d3d; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
