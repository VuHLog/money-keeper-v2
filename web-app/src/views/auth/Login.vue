<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'

const router = useRouter()
const email = ref('')
const password = ref('')
const rememberMe = ref(false)

const handleLogin = async () => {
  try {
    // TODO: Implement login logic with backend API
    await router.push('/dashboard')
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi đăng nhập',
      text: 'Email hoặc mật khẩu không chính xác',
      confirmButtonColor: '#007AFF'
    })
  }
}

const handleGoogleLogin = async () => {
  try {
    // TODO: Implement Google login logic
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi đăng nhập',
      text: 'Không thể đăng nhập bằng Google',
      confirmButtonColor: '#007AFF'
    })
  }
}
</script>

<template>
  <div class="h-full flex">
    <!-- Left side with image and text -->
    <div class="hidden lg:flex lg:w-1/2 bg-primary items-center justify-center relative">
      <div class="text-center text-white z-10 p-8">
        <h1 class="text-3xl font-bold mb-3">MONEY KEEPER</h1>
        <p class="text-lg opacity-80">Quản lý chi tiêu thông minh, tương lai tài chính vững vàng</p>
      </div>
      <div class="absolute inset-0 bg-black opacity-40"></div>
    </div>

    <!-- Right side with form -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-6">
      <div class="w-full max-w-[400px]">
        <div class="text-center mb-6">
          <h2 class="text-2xl font-bold text-text mb-2">Đăng Nhập</h2>
          <p class="text-text-secondary">Chào mừng bạn quay trở lại</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <input
              type="email"
              v-model="email"
              required
              class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors duration-200"
              placeholder="Email"
            />
          </div>

          <div>
            <input
              type="password"
              v-model="password"
              required
              class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors duration-200"
              placeholder="Mật khẩu"
            />
          </div>

          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input type="checkbox" v-model="rememberMe" class="rounded border-gray-300 text-primary focus:ring-primary" />
              <span class="ml-2 text-sm text-text-secondary">Ghi nhớ đăng nhập</span>
            </label>
            <router-link to="/forgot-password" class="text-sm text-primary hover:text-primary/80">
              Quên mật khẩu?
            </router-link>
          </div>

          <button
            type="submit"
            class="w-full bg-primary text-white py-2.5 rounded-lg font-medium hover:bg-primary/90 transition-colors duration-200"
          >
            Đăng nhập
          </button>

          <div class="relative my-6">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-surface text-text-secondary">Hoặc đăng nhập với</span>
            </div>
          </div>

          <button
            type="button"
            @click="handleGoogleLogin"
            class="w-full flex items-center justify-center space-x-2 border border-gray-300 bg-white text-text py-2.5 rounded-lg font-medium hover:bg-gray-50 transition-colors duration-200"
          >
            <img src="@/assets/google.svg" alt="Google" class="w-5 h-5" />
            <span>Google</span>
          </button>

          <div class="text-center mt-6">
            <p class="text-sm text-text-secondary">
              Chưa có tài khoản?
              <router-link to="/register" class="text-primary hover:text-primary/80 font-medium">
                Đăng ký ngay
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Component styles will be added here */
</style> 