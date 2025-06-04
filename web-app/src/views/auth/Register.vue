<script setup>
import { ref, getCurrentInstance, onMounted } from "vue";
import { useAuthStore } from "@stores/AuthStore.js";
import { useRouter, useRoute } from "vue-router";
import swal from 'sweetalert2'

const { proxy } = getCurrentInstance();

const store = useAuthStore();

const router = useRouter();
const route = useRoute();
const previewImage = ref(store.avatarUserDefault);
const errMsg = ref("");

const showPassword = ref(false)
const showPasswordConfirm = ref(false);

const user = ref({
  username: "",
  password: "",
  email: "",
  fullName: "",
  avatarUrl: "",
  roles: [],
});

const redirect = route.query.redirect ? route.query.redirect : "/home";
onMounted(() => {
  if (store.username !== "" && store.isLoggedIn) {
    router.push(redirect);
  }
});

// xử lý ảnh
const file = ref(null);
async function handleFileUpload(event) {
  file.value = event.target.files[0];
  if (file.value) {
    // Tạo URL tạm thời cho ảnh vừa chọn
    previewImage.value = URL.createObjectURL(file.value);
  }
}

async function submitFile() {
  let formData = new FormData();

  formData.append("image", file.value);
  await proxy.$api
    .postFile("/cloudinary/upload/image", formData)
    .then((res) => {
      user.value.avatarUrl = res.url;
      console.log(res.url);
    })
    .catch((error) => console.log(error));
}

function isValidUserInfo() {
  errMsg.value = "";
  if (!/^.{8,}$/.test(user.value.fullName.trim())) {
    errMsg.value = "Họ tên phải có ít nhất 8 ký tự";
    return false;
  }
  if (
    !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(
      user.value.email.trim()
    )
  ) {
    errMsg.value = "Email không hợp lệ";
    return false;
  }
  if (!/^.{4,}$/.test(user.value.username.trim())) {
    errMsg.value = "Tên đăng nhập phải có ít nhất 4 ký tự";
    return false;
  }
  if (
    !/^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{8,}$/.test(user.value.password.trim())
  ) {
    errMsg.value =
      "Mật khẩu phải có ít nhất 8 ký tự chứa ít nhất một ký tự viết hoa, viết thường và chữ số";
    return false;
  }
  if (passwordConfirm.value !== user.value.password) {
    errMsg.value = "Mật khẩu không khớp";
    return false;
  }
  return true;
}

const passwordConfirm = ref("");
async function signUp() {
  if (!isValidUserInfo()) {
    return;
  }
  if (file.value !== null) {
    await submitFile();
  }

  await proxy.$api
    .post("/users/registration", user.value)
    .then((res) => {
      if (res.message) {
        errMsg.value = res.message;
      } else {
        swal.fire({
          title: "Đăng ký Thành Công!",
          icon: "success",
        });
        router.push("/auth/sign-in");
      }
    })
    .catch((error) => {
      errMsg.value = error.response.data.message;
      console.log(error);
    });
}
</script>

<template>
  <!-- Container chính với nền gradient -->
  <div
    class="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-blue-400 via-blue-500 to-purple-600">
    <!-- Form đăng ký -->
    <div class="w-full max-w-2xl bg-white rounded-lg shadow-xl overflow-hidden">
      <!-- Tiêu đề -->
      <div class="p-6 pb-0">
        <h1 class="text-3xl font-bold text-center text-gray-800">Đăng ký tài khoản</h1>
      </div>

      <!-- Form nhập thông tin -->
      <div class="p-6">
        <form method="POST" @submit.prevent="signUp()">
          <!-- Chia form thành 2 cột -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Cột trái -->
            <div class="space-y-6">
              <!-- Họ và tên field -->
              <div>
                <label class="block text-gray-600 text-sm mb-2">Họ và tên</label>
                <div class="relative">
                  <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </span>
                  <input v-model.trim="user.fullName"
                    class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                    type="text" placeholder="Họ và tên" />
                </div>
              </div>

              <!-- Password field -->
              <div>
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

              <!-- Email field -->
              <div>
                <label class="block text-gray-600 text-sm mb-2">Email</label>
                <div class="relative">
                  <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </span>
                  <input v-model.trim="user.email"
                    class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                    type="text" placeholder="Email" />
                </div>
              </div>
            </div>
            
            <!-- Cột phải -->
            <div class="space-y-6">
              
              <!-- Username field -->
              <div>
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

              <!-- Password Confirm field -->
              <div>
                <label class="block text-gray-600 text-sm mb-2">Xác nhận mật khẩu</label>
                <div class="relative">
                  <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </span>
                  <input v-model.trim="passwordConfirm"
                    class="w-full border border-gray-300 rounded-md py-2 px-10 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                    :type="showPasswordConfirm ? 'text' : 'password'" placeholder="Xác nhận mật khẩu" />
                  <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                    @click="showPasswordConfirm = !showPasswordConfirm">
                    <font-awesome-icon v-if="!showPasswordConfirm" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
                    <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
                  </span>
                </div>
              </div>

              <!-- Ảnh đại diện -->
              <div>
                <label class="block text-gray-600 text-sm mb-2">Ảnh đại diện</label>
                <div class="relative">
                  <span class="absolute inset-y-0 left-0 flex items-center pl-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24"
                      stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </span>
                  <input type="file" accept="image/*" @change="handleFileUpload" 
                    class="hidden" id="avatarInput" />
                  <label for="avatarInput" 
                    class="block w-full border border-gray-300 rounded-md py-2 pl-10 pr-3 text-gray-700 cursor-pointer bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent">
                    {{ file ? file.name : "Chọn ảnh đại diện" }}
                  </label>
                  <div class="absolute right-2 top-1/2 transform -translate-y-1/2 h-8 w-8 rounded-full overflow-hidden bg-gray-200">
                    <img :src="previewImage || user.avatarUrl" alt="Avatar preview" class="h-full w-full object-cover" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Error message -->
          <div v-if="errMsg" class="mt-6 text-red-600 text-sm">
            {{ errMsg }}
          </div>

          <!-- Register button -->
          <button
            class="w-full mt-6 py-3 rounded-md font-medium text-white bg-gradient-to-r from-cyan-400 to-purple-500 hover:opacity-90 transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-400"
            type="submit">
            Đăng ký
          </button>
        </form>

        <!-- Login link -->
        <div class="mt-8 text-center">
          <p class="text-sm text-gray-500 mb-2">Đã có tài khoản?</p>
          <router-link to="/auth/sign-in" class="font-medium text-blue-600 hover:text-blue-800">
            Đăng nhập
          </router-link>
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