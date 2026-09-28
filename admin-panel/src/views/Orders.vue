<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">订单管理</h2>
        <p class="page-subtitle">优惠券购买订单，可对已支付订单办理退款</p>
      </div>
    </div>

    <el-card class="page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="搜索订单号 / 用户ID"
            clearable
            :prefix-icon="Search"
          />
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:150px" @change="reloadFirstPage">
            <el-option v-for="(t,k) in statusMap" :key="k" :label="t" :value="Number(k)" />
          </el-select>
        </div>
      </div>

      <el-table :data="records" border stripe v-loading="loading">
        <el-table-column prop="id" label="订单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="voucherId" label="券ID" width="90" />
        <el-table-column label="支付方式" width="110">
          <template #default="{row}">{{ payTypeText(row.payType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="190" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status===2" size="small" type="warning" @click="refundOrder(row)">退款</el-button>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'
import serverPage from '../mixins/serverPage'

export default {
  mixins: [serverPage],
  data() {
    return {
      Search,
      filterStatus: '',
      statusMap: { 1:'待支付', 2:'已支付', 3:'已核销', 4:'已取消', 5:'退款中', 6:'已退款' }
    }
  },
  mounted() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/admin/stats/orders', {
          params: { page: this.currentPage, size: this.pageSize, keyword: this.keyword, status: this.filterStatus === '' ? undefined : this.filterStatus }
        })
        if (res.success !== false) this.applyPageResult(res)
      } catch (e) {} finally { this.loading = false }
    },
    payTypeText(t) { return {1:'余额',2:'支付宝',3:'微信'}[t] || '-' },
    statusText(s) { return this.statusMap[s] || '未知' },
    statusTag(s) { return s===1?'warning':s===2?'success':s===6?'danger':'info' },
    async refundOrder(row) {
      await this.$confirm('确定为该订单办理退款？', '提示', { type: 'warning' })
      try { await request.post('/voucher-order/' + row.id + '/refund'); this.loadData(); this.$message.success('已退款') } catch (e) {}
    }
  }
}
</script>
