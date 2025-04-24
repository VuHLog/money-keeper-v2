import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@pages/auth/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@pages/Dashboard.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/accounts',
    name: 'Accounts',
    component: () => import('@pages/Accounts.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/income',
    name: 'Income',
    component: () => import('@pages/Income.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/expense',
    name: 'Expense',
    component: () => import('@pages/Expense.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('@pages/Categories.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@pages/Statistics.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@pages/History.vue'),
    // meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@pages/Profile.vue'),
    // meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router 