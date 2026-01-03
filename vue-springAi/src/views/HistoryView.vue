<template>
  <div class="history-container">
    <div class="header">
      <el-button type="primary" @click="fetchBack">返回</el-button>
      <h2>历史对话记录</h2>
    </div>
    
    <el-card class="history-card">
      <el-table 
        :data="tableData" 
        stripe 
        style="width: 100%"
        v-loading="loading"
        element-loading-text="加载中..."
      >
        <el-table-column type="index" :index="indexMethod" label="序号" width="80" align="center" />
        <el-table-column prop="question" label="问题" min-width="200">
          <template #default="scope">
            <div class="question-cell">
              {{ scope.row.question }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="回答" min-width="300">
          <template #default="scope">
            <div class="result-cell">
              {{ scope.row.result }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="180" align="center">
          <template #default="scope">
            <span>{{ formatDate(scope.row.time) }}</span>
          </template>
        </el-table-column>
      </el-table>
  
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>
  
<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import dayjs from 'dayjs';

const router = useRouter();
const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

const fetchBack = () => {
  router.push('/wsChatView');
};

// 自定义索引方法
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};

// 获取历史记录
const fetchHistory = async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  loading.value = true;
  try {
    const response = await axios.get("/api/history/getHistory", {
      params: {
        userId,
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      },
    });
    
    if (response.data && response.data.records) {
      tableData.value = response.data.records;
      total.value = response.data.total;
    }
  } catch (error) {
    console.error("获取历史记录失败:", error);
    ElMessage.error('获取历史记录失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loading.value = false;
  }
};

// 页码改变时触发
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchHistory();
};

// 每页条数改变时触发
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1; // 重置到第一页
  fetchHistory();
};

// 格式化日期
const formatDate = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

onMounted(() => {
  fetchHistory();
});
</script>
  
<style scoped>
/* 页面容器 */
.history-container {
  padding: 32px 24px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'PingFang SC', 'Noto Sans SC', -apple-system, sans-serif;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1e1b4b;
}

/* 卡片 */
.history-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.06);
}

/* 单元格 */
.question-cell,
.result-cell {
  max-height: 100px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 8px 0;
  font-size: 14px;
  color: #1e1b4b;
  line-height: 1.5;
}

/* 分页 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* Element Plus 覆盖 */
:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-table) {
  margin-bottom: 0;
}

:deep(.el-table th.el-table__cell) {
  background: #f5f3ff;
  color: #6366f1;
  font-weight: 600;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover > td) {
  background-color: #faf5ff !important;
}

:deep(.el-button--primary) {
  background: #6366f1;
  border-color: #6366f1;
}

:deep(.el-button--primary:hover) {
  background: #4f46e5;
  border-color: #4f46e5;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: #6366f1;
  color: #fff;
}

:deep(.el-pagination .el-pager li:hover) {
  color: #6366f1;
}
</style>