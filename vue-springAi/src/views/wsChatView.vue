<template>
  <div class="chat-page">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="sidebar-title" v-if="!isCollapsed">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
          <span>会话记录</span>
        </div>
        <button class="toggle-btn" @click="toggleSidebar">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline :points="isCollapsed ? '9 18 15 12 9 6' : '15 18 9 12 15 6'"></polyline>
          </svg>
        </button>
      </div>

      <div class="sidebar-content" v-if="!isCollapsed">
        <div v-if="historyList.length === 0" class="empty-state">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"></circle>
            <path d="M12 6v6l4 2"></path>
          </svg>
          <p>暂无历史记录</p>
        </div>
        <ul v-else class="history-list">
          <li v-for="history in historyList" :key="history.id" class="history-item" @click="showHistoryDetail(history.id)">
            <div class="history-content">
              <span class="history-text">{{ history.question ? history.question.substring(0, 28) + '...' : '无内容' }}</span>
              <span class="history-time">{{ formatTime(history.time) }}</span>
            </div>
          </li>
        </ul>
      </div>

      <div class="sidebar-footer" v-if="!isCollapsed">
        <button class="footer-btn" @click="openSettingsDialog">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"></circle>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
          </svg>
          设置
        </button>
      </div>

      <!-- 设置弹窗 -->
      <el-dialog v-model="settingsDialogVisible" title="系统设置" width="360px" center>
        <div class="settings-content">
          <div class="setting-item">
            <span>主题模式</span>
            <div class="theme-toggle">
              <button :class="['theme-btn', theme === 'light' ? 'active' : '']" @click="changeTheme('light')">浅色</button>
              <button :class="['theme-btn', theme === 'dark' ? 'active' : '']" @click="changeTheme('dark')">深色</button>
            </div>
          </div>
        </div>
      </el-dialog>
    </aside>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <div class="chat-container">
        <!-- 消息列表 -->
        <div class="message-list">
          <div v-if="messageList.length === 0" class="welcome-card">
            <div class="welcome-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
              </svg>
            </div>
            <h2>Hi，我是职达AI助手</h2>
            <p>我可以帮你答疑解惑、生成简历、推荐职位，让求职更轻松！</p>
            <div class="quick-actions">
              <button class="quick-btn" @click="inputMessage = '帮我优化简历'">优化简历</button>
              <button class="quick-btn" @click="inputMessage = '推荐适合我的职位'">推荐职位</button>
              <button class="quick-btn" @click="inputMessage = '面试技巧有哪些'">面试技巧</button>
            </div>
          </div>

          <div v-for="(message, index) in messageList" :key="index" :class="['message-row', message.sender]">
            <div v-if="message.sender === 'ai'" class="avatar-wrapper">
              <div class="ai-avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                </svg>
              </div>
            </div>
            <div :class="['message-bubble', message.sender]">
              {{ message.text }}
            </div>
            <div v-if="message.sender === 'user'" class="avatar-wrapper">
              <div class="user-avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <button class="history-btn" @click="clickHistory">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            历史
          </button>
          <div class="input-wrapper">
            <input 
              v-model="inputMessage" 
              type="text" 
              placeholder="输入你的问题..." 
              @keyup.enter="sendMessage"
              :disabled="loading"
            />
          </div>
          <button class="send-btn" @click="sendMessage" :disabled="loading || !inputMessage.trim()">
            <svg v-if="!loading" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
            <div v-else class="btn-spinner"></div>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import {ref, nextTick, onMounted, onBeforeUnmount, computed} from 'vue';
import { ElButton, ElInput, ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import axios from 'axios';
import dayjs from 'dayjs';
import 'element-plus/dist/index.css';

const router = useRouter();
const messageList = ref([]); // 消息列表
const inputMessage = ref(''); // 输入框内容
const loading = ref(false); // 加载状态
const isCollapsed = ref(false); // 侧边栏默认保持展开状态
const toggleSidebar = () => isCollapsed.value = !isCollapsed.value;
const historyList = ref([]);
const currentHistory = ref(null);

// 用户和AI的头像
const userAvatar = 'src/assets/user.jpg'; // 用户头像链接
const aiAvatar = 'src/assets/ai.jpg'; // AI头像链接

let socket = null; // WebSocket对象

const clickHistory = () => {
    router.push('/history')
  }
  

const clickProfile = () => router.push('/profile');

// 从 localStorage 获取用户信息
const userId = localStorage.getItem('userId');
const username = localStorage.getItem('username');
const parentEmail = localStorage.getItem('parentEmail');

// 发送消息到 WebSocket
const sendMessage = () => {
  if (inputMessage.value.trim() === '') return; // 输入为空时不发送消息

  // 将用户消息加入消息列表
  messageList.value.push({
    text: inputMessage.value,
    sender: 'user',
    avatar: userAvatar,
  });

  const userMessage = inputMessage.value; // 保存用户输入内容
  inputMessage.value = ''; // 清空输入框

  // 初始化AI消息（用于动态更新）
  const aiMessage = {
    text: '',
    sender: 'ai',
    avatar: aiAvatar,
  };
  messageList.value.push(aiMessage);

  loading.value = true;

  // 发送用户消息到 WebSocket 服务器，同时携带 userId 和 username
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({
      type: 'message',
      text: userMessage,
      userId: userId,
      username: username
    }));
  } else {
    ElMessage.error('WebSocket 连接未建立');
  }
};

// WebSocket连接及接收处理
const setupWebSocket = () => {
  socket = new WebSocket('ws://localhost:9090/ws/chat'); // 根据需要修改WebSocket服务器地址

  socket.onopen = () => {
    console.log('WebSocket 已连接');
  };

  socket.onmessage = (event) => {
    try {
      let message = event.data;
      // 检查消息是否包含 "data:" 前缀并去除它
      if (message.startsWith("data:")) {
        message = message.substring(5); // 移除 "data:" 前缀
        if (message.includes("[DONE]")) {
          loading.value = false; // 停止加载状态
          console.log('AI 流已完成');
          return; // 结束处理
        }
        // 找到最新的 AI 消息并更新其文本内容
        const aiMessage = message;
        const latestMessage = messageList.value[messageList.value.length - 1];
        if (latestMessage && latestMessage.sender === 'ai') {
          latestMessage.text += aiMessage; // 累积更新 AI 回复
          nextTick(); // 强制 DOM 更新
        }
      }
    } catch (error) {
      console.error('处理 WebSocket 消息时出错:', error);
    }
  };

  socket.onerror = (error) => {
    console.error('WebSocket 错误:', error);
    ElMessage.error('WebSocket 连接出错');
  };

  socket.onclose = (event) => {
    if (event.wasClean) {
      console.log('WebSocket 连接关闭');
    } else {
      console.error('WebSocket 连接异常关闭');
    }
  };
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 加载历史记录列表
const loadHistoryList = async () => {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    console.log('开始加载历史记录...');
    const response = await axios.get('/api/history/list', {
      params: {
        userId,
        pageNum: 1,
        pageSize: 10
      }
    });
    console.log('历史记录数据:', response.data);
    if (response.data && response.data.records) {
      historyList.value = response.data.records;
    }
  } catch (error) {
    console.error('加载历史记录失败:', error);
    ElMessage.error('加载历史记录失败: ' + (error.response?.data?.message || error.message));
  }
};

// 显示历史记录详情
const showHistoryDetail = async (historyId) => {
  try {
    console.log('加载历史记录详情:', historyId);
    const response = await axios.get(`/api/history/detail/${historyId}`);
    const history = response.data;
    
    // 清空当前消息列表
    messageList.value = [];
    
    // 添加历史对话到消息列表
    messageList.value.push({
      text: history.question,
      sender: 'user',
      avatar: userAvatar
    });
    
    messageList.value.push({
      text: history.result,
      sender: 'ai',
      avatar: aiAvatar
    });
    
    currentHistory.value = history;
  } catch (error) {
    console.error('加载对话详情失败:', error);
    ElMessage.error('加载对话详情失败: ' + (error.response?.data?.message || error.message));
  }
};

onMounted(async () => {
  console.log('组件挂载，准备加载历史记录...');
  await loadHistoryList();
  setupWebSocket();
});

onBeforeUnmount(() => {
  if (socket) {
    socket.close(); // 在组件卸载时关闭 WebSocket 连接
  }
});

// 新增计算属性用于布局
const sidebarWidth = computed(() =>
    isCollapsed.value ? '60px' : '240px'
);

const profilePopoverVisible = ref(false);
const settingsDialogVisible = ref(false);
const contactDialogVisible = ref(false);
const theme = ref('light'); // 默认浅色

const openSettingsDialog = () => {
  profilePopoverVisible.value = false;
  settingsDialogVisible.value = true;
};

const openContactDialog = () => {
  profilePopoverVisible.value = false;
  contactDialogVisible.value = true;
};

const changeTheme = (val) => {
  if (val === 'dark') {
    document.documentElement.setAttribute('class', 'dark');
  } else {
    document.documentElement.removeAttribute('class');
  }
};

const logout = () => {
  localStorage.clear();
  router.push('/login'); // 跳转到登录页
};

</script>



<style scoped>
/* 页面容器 */
.chat-page {
  display: flex;
  height: calc(100vh - 56px);
  background: #f5f3ff;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
}

.sidebar.collapsed {
  width: 56px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1e1b4b;
}

.sidebar-title svg {
  color: #6366f1;
}

.toggle-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: #f5f3ff;
  color: #6366f1;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.empty-state svg {
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.history-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.history-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 8px;
  background: #fafafa;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f5f3ff;
}

.history-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-text {
  font-size: 14px;
  color: #1e1b4b;
  line-height: 1.4;
}

.history-time {
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.footer-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  font-size: 14px;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.footer-btn:hover {
  background: #f5f3ff;
  color: #6366f1;
}

/* 设置弹窗 */
.settings-content {
  padding: 8px 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setting-item span {
  font-size: 14px;
  color: #1e1b4b;
}

.theme-toggle {
  display: flex;
  gap: 8px;
}

.theme-btn {
  padding: 8px 16px;
  font-size: 13px;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-btn.active {
  color: #6366f1;
  background: #f5f3ff;
  border-color: #6366f1;
}

/* 主聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 24px;
}

.chat-container {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.06);
  overflow: hidden;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.welcome-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  text-align: center;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f3ff;
  border-radius: 20px;
  margin-bottom: 24px;
}

.welcome-icon svg {
  color: #6366f1;
}

.welcome-card h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 12px 0;
}

.welcome-card p {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 32px 0;
  max-width: 400px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.quick-btn {
  padding: 10px 20px;
  font-size: 14px;
  color: #6366f1;
  background: #f5f3ff;
  border: 1px solid #c7d2fe;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #ede9fe;
}

/* 消息行 */
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.ai-avatar,
.user-avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.ai-avatar {
  background: #f5f3ff;
  color: #6366f1;
}

.user-avatar {
  background: #ecfdf5;
  color: #10b981;
}

.message-bubble {
  max-width: 70%;
  padding: 14px 18px;
  font-size: 15px;
  line-height: 1.6;
  border-radius: 16px;
}

.message-bubble.ai {
  background: #f8fafc;
  color: #1e1b4b;
  border-radius: 16px 16px 16px 4px;
}

.message-bubble.user {
  background: #6366f1;
  color: #fff;
  border-radius: 16px 16px 4px 16px;
}

/* 输入区域 */
.input-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #fafafa;
}

.history-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 14px;
  color: #64748b;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-btn:hover {
  color: #6366f1;
  border-color: #c7d2fe;
}

.input-wrapper {
  flex: 1;
}

.input-wrapper input {
  width: 100%;
  height: 48px;
  padding: 0 18px;
  font-size: 15px;
  color: #1e1b4b;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.input-wrapper input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.input-wrapper input::placeholder {
  color: #94a3b8;
}

.send-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #6366f1;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.send-btn:disabled {
  background: #c7d2fe;
  cursor: not-allowed;
}

.send-btn svg {
  color: #fff;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 响应式 */
@media (max-width: 900px) {
  .sidebar {
    width: 56px;
  }
  
  .sidebar-content,
  .sidebar-footer,
  .sidebar-title {
    display: none;
  }
  
  .chat-main {
    padding: 16px;
  }
  
  .message-bubble {
    max-width: 85%;
  }
}
</style>

