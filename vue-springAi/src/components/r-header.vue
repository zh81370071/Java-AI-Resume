<template>
  <div class="header-container">
    <!-- 左侧 Logo 和欢迎语 -->
    <div class="header-left">
      <div class="logo-area">
        <svg class="logo-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <span class="logo-text">职达助手</span>
      </div>
      <span class="welcome-text">欢迎，{{ username || '用户' }}</span>
    </div>

    <!-- 中间导航 -->
    <nav class="header-nav">
      <button class="nav-btn nav-btn-info" @click="goToProfile">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        个人信息
      </button>
      <button class="nav-btn" @click="showMyResume">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
        </svg>
        我的简历
      </button>
      <button class="nav-btn nav-btn-primary" @click="openResumeModal">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
        </svg>
        简历AI
      </button>
      <button v-if="isAdmin" class="nav-btn nav-btn-success" @click="clickJobPool">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
          <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
        </svg>
        职位库管理
      </button>
      <button v-if="!isAdmin" class="nav-btn nav-btn-success" @click="clickRecommend">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
        推荐职位
      </button>
    </nav>

    <!-- 右侧退出按钮 -->
    <div class="header-right">
      <el-popconfirm
        confirmButtonText="确定"
        cancelButtonText="取消"
        title="确定退出登录吗？"
        @confirm="handleLogout"
      >
        <template #reference>
          <button class="logout-btn">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            退出
          </button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 简历生成弹窗 -->
    <el-dialog v-model="showResumeModal" title="AI简历生成" width="440px" :close-on-click-modal="false" center>
      <div class="modal-content">
        <div class="version-tabs">
          <button 
            :class="['tab-btn', resumeVersion === '应届生版' ? 'active' : '']" 
            @click="resumeVersion = '应届生版'"
          >应届生版</button>
          <button 
            :class="['tab-btn', resumeVersion === '标准版' ? 'active' : '']" 
            @click="resumeVersion = '标准版'"
          >标准版</button>
        </div>
        
        <div v-if="resumeVersion === '应届生版'" class="form-fields">
          <div class="field-group">
            <label>我的专业</label>
            <input v-model="form.major" maxlength="20" placeholder="请输入专业" />
          </div>
          <div class="field-group">
            <label>期望职位</label>
            <input v-model="form.position" maxlength="20" placeholder="请输入职位" />
          </div>
          <div class="field-group">
            <label>补充信息</label>
            <textarea v-model="form.extra" maxlength="40" placeholder="补充内容（选填）"></textarea>
          </div>
        </div>
        
        <div v-if="resumeVersion === '标准版'" class="form-fields">
          <div class="field-group">
            <label>工作经历</label>
            <input v-model="form.experience" maxlength="20" placeholder="请输入经历" />
          </div>
          <div class="field-group">
            <label>期望职位</label>
            <input v-model="form.position" maxlength="20" placeholder="请输入职位" />
          </div>
          <div class="field-group">
            <label>补充信息</label>
            <textarea v-model="form.extra" maxlength="40" placeholder="补充内容（选填）"></textarea>
          </div>
        </div>
      </div>
      <template #footer>
        <button 
          class="generate-btn" 
          @click="generateResume" 
          :disabled="!isFormValid || generating"
        >
          {{ generating ? '正在生成...' : '生成简历' }}
        </button>
      </template>
    </el-dialog>

    <!-- 生成状态弹窗 -->
    <el-dialog v-model="showGeneratingDialog" :show-close="false" width="320px" center>
      <div class="generating-status">
        <div class="spinner"></div>
        <p class="status-text">{{ statusText }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'r-header',
  data() {
    return {
      username: null,
      isAdmin: false, // 默认不显示心理分析按钮
      // ▼▼▼ 新增以下三个关键变量 ▼▼▼
      showResumeModal: false,  // 控制弹窗显示
      resumeVersion: '应届生版', // 默认选中的简历版本
      form: {                  // 表单数据模型
        major: '',
        position: '',
        extra: '',
        experience: ''
      },
      generating: false,
      showGeneratingDialog: false,
      statusText: '',
      // 个人信息缓存（用于联动）
      profileData: null
    };
  },
  computed: {
    isFormValid() {
      if (this.resumeVersion === '应届生版') {
        return this.form.major && this.form.position;
      }
      return this.form.experience && this.form.position;
    }
  },
  created() {
    this.username = localStorage.getItem('username');
    // 根据 userType 判断是否为管理员 (1=管理员)
    const userType = localStorage.getItem('userType');
    if (userType === '1') {
      this.isAdmin = true;
    }
  },
  methods: {
    clickJobPool() {
      this.$router.push('/jobPool');
    },
    clickRecommend() {
      this.$router.push('/recommend');
    },
    handleLogout() {
      localStorage.clear();
      this.$router.push('/login');
    },
    goToHome() {
      this.$router.push('/');
    },
    showMyResume() {
      // 清除本地存储的简历数据，以便加载最新的数据库数据
      localStorage.removeItem('resumeData');
      this.$router.push('/resume'); 
    },
    goToProfile() {
      this.$router.push('/profile');
    },
    // 打开简历AI生成弹窗（从个人信息预填充）
    async openResumeModal() {
      // 先加载个人信息
      await this.loadProfileForResume();
      this.showResumeModal = true;
    },
    // 加载个人信息用于预填充简历表单
    async loadProfileForResume() {
      const userId = localStorage.getItem('userId');
      if (!userId) return;

      try {
        const response = await fetch(`http://localhost:9090/api/profile/${userId}`);
        const result = await response.json();

        if (result.success && result.data) {
          this.profileData = result.data;
          // 预填充表单
          if (result.data.major) {
            this.form.major = result.data.major;
          }
          if (result.data.jobTitle) {
            this.form.position = result.data.jobTitle;
          }
          // 根据求职状态自动选择版本
          if (result.data.jobStatus === '应届生') {
            this.resumeVersion = '应届生版';
          } else if (result.data.jobStatus === '在职' || result.data.jobStatus === '离职') {
            this.resumeVersion = '标准版';
          }
        }
      } catch (error) {
        console.error('加载个人信息失败:', error);
      }
    },
    // ✅ 新增：生成简历逻辑（你可以替换为调用后端接口）
    async generateResume() {
      if (!this.isFormValid) {
        this.$message.warning('请填写必要信息');
        return;
      }

      this.generating = true;
      this.showGeneratingDialog = true;
      this.statusText = '正在生成简历...';

      try {
        const userId = localStorage.getItem('userId');
        const username = localStorage.getItem('username');
        
        const response = await fetch('http://localhost:9090/ai/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            userId,
            username,
            resumeVersion: this.resumeVersion,
            formData: this.form,
            // 传递个人信息用于AI生成时参考
            profileData: this.profileData
          })
        });

        if (!response.ok) {
          throw new Error('网络请求失败');
        }

        const result = await response.json();
        
        if (!result.success) {
          throw new Error(result.error || '生成失败');
        }

        // 存储简历数据
        localStorage.setItem('resumeData', JSON.stringify(result.content));
        
        // 显示成功状态
        this.statusText = '简历生成成功！';
        
        // 延迟关闭对话框并跳转
        setTimeout(() => {
          this.showResumeModal = false;
          this.showGeneratingDialog = false;
          this.$router.push('/resume');
        }, 1000);

      } catch (error) {
        console.error('生成简历失败:', error);
        this.statusText = '生成失败: ' + error.message;
        this.$message.error(error.message);
      } finally {
        this.generating = false;
      }
    },

    // 解析AI返回的内容
    parseResumeContent(content) {
      // 初始化简历数据结构
      const resumeData = {
        name: content.name || '',
        phone: content.phone || '',
        email: content.email || '',
        jobStatus: content.jobStatus || '',
        jobTitle: content.jobTitle || '',
        salaryExpectation: content.salaryExpectation || '',
        education: {
          school: '',
          major: '',
          degree: '',
          studyPeriod: []
        },
        profession: {
          skill: ''
        },
        work: {
          company: '',
          department: '',
          position: '',
          period: [],
          details: ''
        },
        project: {
          name: '',
          period: [],
          details: ''
        },
        award: {
          details: ''
        }
      };

      // 如果是对象格式的响应，直接使用
      if (typeof content === 'object') {
        if (content.education) resumeData.education = { ...resumeData.education, ...content.education };
        if (content.profession) resumeData.profession = { ...resumeData.profession, ...content.profession };
        if (content.work) resumeData.work = { ...resumeData.work, ...content.work };
        if (content.project) resumeData.project = { ...resumeData.project, ...content.project };
        if (content.award) resumeData.award = { ...resumeData.award, ...content.award };
        return resumeData;
      }

      // 如果是字符串格式，按原有逻辑解析
      const sections = content.split('\n\n');
      sections.forEach(section => {
        if (section.includes('求职意向')) {
          const lines = section.split('\n');
          lines.forEach(line => {
            if (line.includes('职位')) resumeData.jobTitle = line.split('：')[1];
            if (line.includes('薪资')) resumeData.salaryExpectation = line.split('：')[1];
          });
        } else if (section.includes('教育经历')) {
          const lines = section.split('\n');
          lines.forEach(line => {
            if (line.includes('学校')) resumeData.education.school = line.split('：')[1];
            if (line.includes('专业')) resumeData.education.major = line.split('：')[1];
            if (line.includes('学历')) resumeData.education.degree = line.split('：')[1];
          });
        } else if (section.includes('专业技能')) {
          resumeData.profession.skill = section.split('\n').slice(1).join('\n');
        } else if (section.includes('工作经历')) {
          const lines = section.split('\n');
          lines.forEach(line => {
            if (line.includes('公司')) resumeData.work.company = line.split('：')[1];
            if (line.includes('职位')) resumeData.work.position = line.split('：')[1];
            if (line.includes('工作内容')) resumeData.work.details = line.split('：')[1];
          });
        } else if (section.includes('项目经历')) {
          resumeData.project.details = section.split('\n').slice(1).join('\n');
        } else if (section.includes('获奖情况')) {
          resumeData.award.details = section.split('\n').slice(1).join('\n');
        }
      });

      return resumeData;
    }
  },
};
</script>

<style scoped>
/* 头部容器 */
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  color: #6366f1;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.welcome-text {
  font-size: 14px;
  color: #64748b;
}

/* 中间导航 */
.header-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover {
  color: #6366f1;
  background: #f5f3ff;
  border-color: #c7d2fe;
}

.nav-btn svg {
  flex-shrink: 0;
}

.nav-btn-primary {
  color: #6366f1;
  background: #f5f3ff;
  border-color: #c7d2fe;
}

.nav-btn-primary:hover {
  background: #ede9fe;
}

.nav-btn-success {
  color: #10b981;
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.nav-btn-success:hover {
  background: #d1fae5;
}

.nav-btn-info {
  color: #0ea5e9;
  background: #f0f9ff;
  border-color: #bae6fd;
}

.nav-btn-info:hover {
  background: #e0f2fe;
}

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #f43f5e;
  background: #fff1f2;
  border: 1px solid #fecdd3;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ffe4e6;
}

/* 弹窗内容 */
.modal-content {
  padding: 0 8px;
}

.version-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  color: #6366f1;
  background: #f5f3ff;
  border-color: #6366f1;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-group label {
  font-size: 14px;
  font-weight: 500;
  color: #1e1b4b;
}

.field-group input,
.field-group textarea {
  padding: 12px 14px;
  font-size: 14px;
  color: #1e1b4b;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.field-group input:focus,
.field-group textarea:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.field-group textarea {
  min-height: 80px;
  resize: vertical;
}

.generate-btn {
  width: 100%;
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #6366f1;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.generate-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.generate-btn:disabled {
  background: #c7d2fe;
  cursor: not-allowed;
}

/* 生成状态 */
.generating-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 32px 20px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.status-text {
  margin: 0;
  font-size: 15px;
  color: #64748b;
  text-align: center;
}

/* 响应式 */
@media (max-width: 900px) {
  .header-container {
    padding: 0 16px;
  }
  
  .header-nav {
    gap: 8px;
  }
  
  .nav-btn {
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .welcome-text {
    display: none;
  }
}
</style>
