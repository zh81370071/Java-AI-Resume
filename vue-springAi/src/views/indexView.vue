<template>
  <div class="app-layout">
    <header class="app-header">
      <rHeader></rHeader>
    </header>
    <main class="app-main">
      <router-view></router-view>
    </main>

    <!-- 补充个人信息弹窗 -->
    <el-dialog 
      v-model="showProfileDialog" 
      title="完善个人信息" 
      width="600px" 
      :close-on-click-modal="false"
      :show-close="true"
      @close="handleDialogClose"
    >
      <div class="profile-dialog-content">
        <div class="dialog-tip">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
          <span>完善个人信息后，可自动填充简历，提升求职效率！</span>
        </div>

        <el-form :model="quickProfile" label-width="100px" class="quick-form">
          <el-divider content-position="left">基本信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" required>
                <el-input v-model="quickProfile.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="quickProfile.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="quickProfile.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="求职状态">
                <el-select v-model="quickProfile.jobStatus" placeholder="请选择" style="width: 100%">
                  <el-option label="在职-考虑机会" value="在职" />
                  <el-option label="离职-随时到岗" value="离职" />
                  <el-option label="应届生" value="应届生" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">求职意向</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="期望职位" required>
                <el-input v-model="quickProfile.jobTitle" placeholder="如：Java开发工程师" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="期望城市">
                <el-input v-model="quickProfile.jobCity" placeholder="如：北京、上海" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">教育背景</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="毕业院校">
                <el-input v-model="quickProfile.school" placeholder="请输入毕业院校" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业">
                <el-input v-model="quickProfile.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学历">
                <el-select v-model="quickProfile.degree" placeholder="请选择学历" style="width: 100%">
                  <el-option label="大专" value="大专" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="技能标签">
                <el-input v-model="quickProfile.skills" placeholder="如：Java, Vue, MySQL" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="skipProfile">稍后完善</el-button>
          <el-button type="primary" @click="saveQuickProfile" :loading="saving">
            保存信息
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import rHeader from '@/components/r-header.vue'

// 弹窗控制
const showProfileDialog = ref(false)
const saving = ref(false)

// 快速填写的个人信息
const quickProfile = ref({
  name: '',
  phone: '',
  email: '',
  jobStatus: '',
  jobTitle: '',
  jobCity: '',
  school: '',
  major: '',
  degree: '',
  skills: ''
})

// 检查是否需要补充个人信息
const checkProfileComplete = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) return

  // 检查是否已经跳过过（本次会话）
  const skipped = sessionStorage.getItem('profileSkipped')
  if (skipped) return

  try {
    const response = await fetch(`http://localhost:9090/api/profile/${userId}`)
    const result = await response.json()

    if (result.success) {
      const data = result.data
      // 检查关键字段是否为空
      if (!data || !data.name || !data.jobTitle) {
        // 如果有部分数据，预填充
        if (data) {
          quickProfile.value = {
            name: data.name || '',
            phone: data.phone || '',
            email: data.email || '',
            jobStatus: data.jobStatus || '',
            jobTitle: data.jobTitle || '',
            jobCity: data.jobCity || '',
            school: data.school || '',
            major: data.major || '',
            degree: data.degree || '',
            skills: data.skills || ''
          }
        }
        // 延迟显示弹窗，让页面先加载完成
        setTimeout(() => {
          showProfileDialog.value = true
        }, 500)
      }
    } else {
      // 没有个人信息记录，显示弹窗
      setTimeout(() => {
        showProfileDialog.value = true
      }, 500)
    }
  } catch (error) {
    console.error('检查个人信息失败:', error)
  }
}

// 保存快速填写的个人信息
const saveQuickProfile = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 验证必填字段
  if (!quickProfile.value.name) {
    ElMessage.warning('请填写姓名')
    return
  }
  if (!quickProfile.value.jobTitle) {
    ElMessage.warning('请填写期望职位')
    return
  }

  try {
    saving.value = true

    const response = await fetch('http://localhost:9090/api/profile/save', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId,
        ...quickProfile.value
      })
    })

    const result = await response.json()

    if (result.success) {
      ElMessage.success('个人信息保存成功！')
      showProfileDialog.value = false
    } else {
      throw new Error(result.error || '保存失败')
    }
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 跳过补充信息
const skipProfile = () => {
  sessionStorage.setItem('profileSkipped', 'true')
  showProfileDialog.value = false
  ElMessage.info('您可以稍后在"个人信息"页面完善资料')
}

// 关闭弹窗时的处理
const handleDialogClose = () => {
  sessionStorage.setItem('profileSkipped', 'true')
}

// 页面加载时检查
onMounted(() => {
  checkProfileComplete()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

.app-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.app-main {
  flex: 1;
  background: #f5f3ff;
  padding: 0;
  overflow-y: auto;
}

/* 补充信息弹窗样式 */
.profile-dialog-content {
  padding: 0 10px;
}

.dialog-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #e0e7ff 0%, #f5f3ff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  color: #4f46e5;
  font-size: 14px;
}

.tip-icon {
  font-size: 18px;
  color: #6366f1;
}

.quick-form :deep(.el-divider__text) {
  font-size: 14px;
  font-weight: 600;
  color: #6366f1;
  background: #fff;
}

.quick-form :deep(.el-divider) {
  margin: 16px 0;
}

.quick-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.quick-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #374151;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid #e5e7eb;
}
</style>
