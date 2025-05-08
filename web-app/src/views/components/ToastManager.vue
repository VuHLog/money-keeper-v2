<script setup>
import { ref, provide, onMounted, onUnmounted } from 'vue'
import NotificationToast from './NotificationToast.vue'

// Mảng chứa danh sách thông báo đang hiển thị
const toasts = ref([])

// Tạo hàm để thêm toast mới
const addToast = (notification, duration = 5000) => {
  const id = Date.now()
  toasts.value.push({
    id,
    notification,
    duration
  })
  
  return id
}

// Tạo hàm để xóa toast theo id
const removeToast = (id) => {
  const index = toasts.value.findIndex(toast => toast.id === id)
  if (index !== -1) {
    toasts.value.splice(index, 1)
  }
}

// Cung cấp hàm addToast cho các component khác sử dụng
provide('addToast', addToast)
provide('removeToast', removeToast)

// Xử lý các thông báo từ API thông qua giao tiếp WebSocket
// Thông báo này sẽ được tự động thêm vào danh sách toasts
const handleApiNotification = (notificationData) => {
  addToast(notificationData)
}

// Export các hàm này để có thể sử dụng từ bên ngoài component
defineExpose({
  addToast,
  removeToast,
  handleApiNotification
})
</script>

<template>
  <div>
    <transition-group name="notification">
      <NotificationToast
        v-for="(toast, index) in toasts"
        :key="toast.id"
        :notification="toast.notification"
        :duration="toast.duration"
        :index="index"
        @close="removeToast(toast.id)"
      />
    </transition-group>
  </div>
</template>

<style scoped>
.notification-move {
  transition: transform 0.3s ease;
}
</style> 