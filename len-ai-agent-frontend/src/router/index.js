import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/love-app',
    name: 'loveApp',
    component: () => import('../views/LoveAppView.vue')
  },
  {
    path: '/manus-app',
    name: 'manusApp',
    component: () => import('../views/ManusAppView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 