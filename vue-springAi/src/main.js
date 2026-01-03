import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'

// 配置axios默认URL
axios.defaults.baseURL = 'http://localhost:9090'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
