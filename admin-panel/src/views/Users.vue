<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">平台注册用户列表</p>
      </div>
    </div>

    <el-card class="page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="搜索昵称 / 手机号"
            clearable
            :prefix-icon="Search"
          />
        </div>
      </div>

      <el-table :data="records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{row}">
            <el-avatar :size="40" :src="row.icon">{{ (row.nickName || 'U').charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="nickName" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="注册时间" width="190" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="danger" :icon="Delete" @click="deleteUser(row)">删除</el-button>
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
import { Search, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'
import serverPage from '../mixins/serverPage'

export default {
  mixins: [serverPage],
  data() {
    return { Search, Delete }
  },
  mounted() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/admin/stats/users', { params: { page: this.currentPage, size: this.pageSize, keyword: this.keyword } })
        if (res.success !== false) this.applyPageResult(res)
      } catch (e) {} finally { this.loading = false }
    },
    async deleteUser(row) {
      await this.$confirm('确定删除该用户？', '提示', { type: 'warning' })
      try {
        await request.delete('/user/' + row.id)
        await this.refreshAfterMutation()
        this.$message.success('删除成功')
      } catch (e) {}
    }
  }
}
</script>
