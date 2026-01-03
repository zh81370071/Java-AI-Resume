<template>
  <div class="profile-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-title">个人信息</div>
      <el-button type="primary" @click="saveProfile" :loading="saving">保存信息</el-button>
    </div>

    <div class="profile-content">
      <!-- 左侧：基本信息 -->
      <div class="form-section">
        <h3>基本信息</h3>
        <el-form label-width="100px">
          <el-form-item label="姓名">
            <el-input v-model="profile.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profile.gender">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="profile.birthDate" type="date" placeholder="选择出生日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profile.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profile.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="现居地址">
            <el-input v-model="profile.address" placeholder="请输入现居地址" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 中间：求职意向 -->
      <div class="form-section">
        <h3>求职意向</h3>
        <el-form label-width="100px">
          <el-form-item label="求职状态">
            <el-select v-model="profile.jobStatus" placeholder="请选择求职状态">
              <el-option label="在职-考虑机会" value="在职" />
              <el-option label="离职-随时到岗" value="离职" />
              <el-option label="应届生" value="应届生" />
            </el-select>
          </el-form-item>
          <el-form-item label="期望职位">
            <el-input v-model="profile.jobTitle" placeholder="如：Java开发工程师" />
          </el-form-item>
          <el-form-item label="期望城市">
            <el-input v-model="profile.jobCity" placeholder="如：北京、上海" />
          </el-form-item>
          <el-form-item label="期望薪资">
            <div class="salary-range">
              <el-input-number v-model="profile.salaryMin" :min="1" :max="100" placeholder="最低" />
              <span class="salary-separator">-</span>
              <el-input-number v-model="profile.salaryMax" :min="1" :max="100" placeholder="最高" />
              <span class="salary-unit">K</span>
            </div>
          </el-form-item>
        </el-form>

        <h3>教育背景</h3>
        <el-form label-width="100px">
          <el-form-item label="毕业院校">
            <el-input v-model="profile.school" placeholder="请输入毕业院校" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="profile.major" placeholder="请输入专业" />
          </el-form-item>
          <el-form-item label="学历">
            <el-select v-model="profile.degree" placeholder="请选择学历">
              <el-option label="大专" value="大专" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>
          <el-form-item label="毕业时间">
            <el-date-picker v-model="profile.graduationDate" type="date" placeholder="选择毕业时间" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：技能与自我介绍 -->
      <div class="form-section">
        <h3>专业技能</h3>
        <el-form label-width="100px">
          <el-form-item label="技能标签">
            <el-input v-model="profile.skills" type="textarea" :rows="4" placeholder="请输入您的技能，用逗号分隔，如：Java, Spring Boot, Vue.js, MySQL" />
          </el-form-item>
        </el-form>

        <h3>自我介绍</h3>
        <el-form label-width="100px">
          <el-form-item label="个人简介">
            <el-input v-model="profile.selfIntroduction" type="textarea" :rows="6" placeholder="简单介绍一下自己，包括工作经验、项目经历、个人优势等" />
          </el-form-item>
        </el-form>

        <!-- 信息预览 -->
        <div class="preview-card">
          <h4>信息预览</h4>
          <div class="preview-item">
            <span class="label">姓名：</span>
            <span class="value">{{ profile.name || '未填写' }}</span>
          </div>
          <div class="preview-item">
            <span class="label">期望职位：</span>
            <span class="value">{{ profile.jobTitle || '未填写' }}</span>
          </div>
          <div class="preview-item">
            <span class="label">期望薪资：</span>
            <span class="value">{{ salaryDisplay }}</span>
          </div>
          <div class="preview-item">
            <span class="label">学历：</span>
            <span class="value">{{ profile.degree || '未填写' }} · {{ profile.school || '未填写' }}</span>
          </div>
          <div class="preview-item">
            <span class="label">技能：</span>
            <span class="value skills-preview">{{ profile.skills || '未填写' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const saving = ref(false);

const profile = ref({
  name: '',
  gender: '',
  birthDate: null,
  phone: '',
  email: '',
  address: '',
  jobStatus: '',
  jobTitle: '',
  jobCity: '',
  salaryMin: null,
  salaryMax: null,
  school: '',
  major: '',
  degree: '',
  graduationDate: null,
  skills: '',
  selfIntroduction: ''
});

// 薪资显示
const salaryDisplay = computed(() => {
  if (profile.value.salaryMin && profile.value.salaryMax) {
    return `${profile.value.salaryMin}K - ${profile.value.salaryMax}K`;
  } else if (profile.value.salaryMin) {
    return `${profile.value.salaryMin}K以上`;
  } else if (profile.value.salaryMax) {
    return `${profile.value.salaryMax}K以内`;
  }
  return '未填写';
});

// 加载用户信息
const loadProfile = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    ElMessage.warning('请先登录');
    return;
  }

  try {
    const response = await fetch(`http://localhost:9090/api/profile/${userId}`);
    const result = await response.json();

    if (result.success && result.data) {
      // 填充数据
      const data = result.data;
      profile.value = {
        name: data.name || '',
        gender: data.gender || '',
        birthDate: data.birthDate || null,
        phone: data.phone || '',
        email: data.email || '',
        address: data.address || '',
        jobStatus: data.jobStatus || '',
        jobTitle: data.jobTitle || '',
        jobCity: data.jobCity || '',
        salaryMin: data.salaryMin || null,
        salaryMax: data.salaryMax || null,
        school: data.school || '',
        major: data.major || '',
        degree: data.degree || '',
        graduationDate: data.graduationDate || null,
        skills: data.skills || '',
        selfIntroduction: data.selfIntroduction || ''
      };
    }
  } catch (error) {
    console.error('加载个人信息失败:', error);
  }
};

// 保存用户信息
const saveProfile = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    ElMessage.warning('请先登录');
    return;
  }

  try {
    saving.value = true;

    const response = await fetch('http://localhost:9090/api/profile/save', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId,
        ...profile.value
      })
    });

    const result = await response.json();

    if (result.success) {
      ElMessage.success('保存成功');
    } else {
      throw new Error(result.error || '保存失败');
    }
  } catch (error) {
    console.error('保存个人信息失败:', error);
    ElMessage.error(error.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f3ff;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.toolbar-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.profile-content {
  display: flex;
  padding: 24px;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.form-section {
  flex: 1;
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

.form-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #6366f1;
}

.form-section h3:not(:first-child) {
  margin-top: 32px;
}

.salary-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.salary-separator {
  color: #64748b;
}

.salary-unit {
  color: #64748b;
  font-size: 14px;
}

.preview-card {
  margin-top: 32px;
  padding: 20px;
  background: #f5f3ff;
  border-radius: 12px;
}

.preview-card h4 {
  font-size: 15px;
  font-weight: 600;
  color: #6366f1;
  margin: 0 0 16px 0;
}

.preview-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.preview-item .label {
  color: #64748b;
  width: 80px;
  flex-shrink: 0;
}

.preview-item .value {
  color: #1e1b4b;
  flex: 1;
}

.skills-preview {
  word-break: break-all;
}

/* 响应式 */
@media (max-width: 1200px) {
  .profile-content {
    flex-direction: column;
  }
}
</style>
