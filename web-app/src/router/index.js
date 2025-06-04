import { createRouter, createWebHistory } from 'vue-router'
import { jwtDecode } from "jwt-decode";
import { useAuthStore } from "@/store/AuthStore.js";

const routes = [
  {
    path: '/auth/sign-in',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/auth/sign-up',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/auth/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@pages/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/accounts',
    name: 'Accounts',
    component: () => import('@pages/Accounts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/income',
    name: 'Income',
    component: () => import('@pages/Income.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/expense',
    name: 'Expense',
    component: () => import('@pages/Expense.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/goals',
    name: 'Goals',
    component: () => import('@pages/Goals.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('@pages/Categories.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/transaction-history',
    name: 'Transaction history',
    component: () => import('@pages/TransactionHistory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@pages/Statistics.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/report',
    name: 'Report',
    component: () => import('@pages/Report.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/spending-limits',
    name: 'SpendingLimit',
    component: () => import('@pages/SpendingLimit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@pages/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/dashboard",
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  linkActiveClass: "active",
});

router.beforeEach((to, from, next) => {
  const store = useAuthStore();
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!isLoggedIn() || !store.isLoggedIn) {
      next({
        path: "/auth/sign-in",
        query: { redirect: to.fullPath },
      });
    } else {
      next();
    }
  } else {
    next();
  }
});

//hàm check login với token
function isLoggedIn() {
  const store = useAuthStore();
  const token = sessionStorage.getItem("token");

  if (token) {
    //giải mã token
    const decoded = jwtDecode(token);
    store.roles = decoded.scope.replaceAll("ROLE_", "");
    store.username = decoded.sub;
    store.fullName = decoded.name;

    // Kiểm tra xem token có hết hạn hay không
    const expirationDate = new Date(decoded.exp * 1000);
    if (expirationDate <= new Date()) {
      // Nếu token đã hết hạn, xóa nó khỏi sessionStorage và trả về false
      sessionStorage.removeItem("token");
      return false;
    } else {
      // Nếu token hợp lệ, trả về true
      return true;
    }
  } else {
    // Nếu token không tồn tại trong sessionStorage, trả về false
    return false;
  }
}

export default router;