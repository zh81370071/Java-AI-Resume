<template>
  <div class="auth-page">
    <!-- 登录卡片 -->
    <div class="auth-card">
      <!-- Logo区域 -->
      <div class="auth-header">
        <div class="logo-wrapper">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
            <path d="M2 17l10 5 10-5"></path>
            <path d="M2 12l10 5 10-5"></path>
          </svg>
        </div>
        <h1 class="auth-title">职达助手</h1>
        <p class="auth-subtitle">AI驱动的智能求职平台</p>
      </div>

      <!-- 表单区域 -->
      <div class="auth-form">
        <div class="form-group">
          <label class="form-label">账号</label>
          <input 
            type="text" 
            v-model="ruleForm.email" 
            class="auth-input" 
            placeholder="请输入用户名或邮箱"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input 
            type="password" 
            v-model="ruleForm.password" 
            class="auth-input" 
            placeholder="请输入密码"
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

        <button class="auth-btn" @click="submitForm()">登 录</button>
      </div>

      <!-- 底部链接 -->
      <div class="auth-footer">
        <span class="footer-text">还没有账号？</span>
        <a class="footer-link" @click="register()">立即注册</a>
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

//获取路由器
let sidentifyMode = ref('') //输入框验证码
let identifyCode = ref('') //图形验证码
// 只使用数字，更容易辨认
let identifyCodes = ref('0123456789')

//组件挂载
onMounted(() => {
  identifyCode.value = ''
  makeCode(identifyCodes.value, 4)
})

// 生成随机数
const randomNum = (min, max) => {
  max = max + 1
  return Math.floor(Math.random() * (max - min) + min)
}
// 随机生成验证码字符串
const makeCode = (o, l) => {
  for (let i = 0; i < l; i++) {
    identifyCode.value += o[randomNum(0, o.length - 1)]
  }
}
// 更新验证码
const refreshCode = () => {
  identifyCode.value = ''
  makeCode(identifyCodes.value, 4)
}

const ruleForm = ref({
  email: '',
  password: ''
})

const submitForm = () => {
  //验证验证码不为空
  if (!sidentifyMode.value) {
    ElMessage({ type: 'error', message: '验证码不能为空！' })
    return
  }
  // 验证验证码是否正确（忽略大小写）
  if (sidentifyMode.value.toLowerCase() !== identifyCode.value.toLowerCase()) {
    ElMessage({ type: 'error', message: '验证码错误' })
    refreshCode()
    return
  } else {
    const requestData = {
      email: ruleForm.value.email,
      password: ruleForm.value.password
    }

    console.log('请求数据' + requestData.email + ' ' + requestData.password)

    axios
        .post('user/login', requestData)
        .then(function (response) {
          console.log('响应数据：', response.data)
          if (response.data !== null ) {
            ElMessage({ type: 'success', message: '登录成功' })
            localStorage.setItem('username', response.data.username)
            localStorage.setItem('userId', response.data.id)
            localStorage.setItem('email', response.data.email)
            localStorage.setItem('userType', response.data.userType)
            console.log('localStorage.getItem(username):' + localStorage.getItem('username'))
            router.push('/wschatView')
          } else {
            ElMessage({ type: 'error', message: response.data.msg })
          }
        })
        .catch(function (error) {
          console.error('请求出错：', error)
        })
  }
}

const register = () => {
  router.push('/register')
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
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.1);
}

/* 头部区域 */
.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: #6366f1;
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
  gap: 20px;
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
  height: 48px;
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
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
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
  height: 48px;
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

/* 登录按钮 */
.auth-btn {
  width: 100%;
  height: 48px;
  background: #6366f1;
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
  background: #4f46e5;
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
    padding: 32px 24px;
  }
  
  .auth-title {
    font-size: 22px;
  }
}
</style>