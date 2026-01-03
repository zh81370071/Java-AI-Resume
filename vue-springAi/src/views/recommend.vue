<template>
  <div class="recommend-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>智能职位推荐</h1>
      <p>基于您的简历和技能，AI为您精准匹配最合适的职位</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在分析您的简历，匹配最佳职位...</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-card">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <span>{{ errorMessage }}</span>
    </div>

    <!-- 职业发展建议 -->
    <div v-if="careerAdvice && !loading" class="advice-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
        职业发展建议
      </h2>
      <div class="advice-grid">
        <div class="advice-card">
          <div class="advice-icon short-term">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
          </div>
          <h3>短期目标</h3>
          <span class="advice-label">1年内</span>
          <p>{{ careerAdvice.short_term }}</p>
        </div>
        <div class="advice-card">
          <div class="advice-icon skills">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
            </svg>
          </div>
          <h3>技能提升</h3>
          <span class="advice-label">建议</span>
          <p>{{ careerAdvice.skills_improvement }}</p>
        </div>
        <div class="advice-card">
          <div class="advice-icon long-term">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="2" x2="12" y2="6"></line>
              <line x1="12" y1="18" x2="12" y2="22"></line>
              <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
              <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
              <line x1="2" y1="12" x2="6" y2="12"></line>
              <line x1="18" y1="12" x2="22" y2="12"></line>
              <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
              <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
            </svg>
          </div>
          <h3>长期规划</h3>
          <span class="advice-label">3-5年</span>
          <p>{{ careerAdvice.long_term }}</p>
        </div>
      </div>
    </div>

    <!-- 推荐职位列表 -->
    <div v-if="recommendedJobs.length > 0" class="jobs-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
          <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
        </svg>
        为您推荐的职位
        <span class="job-count">{{ recommendedJobs.length }}</span>
      </h2>
      <div class="jobs-grid">
        <div v-for="job in recommendedJobs" :key="job.id" class="job-card">
          <div class="job-card-header">
            <div class="job-title-area">
              <h3>{{ job.title }}</h3>
              <span class="company-name">{{ job.company_name }}</span>
            </div>
            <div class="match-badge">
              <span class="match-value">{{ job.matchScore }}%</span>
              <span class="match-label">匹配</span>
            </div>
          </div>
          
          <div class="job-tags">
            <span class="tag">{{ job.salary_range }}</span>
            <span class="tag">{{ job.location }}</span>
            <span class="tag">{{ job.education_requirements }}</span>
          </div>

          <div v-if="job.recommendation_reason" class="reason-block">
            <div class="block-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              推荐理由
            </div>
            <p>{{ job.recommendation_reason }}</p>
          </div>

          <div v-if="job.resume_improvement" class="improvement-block">
            <div class="block-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
              简历优化建议
            </div>
            <p>{{ job.resume_improvement }}</p>
          </div>

          <div class="job-card-footer">
            <button class="btn-apply">申请职位</button>
            <button class="btn-save">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 无结果 -->
    <div v-else-if="!loading && searched" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="11" cy="11" r="8"></circle>
        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
      </svg>
      <h3>暂无匹配职位</h3>
      <p>完善您的简历信息，获得更精准的职位推荐</p>
      <button @click="goToResume" class="btn-primary">完善简历</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RecommendView',
  data() {
    return {
      recommendedJobs: [],
      careerAdvice: null,
      loading: true,
      searched: false,
      errorMessage: ''
    }
  },
  created() {
    this.getRecommendations();
  },
  methods: {
    async getRecommendations() {
      // 检查用户是否登录
      const userId = localStorage.getItem('userId');
      if (!userId) {
        this.errorMessage = '请先登录以获取职位推荐';
        this.loading = false;
        return;
      }
      
      this.loading = true;
      this.errorMessage = '';
      
      try {
        // 直接发送请求，仅传递用户ID
        const response = await axios.post('http://localhost:9090/api/recommend/jobs', { userId });
        
        // 检查响应格式，适应新的后端返回结构
        if (response.data) {
          if (response.data.jobs) {
            // 新格式：包含职位列表和职业建议
            this.recommendedJobs = response.data.jobs.map(job => {
              const normalizedScore = Math.min(job.matchScore, 100);
              return { ...job, matchScore: normalizedScore };
            });
            
            // 设置职业建议
            this.careerAdvice = response.data.career_advice;
          } else if (Array.isArray(response.data)) {
            // 旧格式：直接返回职位列表
            this.recommendedJobs = response.data.map(job => {
              const normalizedScore = Math.min(job.matchScore, 100);
              return { ...job, matchScore: normalizedScore };
            });
          } else {
            this.recommendedJobs = [];
          }
        } else {
          this.recommendedJobs = [];
        }
        
        this.searched = true;
      } catch (error) {
        console.error('Error fetching job recommendations:', error);
        if (error.response?.status === 404) {
          this.errorMessage = '未找到您的简历信息，请先完善简历';
        } else {
          this.errorMessage = error.response?.data?.error || '加载职位推荐时出错，请稍后再试';
        }
        this.recommendedJobs = [];
      } finally {
        this.loading = false;
      }
    },
    goToResume() {
      this.$router.push('/resume');
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.recommend-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 12px 0;
}

.page-header p {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  font-size: 15px;
  color: #64748b;
}

/* 错误提示 */
.error-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff1f2;
  border: 1px solid #fecdd3;
  border-radius: 12px;
  margin-bottom: 24px;
}

.error-card svg {
  color: #f43f5e;
  flex-shrink: 0;
}

.error-card span {
  font-size: 14px;
  color: #be123c;
}

/* 区块标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 24px 0;
}

.section-title svg {
  color: #6366f1;
}

.job-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  background: #f5f3ff;
  border-radius: 14px;
  margin-left: 8px;
}

/* 职业建议区块 */
.advice-section {
  margin-bottom: 48px;
}

.advice-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.advice-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

.advice-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  margin-bottom: 16px;
}

.advice-icon.short-term {
  background: #f5f3ff;
  color: #6366f1;
}

.advice-icon.skills {
  background: #fef3c7;
  color: #f59e0b;
}

.advice-icon.long-term {
  background: #ecfdf5;
  color: #10b981;
}

.advice-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 4px 0;
}

.advice-label {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  margin-bottom: 12px;
}

.advice-card p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

/* 职位列表区块 */
.jobs-section {
  margin-bottom: 40px;
}

.jobs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.job-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.job-title-area h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 4px 0;
}

.company-name {
  font-size: 14px;
  color: #64748b;
}

.match-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  background: #f5f3ff;
  border-radius: 12px;
}

.match-value {
  font-size: 18px;
  font-weight: 700;
  color: #6366f1;
}

.match-label {
  font-size: 11px;
  color: #94a3b8;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  padding: 6px 12px;
  font-size: 13px;
  color: #64748b;
  background: #f8fafc;
  border-radius: 6px;
}

.reason-block,
.improvement-block {
  padding: 14px 16px;
  border-radius: 10px;
  margin-bottom: 12px;
}

.reason-block {
  background: #f5f3ff;
}

.improvement-block {
  background: #ecfdf5;
}

.block-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}

.reason-block .block-title {
  color: #6366f1;
}

.improvement-block .block-title {
  color: #10b981;
}

.reason-block p,
.improvement-block p {
  font-size: 14px;
  color: #1e1b4b;
  line-height: 1.5;
  margin: 0;
}

.job-card-footer {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn-apply {
  flex: 1;
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: #6366f1;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-apply:hover {
  background: #4f46e5;
}

.btn-save {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
}

.btn-save:hover {
  background: #f5f3ff;
  color: #6366f1;
  border-color: #c7d2fe;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-state svg {
  color: #c7d2fe;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 24px 0;
}

.btn-primary {
  padding: 14px 32px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #6366f1;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #4f46e5;
}

/* 响应式 */
@media (max-width: 900px) {
  .advice-grid {
    grid-template-columns: 1fr;
  }
  
  .jobs-grid {
    grid-template-columns: 1fr;
  }
}
</style>
