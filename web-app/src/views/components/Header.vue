<script setup>
import { ref, computed, onMounted, onUnmounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@stores/AuthStore.js'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { useNotificationStore } from "@/store/NotificationStore";
import ToastManager from '@/views/components/ToastManager.vue'

const router = useRouter()
const store = useAuthStore();
const notificationStore = useNotificationStore();
const route = useRoute()
const isUserMenuOpen = ref(false)
const userAvatar = computed(() => store.avatarUrl || store.avatarUrlDefault)
const isNotificationsOpen = ref(false)
const selectedNotification = ref(null)
const isDetailModalOpen = ref(false)
const pageSize = ref(4)
const toastManagerRef = ref(null)

// Theo dõi số lượng thông báo chưa đọc
const countNewNotifications = computed(() => notificationStore.countNewNotifications || 0)

// Thêm biến state để theo dõi tab đang active
const activeNotificationTab = ref('all') // 'all' hoặc 'unread'

// Thêm hàm lọc thông báo theo tab
const filteredNotifications = computed(() => {
  return notificationStore.notifications
})

// Thêm hàm chuyển đổi tab và tải lại dữ liệu
const switchNotificationTab = async (tab) => {
  activeNotificationTab.value = tab
  
  try {
    // Reset pagination về mặc định
    notificationStore.resetPagination();
    
    if (tab === 'unread') {
      notificationStore.readStatus = 0;
      await notificationStore.getNotifications();
    } else {
      notificationStore.readStatus = -1;
      await notificationStore.getNotifications();
    }
  } catch (error) {
    console.error('Error loading notifications for tab:', error);
  }
}

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
  
  // Cập nhật trạng thái đã đọc khi mở dropdown thông báo
  if (isNotificationsOpen.value) {
    if (activeNotificationTab.value !== 'all') {
      activeNotificationTab.value = 'all';
    }
  }
}

const closeNotifications = () => {
  isNotificationsOpen.value = false
}

const showNotificationDetail = async (notification) => {
  selectedNotification.value = notification
  isDetailModalOpen.value = true
  // Đánh dấu là đã đọc
  if (!notification.readStatus) {
    try{
      await notificationStore.updateReadStatus(notification.id, 1)
      notificationStore.countNewNotifications -= 1;
    } catch (error) {
      console.error('Error updating read status:', error)
    }
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
    case 'expense':
      return ['fas', 'arrow-down']
    case 'revenue':
      return ['fas', 'arrow-up']
    case 'expense limit':
      return ['fas', 'exclamation-triangle']
    case 'reminder':
      return ['fas', 'clock']
    case 'success':
      return ['fas', 'check-circle']
    case 'info':
      return ['fas', 'info-circle']
    default:
      return ['fas', 'bell']
  }
}

const getNotificationColor = (type) => {
  switch (type) {
    case 'expense':
      return 'text-red-500'
    case 'revenue':
      return 'text-green-500'
    case 'expense limit':
      return 'text-orange-500'
    case 'reminder':
      return 'text-blue-500'
    case 'success':
      return 'text-green-500'
    case 'info':
      return 'text-purple-500'
    case 'danger':
      return 'text-red-500'
    default:
      return 'text-primary'
  }
}

const getNotificationBgColor = (type, isRead) => {
  if (!isRead) {
    switch (type) {
      case 'expense':
        return 'bg-red-50 border-l-4 border-red-500'
      case 'revenue':
        return 'bg-green-50 border-l-4 border-green-500'
      case 'expense limit':
        return 'bg-orange-50 border-l-4 border-orange-500'
      case 'reminder':
        return 'bg-blue-50 border-l-4 border-blue-500'
      case 'warning':
        return 'bg-orange-50 border-l-4 border-orange-500'
      case 'success':
        return 'bg-green-50 border-l-4 border-green-500'
      case 'info':
        return 'bg-purple-50 border-l-4 border-purple-500'
      case 'danger':
        return 'bg-red-50 border-l-4 border-red-500'
      default:
        return 'bg-gray-50 border-l-4 border-primary'
    }
  }
  return ''
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

// Xử lý tải thêm thông báo khi cuộn
function handleScroll(event) {
  const element = event.target;
  
  if (element.scrollHeight - element.scrollTop <= element.clientHeight + 1) {
    loadMoreNotifications();
  }
}

async function loadMoreNotifications() {
  if (notificationStore.pageNumber < notificationStore.totalPages) {
    try {
      await notificationStore.changePageSize(notificationStore.pageSize + pageSize.value);
    } catch (error) {
      console.error('Error loading more notifications:', error);
    }
  }
}

async function handleSeePreviousNotification() {
  try {
    // Nếu đang ở tab "Chưa đọc", khi click nút này sẽ chuyển sang tab "Tất cả"
    if (activeNotificationTab.value === 'unread') {
      activeNotificationTab.value = 'all';
    }
    
    await notificationStore.changePageSize(notificationStore.pageSize + pageSize.value);
  } catch (error) {
    console.error('Error loading previous notifications:', error);
  }
}

// Function to toggle notification read status
const toggleReadStatus = async (event, notification) => {
  event.stopPropagation() // Prevent opening detail modal
  try {
    const newStatus = notification.readStatus === 0 ? 1 : 0
    await notificationStore.updateReadStatus(notification.id, newStatus)
    
    // Update local counters
    if (newStatus === 1) {
      notificationStore.countNewNotifications -= 1
    } else {
      notificationStore.countNewNotifications += 1
    }
  } catch (error) {
    console.error('Error toggling read status:', error)
  }
}

// Function to delete notification
const deleteNotification = async (event, notificationId) => {
  event.stopPropagation() // Prevent opening detail modal
  try {
    await notificationStore.deleteNotification(notificationId)
    
    // Check if it was an unread notification to update counter
    const deletedNotification = notificationStore.notifications.find(n => n.id === notificationId)
    if (deletedNotification && deletedNotification.readStatus === 0) {
      notificationStore.countNewNotifications -= 1
    }
  } catch (error) {
    console.error('Error deleting notification:', error)
  }
}

// Thêm biến ref mới để theo dõi thông báo đang hover
const hoveredNotificationId = ref(null)

// Thêm các hàm xử lý hover
const handleMouseEnter = (notificationId) => {
  hoveredNotificationId.value = notificationId
}

const handleMouseLeave = () => {
  hoveredNotificationId.value = null
}

// Thêm hàm đánh dấu tất cả là đã đọc
const markAllAsRead = async () => {
  try {
    if (notificationStore.countNewNotifications > 0) {
      await notificationStore.updateAllReadStatus(1);
      notificationStore.countNewNotifications = 0;
    }
  } catch (error) {
    console.error('Error marking all notifications as read:', error);
  }
}

onMounted(async () => {
  let user = await store.getMyInfo();
  store.avatarUrl = user.avatarUrl
  store.fullName = user.fullName
  document.addEventListener('click', closeUserMenu)
  document.addEventListener('click', closeNotifications)

  await notificationStore.getNotifications();
  await notificationStore.countNotificationByReadStatus(0);
  store.connectStompClient();
  store.stompClient.onConnect = (frame) =>{
    console.log("Connected: " + frame);
        store.stompClient.subscribe(
          "/topic/notifications/" + user.id,
          (response) => {
            let responseBody = JSON.parse(response.body);
            notificationStore.notifications.unshift(responseBody);
            notificationStore.countNewNotifications += 1;
          }
        );
  }
})

onUnmounted(() => {
  document.removeEventListener('click', closeUserMenu)
  document.removeEventListener('click', closeNotifications)

  store.disconnectStompClient();
})
</script>

<template>
  <header class="fixed top-0 right-0 left-0 bg-surface shadow-sm z-40">
    <!-- Thêm ToastManager component -->
    <ToastManager ref="toastManagerRef" />
    
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
          <div class="relative" @click.stop>
            <button 
              @click="toggleNotifications"
              class="relative p-2 text-text-secondary hover:text-text rounded-full hover:bg-gray-100 focus:outline-none"
            >
              <FontAwesomeIcon icon="bell" class="text-xl" />
              <span 
                v-if="countNewNotifications > 0"
                class="absolute top-0 right-0 inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-danger rounded-full"
              >
                {{ countNewNotifications }}
              </span>
            </button>

            <!-- Notifications Dropdown -->
            <div 
              v-if="isNotificationsOpen"
              class="absolute right-0 mt-2 w-[calc(100vw-2rem)] sm:w-80 max-w-sm bg-white rounded-lg shadow-lg overflow-hidden z-50"
            >
              <!-- Header -->
              <div class="px-4 py-3 bg-gray-50 border-b border-gray-100">
                <div class="flex justify-between items-center mb-2">
                  <h3 class="text-sm font-semibold text-text">Thông báo</h3>
                  <button 
                    v-if="countNewNotifications > 0"
                    @click.stop="markAllAsRead" 
                    class="text-xs text-primary hover:text-primary/80"
                  >
                    Đánh dấu tất cả đã đọc
                  </button>
                </div>
                
                <!-- Tab Navigation -->
                <div class="flex border-b">
                  <button 
                    @click.stop="switchNotificationTab('all')"
                    class="flex-1 py-2 text-center text-sm font-medium transition-colors duration-200"
                    :class="activeNotificationTab === 'all' 
                      ? 'text-primary border-b-2 border-primary' 
                      : 'text-text-secondary hover:text-primary'"
                  >
                    Tất cả
                  </button>
                  <button 
                    @click.stop="switchNotificationTab('unread')"
                    class="flex-1 py-2 text-center text-sm font-medium transition-colors duration-200 relative"
                    :class="activeNotificationTab === 'unread' 
                      ? 'text-primary border-b-2 border-primary' 
                      : 'text-text-secondary hover:text-primary'"
                  >
                    Chưa đọc
                    <span 
                      v-if="countNewNotifications > 0"
                      class="absolute top-0 right-2 inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-danger rounded-full"
                    >
                      {{ countNewNotifications }}
                    </span>
                  </button>
                </div>
              </div>

              <!-- Notifications List -->
              <div class="scrollable-list" @scroll="handleScroll">
                <div 
                  v-for="notification in filteredNotifications" 
                  :key="notification.id"
                  :class="[
                    'p-4 hover:bg-gray-100 transition-colors duration-200 cursor-pointer relative',
                    notification.readStatus === 1 ? 'border-b' : '',
                    getNotificationBgColor(notification.type, notification.readStatus === 1)
                  ]"
                  @click="showNotificationDetail(notification)"
                  @mouseenter="handleMouseEnter(notification.id)"
                  @mouseleave="handleMouseLeave"
                >
                  <div class="flex items-start space-x-3">
                    <div class="flex-shrink-0">
                      <div class="w-8 h-8 rounded-full flex items-center justify-center" 
                           :class="[getNotificationColor(notification.type), 'bg-opacity-20', getNotificationBgColor(notification.type, true)]">
                        <FontAwesomeIcon 
                          :icon="getNotificationIcon(notification.type || 'info')"
                          :class="[getNotificationColor(notification.type), 'text-lg']"
                        />
                      </div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center justify-between">
                        <p class="text-sm font-medium text-text mr-2">
                          {{ notification.title }}
                        </p>
                        <span v-if="notification.readStatus === 0" 
                              class="text-xs px-1.5 py-0.5 rounded" 
                              :class="[getNotificationColor(notification.type), 'bg-opacity-20']">
                          Chưa đọc
                        </span>
                      </div>
                      <p class="text-sm text-text-secondary mt-0.5 multiline-truncate" style="max-width: 200px;" v-html="notification.content">
                      </p>
                      <p class="text-xs text-text-secondary mt-1">
                        {{ notification.createdAt || getTimeAgo(notification.time) }}
                      </p>
                    </div>
                    <div v-if="notification.readStatus === 0" class="ml-auto">
                      <div class="w-2.5 h-2.5 rounded-full" :class="[getNotificationColor(notification.type)]"></div>
                    </div>
                  </div>
                  
                  <!-- Notification Options - Cải thiện hiển thị -->
                  <div v-if="hoveredNotificationId === notification.id"
                       class="absolute top-1 right-1 p-1.5 flex space-x-2 bg-white shadow-md rounded-lg z-50"
                       @click.stop>
                    <button @click.stop="toggleReadStatus($event, notification)" 
                            class="p-1.5 text-gray-500 hover:text-primary rounded-full hover:bg-gray-100"
                            :title="notification.readStatus === 0 ? 'Đánh dấu đã đọc' : 'Đánh dấu chưa đọc'">
                      <FontAwesomeIcon :icon="notification.readStatus === 0 ? 'envelope-open' : 'envelope'" class="text-sm" />
                    </button>
                    <button @click.stop="deleteNotification($event, notification.id)" 
                            class="p-1.5 text-gray-500 hover:text-danger rounded-full hover:bg-gray-100"
                            title="Xóa thông báo">
                      <FontAwesomeIcon icon="trash" class="text-sm" />
                    </button>
                  </div>
                </div>
                
                <!-- Empty state for each tab -->
                <div v-if="filteredNotifications.length === 0" class="flex flex-col items-center justify-center py-8 px-4 text-center">
                  <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-2">
                    <FontAwesomeIcon :icon="['fas', 'bell']" class="text-gray-400 text-xl" />
                  </div>
                  <p class="text-text-secondary">
                    {{ activeNotificationTab === 'unread' 
                      ? 'Không có thông báo chưa đọc' 
                      : 'Không có thông báo nào' }}
                  </p>
                </div>
              </div>

              <!-- Footer -->
              <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 text-center">
                <button 
                  v-if="notificationStore.pageNumber < notificationStore.totalPages"
                  class="text-sm text-primary hover:text-primary/80"
                  @click.stop="handleSeePreviousNotification()"
                >
                  Xem thông báo trước đó
                </button>
                <div v-else-if="filteredNotifications.length === 0" class="text-sm text-text-secondary">
                  {{ activeNotificationTab === 'unread' 
                    ? 'Không có thông báo chưa đọc nào' 
                    : 'Không có thông báo nào' }}
                </div>
                <div v-else class="text-sm text-text-secondary">
                  Đã hiển thị tất cả thông báo
                </div>
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
              <div class="px-4 py-2 text-sm text-text border-b">
                <span class="font-medium">{{ store.fullName }}</span>
              </div>
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
                      <div class="w-8 h-8 rounded-full flex items-center justify-center" v-if="selectedNotification" :class="[getNotificationColor(selectedNotification.type), 'bg-opacity-20', getNotificationBgColor(selectedNotification.type, true)]">
                        <FontAwesomeIcon 
                          v-if="selectedNotification"
                          :icon="getNotificationIcon(selectedNotification.type || 'info')"
                          :class="[getNotificationColor(selectedNotification.type), 'text-xl']"
                        />
                      </div>
                      <h3 class="text-lg font-semibold text-text">
                        {{ selectedNotification?.title }}
                      </h3>
                    </div>
                    <p class="text-sm text-text-secondary mt-1">
                      {{ selectedNotification ? (selectedNotification.createdAt || formatDateTime(selectedNotification.time)) : '' }}
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
                    <p class="whitespace-pre-line" v-html="selectedNotification?.content"></p>
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

.scrollable-list {
  max-height: 520px; 
  overflow-y: auto;
  max-width: 360px;
}

.multiline-truncate {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 