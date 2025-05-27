<script setup>
import { ref, getCurrentInstance, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@stores/AuthStore.js";
import TokenService from "@/service/TokenService.js";
import { OAuthConfig } from "@/config/OAuthConfig.js";
import logoFull from '@/assets/img/logo/logo.png';

const { proxy } = getCurrentInstance();

const store = useAuthStore();

const router = useRouter();
const route = useRoute();

const showPassword = ref(false);

const redirect = route.query.redirect ? route.query.redirect : "/home";

onMounted(async () => {
  if (store.username !== "") {
    router.push(redirect);
  }

  await signInWithGoogle();
});

const errMsg = ref("");
const user = ref({
  username: "",
  password: "",
});

async function signIn() {
  errMsg.value = "";
  let userVal = user.value;
  if (userVal.username === "" || userVal.password === "") {
    errMsg.value = "Phải nhập tài khoản và mật khẩu";
    return;
  }

  await proxy.$api
    .post("/auth/token", userVal)
    .then((res) => {
      TokenService.setSessionAccessToken(res.result.token);
      store.isLoggedIn = true;
      router.push(redirect);
    })
    .catch(() => (errMsg.value = "Tài khoản hoặc mật khẩu không chính xác"));
}

function handleContinueWithGoogle() {
  const targetUrl = `${OAuthConfig.authUri}?redirect_uri=${OAuthConfig.redirectUri}&response_type=code&client_id=${OAuthConfig.clientId}&scope=openid%20email%20profile`;

  window.location.href = targetUrl;
}

async function signInWithGoogle() {
  const authCodeRegex = /code=([^&]+)/;
  const isMatch = window.location.href.match(authCodeRegex);

  if (isMatch) {
    const authCode = isMatch[1];

    await proxy.$api
      .post("/auth/outbound/authentication?code=" + authCode, {})
      .then((res) => {
        TokenService.setSessionAccessToken(res.result.token);
        store.isLoggedIn = true;
        router.push(redirect);
      })
      .catch(() => errMsg.value = "Đăng nhập bằng Google không thành công");
  }
}

function handleSignUp() {
  router.push('/auth/sign-up');
}

function handleForgotPassword() {
  router.push('/auth/forgot-password');
}
</script>

<template>
  <!-- Container chính với nền gradient -->
  <div
    class="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-blue-400 via-blue-500 to-purple-600">
    <!-- Form đăng nhập -->
    <div class="w-full max-w-md bg-white rounded-lg shadow-xl overflow-hidden">
      <!-- Logo và Tiêu đề -->
      <div class="p-6 pb-2 flex flex-col items-center">
        <div class="mb-4 flex justify-center">
          <img src="@/assets/img/logo/logo.png" alt="Money Keeper Logo" class="h-16 w-auto" />
        </div>
        <div class="mt-2 mb-4">
          <span class="text-3xl font-semibold text-transparent bg-clip-text bg-gradient-to-r from-indigo-500 to-cyan-500">Sổ thu chi</span>
        </div>
      </div>

      <!-- Form nhập thông tin -->
      <div class="px-6 pb-6">
        <form method="POST" @submit.prevent="signIn()">
          <!-- Username field -->
          <div class="mb-6">
            <label class="block text-gray-600 text-sm mb-2">Tên đăng nhập</label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </span>
              <input v-model.trim="user.username"
                class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                type="text" placeholder="Tên đăng nhập" />
            </div>
          </div>

          <!-- Password field -->
          <div class="mb-4">
            <label class="block text-gray-600 text-sm mb-2">Mật khẩu</label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </span>
              <input v-model.trim="user.password"
                class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                :type="showPassword ? 'text' : 'password'" placeholder="Mật khẩu" />
              <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                @click="showPassword = !showPassword">
                <font-awesome-icon v-if="!showPassword" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
                <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
              </span>
            </div>
          </div>

          <!-- Forgot password link -->
          <div class="text-right mb-6">
            <a href="#" @click.prevent="handleForgotPassword" class="text-sm text-gray-500 hover:text-blue-500">Quên mật khẩu?</a>
          </div>

          <!-- Error message -->
          <div v-if="errMsg" class="mb-4 text-red-600 text-sm">
            {{ errMsg }}
          </div>

          <!-- Login button -->
          <button
            class="w-full py-3 rounded-md font-medium text-white bg-gradient-to-r from-cyan-400 to-purple-500 hover:opacity-90 transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
            type="submit">
            Đăng nhập
          </button>
        </form>

        <!-- Social login options -->
        <div class="mt-6">
          <div class="flex items-center justify-center space-x-2 mb-4">
            <span class="flex-grow h-px bg-gray-300"></span>
            <span class="text-sm text-gray-500">Hoặc</span>
            <span class="flex-grow h-px bg-gray-300"></span>
          </div>

          <div class="flex justify-center space-x-4">
            <button class="gsi-material-button" @click="handleContinueWithGoogle">
              <div class="gsi-material-button-state"></div>
              <div class="gsi-material-button-content-wrapper">
                <div class="gsi-material-button-icon">
                  <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48"
                    xmlns:xlink="http://www.w3.org/1999/xlink" style="display: block">
                    <path fill="#EA4335"
                      d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z">
                    </path>
                    <path fill="#4285F4"
                      d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z">
                    </path>
                    <path fill="#FBBC05"
                      d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z">
                    </path>
                    <path fill="#34A853"
                      d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z">
                    </path>
                    <path fill="none" d="M0 0h48v48H0z"></path>
                  </svg>
                </div>
                <span class="gsi-material-button-contents">Đăng nhập bằng Google</span>
              </div>
            </button>
          </div>
        </div>

        <!-- Sign up link -->
        <div class="mt-8 d-flex text-center">
          <p class="text-sm text-gray-500 mb-2">Hoặc đăng ký bằng cách</p>
          <button @click="handleSignUp" class="font-medium text-blue-600 hover:text-blue-800">
            Đăng ký
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
//css google
.gsi-material-button {
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
  -webkit-appearance: none;
  background-color: WHITE;
  background-image: none;
  border: 1px solid #747775;
  -webkit-border-radius: 4px;
  border-radius: 4px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  color: #1f1f1f;
  cursor: pointer;
  font-family: 'Roboto', arial, sans-serif;
  font-size: 14px;
  height: 40px;
  letter-spacing: 0.25px;
  outline: none;
  overflow: hidden;
  padding: 0 12px;
  position: relative;
  text-align: center;
  -webkit-transition: background-color .218s, border-color .218s, box-shadow .218s;
  transition: background-color .218s, border-color .218s, box-shadow .218s;
  vertical-align: middle;
  white-space: nowrap;
  width: auto;
  max-width: 400px;
  min-width: min-content;
}

.gsi-material-button .gsi-material-button-icon {
  height: 20px;
  margin-right: 12px;
  min-width: 20px;
  width: 20px;
}

.gsi-material-button .gsi-material-button-content-wrapper {
  -webkit-align-items: center;
  align-items: center;
  display: flex;
  -webkit-flex-direction: row;
  flex-direction: row;
  -webkit-flex-wrap: nowrap;
  flex-wrap: nowrap;
  height: 100%;
  justify-content: space-between;
  position: relative;
  width: 100%;
}

.gsi-material-button .gsi-material-button-contents {
  -webkit-flex-grow: 1;
  flex-grow: 1;
  font-family: 'Roboto', arial, sans-serif;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: top;
}

.gsi-material-button .gsi-material-button-state {
  -webkit-transition: opacity .218s;
  transition: opacity .218s;
  bottom: 0;
  left: 0;
  opacity: 0;
  position: absolute;
  right: 0;
  top: 0;
}

.gsi-material-button:disabled {
  cursor: default;
  background-color: #ffffff61;
  border-color: #1f1f1f1f;
}

.gsi-material-button:disabled .gsi-material-button-contents {
  opacity: 38%;
}

.gsi-material-button:disabled .gsi-material-button-icon {
  opacity: 38%;
}

.gsi-material-button:not(:disabled):active .gsi-material-button-state,
.gsi-material-button:not(:disabled):focus .gsi-material-button-state {
  background-color: #303030;
  opacity: 12%;
}

.gsi-material-button:not(:disabled):hover {
  -webkit-box-shadow: 0 1px 2px 0 rgba(60, 64, 67, .30), 0 1px 3px 1px rgba(60, 64, 67, .15);
  box-shadow: 0 1px 2px 0 rgba(60, 64, 67, .30), 0 1px 3px 1px rgba(60, 64, 67, .15);
}

.gsi-material-button:not(:disabled):hover .gsi-material-button-state {
  background-color: #303030;
  opacity: 8%;
}
</style>
