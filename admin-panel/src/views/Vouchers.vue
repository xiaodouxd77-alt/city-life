<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">优惠券管理</h2>
        <p class="page-subtitle">平台普通券与秒杀券，可新增与删除</p>
      </div>
    </div>

    <el-card class="page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="搜索优惠券标题"
            clearable
            :prefix-icon="Search"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="showAddDialog">新增优惠券</el-button>
      </div>

      <el-table :data="records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="payValue" label="支付金额" width="110" />
        <el-table-column prop="actualValue" label="优惠面值" width="110" />
        <el-table-column label="类型" width="90">
          <template #default="{row}"><el-tag :type="row.type===1?'danger':'info'">{{ row.type===1?'秒杀':'普通' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'warning'">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="danger" :icon="Delete" @click="deleteVoucher(row)">删除</el-button>
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

    <el-dialog title="新增优惠券" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subTitle" /></el-form-item>
        <el-form-item label="支付金额"><el-input-number v-model="form.payValue" :min="0" /></el-form-item>
        <el-form-item label="优惠面值"><el-input-number v-model="form.actualValue" :min="0" /></el-form-item>
        <el-form-item label="店铺ID"><el-input-number v-model="form.shopId" :min="1" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="0">普通券</el-radio>
            <el-radio :label="1">秒杀券</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.type===1">
          <el-form-item label="库存"><el-input-number v-model="form.stock" :min="1" /></el-form-item>
          <el-form-item label="开始时间"><el-date-picker v-model="form.beginTime" type="datetime" /></el-form-item>
          <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveVoucher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'
import serverPage from '../mixins/serverPage'

export default {
  mixins: [serverPage],
  data() {
    return {
      Search, Plus, Delete,
      dialogVisible: false,
      form: { title:'',subTitle:'',payValue:0,actualValue:0,shopId:1,type:0,stock:100,beginTime:'',endTime:'' }
    }
  },
  mounted() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/admin/stats/vouchers', { params: { page: this.currentPage, size: this.pageSize, keyword: this.keyword } })
        if (res.success !== false) this.applyPageResult(res)
      } catch (e) {} finally { this.loading = false }
    },
    showAddDialog() {
      this.form = { title:'',subTitle:'',payValue:0,actualValue:0,shopId:1,type:0,stock:100,beginTime:'',endTime:'' }
      this.dialogVisible = true
    },
    async saveVoucher() {
      try {
        if (this.form.type === 1) {
          await request.post('/voucher/seckill', {
            title:this.form.title, subTitle:this.form.subTitle, payValue:this.form.payValue,
            actualValue:this.form.actualValue, shopId:this.form.shopId, type:1, status:1,
            stock:this.form.stock, beginTime:this.form.beginTime, endTime:this.form.endTime
          })
        } else {
          await request.post('/voucher', this.form)
        }
        this.dialogVisible = false
        this.loadData()
        this.$message.success('创建成功')
      } catch (e) {}
    },
    async deleteVoucher(row) {
      await this.$confirm('确定删除该优惠券？', '提示', { type: 'warning' })
      try {
        await request.delete('/voucher/' + row.id)
        await this.refreshAfterMutation()
        this.$message.success('已删除')
      } catch (e) {}
    }
  }
}
</script>
