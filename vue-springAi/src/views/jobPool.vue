<template>
    <div class="job-pool-container">
      <!-- 顶部操作栏 -->
      <div class="operation-bar">
        <div class="left-operations">
          <el-button type="primary" @click="showAddDialog">添加职位</el-button>
          <el-button type="success" @click="showAddCategoryDialog">添加职位分类</el-button>
          <el-button type="warning" @click="showCategoryManageDialog">管理分类</el-button>
          <el-select v-model="selectedCategory" placeholder="选择职位分类" clearable @change="handleCategoryChange">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        <div class="right-operations">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索职位"
            prefix-icon="Search"
            @input="handleSearch"
          />
        </div>
      </div>
  
      <!-- 职位卡片列表 -->
      <div class="job-card-container" v-loading="loading">
        <el-row :gutter="20">
          <el-col :span="8" v-for="job in jobList" :key="job.id" class="job-card-col">
            <el-card class="job-card" shadow="hover">
              <div class="job-card-header">
                <div class="job-title-container">
                  <h3 class="job-title">{{ job.title }}</h3>
                  <el-tag size="small" :type="job.status ? 'success' : 'info'" class="status-tag">
                    {{ job.status ? '招聘中' : '已下线' }}
                  </el-tag>
                </div>
                <div class="job-salary">{{ job.salaryRange }}</div>
              </div>

              <div class="job-requirements">
                <el-tag size="small" class="requirement-tag">{{ job.educationRequirement || '学历不限' }}</el-tag>
                <el-tag size="small" class="requirement-tag">{{ job.experienceRequirement ? job.experienceRequirement + '年' : '经验不限' }}</el-tag>
                <el-tag size="small" type="info" class="requirement-tag">{{ job.categoryName }}</el-tag>
              </div>

              <div class="job-company-info">
                <div class="company-name">
                  <i class="el-icon-office-building"></i>
                  {{ job.companyName || '未设置公司' }}
                </div>
                <div class="job-location">
                  <i class="el-icon-location"></i>
                  {{ job.location || '地址未设置' }}
                </div>
              </div>

              <div class="job-card-actions">
                <el-button size="small" @click="showEditDialog(job)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(job)">删除</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
  
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
  
      <!-- 添加/编辑职位对话框 -->
      <el-dialog
        :title="dialogType === 'add' ? '添加职位' : '编辑职位'"
        v-model="dialogVisible"
        width="50%"
      >
        <el-form :model="jobForm" :rules="rules" ref="jobFormRef" label-width="100px">
          <el-form-item label="职位名称" prop="title">
            <el-input v-model="jobForm.title" />
          </el-form-item>
          <el-form-item label="职位分类" prop="categoryId">
            <el-select v-model="jobForm.categoryId" placeholder="请选择职位分类">
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="公司名称" prop="companyName">
            <el-input v-model="jobForm.companyName" />
          </el-form-item>
          <el-form-item label="工作地点" prop="location">
            <el-input v-model="jobForm.location" />
          </el-form-item>
          <el-form-item label="职位描述" prop="description">
            <el-input type="textarea" v-model="jobForm.description" rows="4" />
          </el-form-item>
          <el-form-item label="所需技能" prop="requiredSkills">
            <el-input type="textarea" v-model="jobForm.requiredSkills" rows="3" />
          </el-form-item>
          <el-form-item label="经验要求" prop="experienceRequirement">
            <el-input v-model="jobForm.experienceRequirement" />
          </el-form-item>
          <el-form-item label="学历要求" prop="educationRequirement">
            <el-select v-model="jobForm.educationRequirement">
              <el-option label="不限" value="不限" />
              <el-option label="大专" value="大专" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>
          <el-form-item label="薪资范围" prop="salaryRange">
            <el-input v-model="jobForm.salaryRange" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="jobForm.status" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 添加职位分类对话框 -->
      <el-dialog
        title="添加职位分类"
        v-model="categoryDialogVisible"
        width="40%"
      >
        <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
          <el-form-item label="分类名称" prop="name">
            <el-input v-model="categoryForm.name" />
          </el-form-item>
          <el-form-item label="分类描述" prop="description">
            <el-input type="textarea" v-model="categoryForm.description" rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="categoryDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleCategorySubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 分类管理对话框 -->
      <el-dialog
        title="分类管理"
        v-model="categoryManageDialogVisible"
        width="50%"
      >
        <el-table :data="categories" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="分类名称" width="150" />
          <el-table-column prop="description" label="分类描述" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button size="small" type="danger" @click="handleDeleteCategory(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, reactive } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Search } from '@element-plus/icons-vue'
  import axios from '@/utils/axios-config'
  
  // 响应式状态
  const jobList = ref([])
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const searchKeyword = ref('')
  const selectedCategory = ref('')
  const categories = ref([])
  const dialogVisible = ref(false)
  const dialogType = ref('add')
  const jobFormRef = ref(null)
  const categoryDialogVisible = ref(false)
  const categoryFormRef = ref(null)
  const categoryManageDialogVisible = ref(false)
  
  // 表单数据
  const jobForm = reactive({
    id: null,
    title: '',
    categoryId: '',
    description: '',
    requiredSkills: '',
    experienceRequirement: '',
    educationRequirement: '',
    salaryRange: '',
    status: true,
    companyName: '',
    location: ''
  })
  
  // 分类表单数据
  const categoryForm = reactive({
    name: '',
    description: ''
  })
  
  // 表单验证规则
  const rules = {
    title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择职位分类', trigger: 'change' }],
    description: [{ required: true, message: '请输入职位描述', trigger: 'blur' }],
    requiredSkills: [{ required: true, message: '请输入所需技能', trigger: 'blur' }]
  }
  
  // 分类表单验证规则
  const categoryRules = {
    name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
    description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }]
  }
  
  // 加载职位分类
  const loadCategories = async () => {
    try {
      const { data } = await axios.get('api/job/categories')
      if (data.success) {
        categories.value = data.data
      }
    } catch (error) {
      ElMessage.error('加载职位分类失败')
    }
  }
  
  // 加载职位列表
  const loadJobList = async () => {
    loading.value = true
    try {
      const params = {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        categoryId: selectedCategory.value
      }
      
      const { data } = await axios.get('api/job/list', { params })
      if (data.success) {
        // 处理返回的数据，确保字段格式正确
        const list = data.data.list.map(item => ({
          ...item,
          // 确保status显示正确 (可能是数字或布尔值)
          status: item.status === 1 || item.status === true
        }))
        
        jobList.value = list
        total.value = data.data.total
      } else {
        ElMessage.error(data.error || '加载职位列表失败')
      }
    } catch (error) {
      console.error('加载失败:', error)
      ElMessage.error('加载职位列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 显示添加对话框
  const showAddDialog = () => {
    dialogType.value = 'add'
    Object.assign(jobForm, {
      id: null,
      title: '',
      categoryId: '',
      description: '',
      requiredSkills: '',
      experienceRequirement: '',
      educationRequirement: '',
      salaryRange: '',
      status: true,
      companyName: '',
      location: ''
    })
    dialogVisible.value = true
  }
  
  // 显示编辑对话框
  const showEditDialog = (row) => {
    dialogType.value = 'edit'
    // 深拷贝避免直接修改表格数据
    const rowData = JSON.parse(JSON.stringify(row))
    
    // 确保status字段是布尔类型 (1->true, 0->false)
    rowData.status = rowData.status === 1 || rowData.status === true
    
    // 赋值给表单
    Object.assign(jobForm, rowData)
    dialogVisible.value = true
  }
  
  // 显示添加分类对话框
  const showAddCategoryDialog = () => {
    categoryForm.name = ''
    categoryForm.description = ''
    categoryDialogVisible.value = true
  }
  
  // 提交表单
  const handleSubmit = async () => {
    if (!jobFormRef.value) return
    
    await jobFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 转换数据格式，确保与后端兼容
          const jobData = { ...jobForm }
          
          // 确保status字段是数字类型 (true->1, false->0)
          jobData.status = jobData.status ? 1 : 0
          
          // 确保categoryId是数字类型
          if (typeof jobData.categoryId === 'string') {
            jobData.categoryId = parseInt(jobData.categoryId, 10)
          }
          
          const url = dialogType.value === 'add' ? 'api/job/add' : 'api/job/update'
          const { data } = await axios.post(url, jobData)
          
          if (data.success) {
            ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
            dialogVisible.value = false
            loadJobList()
          } else {
            ElMessage.error(data.error || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
          }
        } catch (error) {
          console.error('操作失败:', error)
          ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
        }
      }
    })
  }
  
  // 提交职位分类表单
  const handleCategorySubmit = async () => {
    if (!categoryFormRef.value) return
    
    await categoryFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const { data } = await axios.post('api/job/category/add', categoryForm)
          
          if (data.success) {
            ElMessage.success('添加分类成功')
            categoryDialogVisible.value = false
            // 重新加载分类列表
            loadCategories()
          }
        } catch (error) {
          ElMessage.error('添加分类失败')
        }
      }
    })
  }
  
  // 删除职位
  const handleDelete = async (row) => {
    try {
      await ElMessageBox.confirm('确认删除该职位?', '提示', {
        type: 'warning'
      })
      
      const { data } = await axios.delete(`api/job/delete/${row.id}`)
      if (data.success) {
        ElMessage.success('删除成功')
        loadJobList()
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
  
  // 处理分页变化
  const handlePageChange = (page) => {
    currentPage.value = page
    loadJobList()
  }
  
  // 处理分类变化
  const handleCategoryChange = () => {
    currentPage.value = 1
    loadJobList()
  }
  
  // 处理搜索
  const handleSearch = () => {
    currentPage.value = 1
    loadJobList()
  }
  
  // 显示分类管理对话框
  const showCategoryManageDialog = () => {
    categoryManageDialogVisible.value = true
  }
  
  // 删除分类
  const handleDeleteCategory = async (row) => {
    try {
      await ElMessageBox.confirm(
        `确认删除分类"${row.name}"吗？删除后该分类下的职位将无法正常显示。`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        }
      )
      
      const { data } = await axios.delete(`api/job/category/delete/${row.id}`)
      if (data.success) {
        ElMessage.success('删除分类成功')
        loadCategories()
        loadJobList()
      } else {
        ElMessage.error(data.error || '删除分类失败')
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除分类失败')
      }
    }
  }
  
  // 组件挂载时加载数据
  onMounted(() => {
    loadCategories()
    loadJobList()
  })
</script>

<style scoped>
/* 页面容器 */
.job-pool-container {
  padding: 32px 24px;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 操作栏 */
.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

.left-operations {
  display: flex;
  gap: 12px;
  align-items: center;
}

.right-operations {
  width: 240px;
}

/* 分页 */
.pagination-container {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* 职位卡片容器 */
.job-card-container {
  margin-top: 0;
}

.job-card-col {
  margin-bottom: 24px;
}

/* 职位卡片 */
.job-card {
  height: 100%;
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.job-title-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.job-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.status-tag {
  margin-left: 0;
}

.job-salary {
  color: #f43f5e;
  font-size: 16px;
  font-weight: 600;
}

/* 职位要求标签 */
.job-requirements {
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.requirement-tag {
  margin-right: 0;
  background: #f5f3ff;
  color: #6366f1;
  border: none;
}

/* 公司信息 */
.job-company-info {
  margin-bottom: 16px;
  color: #64748b;
  font-size: 14px;
}

.company-name,
.job-location {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 操作按钮 */
.job-card-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  gap: 12px;
}

/* Element Plus 组件覆盖 */
:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-button--primary) {
  background: #6366f1;
  border-color: #6366f1;
}

:deep(.el-button--primary:hover) {
  background: #4f46e5;
  border-color: #4f46e5;
}

:deep(.el-button--success) {
  background: #10b981;
  border-color: #10b981;
}

:deep(.el-button--success:hover) {
  background: #059669;
  border-color: #059669;
}

:deep(.el-button--danger) {
  background: #f43f5e;
  border-color: #f43f5e;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
}

:deep(.el-tag--success) {
  background: #ecfdf5;
  color: #10b981;
  border: none;
}

:deep(.el-tag--info) {
  background: #f8fafc;
  color: #64748b;
  border: none;
}
</style>