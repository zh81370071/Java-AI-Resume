import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
     },
     {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
     },
     {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
     },
     {
      path: '/index',
      name: 'index',
      component: () => import('../views/indexView.vue'),
      children: [
        {
          path: '/wsChatView',
          name: 'wsChatView',
          component: () => import('../views/wsChatView.vue'),
        },
        {
          path: '/history',
          name: 'history',
          component: () => import('../views/HistoryView.vue')
        },
        {
          path: '/resume',
          name: 'resume',
          component: () => import('../views/Resume.vue')
        },
        {
          path: '/jobPool',
          name: 'jobPool',
          component: () => import('../views/jobPool.vue')
        },
        {
          path: '/recommend',
          name: 'recommend',
          component: () => import('../views/recommend.vue')
        },
        {
          path: '/profile',
          name: 'profile',
          component: () => import('../views/Profile.vue')
        }
      ]
     }
  ],
})

export default router
