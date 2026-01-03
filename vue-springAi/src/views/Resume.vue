<template>
  <div class="resume-page">
    <!-- 加载状态提示 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-text">{{ loadingText }}</div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-title">简历编辑器</div>
      <div class="toolbar-actions">
        <el-button type="warning" @click="showOptimizeAllDialog" :loading="optimizingAll">
          <el-icon><MagicStick /></el-icon> AI优化整份简历
        </el-button>
        <el-button type="success" @click="updateResume" :loading="updating">更新简历</el-button>
        <el-button type="primary" @click="exportResume('pdf')">导出 PDF</el-button>
        <el-button type="primary" @click="exportResume('word')">导出 Word</el-button>
      </div>
    </div>

    <div class="resume-content">
      <!-- 左侧：模块选择 -->
      <div class="sidebar" :class="{ loading }">
        <div class="module-list">
          <h3>模块选择</h3>
          <el-checkbox-group v-model="selectedModules">
            <el-checkbox label="基本信息" />
            <el-checkbox label="求职意向" />
            <el-checkbox label="教育经历" />
            <el-checkbox label="专业技能" />
            <el-checkbox label="工作经历" />
            <el-checkbox label="项目经历" />
            <el-checkbox label="荣誉奖项" />
          </el-checkbox-group>
        </div>
      </div>

      <!-- 中间：信息填写 -->
      <div class="form-container" :class="{ loading }">
        <!-- 基本信息 -->
        <h3 v-if="selectedModules.includes('基本信息')">基本信息</h3>
        <el-form v-if="selectedModules.includes('基本信息')" label-width="100px">
          <el-form-item label="姓名">
            <el-input v-model="resume.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="resume.phone" placeholder="请输入电话" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="resume.email" placeholder="请输入邮箱" />
          </el-form-item>

          <!-- 用户头像上传 -->
          <el-form-item label="头像">
            <el-upload
                class="avatar-uploader"
                action=""
                :show-file-list="false"
                :before-upload="handleAvatarUpload"
            >
              <img v-if="resume.avatar" :src="resume.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>

        <!-- 求职意向 -->
        <h3 v-if="selectedModules.includes('求职意向')">求职意向</h3>
        <el-form v-if="selectedModules.includes('求职意向')" label-width="100px">
          <el-form-item label="当前状态">
            <el-select v-model="resume.jobStatus" placeholder="请选择当前状态">
              <el-option label="在职" value="在职" />
              <el-option label="离职" value="离职" />
              <el-option label="应届生" value="应届生" />
            </el-select>
          </el-form-item>
          <el-form-item label="职位名称">
            <el-input v-model="resume.jobTitle" placeholder="请输入期望职位" />
          </el-form-item>
          <el-form-item label="期望薪资">
            <el-input v-model="resume.salaryExpectation" placeholder="请输入期望薪资" />
          </el-form-item>
        </el-form>

        <!-- 教育经历 -->
        <h3 v-if="selectedModules.includes('教育经历')">教育经历</h3>
        <el-form v-if="selectedModules.includes('教育经历')" label-width="100px">
          <el-form-item label="学校">
            <el-input v-model="resume.education.school" placeholder="请输入学校" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="resume.education.major" placeholder="请输入专业" />
          </el-form-item>
          <el-form-item label="学历">
            <el-select v-model="resume.education.degree" placeholder="请选择学历">
              <el-option label="本科" value="本科"></el-option>
              <el-option label="硕士" value="硕士"></el-option>
              <el-option label="博士" value="博士"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="在读时间">
            <el-date-picker v-model="resume.education.studyPeriod" type="daterange" start-placeholder="开始时间" end-placeholder="结束时间"/>
          </el-form-item>
        </el-form>

        <!-- 专业技能 -->
        <div v-if="selectedModules.includes('专业技能')" class="module-header">
          <h3>专业技能</h3>
          <el-button size="small" type="primary" plain @click="handleOptimizeProfession" :loading="optimizingModule === 'profession'">
            <el-icon><MagicStick /></el-icon> AI润色
          </el-button>
        </div>
        <el-form v-if="selectedModules.includes('专业技能')" label-width="100px">
          <el-form-item label="技能描述">
            <el-input v-model="resume.profession.skill" type="textarea" :rows="4" placeholder="请输入专业技能" />
          </el-form-item>
        </el-form>

        <!-- 工作经历 -->
        <div v-if="selectedModules.includes('工作经历')" class="module-header">
          <h3>工作经历</h3>
          <el-button size="small" type="primary" plain @click="handleOptimizeWork" :loading="optimizingModule === 'work'">
            <el-icon><MagicStick /></el-icon> AI润色
          </el-button>
        </div>
        <el-form v-if="selectedModules.includes('工作经历')" label-width="100px">
          <el-form-item label="公司名称">
            <el-input v-model="resume.work.company" placeholder="请输入公司名称" />
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="resume.work.department" placeholder="请输入部门" />
          </el-form-item>
          <el-form-item label="职位">
            <el-input v-model="resume.work.position" placeholder="请输入职位" />
          </el-form-item>
          <el-form-item label="在职时间">
            <el-date-picker v-model="resume.work.period" type="daterange" start-placeholder="开始时间" end-placeholder="结束时间"/>
          </el-form-item>
          <el-form-item label="工作内容">
            <el-input v-model="resume.work.details" type="textarea" :rows="4" placeholder="请输入工作内容" />
          </el-form-item>
        </el-form>

        <!-- 项目经历 -->
        <div v-if="selectedModules.includes('项目经历')" class="module-header">
          <h3>项目经历</h3>
          <el-button size="small" type="primary" plain @click="handleOptimizeProject" :loading="optimizingModule === 'project'">
            <el-icon><MagicStick /></el-icon> AI润色
          </el-button>
        </div>
        <el-form v-if="selectedModules.includes('项目经历')" label-width="100px">
          <el-form-item label="项目名称">
            <el-input v-model="resume.project.name" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="项目时间">
            <el-date-picker v-model="resume.project.period" type="daterange" start-placeholder="开始时间" end-placeholder="结束时间"/>
          </el-form-item>
          <el-form-item label="项目描述">
            <el-input v-model="resume.project.details" type="textarea" :rows="4" placeholder="请输入项目描述" />
          </el-form-item>
        </el-form>

        <!-- 荣誉奖项 -->
        <div v-if="selectedModules.includes('荣誉奖项')" class="module-header">
          <h3>荣誉奖项</h3>
          <el-button size="small" type="primary" plain @click="handleOptimizeAward" :loading="optimizingModule === 'award'">
            <el-icon><MagicStick /></el-icon> AI润色
          </el-button>
        </div>
        <el-form v-if="selectedModules.includes('荣誉奖项')" label-width="100px">
          <el-form-item label="奖项名称">
            <el-input v-model="resume.award.details" type="textarea" :rows="3" placeholder="请输入奖项名称" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：预览简历 -->
      <div class="preview-container" :class="{ loading }">
        <!-- 预览区头部 -->
        <div class="preview-header">
          <!-- 左侧：姓名、电话、邮箱 -->
          <div class="preview-contact">
            <h2>{{ resume.name || '姓名' }}</h2>
            <p>📞 {{ resume.phone || '电话' }}</p>
            <p>✉️ {{ resume.email || '邮箱' }}</p>
          </div>

          <!-- 右侧：头像 -->
          <div class="preview-avatar-container">
            <img v-if="resume.avatar" :src="resume.avatar" class="preview-avatar" />
          </div>
        </div>

        <template v-for="module in selectedModules" :key="module">
          <div v-if="module !== '基本信息'" class="section">
            <h3 class="section-title">{{ module }}</h3>
            <div v-html="getFormattedResumeData(module)"></div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import html2pdf from 'html2pdf.js';
import { Document, Packer, Paragraph, TextRun } from "docx";
import { saveAs } from "file-saver";
import { Plus, MagicStick } from '@element-plus/icons-vue';
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const selectedModules = ref(['基本信息', '求职意向', '教育经历', '专业技能', '工作经历', '项目经历', '荣誉奖项']);
const resume = ref({
  name: '', 
  phone: '', 
  email: '',
  avatar:'',
  jobStatus: '', 
  jobTitle: '', 
  salaryExpectation: '',
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
  },
});

const loading = ref(false);
const loadingText = ref('');
const updating = ref(false);

const getFormComponent = (module) => {
  const components = {
    '基本信息': 'BasicInfoForm',
    '求职意向': 'JobIntentForm',
    '教育经历': 'EducationForm',
    '专业技能': 'SkillsForm',
    '工作经历': 'WorkExperienceForm',
    '项目经历': 'ProjectExperienceForm',
    '荣誉奖项': 'AwardsForm',
  };
  return components[module] || 'div';
};

// 头像上传处理
const handleAvatarUpload = (file) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    resume.value.avatar = e.target.result; // 读取文件并转换为 Base64
  };
  reader.readAsDataURL(file);
  return false; // 阻止默认上传行为
};

// 获取格式化的简历数据（使用 HTML）
const getFormattedResumeData = (module) => {
  switch (module) {
      case '基本信息':
        return `
          <p><strong>姓名：</strong>${resume.value.name || '未填写'}</p>
          <p><strong>电话：</strong>${resume.value.phone || '未填写'}</p>
          <p><strong>邮箱：</strong>${resume.value.email || '未填写'}</p>
        `;
    case '求职意向':
      return `
        <p><strong>状态：</strong>${resume.value.jobStatus || '未填写'}</p>
        <p><strong>期望职位：</strong>${resume.value.jobTitle || '未填写'}</p>
        <p><strong>期望薪资：</strong>${resume.value.salaryExpectation || '未填写'}</p>
      `;
    case '教育经历':
      return `
        <p><strong>毕业院校：</strong>${resume.value.education.school || '未填写'}</p>
        <p><strong>专业：</strong>${resume.value.education.major || '未填写'}</p>
        <p><strong>学历：</strong>${resume.value.education.degree || '未填写'}</p>
        <p><strong>在读时间：</strong>${formatDate(resume.value.education.studyPeriod?.[0])} - ${formatDate(resume.value.education.studyPeriod?.[1])}</p>
      `;
    case '工作经历':
      return `
        <p><strong>公司名称：</strong>${resume.value.work.company || '未填写'}</p>
        <p><strong>部门：</strong>${resume.value.work.department || '未填写'}</p>
        <p><strong>职位：</strong>${resume.value.work.position || '未填写'}</p>
        <p><strong>在职时间：</strong>${formatDate(resume.value.work.period?.[0])} - ${formatDate(resume.value.work.period?.[1])}</p>
        <p><strong>工作内容：</strong>${resume.value.work.details || '未填写'}</p>
      `;
    case '项目经历':
      return `
        <p><strong>项目名称：</strong>${resume.value.project.name || '未填写'}</p>
        <p><strong>项目时间：</strong>${formatDate(resume.value.project.period?.[0])} - ${formatDate(resume.value.project.period?.[1])}</p>
        <p><strong>项目描述：</strong>${resume.value.project.details || '未填写'}</p>
      `;
    case '荣誉奖项':
      return `
        <p><strong>奖项名称：</strong>${resume.value.award.details || '未填写'}</p>
      `;
    case '专业技能':
      return `
        <p><strong>专业技能：</strong>${resume.value.profession.skill || '未填写'}</p>
      `;
    default:
      return '<p>暂无数据</p>';
  }
};

const formatDate = (date) => {
  if (!date) return '未知日期';
  const d = new Date(date);
  return isNaN(d) ? '未知日期' : d.toISOString().split('T')[0]; // 只保留 YYYY-MM-DD
};

// 获取模块数据（用于Word导出）
const getResumeData = (module) => {
  switch (module) {
    case '基本信息':
      return `姓名：${resume.value.name || '未填写'}\n电话：${resume.value.phone || '未填写'}\n邮箱：${resume.value.email || '未填写'}`;
    case '求职意向':
      return `求职状态：${resume.value.jobStatus || '未填写'}\n期望职位：${resume.value.jobTitle || '未填写'}\n期望薪资：${resume.value.salaryExpectation || '未填写'}`;
    case '教育经历':
      return `学校：${resume.value.education?.school || '未填写'}\n专业：${resume.value.education?.major || '未填写'}\n学历：${resume.value.education?.degree || '未填写'}\n在读时间：${formatDate(resume.value.education?.studyPeriod?.[0])} - ${formatDate(resume.value.education?.studyPeriod?.[1])}`;
    case '工作经历':
      return `公司：${resume.value.work?.company || '未填写'}\n部门：${resume.value.work?.department || '未填写'}\n职位：${resume.value.work?.position || '未填写'}\n在职时间：${formatDate(resume.value.work?.period?.[0])} - ${formatDate(resume.value.work?.period?.[1])}\n工作内容：${resume.value.work?.details || '未填写'}`;
    case '项目经历':
      return `项目名称：${resume.value.project?.name || '未填写'}\n项目时间：${formatDate(resume.value.project?.period?.[0])} - ${formatDate(resume.value.project?.period?.[1])}\n项目描述：${resume.value.project?.details || '未填写'}`;
    case '荣誉奖项':
      return `奖项：${resume.value.award?.details || '未填写'}`;
    case '专业技能':
      return `技能：${resume.value.profession?.skill || '未填写'}`;
    default:
      return '暂无数据';
  }
};

// 导出功能
const exportResume = (format) => {
  const element = document.querySelector('.preview-container');
  if (format === 'pdf') {
    html2pdf().from(element).save('resume.pdf');
  } else if (format === 'word') {
    const doc = new Document({
      sections: [
        {
          properties: {},
          children: selectedModules.value.map(module => {
            return new Paragraph({
              children: [
                new TextRun({ text: module, bold: true, size: 24 }), // 模块标题
                new TextRun("\n"), // 换行
                new TextRun(getResumeData(module)), // 模块内容
                new TextRun("\n\n") // 额外换行
              ]
            });
          })
        }
      ]
    });

    Packer.toBlob(doc).then(blob => {
      saveAs(blob, "resume.docx");
    });
  }
};

// ==================== AI 优化功能 ====================
const optimizingAll = ref(false);
const optimizingModule = ref('');

// 显示优化整份简历对话框
const showOptimizeAllDialog = async () => {
  try {
    await ElMessageBox.confirm(
      '将使用 AI 优化您的整份简历，包括技能描述、工作经历、项目经历等内容。优化后您可以选择是否采用。',
      'AI 智能优化',
      {
        confirmButtonText: '开始优化',
        cancelButtonText: '取消',
        type: 'info',
      }
    );
    await optimizeFullResume();
  } catch {
    // 用户取消
  }
};

// 优化整份简历
const optimizeFullResume = async () => {
  try {
    optimizingAll.value = true;
    loading.value = true;
    loadingText.value = 'AI 正在优化您的简历...';

    const response = await fetch('http://localhost:9090/ai/resume/optimize-all', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        resumeData: {
          jobStatus: resume.value.jobStatus,
          jobTitle: resume.value.jobTitle,
          salaryExpectation: resume.value.salaryExpectation,
          education: resume.value.education,
          profession: resume.value.profession,
          work: resume.value.work,
          project: resume.value.project,
          award: resume.value.award
        },
        targetPosition: resume.value.jobTitle
      })
    });

    const result = await response.json();

    if (!result.success) {
      throw new Error(result.error || '优化失败');
    }

    // 显示优化结果确认
    await ElMessageBox.confirm(
      '简历优化完成！是否应用优化后的内容？',
      '优化完成',
      {
        confirmButtonText: '应用',
        cancelButtonText: '放弃',
        type: 'success',
      }
    );

    // 应用优化后的数据（保留基本信息和时间字段）
    const optimized = result.optimizedResume;
    if (optimized.jobStatus) resume.value.jobStatus = optimized.jobStatus;
    if (optimized.jobTitle) resume.value.jobTitle = optimized.jobTitle;
    if (optimized.salaryExpectation) resume.value.salaryExpectation = optimized.salaryExpectation;
    if (optimized.education) {
      resume.value.education = {
        ...resume.value.education,
        school: optimized.education.school || resume.value.education.school,
        major: optimized.education.major || resume.value.education.major,
        degree: optimized.education.degree || resume.value.education.degree
      };
    }
    if (optimized.profession) {
      resume.value.profession.skill = optimized.profession.skill || resume.value.profession.skill;
    }
    if (optimized.work) {
      resume.value.work = {
        ...resume.value.work,
        company: optimized.work.company || resume.value.work.company,
        department: optimized.work.department || resume.value.work.department,
        position: optimized.work.position || resume.value.work.position,
        details: optimized.work.details || resume.value.work.details
      };
    }
    if (optimized.project) {
      resume.value.project = {
        ...resume.value.project,
        name: optimized.project.name || resume.value.project.name,
        details: optimized.project.details || resume.value.project.details
      };
    }
    if (optimized.award) {
      resume.value.award.details = optimized.award.details || resume.value.award.details;
    }

    ElMessage.success('已应用 AI 优化内容');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('优化简历失败:', error);
      ElMessage.error(error.message || '优化失败');
    }
  } finally {
    optimizingAll.value = false;
    loading.value = false;
    loadingText.value = '';
  }
};

// 优化单个模块
const optimizeModule = async (moduleType, content) => {
  try {
    optimizingModule.value = moduleType;
    
    const response = await fetch('http://localhost:9090/ai/resume/optimize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        moduleType,
        content,
        targetPosition: resume.value.jobTitle
      })
    });

    const result = await response.json();

    if (!result.success) {
      throw new Error(result.error || '优化失败');
    }

    return result.optimizedContent;
  } catch (error) {
    console.error('优化模块失败:', error);
    ElMessage.error(error.message || '优化失败');
    return null;
  } finally {
    optimizingModule.value = '';
  }
};

// 润色专业技能
const handleOptimizeProfession = async () => {
  if (!resume.value.profession.skill) {
    ElMessage.warning('请先填写技能描述');
    return;
  }
  const optimized = await optimizeModule('profession', resume.value.profession.skill);
  if (optimized) {
    resume.value.profession.skill = optimized;
    ElMessage.success('专业技能润色完成');
  }
};

// 润色工作经历
const handleOptimizeWork = async () => {
  if (!resume.value.work.details) {
    ElMessage.warning('请先填写工作内容');
    return;
  }
  const optimized = await optimizeModule('work', resume.value.work);
  if (optimized && typeof optimized === 'object') {
    resume.value.work = {
      ...resume.value.work,
      company: optimized.company || resume.value.work.company,
      department: optimized.department || resume.value.work.department,
      position: optimized.position || resume.value.work.position,
      details: optimized.details || resume.value.work.details
    };
    ElMessage.success('工作经历润色完成');
  }
};

// 润色项目经历
const handleOptimizeProject = async () => {
  if (!resume.value.project.details) {
    ElMessage.warning('请先填写项目描述');
    return;
  }
  const optimized = await optimizeModule('project', resume.value.project);
  if (optimized && typeof optimized === 'object') {
    resume.value.project = {
      ...resume.value.project,
      name: optimized.name || resume.value.project.name,
      details: optimized.details || resume.value.project.details
    };
    ElMessage.success('项目经历润色完成');
  }
};

// 润色荣誉奖项
const handleOptimizeAward = async () => {
  if (!resume.value.award.details) {
    ElMessage.warning('请先填写奖项内容');
    return;
  }
  const optimized = await optimizeModule('award', resume.value.award.details);
  if (optimized) {
    resume.value.award.details = optimized;
    ElMessage.success('荣誉奖项润色完成');
  }
};

// 加载生成的简历数据
const loadGeneratedResume = async () => {
  const savedResumeData = localStorage.getItem('resumeData');
  if (savedResumeData) {
    try {
      loading.value = true;
      loadingText.value = '正在加载简历数据...';
      const parsedData = JSON.parse(savedResumeData);
      
      if (parsedData.name) {
        resume.value.name = parsedData.name;
        resume.value.phone = parsedData.phone;
        resume.value.email = parsedData.email;
      }
      
      const sections = ['jobStatus', 'jobTitle', 'salaryExpectation', 'education', 'profession', 'work', 'project', 'award'];
      for (let i = 0; i < sections.length; i++) {
        const section = sections[i];
        loadingText.value = `正在加载${getModuleName(section)}...`;
        if (typeof parsedData[section] === 'object') {
          resume.value[section] = { ...resume.value[section], ...parsedData[section] };
        } else {
          resume.value[section] = parsedData[section];
        }
        await new Promise(resolve => setTimeout(resolve, 200));
      }
      localStorage.removeItem('resumeData');
      ElMessage.success('简历数据加载完成');
    } catch (error) {
      console.error('解析简历数据失败:', error);
      ElMessage.error('加载简历数据失败: ' + error.message);
    } finally {
      loading.value = false;
      loadingText.value = '';
    }
  }
};

// 获取模块名称
const getModuleName = (section) => {
  const moduleNames = {
    jobStatus: '求职状态',
    jobTitle: '期望职位',
    salaryExpectation: '期望薪资',
    education: '教育经历',
    profession: '专业技能',
    work: '工作经历',
    project: '项目经历',
    award: '获奖情况'
  };
  return moduleNames[section] || section;
};

// 加载最新简历数据
const loadLatestResume = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    ElMessage.warning('请先登录');
    return;
  }

  try {
    loading.value = true;
    loadingText.value = '正在加载最新简历...';

    const response = await fetch(`http://localhost:9090/ai/resume/latest?userId=${userId}`);
    const result = await response.json();

    if (!result.success) {
      throw new Error(result.error || '加载失败');
    }

    // 更新简历数据
    const resumeData = result.content;
    updateResumeData(resumeData);

    ElMessage.success('简历加载完成');
  } catch (error) {
    console.error('加载简历失败:', error);
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
    loadingText.value = '';
  }
};

// 更新简历到数据库
const updateResume = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    ElMessage.warning('请先登录');
    return;
  }

  try {
    updating.value = true;
    
    const response = await fetch('http://localhost:9090/ai/resume/update', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId,
        resumeData: {
          name: resume.value.name,
          phone: resume.value.phone,
          email: resume.value.email,
          avatar: resume.value.avatar,
          jobStatus: resume.value.jobStatus,
          jobTitle: resume.value.jobTitle,
          salaryExpectation: resume.value.salaryExpectation,
          education: resume.value.education,
          profession: resume.value.profession,
          work: resume.value.work,
          project: resume.value.project,
          award: resume.value.award
        }
      })
    });

    const result = await response.json();
    
    if (!result.success) {
      throw new Error(result.error || '更新失败');
    }

    ElMessage.success('简历更新成功');
  } catch (error) {
    console.error('更新简历失败:', error);
    ElMessage.error(error.message);
  } finally {
    updating.value = false;
  }
};

// 更新简历数据
const updateResumeData = (data) => {
  resume.value = {
    name: data.name || '',
    phone: data.phone || '',
    email: data.email || '',
    avatar: data.avatar || '',
    jobStatus: data.jobStatus || '',
    jobTitle: data.jobTitle || '',
    salaryExpectation: data.salaryExpectation || '',
    education: data.education || { 
      school: '', 
      major: '', 
      degree: '', 
      studyPeriod: [] 
    },
    profession: data.profession || {
      skill: ''
    },
    work: data.work || { 
      company: '', 
      department: '', 
      position: '', 
      period: [], 
      details: '' 
    },
    project: data.project || { 
      name: '', 
      period: [], 
      details: '' 
    },
    award: data.award || {
      details: ''
    }
  };
};

// 从个人信息中填充基本数据
const loadProfileData = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) return;

  try {
    const response = await fetch(`http://localhost:9090/api/profile/resume-data/${userId}`);
    const result = await response.json();

    if (result.success && result.data) {
      const data = result.data;
      // 只填充空字段，不覆盖已有数据
      if (!resume.value.name && data.name) resume.value.name = data.name;
      if (!resume.value.phone && data.phone) resume.value.phone = data.phone;
      if (!resume.value.email && data.email) resume.value.email = data.email;
      if (!resume.value.jobStatus && data.jobStatus) resume.value.jobStatus = data.jobStatus;
      if (!resume.value.jobTitle && data.jobTitle) resume.value.jobTitle = data.jobTitle;
      if (!resume.value.salaryExpectation && data.salaryExpectation) resume.value.salaryExpectation = data.salaryExpectation;
      
      // 教育信息
      if (data.education) {
        if (!resume.value.education.school && data.education.school) resume.value.education.school = data.education.school;
        if (!resume.value.education.major && data.education.major) resume.value.education.major = data.education.major;
        if (!resume.value.education.degree && data.education.degree) resume.value.education.degree = data.education.degree;
      }
      
      // 专业技能
      if (data.profession && !resume.value.profession.skill && data.profession.skill) {
        resume.value.profession.skill = data.profession.skill;
      }
    }
  } catch (error) {
    console.error('加载个人信息失败:', error);
  }
};

// 在组件挂载时加载数据
onMounted(async () => {
  const savedResumeData = localStorage.getItem('resumeData');
  if (savedResumeData) {
    await loadGeneratedResume();
  } else {
    await loadLatestResume();
  }
  // 尝试从个人信息补充空字段
  await loadProfileData();
});
</script>

<style scoped>
/* 页面容器 */
.resume-page {
  min-height: calc(100vh - 56px);
  background: #f5f3ff;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 内容区域 */
.resume-content {
  display: flex;
  padding: 24px;
  gap: 24px;
}

/* 工具栏 */
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

.toolbar-actions {
  display: flex;
  gap: 12px;
}

/* 左侧模块选择 */
.sidebar {
  width: 200px;
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
  flex-shrink: 0;
  height: fit-content;
}

.module-list h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 16px 0;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.el-checkbox) {
  margin-right: 0;
}

/* 中间表单区域 */
.form-container {
  flex: 1;
  max-width: 520px;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
  overflow-y: auto;
  max-height: calc(100vh - 160px);
}

.form-container h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 24px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #6366f1;
}

.form-container h3:first-child {
  margin-top: 0;
}

/* 模块标题栏（带AI润色按钮） */
.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #6366f1;
}

.module-header h3 {
  margin: 0 !important;
  padding-bottom: 0 !important;
  border-bottom: none !important;
}

.module-header .el-button {
  font-size: 12px;
}

/* 头像上传 */
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 2px dashed #c7d2fe;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  background: #f5f3ff;
  transition: border-color 0.2s;
}

.avatar-uploader:hover {
  border-color: #6366f1;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

/* 右侧预览区域 */
.preview-container {
  flex: 1;
  min-width: 400px;
  max-width: 560px;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
  overflow-y: auto;
  max-height: calc(100vh - 160px);
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.preview-contact {
  flex: 1;
}

.preview-contact h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 12px 0;
}

.preview-contact p {
  font-size: 14px;
  color: #64748b;
  margin: 6px 0;
}

.preview-avatar-container {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #f5f3ff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.preview-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 预览区块 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #6366f1;
  background: #f5f3ff;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.section p {
  font-size: 14px;
  color: #1e1b4b;
  line-height: 1.6;
  margin: 8px 0;
}

.section strong {
  color: #64748b;
  font-weight: 500;
}

/* 滚动条样式 */
.form-container::-webkit-scrollbar,
.preview-container::-webkit-scrollbar {
  width: 6px;
}

.form-container::-webkit-scrollbar-track,
.preview-container::-webkit-scrollbar-track {
  background: #f5f3ff;
  border-radius: 3px;
}

.form-container::-webkit-scrollbar-thumb,
.preview-container::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 3px;
}

/* 加载状态 */
.loading-text {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
  background: #fff;
  padding: 24px 32px;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.15);
  color: #6366f1;
  font-size: 15px;
  font-weight: 500;
}

.form-container.loading,
.preview-container.loading {
  opacity: 0.5;
  pointer-events: none;
}

/* 响应式 */
@media (max-width: 1200px) {
  .preview-container {
    min-width: 360px;
  }
}

@media (max-width: 900px) {
  .resume-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .form-container,
  .preview-container {
    max-width: none;
    min-width: auto;
    max-height: none;
  }
}
</style>