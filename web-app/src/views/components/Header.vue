<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { jwtDecode } from "jwt-decode";
import { useAuthStore } from '@stores/AuthStore.js'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

const router = useRouter()
const store = useAuthStore();
const route = useRoute()
const isUserMenuOpen = ref(false)
const userAvatar = computed(() => store.avatarUrl || store.avatarUrlDefault)
const isNotificationsOpen = ref(false)
const selectedNotification = ref(null)
const isDetailModalOpen = ref(false)

// Mock notifications data
const notifications = ref([
  {
    id: 1,
    title: 'Nhắc nhở thanh toán',
    description: 'Đến hạn thanh toán tiền điện tháng 3/2024',
    time: new Date(2024, 2, 15, 10, 30),
    read: false,
    type: 'reminder',
    content: 'Kính gửi người dùng,\n\nHóa đơn tiền điện tháng 3/2024 của bạn đã đến hạn thanh toán. Vui lòng thanh toán trước ngày 20/3/2024 để tránh bị phạt trễ hạn.\n\nSố tiền cần thanh toán: 850,000đ\nHạn thanh toán: 20/3/2024\n\nXin cảm ơn!'
  },
  {
    id: 2,
    title: 'Vượt hạn mức chi tiêu',
    description: 'Chi tiêu tháng này đã vượt quá hạn mức đề ra',
    time: new Date(2024, 2, 14, 15, 45),
    read: false,
    type: 'warning',
    content: 'Thông báo vượt hạn mức chi tiêu!\n\nChi tiêu tháng này của bạn đã vượt quá hạn mức đề ra:\nHạn mức: 5,000,000đ\nĐã chi tiêu: 5,500,000đ\nVượt quá: 500,000đ\n\nBạn nên xem xét lại các khoản chi tiêu để đảm bảo mục tiêu tài chính của mình.'
  },
  {
    id: 3,
    title: 'Giao dịch thành công',
    description: 'Đã nhận được khoản lương tháng 3/2024',
    time: new Date(2024, 2, 13, 8, 0),
    read: true,
    type: 'success',
    content: 'Xác nhận giao dịch thành công!\n\nTài khoản của bạn vừa nhận được khoản tiền:\nSố tiền: 15,000,000đ\nNội dung: Lương tháng 3/2024\nThời gian: 13/3/2024 08:00\n\nKiểm tra và phân bổ ngân sách của bạn ngay!'
  }
])

const currentPageTitle = computed(() => {
  console.log('Current route:', route.name) // Debug log
  const routePath = route.path
  switch (routePath) {
    case '/dashboard':
      return 'Dashboard'
    case '/accounts':
      return 'Tài khoản'
    case '/income':
      return 'Ghi Thu'
    case '/expense':
      return 'Ghi Chi'
    case '/spending-limits':
      return 'Hạn Mức Chi'
    case '/categories':
      return 'Danh Mục'
    case '/statistics':
      return 'Thống Kê'
    case '/profile':
      return 'Thông Tin Người Dùng'
    default:
      return 'Money Keeper'
  }
})

const emit = defineEmits(['toggle-sidebar'])

const toggleSidebar = () => {
  emit('toggle-sidebar')
}

const toggleUserMenu = (event) => {
  event.stopPropagation()
  isUserMenuOpen.value = !isUserMenuOpen.value
}

const closeUserMenu = () => {
  isUserMenuOpen.value = false
}

async function logOut() {
  await store.logOut()
  router.push("/auth/sign-in");
}

const toggleNotifications = (event) => {
  event.stopPropagation()
  isNotificationsOpen.value = !isNotificationsOpen.value
}

const closeNotifications = () => {
  isNotificationsOpen.value = false
}

const showNotificationDetail = (notification) => {
  selectedNotification.value = notification
  isDetailModalOpen.value = true
  // Đánh dấu là đã đọc
  if (!notification.read) {
    notification.read = true
  }
}

const closeDetailModal = () => {
  isDetailModalOpen.value = false
  selectedNotification.value = null
}

const getTimeAgo = (date) => {
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days} ngày trước`
  } else if (hours > 0) {
    return `${hours} giờ trước`
  } else if (minutes > 0) {
    return `${minutes} phút trước`
  } else {
    return 'Vừa xong'
  }
}

const getNotificationIcon = (type) => {
  switch (type) {
    case 'reminder':
      return ['fas', 'clock']
    case 'warning':
      return ['fas', 'exclamation-triangle']
    case 'success':
      return ['fas', 'check-circle']
    default:
      return ['fas', 'bell']
  }
}

const getNotificationColor = (type) => {
  switch (type) {
    case 'reminder':
      return 'text-primary'
    case 'warning':
      return 'text-danger'
    case 'success':
      return 'text-success'
    default:
      return 'text-text-secondary'
  }
}

const formatDateTime = (date) => {
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date)
}

onMounted(async () => {
  let user = await store.getMyInfo();
  store.avatarUrl = user.avatarUrl
  store.fullName = user.fullName
  document.addEventListener('click', closeUserMenu)
  document.addEventListener('click', closeNotifications)
})

onUnmounted(() => {
  document.removeEventListener('click', closeUserMenu)
  document.removeEventListener('click', closeNotifications)
})
</script>

<template>
  <header class="fixed top-0 right-0 left-0 bg-surface shadow-sm z-40">
    <div class="h-16 px-4">
      <div class="flex justify-between items-center h-full">
        <!-- Left side -->
        <div class="flex items-center space-x-4">
          <button @click="toggleSidebar" class="text-text-secondary hover:text-text p-2 rounded-lg hover:bg-gray-100">
            <font-awesome-icon :icon="['fas', 'bars']" class="text-xl" />
          </button>
          <h1 class="text-xl font-semibold text-text">{{ currentPageTitle }}</h1>
        </div>

        <!-- Right side -->
        <div class="flex items-center space-x-2 sm:space-x-4">
          <div class="hidden sm:flex space-x-2">
            <button class="bg-success text-white px-4 py-2 rounded-lg flex items-center space-x-2 hover:bg-success/90" @click="router.push('/income')">
              <font-awesome-icon :icon="['fas', 'plus']" />
              <span>Ghi Thu</span>
            </button>
            <button class="bg-danger text-white px-4 py-2 rounded-lg flex items-center space-x-2 hover:bg-danger/90" @click="router.push('/expense')">
              <font-awesome-icon :icon="['fas', 'minus']" />
              <span>Ghi Chi</span>
            </button>
          </div>
          
          <!-- Mobile Quick Actions -->
          <div class="flex sm:hidden space-x-1">
            <button class="bg-success text-white p-2 rounded-lg hover:bg-success/90" title="Ghi Thu">
              <font-awesome-icon :icon="['fas', 'plus']" class="text-lg" />
            </button>
            <button class="bg-danger text-white p-2 rounded-lg hover:bg-danger/90" title="Ghi Chi">
              <font-awesome-icon :icon="['fas', 'minus']" class="text-lg" />
            </button>
          </div>
          
          <!-- Notifications -->
          <div class="relative">
            <button 
              @click="toggleNotifications"
              class="relative p-2 text-text-secondary hover:text-text rounded-full hover:bg-gray-100 focus:outline-none"
            >
              <FontAwesomeIcon icon="bell" class="text-xl" />
              <span 
                v-if="notifications.filter(n => !n.read).length > 0"
                class="absolute top-0 right-0 inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-danger rounded-full"
              >
                {{ notifications.filter(n => !n.read).length }}
              </span>
            </button>

            <!-- Notifications Dropdown -->
            <div 
              v-if="isNotificationsOpen"
              class="absolute right-0 mt-2 w-[calc(100vw-2rem)] sm:w-80 max-w-sm bg-white rounded-lg shadow-lg overflow-hidden z-50"
            >
              <!-- Header -->
              <div class="px-4 py-3 bg-gray-50 border-b border-gray-100">
                <h3 class="text-sm font-semibold text-text">Thông báo</h3>
              </div>

              <!-- Notifications List -->
              <div class="divide-y divide-gray-100 max-h-96 overflow-y-auto">
                <div 
                  v-for="notification in notifications" 
                  :key="notification.id"
                  class="p-4 hover:bg-gray-50 transition-colors duration-200 cursor-pointer"
                  :class="{ 'bg-gray-50/50': !notification.read }"
                  @click="showNotificationDetail(notification)"
                >
                  <div class="flex items-start space-x-3">
                    <div class="flex-shrink-0">
                      <FontAwesomeIcon 
                        :icon="getNotificationIcon(notification.type)"
                        :class="[getNotificationColor(notification.type), 'text-lg']"
                      />
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-text">
                        {{ notification.title }}
                      </p>
                      <p class="text-sm text-text-secondary mt-0.5">
                        {{ notification.description }}
                      </p>
                      <p class="text-xs text-text-secondary mt-1">
                        {{ getTimeAgo(notification.time) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Footer -->
              <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 text-center">
                <button class="text-sm text-primary hover:text-primary/80">
                  Xem tất cả thông báo
                </button>
              </div>
            </div>

            <!-- Overlay to close notifications when clicking outside -->
            <div 
              v-if="isNotificationsOpen"
              class="fixed inset-0 z-40"
              @click="closeNotifications"
            ></div>
          </div>
          
          <div class="relative">
            <button
              @click.stop="toggleUserMenu"
              class="flex items-center text-sm rounded-full focus:outline-none"
            >
              <img :src="userAvatar" alt="User Avatar" class="w-8 h-8 rounded-full" />
            </button>

            <!-- User Menu Dropdown -->
            <div
              v-if="isUserMenuOpen"
              class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50"
              @click.stop
            >
              <router-link 
                to="/profile" 
                class="block px-4 py-2 text-sm text-text hover:bg-gray-100"
              >
                Thông tin người dùng
              </router-link>
              <button 
                @click="logOut" 
                class="w-full text-left px-4 py-2 text-sm text-danger hover:bg-gray-100"
              >
                Đăng xuất
              </button>
            </div>

            <!-- Overlay to close user menu when clicking outside -->
            <div 
              v-if="isUserMenuOpen"
              class="fixed inset-0 z-40"
              @click="closeUserMenu"
            ></div>
          </div>

          <!-- Notification Detail Modal -->
          <Teleport to="body">
            <div 
              v-if="isDetailModalOpen" 
              class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100]"
              @click="closeDetailModal"
            >
              <div 
                class="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4"
                @click.stop
              >
                <!-- Modal Header -->
                <div class="px-6 py-4 border-b border-gray-200 flex items-start">
                  <div class="flex-1">
                    <div class="flex items-center space-x-3">
                      <FontAwesomeIcon 
                        v-if="selectedNotification"
                        :icon="getNotificationIcon(selectedNotification.type)"
                        :class="[getNotificationColor(selectedNotification.type), 'text-xl']"
                      />
                      <h3 class="text-lg font-semibold text-text">
                        {{ selectedNotification?.title }}
                      </h3>
                    </div>
                    <p class="text-sm text-text-secondary mt-1">
                      {{ selectedNotification ? formatDateTime(selectedNotification.time) : '' }}
                    </p>
                  </div>
                  <button 
                    @click="closeDetailModal"
                    class="text-text-secondary hover:text-text"
                  >
                    <FontAwesomeIcon icon="times" class="text-xl" />
                  </button>
                </div>

                <!-- Modal Content -->
                <div class="px-6 py-4">
                  <div class="prose prose-sm max-w-none">
                    <p class="whitespace-pre-line">{{ selectedNotification?.content }}</p>
                  </div>
                </div>

                <!-- Modal Footer -->
                <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end">
                  <button 
                    @click="closeDetailModal"
                    class="px-4 py-2 bg-gray-200 text-text-secondary rounded-lg hover:bg-gray-300"
                  >
                    Đóng
                  </button>
                </div>
              </div>
            </div>
          </Teleport>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
header {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
</style> 