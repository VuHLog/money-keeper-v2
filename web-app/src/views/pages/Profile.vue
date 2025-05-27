<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { useAuthStore } from "@stores/AuthStore.js";
import Swal from 'sweetalert2';

const authStore = useAuthStore()
const { proxy } = getCurrentInstance()
const user = ref({})
const previewImage = ref("");
const file = ref(null);

// Biến kiểm soát hiển thị password
const showCurrentPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

// Form data và validation cho thông tin cá nhân
const profileForm = reactive({})

const profileErrors = reactive({
  fullName: '',
  email: ''
})

// Form data và validation cho đổi mật khẩu
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordErrors = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// State cho việc edit
const isEditing = ref(false)
const isChangingPassword = ref(false)

onMounted(async () => {
  user.value = await authStore.getMyInfo()
  profileForm.fullName = user.value.fullName
  profileForm.email = user.value.email
  profileForm.avatarUrl = user.value.avatarUrl
})

// Validation rules
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const validatePassword = (password) => {
  // Ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
  const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{8,}$/
  return passwordRegex.test(password)
}

// Validate profile form
const validateProfileForm = () => {
  let isValid = true

  // Validate fullName
  if (!profileForm.fullName.trim()) {
    profileErrors.fullName = 'Họ và tên không được để trống'
    isValid = false
  } else if (profileForm.fullName.trim().length < 2) {
    profileErrors.fullName = 'Họ và tên phải có ít nhất 2 ký tự'
    isValid = false
  } else {
    profileErrors.fullName = ''
  }

  // Validate email
  if (!profileForm.email.trim()) {
    profileErrors.email = 'Email không được để trống'
    isValid = false
  } else if (!validateEmail(profileForm.email)) {
    profileErrors.email = 'Email không hợp lệ'
    isValid = false
  } else {
    profileErrors.email = ''
  }

  return isValid
}

// Validate password form
const validatePasswordForm = () => {
  let isValid = true

  // Validate current password
  if (!passwordForm.currentPassword) {
    passwordErrors.currentPassword = 'Vui lòng nhập mật khẩu hiện tại'
    isValid = false
  } else {
    passwordErrors.currentPassword = ''
  }

  // Validate new password
  if (!passwordForm.newPassword) {
    passwordErrors.newPassword = 'Vui lòng nhập mật khẩu mới'
    isValid = false
  } else if (!validatePassword(passwordForm.newPassword)) {
    passwordErrors.newPassword = 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất một ký tự viết hoa, viết thường và chữ số'
    isValid = false
  } else {
    passwordErrors.newPassword = ''
  }

  // Validate confirm password
  if (!passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu mới'
    isValid = false
  } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = 'Mật khẩu xác nhận không khớp'
    isValid = false
  } else {
    passwordErrors.confirmPassword = ''
  }

  return isValid
}

// Reset forms
const resetProfileForm = () => {
  profileForm.fullName = user.value.fullName
  profileForm.email = user.value.email
  profileErrors.fullName = ''
  profileErrors.email = ''
  previewImage.value = user.value.avatarUrl
}

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordErrors.currentPassword = ''
  passwordErrors.newPassword = ''
  passwordErrors.confirmPassword = ''
}

// Xử lý upload avatar
const handleAvatarUpload = (event) => {
  isEditing.value = true
  const selectedFile = event.target.files[0]
  if (selectedFile) {
    // Validate file type
    if (!selectedFile.type.startsWith('image/')) {
      ElMessage({
        type: 'error',
        message: 'Vui lòng chọn file hình ảnh'
      })
      return
    }

    // Validate file size (max 5MB)
    if (selectedFile.size > 5 * 1024 * 1024) {
      ElMessage({
        type: 'error',
        message: 'Kích thước file không được vượt quá 5MB'
      })
      return
    }

    file.value = selectedFile;
    previewImage.value = URL.createObjectURL(selectedFile);
  }
}

async function submitFile() {
  let formData = new FormData();

  formData.append("image", file.value);
  await proxy.$api
    .postFile("/cloudinary/upload/image", formData)
    .then((res) => {
      profileForm.avatarUrl = res.url;
      user.value.avatarUrl = res.url;
      authStore.avatarUrl = res.url;
      console.log(res.url);
    })
    .catch((error) => console.log(error));
}

// Xử lý cập nhật thông tin
const handleUpdateProfile = async () => {
  if (!validateProfileForm()) {
    return
  }

  if (file.value !== null) {
    await submitFile();
  }

  await proxy.$api
    .put("/users/" + user.value.id, profileForm)
    .then((res) => {
      authStore.fullName = user.value.fullName;
      authStore.avatarUrl = user.value.avatarUrl;
      user.value = res.result
      user.fullName = profileForm.fullName
      user.email = profileForm.email
      isEditing.value = false
      Swal.fire({
        title: "Thành công",
        text: "Bạn đã thay đổi thông tin tài khoản thành công!",
        icon: "success",
      });
    })
    .catch((error) => {
      if(error.response.data.code === 1009){
        profileErrors.email = "Email đã tồn tại";
      } else {
        console.log(error);
      }
    });
}

// Xử lý đổi mật khẩu
const handleChangePassword = async () => {
  if (!validatePasswordForm()) {
    return
  }
  await proxy.$api.patch("/users/change-password", {
    currPassword: passwordForm.currentPassword,
    newPassword: passwordForm.newPassword,
  }).then(() => {
    isChangingPassword.value = false
    resetPasswordForm()
    Swal.fire({
      title: "Thành công",
      text: "Bạn đã thay đổi mật khẩu thành công!",
      icon: "success",
    });
  }).catch((error) => {
    console.log(error)
  });
}

// Handle cancel actions
const handleCancelEdit = () => {
  isEditing.value = false
  resetProfileForm()
}

const handleCancelPasswordChange = () => {
  isChangingPassword.value = false
  resetPasswordForm()
}
</script>

<template>
  <div class="p-4">
    <h1 class="text-2xl font-semibold text-text mb-6">Thông Tin Người Dùng</h1>

    <!-- Thông tin cá nhân -->
    <div class="bg-surface rounded-xl p-6 shadow-sm mb-6">
      <div class="flex items-start space-x-6">
        <!-- Avatar -->
        <div class="relative group">
          <img :src="previewImage || user.avatarUrl"
            class="w-32 h-32 rounded-full object-cover border-4 border-white shadow-md" alt="Avatar" />
          <label
            class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-full opacity-0 group-hover:opacity-100 cursor-pointer transition-opacity">
            <input type="file" accept="image/*" class="hidden" @change="handleAvatarUpload" />
            <font-awesome-icon :icon="['fas', 'camera']" class="text-white text-xl" />
          </label>
        </div>

        <!-- Thông tin -->
        <div class="flex-1">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h2 class="text-xl font-semibold text-text">{{ user.fullName }}</h2>
              <p class="text-text-secondary">{{ user.email }}</p>
            </div>
            <button v-if="!isEditing" @click="isEditing = true"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
              <font-awesome-icon :icon="['fas', 'edit']" class="mr-2" />
              Chỉnh sửa
            </button>
          </div>

          <!-- Form chỉnh sửa -->
          <div v-if="isEditing" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Họ và tên
              </label>
              <input v-model="profileForm.fullName" type="text"
                class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
                :class="{ 'border-red-500': profileErrors.fullName }" />
              <p v-if="profileErrors.fullName" class="mt-1 text-sm text-red-500">
                {{ profileErrors.fullName }}
              </p>
            </div>
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Email
              </label>
              <input v-model="profileForm.email" type="email"
                class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
                :class="{ 'border-red-500': profileErrors.email }" />
              <p v-if="profileErrors.email" class="mt-1 text-sm text-red-500">
                {{ profileErrors.email }}
              </p>
            </div>
            <div class="flex justify-end space-x-3">
              <button @click="handleCancelEdit" class="px-4 py-2 text-text-secondary hover:text-text">
                Hủy
              </button>
              <button @click="handleUpdateProfile"
                class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
                Lưu thay đổi
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Đổi mật khẩu -->
    <div class="bg-surface rounded-xl p-6 shadow-sm">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold text-text">Đổi mật khẩu</h2>
        <button v-if="!isChangingPassword" @click="isChangingPassword = true"
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
          <font-awesome-icon :icon="['fas', 'key']" class="mr-2" />
          Đổi mật khẩu
        </button>
      </div>

      <!-- Form đổi mật khẩu -->
      <div v-if="isChangingPassword" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-1">
            Mật khẩu hiện tại
          </label>
          <div class="relative">
            <input v-model.trim="passwordForm.currentPassword"
              class="w-full border border-gray-300 rounded-md px-4 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
              :type="showCurrentPassword ? 'text' : 'password'" placeholder="Mật khẩu" />
            <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
              @click="showCurrentPassword = !showCurrentPassword">
              <font-awesome-icon v-if="!showCurrentPassword" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
              <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
            </span>
          </div>
          <p v-if="passwordErrors.currentPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.currentPassword }}
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-1">
            Mật khẩu mới
          </label>
          <div class="relative">
            <input v-model.trim="passwordForm.newPassword"
              class="w-full border border-gray-300 rounded-md px-4 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
              :type="showNewPassword ? 'text' : 'password'" placeholder="Mật khẩu" />
            <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
              @click="showNewPassword = !showNewPassword">
              <font-awesome-icon v-if="!showNewPassword" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
              <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
            </span>
          </div>
          <p v-if="passwordErrors.newPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.newPassword }}
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-1">
            Xác nhận mật khẩu mới
          </label>
          <div class="relative">
            <input v-model.trim="passwordForm.confirmPassword"
              class="w-full border border-gray-300 rounded-md px-4 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
              :type="showConfirmPassword ? 'text' : 'password'" placeholder="Mật khẩu" />
            <span class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
              @click="showConfirmPassword = !showConfirmPassword">
              <font-awesome-icon v-if="!showConfirmPassword" :icon="['fas', 'eye']" class="h-5 w-5 text-gray-400" />
              <font-awesome-icon v-else :icon="['fas', 'eye-slash']" class="h-5 w-5 text-gray-400" />
            </span>
          </div>
          <p v-if="passwordErrors.confirmPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.confirmPassword }}
          </p>
        </div>
        <div class="flex justify-end space-x-3">
          <button @click="handleCancelPasswordChange" class="px-4 py-2 text-text-secondary hover:text-text">
            Hủy
          </button>
          <button @click="handleChangePassword" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.transition-opacity {
  transition: opacity 0.2s ease-in-out;
}
</style>