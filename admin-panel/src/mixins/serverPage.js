/**
 * 管理后台列表的后端分页状态。
 * 子页面负责实现 loadData()，并用 records 渲染当前页数据。
 */
export default {
  data() {
    return {
      records: [],
      total: 0,
      loading: false,
      keyword: '',
      currentPage: 1,
      pageSize: 10,
      searchTimer: null
    }
  },
  watch: {
    keyword() {
      window.clearTimeout(this.searchTimer)
      this.searchTimer = window.setTimeout(() => this.reloadFirstPage(), 300)
    }
  },
  beforeUnmount() {
    window.clearTimeout(this.searchTimer)
  },
  methods: {
    applyPageResult(res) {
      this.records = res.data || []
      this.total = Number(res.total || 0)
    },
    reloadFirstPage() {
      this.currentPage = 1
      return this.loadData()
    },
    onPageChange(page) {
      this.currentPage = page
      return this.loadData()
    },
    onSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      return this.loadData()
    },
    async refreshAfterMutation() {
      await this.loadData()
      if (this.records.length === 0 && this.currentPage > 1) {
        this.currentPage -= 1
        await this.loadData()
      }
    }
  }
}
