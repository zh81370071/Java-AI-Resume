<template>
  <div class="auth-page">
    <!-- 注册卡片 -->
    <div class="auth-card">
      <!-- Logo区域 -->
      <div class="auth-header">
        <div class="logo-wrapper">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <line x1="19" y1="8" x2="19" y2="14"></line>
            <line x1="22" y1="11" x2="16" y2="11"></line>
          </svg>
        </div>
        <h1 class="auth-title">加入职达</h1>
        <p class="auth-subtitle">创建账户，开启智能求职之旅</p>
      </div>

      <!-- 表单区域 -->
      <div class="auth-form">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input 
            type="text" 
            v-model="ruleForm.username" 
            class="auth-input" 
            placeholder="请输入用户名（2-15个字符）"
          />
        </div>

        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input 
            type="email" 
            v-model="ruleForm.email" 
            class="auth-input" 
            placeholder="请输入邮箱地址"
          />
        </div>

        <div class="form-group">
          <label class="form-label">手机号</label>
          <input 
            type="tel" 
            v-model="ruleForm.phone" 
            class="auth-input" 
            placeholder="请输入11位手机号"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input 
            type="password" 
            v-model="ruleForm.password" 
            class="auth-input" 
            placeholder="请输入密码（6-20个字符）"
          />
        </div>

        <div class="form-group">
          <label class="form-label">验证码</label>
          <div class="captcha-wrapper">
            <input 
              type="text" 
              v-model="sidentifyMode" 
              class="auth-input captcha-input" 
              placeholder="请输入验证码"
            />
            <div class="captcha-display" @click="refreshCode">
              <SIdentify :identifyCode="identifyCode"></SIdentify>
            </div>
          </div>
        </div>

        <button class="auth-btn" @click="register()">立即注册</button>
      </div>

      <!-- 底部链接 -->
      <div class="auth-footer">
        <span class="footer-text">已有账号？</span>
        <a class="footer-link" @click="login()">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import SIdentify from '@/utils/SidentifyView.vue'
import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import axios from '@/utils/axios-config.js'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

let sidentifyMode = ref('') //输入框验证码
let identifyCode = ref('') //图形验证码
let identifyCodes = ref('1234567890abcdefjhijklinopqrsduvwxyz') //验证码出现的数字和字母

onMounted(() => {
  identifyCode.value = ''
  makeCode(identifyCodes.value, 4)
})

const randomNum = (min, max) => {
  max = max + 1
  return Math.floor(Math.random() * (max - min) + min)
}

const makeCode = (o, l) => {
  for (let i = 0; i < l; i++) {
    identifyCode.value += o[randomNum(0, o.length)]
  }
}

const refreshCode = () => {
  identifyCode.value = ''
  makeCode(identifyCodes.value, 4)
}

const ruleForm = ref({
  username: '',
  email: '',
  password: '',
  phone: ''
})

const rules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 15, message: '用户名长度在 2 到 15 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '请输入电话号', trigger: 'blur' },
    { min: 11, max: 11, message: '请输入正确的11位电话号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

const register = () => {
  // 验证用户名
  if (!ruleForm.value.username || ruleForm.value.username.length < 2 || ruleForm.value.username.length > 15) {
    ElMessage({ type: 'error', message: '用户名长度需在2-15个字符之间' })
    return
  }
  // 验证邮箱
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!ruleForm.value.email || !emailReg.test(ruleForm.value.email)) {
    ElMessage({ type: 'error', message: '请输入正确的邮箱地址' })
    return
  }
  // 验证手机号
  if (!ruleForm.value.phone || ruleForm.value.phone.length !== 11) {
    ElMessage({ type: 'error', message: '请输入正确的11位手机号' })
    return
  }
  // 验证密码
  if (!ruleForm.value.password || ruleForm.value.password.length < 6 || ruleForm.value.password.length > 20) {
    ElMessage({ type: 'error', message: '密码长度需在6-20个字符之间' })
    return
  }
  // 验证验证码
  if (!sidentifyMode.value) {
    ElMessage({ type: 'error', message: '验证码不能为空！' })
    return
  }
  if (sidentifyMode.value !== identifyCode.value) {
    ElMessage({ type: 'error', message: '验证码错误' })
    refreshCode()
    return
  }
  
  const requestData = {
    username: ruleForm.value.username,
    email: ruleForm.value.email,
    password: ruleForm.value.password,
    phone: ruleForm.value.phone
  }
  axios.post('user/add', requestData)
      .then((response) => {
        if (response.data === "新增用户成功！") {
          ElMessage({ type: 'success', message: '注册成功' })
          setTimeout(() => {
            router.push('/login')
          }, 1000)
        } else {
          ElMessage({ type: 'error', message: '注册失败' })
        }
      })
      .catch((error) => {
        ElMessage({ type: 'error', message: '请求出错：' + error })
      })
}

const login = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 页面容器 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('@/assets/bg.jpeg') no-repeat center center;
  background-size: cover;
  font-family: 'PingFang SC', 'Noto Sans SC', sans-serif;
  padding: 20px;
}

/* 卡片 */
.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.1);
}

/* 头部区域 */
.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: #10b981;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-wrapper .logo-icon {
  width: 32px;
  height: 32px;
  stroke: #fff;
}

.auth-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 6px 0;
}

.auth-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 表单区域 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  width: 100%;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.auth-input {
  width: 100%;
  height: 46px;
  padding: 0 16px;
  font-size: 15px;
  color: #1e1b4b;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.auth-input::placeholder {
  color: #9ca3af;
}

.auth-input:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

/* 验证码区域 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-display {
  height: 46px;
  min-width: 110px;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

/* 注册按钮 */
.auth-btn {
  width: 100%;
  height: 48px;
  background: #10b981;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  margin-top: 4px;
  transition: background 0.2s;
}

.auth-btn:hover {
  background: #059669;
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  margin-top: 24px;
}

.footer-text {
  font-size: 14px;
  color: #64748b;
}

.footer-link {
  font-size: 14px;
  color: #6366f1;
  font-weight: 500;
  cursor: pointer;
  margin-left: 4px;
}

.footer-link:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 480px) {
  .auth-card {
    padding: 28px 20px;
  }
  
  .auth-title {
    font-size: 22px;
  }
}
</style>
