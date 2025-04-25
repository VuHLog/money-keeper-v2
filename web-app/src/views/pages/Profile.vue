<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// Mock user data - sẽ được thay thế bằng API call
const user = reactive({
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  email: 'user@example.com',
  fullName: 'Nguyễn Văn A'
})

// Form data và validation cho thông tin cá nhân
const profileForm = reactive({
  fullName: user.fullName,
  email: user.email
})

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

// Validation rules
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const validatePassword = (password) => {
  // Ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
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
    passwordErrors.newPassword = 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt'
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
  profileForm.fullName = user.fullName
  profileForm.email = user.email
  profileErrors.fullName = ''
  profileErrors.email = ''
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
  const file = event.target.files[0]
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      ElMessage({
        type: 'error',
        message: 'Vui lòng chọn file hình ảnh'
      })
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      ElMessage({
        type: 'error',
        message: 'Kích thước file không được vượt quá 5MB'
      })
      return
    }

    // TODO: Upload file to server
    const reader = new FileReader()
    reader.onload = (e) => {
      user.avatar = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

// Xử lý cập nhật thông tin
const handleUpdateProfile = () => {
  if (!validateProfileForm()) {
    return
  }

  // TODO: Call API to update profile
  user.fullName = profileForm.fullName
  user.email = profileForm.email
  isEditing.value = false
  
  ElMessage({
    type: 'success',
    message: 'Cập nhật thông tin thành công!'
  })
}

// Xử lý đổi mật khẩu
const handleChangePassword = () => {
  if (!validatePasswordForm()) {
    return
  }
  
  // TODO: Call API to change password
  isChangingPassword.value = false
  resetPasswordForm()
  
  ElMessage({
    type: 'success',
    message: 'Đổi mật khẩu thành công!'
  })
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
          <img 
            :src="user.avatar" 
            class="w-32 h-32 rounded-full object-cover border-4 border-white shadow-md"
            alt="Avatar"
          />
          <label class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-full opacity-0 group-hover:opacity-100 cursor-pointer transition-opacity">
            <input 
              type="file" 
              accept="image/*" 
              class="hidden" 
              @change="handleAvatarUpload"
            />
            <font-awesome-icon 
              :icon="['fas', 'camera']" 
              class="text-white text-xl"
            />
          </label>
        </div>

        <!-- Thông tin -->
        <div class="flex-1">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h2 class="text-xl font-semibold text-text">{{ user.fullName }}</h2>
              <p class="text-text-secondary">{{ user.email }}</p>
            </div>
            <button 
              v-if="!isEditing"
              @click="isEditing = true"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
            >
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
              <input 
                v-model="profileForm.fullName"
                type="text"
                class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
                :class="{ 'border-red-500': profileErrors.fullName }"
              />
              <p v-if="profileErrors.fullName" class="mt-1 text-sm text-red-500">
                {{ profileErrors.fullName }}
              </p>
            </div>
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Email
              </label>
              <input 
                v-model="profileForm.email"
                type="email"
                class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
                :class="{ 'border-red-500': profileErrors.email }"
              />
              <p v-if="profileErrors.email" class="mt-1 text-sm text-red-500">
                {{ profileErrors.email }}
              </p>
            </div>
            <div class="flex justify-end space-x-3">
              <button 
                @click="handleCancelEdit"
                class="px-4 py-2 text-text-secondary hover:text-text"
              >
                Hủy
              </button>
              <button 
                @click="handleUpdateProfile"
                class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
              >
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
        <button 
          v-if="!isChangingPassword"
          @click="isChangingPassword = true"
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
        >
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
          <input 
            v-model="passwordForm.currentPassword"
            type="password"
            class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
            :class="{ 'border-red-500': passwordErrors.currentPassword }"
          />
          <p v-if="passwordErrors.currentPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.currentPassword }}
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-1">
            Mật khẩu mới
          </label>
          <input 
            v-model="passwordForm.newPassword"
            type="password"
            class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
            :class="{ 'border-red-500': passwordErrors.newPassword }"
          />
          <p v-if="passwordErrors.newPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.newPassword }}
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-1">
            Xác nhận mật khẩu mới
          </label>
          <input 
            v-model="passwordForm.confirmPassword"
            type="password"
            class="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20"
            :class="{ 'border-red-500': passwordErrors.confirmPassword }"
          />
          <p v-if="passwordErrors.confirmPassword" class="mt-1 text-sm text-red-500">
            {{ passwordErrors.confirmPassword }}
          </p>
        </div>
        <div class="flex justify-end space-x-3">
          <button 
            @click="handleCancelPasswordChange"
            class="px-4 py-2 text-text-secondary hover:text-text"
          >
            Hủy
          </button>
          <button 
            @click="handleChangePassword"
            class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
          >
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