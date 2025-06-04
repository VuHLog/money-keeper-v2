<script setup>
import { ref, getCurrentInstance, onUnmounted, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import ToastManager from '@/views/components/ToastManager.vue';
import { useAuthStore } from "@/store/AuthStore.js";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
const toastManagerRef = ref(null);
const store = useAuthStore();


const redirect = route.query.redirect ? route.query.redirect : "/home";
onMounted(() => {
  if (store.username !== "" && store.isLoggedIn) {
    router.push(redirect);
  }
});

// Function to add toast using ref to ToastManager component
const addToast = (notification, duration) => {
  if (toastManagerRef.value) {
    toastManagerRef.value.addToast(notification, duration)
  }
};

const currentStep = ref(1);
const errMsg = ref("");
const successMsg = ref("");

// Step 1 data
const email = ref("");

// Step 2 data  
const otp = ref("");
const otpInputs = ref([]);
const countdown = ref(60);
const countdownInterval = ref(null);

// Step 3 data
const newPassword = ref("");
const confirmPassword = ref("");
const showPassword = ref(false);
const showPasswordConfirm = ref(false);

// Step 1: Send email for reset password
async function sendResetEmail() {
    errMsg.value = "";
    successMsg.value = "";

    if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email.value.trim())) {
        errMsg.value = "Email không hợp lệ";
        return;
    }

         await proxy.$api.post("forgot-password/verify-mail/" + email.value).then((res) => {
         currentStep.value = 2;
         startCountdown();
    }).catch((error) => {
        if(error.response.data.code === 1010){
            errMsg.value = "Mail chưa được sử dụng";
        }else if(error.response.data.code === 12003){
            errMsg.value = "Gửi mã OTP tới mail thất bại";
        }
        else {
            errMsg.value = error.response?.data?.message || "Có lỗi xảy ra, vui lòng thử lại";
        }
    });
}

// Step 2: Verify OTP
async function verifyOTP() {
  errMsg.value = "";
  successMsg.value = "";
  
  if (otp.value.length !== 6 || !/^\d+$/.test(otp.value)) {
    errMsg.value = "Mã OTP phải gồm 6 chữ số";
    return;
  }
  await proxy.$api.post(`forgot-password/verify-otp/${otp.value}/${email.value}`).then((res) => {
      successMsg.value = "Xác thực thành công";
      currentStep.value = 3;
    }).catch((error) => {
        if(error.response.data.code === 1010){
            errMsg.value = "Mail chưa được sử dụng";
        }else if(error.response.data.code === 12001){
            errMsg.value = "Mã OTP không chính xác";
        }else if(error.response.data.code === 12002){
            errMsg.value = "Mã OTP đã hết hạn";
        }
        else {
            errMsg.value = "Có lỗi xảy ra, vui lòng thử lại";
        }
    });
}

// Step 3: Reset password
async function resetPassword() {
  errMsg.value = "";
  
  if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{8,}$/.test(newPassword.value.trim())) {
    errMsg.value = "Mật khẩu phải có ít nhất 8 ký tự chứa ít nhất một ký tự viết hoa, viết thường và chữ số";
    return;
  }
  
  if (confirmPassword.value !== newPassword.value) {
    errMsg.value = "Mật khẩu không khớp";
    return;
  }
  await proxy.$api.post("/forgot-password/change-password/" + email.value, {
      newPassword: newPassword.value,
      currPassword: confirmPassword.value
    }).then((res) => {
          addToast({
            type: 'success',
            title: 'Thành công!',
            content: 'Đặt lại mật khẩu thành công!'
            }, 3000);
            setTimeout(() => {
                router.push("/auth/sign-in");
            }, 3000);
    }).catch((error) => {
      if(error.response.data.code === 1010){
        errMsg.value = "Mail chưa được sử dụng";
      }
      else {
        errMsg.value = "Có lỗi xảy ra, vui lòng thử lại";
      }
    });
}

// Handle OTP input
function handleOTPInput(event, index) {
  const value = event.target.value;
  if (value && index < 5) {
    otpInputs.value[index + 1].focus();
  }
  updateOTP();
}

function handleOTPKeydown(event, index) {
  if (event.key === 'Backspace' && !event.target.value && index > 0) {
    otpInputs.value[index - 1].focus();
  }
}

function updateOTP() {
  otp.value = otpInputs.value.map(input => input.value).join('');
}

function goBack() {
  if (currentStep.value > 1) {
    currentStep.value--;
    errMsg.value = "";
    successMsg.value = "";
    
    // Stop countdown when going back from step 2
    if (currentStep.value === 1 && countdownInterval.value) {
      clearInterval(countdownInterval.value);
      countdownInterval.value = null;
    }
  }
  if(currentStep.value === 1){
    email.value = "";
  }
}

function goToLogin() {
  router.push('/auth/sign-in');
}

// Countdown functions
function startCountdown() {
  countdown.value = 60;
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value);
  }
  
  countdownInterval.value = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(countdownInterval.value);
      countdownInterval.value = null;
    }
  }, 1000);
}

function resendOTP() {
  if (countdown.value <= 0) {
    // Reset OTP input fields
    otp.value = "";
    if (otpInputs.value) {
      otpInputs.value.forEach(input => {
        if (input) input.value = "";
      });
    }
    errMsg.value = "";
    successMsg.value = "";
    
    sendResetEmail();
  }
}

// Cleanup interval on component unmount
onUnmounted(() => {
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value);
  }
});
</script>

    
    <!-- Container chính với nền gradient -->
<template>
    <div class="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-blue-400 via-blue-500 to-purple-600">
        <!-- Toast Manager Component -->
        <ToastManager ref="toastManagerRef" />
        <!-- Form quên mật khẩu -->
        <div class="w-full max-w-md bg-white rounded-lg shadow-xl overflow-hidden">
        <!-- Tiêu đề -->
        <div class="p-6 pb-2">
            <h1 class="text-3xl font-bold text-center text-gray-800">Quên mật khẩu</h1>
            <div class="flex justify-center mt-4">
            <div class="flex space-x-2">
                <div v-for="step in 3" :key="step" 
                    :class="['w-3 h-3 rounded-full', currentStep >= step ? 'bg-blue-500' : 'bg-gray-300']">
                </div>
            </div>
            </div>
        </div>

      <!-- Form content -->
      <div class="p-6">
        <!-- Step 1: Enter Email -->
        <div v-if="currentStep === 1">
          <h2 class="text-xl font-semibold text-center text-gray-700 mb-6">Nhập địa chỉ email</h2>
          <form @submit.prevent="sendResetEmail()">
            <div class="mb-6">
              <label class="block text-gray-600 text-sm mb-2">Email</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </span>
                <input v-model.trim="email"
                  class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                  type="email" placeholder="Nhập địa chỉ email" />
              </div>
            </div>

            <!-- Success message -->
            <div v-if="successMsg" class="mb-4 text-green-600 text-sm">
              {{ successMsg }}
            </div>

            <!-- Error message -->
            <div v-if="errMsg" class="mb-4 text-red-600 text-sm">
              {{ errMsg }}
            </div>

            <button type="submit" 
              class="w-full py-3 rounded-md font-medium text-white bg-gradient-to-r from-cyan-400 to-purple-500 hover:opacity-90 transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400">
              Gửi mã OTP
            </button>
          </form>
        </div>

        <!-- Step 2: Enter OTP -->
        <div v-if="currentStep === 2">
          <h2 class="text-xl font-semibold text-center text-gray-700 mb-2">Nhập mã OTP</h2>
          <p class="text-sm text-gray-500 text-center mb-2">Mã OTP đã được gửi đến {{ email }}</p>
          
          <!-- Countdown timer -->
          <div class="text-center mb-6">
            <p v-if="countdown > 0" class="text-sm text-blue-600">
              Mã có hiệu lực trong <span class="font-semibold">{{ countdown }}s</span>
            </p>
            <button v-else @click="resendOTP" 
              class="text-sm text-blue-600 hover:text-blue-800 font-medium underline">
              Gửi lại mã OTP
            </button>
          </div>
          
          <form @submit.prevent="verifyOTP()">
            <div class="mb-6">
              <div class="flex justify-center space-x-2">
                <input v-for="(digit, index) in 6" :key="index"
                  :ref="el => otpInputs[index] = el"
                  @input="handleOTPInput($event, index)"
                  @keydown="handleOTPKeydown($event, index)"
                  class="w-12 h-12 border border-gray-300 rounded-md text-center text-lg font-semibold focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                  type="text" maxlength="1" />
              </div>
            </div>

            <!-- Success message -->
            <div v-if="successMsg" class="mb-4 text-green-600 text-sm text-center">
              {{ successMsg }}
            </div>

            <!-- Error message -->
            <div v-if="errMsg" class="mb-4 text-red-600 text-sm text-center">
              {{ errMsg }}
            </div>

            <button type="submit"
              class="w-full py-3 rounded-md font-medium text-white bg-gradient-to-r from-cyan-400 to-purple-500 hover:opacity-90 transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400">
              Xác thực
            </button>
          </form>
        </div>

        <!-- Step 3: Reset Password -->
        <div v-if="currentStep === 3">
          <h2 class="text-xl font-semibold text-center text-gray-700 mb-6">Đặt lại mật khẩu</h2>
          
          <form @submit.prevent="resetPassword()">
            <!-- New Password field -->
            <div class="mb-6">
              <label class="block text-gray-600 text-sm mb-2">Mật khẩu mới</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </span>
                <input v-model.trim="newPassword"
                  class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                  :type="showPassword ? 'text' : 'password'" placeholder="Mật khẩu mới" />
                <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                  @click="showPassword = !showPassword">
                  <font-awesome-icon v-if="!showPassword" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
                  <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
                </span>
              </div>
            </div>

            <!-- Confirm Password field -->
            <div class="mb-6">
              <label class="block text-gray-600 text-sm mb-2">Xác nhận mật khẩu</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </span>
                <input v-model.trim="confirmPassword"
                  class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                  :type="showPasswordConfirm ? 'text' : 'password'" placeholder="Xác nhận mật khẩu" />
                <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                  @click="showPasswordConfirm = !showPasswordConfirm">
                  <font-awesome-icon v-if="!showPasswordConfirm" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
                  <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
                </span>
              </div>
            </div>

            <!-- Error message -->
            <div v-if="errMsg" class="mb-4 text-red-600 text-sm">
              {{ errMsg }}
            </div>

            <button type="submit"
              class="w-full py-3 rounded-md font-medium text-white bg-gradient-to-r from-cyan-400 to-purple-500 hover:opacity-90 transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400">
              Đặt lại mật khẩu
            </button>
          </form>
        </div>

        <!-- Back button (except for step 1) -->
        <div v-if="currentStep > 1" class="mt-4">
          <button @click="goBack" 
            class="w-full py-2 rounded-md font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 transition-all">
            Quay lại
          </button>
        </div>

        <!-- Login link -->
        <div class="mt-6 text-center">
          <p class="text-sm text-gray-500 mb-2">Nhớ mật khẩu?</p>
          <button @click="goToLogin" class="font-medium text-blue-600 hover:text-blue-800">
            Đăng nhập
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
