<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">点评管理</h2>
        <p class="page-subtitle">用户发布的探店点评，可隐藏或删除违规内容</p>
      </div>
    </div>

    <el-card class="page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="搜索标题 / 内容"
            clearable
            :prefix-icon="Search"
          />
        </div>
      </div>

      <el-table :data="records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="liked" label="点赞" width="80" />
        <el-table-column prop="comments" label="评论" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status===1?'danger':'success'">{{ row.status===1?'已隐藏':'正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="toggleBlog(row)">{{ row.status===1?'显示':'隐藏' }}</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="deleteBlog(row)">删除</el-button>
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
        const res = await request.get('/admin/stats/blogs', { params: { page: this.currentPage, size: this.pageSize, keyword: this.keyword } })
        if (res.success !== false) this.applyPageResult(res)
      } catch (e) {} finally { this.loading = false }
    },
    async toggleBlog(row) {
      try { await request.put('/blog/' + row.id + '/toggle-status'); this.loadData() } catch (e) {}
    },
    async deleteBlog(row) {
      await this.$confirm('确定删除该点评？', '提示', { type: 'warning' })
      try {
        await request.delete('/blog/' + row.id)
        await this.refreshAfterMutation()
        this.$message.success('已删除')
      } catch (e) {}
    }
  }
}
</script>
