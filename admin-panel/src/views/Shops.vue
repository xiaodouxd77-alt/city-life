<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">商户管理</h2>
        <p class="page-subtitle">管理平台入驻商户，可手动新增、编辑与删除</p>
      </div>
    </div>

    <el-card class="page-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="搜索店铺名称 / 地址"
            clearable
            :prefix-icon="Search"
          />
          <el-select v-model="filterType" placeholder="全部类型" clearable style="width:160px" @change="reloadFirstPage">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增商户</el-button>
      </div>

      <el-table :data="records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="90">
          <template #default="{row}">
            <el-image
              v-if="firstImage(row.images)"
              class="cell-thumb"
              :src="firstImage(row.images)"
              :preview-src-list="imageList(row.images)"
              :preview-teleported="true"
              fit="cover"
            />
            <div v-else class="cell-thumb-empty"><el-icon><Picture /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{row}">{{ typeName(row.typeId) }}</template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="均价" width="90">
          <template #default="{row}">{{ row.avgPrice ? '¥' + row.avgPrice : '-' }}</template>
        </el-table-column>
        <el-table-column label="评分" width="90">
          <template #default="{row}">{{ (row.score / 10).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="sold" label="销量" width="80" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{row}">
            <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="deleteShop(row)">删除</el-button>
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

    <el-dialog :title="editing ? '编辑商户' : '新增商户'" v-model="dialogVisible" width="640px" @closed="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.typeId" placeholder="请选择类型" style="width:100%">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="商圈">
          <el-input v-model="form.area" placeholder="如 陆家嘴" />
        </el-form-item>
        <el-form-item label="均价">
          <el-input-number v-model="form.avgPrice" :min="0" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.openHours" placeholder="如 10:00-22:00" />
        </el-form-item>
        <el-form-item label="店铺图片">
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :http-request="uploadImage"
            :on-remove="handleRemove"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveShop">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search, Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import request from '../utils/request'
import serverPage from '../mixins/serverPage'

export default {
  mixins: [serverPage],
  data() {
    return {
      Search, Plus, Edit, Delete, Picture,
      filterType: '',
      types: [],
      dialogVisible: false,
      editing: null,
      saving: false,
      form: {},
      fileList: []
    }
  },
  mounted() {
    this.loadTypes()
    this.loadData()
  },
  methods: {
    async loadTypes() {
      try {
        const res = await request.get('/shop-type/list')
        if (res.success !== false) this.types = res.data || []
      } catch (e) {}
    },
    async loadData() {
      this.loading = true
      try {
        const res = await request.get('/admin/stats/shops', {
          params: { page: this.currentPage, size: this.pageSize, keyword: this.keyword, typeId: this.filterType || undefined }
        })
        if (res.success !== false) this.applyPageResult(res)
      } catch (e) {} finally { this.loading = false }
    },
    typeName(id) {
      const t = this.types.find(t => t.id === id)
      return t ? t.name : '-'
    },
    imageList(images) {
      return images ? String(images).split(',').filter(Boolean) : []
    },
    firstImage(images) {
      return this.imageList(images)[0] || ''
    },
    openAdd() {
      this.editing = null
      this.form = { name: '', typeId: '', address: '', area: '', avgPrice: 0, openHours: '' }
      this.fileList = []
      this.dialogVisible = true
    },
    openEdit(row) {
      this.editing = row
      this.form = { ...row }
      // 回显已有图片
      this.fileList = this.imageList(row.images).map((url, i) => ({ name: 'img-' + i, url }))
      this.dialogVisible = true
    },
    resetForm() {
      this.editing = null
      this.form = {}
      this.fileList = []
    },
    // 自定义上传：走 /upload/blog，成功后把 OSS url 写回该文件项
    async uploadImage(option) {
      const fd = new FormData()
      fd.append('file', option.file)
      try {
        const res = await request.post('/upload/blog', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
        const url = res.data || res
        option.onSuccess({ url })
        // el-upload 的 file-list 项补上 url，供提交时收集
        const item = this.fileList.find(f => f.uid === option.file.uid)
        if (item) item.url = url
      } catch (e) {
        option.onError(e)
        this.$message.error('图片上传失败')
      }
    },
    handleRemove() { /* file-list 由 v-model 自动同步 */ },
    collectImages() {
      return this.fileList
        .map(f => f.url || (f.response && f.response.url))
        .filter(Boolean)
        .join(',')
    },
    async saveShop() {
      if (!this.form.name) { this.$message.warning('请输入店铺名称'); return }
      this.saving = true
      const payload = { ...this.form, images: this.collectImages() }
      try {
        if (this.editing) {
          await request.put('/shop', payload)
        } else {
          await request.post('/shop', payload)
        }
        this.dialogVisible = false
        this.loadData()
        this.$message.success('保存成功')
      } catch (e) {} finally { this.saving = false }
    },
    async deleteShop(row) {
      await this.$confirm('确定删除该商户？', '提示', { type: 'warning' })
      try {
        await request.delete('/shop/' + row.id)
        await this.refreshAfterMutation()
        this.$message.success('删除成功')
      } catch (e) {}
    }
  }
}
</script>
