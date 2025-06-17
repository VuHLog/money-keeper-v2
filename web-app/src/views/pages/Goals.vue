<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { formatCurrencyWithSymbol } from '@/utils/formatters'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faBullseye, faPlus, faMapMarkerAlt, faMotorcycle, faHome,faGraduationCap,faHeart,faCalendarAlt,faExclamationTriangle,faInfoCircle,faPlusCircle,faArrowLeft,faCalendarPlus} from '@fortawesome/free-solid-svg-icons'
import GoalFormModal from '@/views/components/GoalFormModal.vue'
import GoalDetailModal from '@/views/components/GoalDetailModal.vue'
import AddMoneyModal from '@/views/components/AddMoneyModal.vue'
import ExtendGoalModal from '@/views/components/ExtendGoalModal.vue'
import ToastManager from '@/views/components/ToastManager.vue'
import ConfettiAnimation from '@/views/components/ConfettiAnimation.vue'
import { useFinancialGoalStore } from '@/store/FinancialGoalStore'
import { useAuthStore } from '@/store/AuthStore'

library.add(faBullseye,faPlus,faMapMarkerAlt,faMotorcycle,faHome,faGraduationCap,faHeart,faCalendarAlt,faExclamationTriangle,faInfoCircle,faPlusCircle,faArrowLeft,faCalendarPlus)

// Store
const financialGoalStore = useFinancialGoalStore()
const authStore = useAuthStore()

// Toast Manager
const toastManagerRef = ref(null)
const responseBodyToast = ref(null)

// Function to add toast using ref to ToastManager component
const addToast = (notification, duration) => {
  if (toastManagerRef.value) {
    toastManagerRef.value.addToast(notification, duration)
  }
}

// Data
const allGoals = ref([])
const displayedGoals = ref([])
const pageSize = 5
const currentPage = ref(1)
const hasMore = ref(true)
const isLoading = ref(false)
const totalElements = ref(0)

// Tab state
const activeTab = ref(0) // 0: Chưa hoàn thành, 1: Đã hoàn thành, 2: Hết hạn

// Confetti animation state
const showConfetti = ref(false)

// Modal states
const isGoalModalOpen = ref(false)
const goalModalMode = ref('create') // 'create' or 'edit'
const isDetailModalOpen = ref(false)
const isAddMoneyModalOpen = ref(false)
const isExtendModalOpen = ref(false)
const selectedGoal = ref(null)
const goalDetailModalRef = ref(null)
const isStompConnected = computed(() => authStore.stompClient !== null)

// Load initial goals
onMounted(async() => {
  await loadGoals()
})

watch(isStompConnected, async (isConnected) => {
  if(isConnected){
    const user = await authStore.getMyInfo()
    authStore.stompClient.subscribe(
      "/topic/notifications/" + user.id,
      (response) => {
        responseBodyToast.value = JSON.parse(response.body)
        // Thay vì sử dụng SweetAlert, sử dụng component Toast tùy chỉnh
        if (toastManagerRef.value && responseBodyToast.value.title === 'Hạn mức chi') {
          toastManagerRef.value.handleApiNotification(responseBodyToast.value)
        }
      }
    )
  }
})

// Load goals from API
const loadGoals = async (status = null) => {
  if (isLoading.value) return
  
  const goalStatus = status !== null ? status : activeTab.value
  
  isLoading.value = true
  try {
    const response = await financialGoalStore.getFinancialGoalsPagination(
      'name',
      currentPage.value,
      pageSize,
      'ASC',
      '',
      goalStatus
    )
    
    if (response && response.content.length > 0) {
      // Map API response to match FinancialGoalResponse structure
      const mappedGoals = response.content.map(goal => ({
        id: goal.id,
        name: goal.name,
        targetAmount: goal.targetAmount,
        deadline: goal.deadline,
        user: goal.user,
        bucketPayment: goal.bucketPayment,
        interpretation: goal.interpretation,
        currency: goal.currency,
        currencySymbol: goal.currencySymbol,
        createdAt: goal.createdAt,
        updatedAt: goal.updatedAt,
        status: goal.status,
        // Additional fields for UI
        icon: getGoalIcon(goal.name),
        currentAmount: goal.currentAmount, 
        title: goal.name,
        description: goal.interpretation,
        accountId: goal.bucketPayment?.id,
        bucketPaymentId: goal.bucketPayment?.id,
        createdDate: goal.createdAt
      }))
      
      if (currentPage.value === 1) {
        displayedGoals.value = mappedGoals
      } else {
        displayedGoals.value.push(...mappedGoals)
      }
      
      hasMore.value = response.content.length === pageSize
      allGoals.value = displayedGoals.value // Keep allGoals in sync
    } else {
      hasMore.value = false
    }
    totalElements.value = response.totalElements
  } catch (error) {
    console.error('Error loading goals:', error)
    hasMore.value = false
    addToast({
      type: 'error',
      title: 'Lỗi!',
      content: 'Có lỗi xảy ra khi tải danh sách mục tiêu!'
    }, 4000)
  } finally {
    isLoading.value = false
  }
}

const loadMoreGoals = () => {
  if (hasMore.value && !isLoading.value) {
    currentPage.value++
    loadGoals()
  }
}

// Helper function to determine icon based on goal name
const getGoalIcon = (goalName) => {
  const name = goalName.toLowerCase()
  if (name.includes('du lịch') || name.includes('travel')) return 'fa-map-marker-alt'
  if (name.includes('xe') || name.includes('motorcycle')) return 'fa-motorcycle'
  if (name.includes('nhà') || name.includes('house') || name.includes('home')) return 'fa-home'
  if (name.includes('học') || name.includes('education')) return 'fa-graduation-cap'
  if (name.includes('cưới') || name.includes('wedding')) return 'fa-heart'
  if (name.includes('khẩn cấp') || name.includes('emergency')) return 'fa-exclamation-triangle'
  if (name.includes('máy tính') || name.includes('laptop')) return 'fa-laptop'
  return 'fa-bullseye' // Default icon
}

// Calculate progress percentage
const getProgress = (current, target) => {
  return Math.round((current / target) * 100)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Check if goal is at risk (less than 50% progress and less than 2 months remaining)
const isAtRisk = (goal) => {
  const progress = getProgress(goal.currentAmount, goal.targetAmount)
  const deadline = new Date(goal.deadline)
  const now = new Date()
  const monthsRemaining = (deadline - now) / (1000 * 60 * 60 * 24 * 30)
  
  return progress < 50 && monthsRemaining < 2
}

const getWarningMessage = (goal) => {
  const progress = getProgress(goal.currentAmount, goal.targetAmount)
  const deadline = new Date(goal.deadline)
  const now = new Date()
  const daysRemaining = Math.ceil((deadline - now) / (1000 * 60 * 60 * 24))
  
  if (daysRemaining < 30) {
    return `Còn ${daysRemaining} ngày mà mới đạt ${progress}%`
  } else if (daysRemaining < 60) {
    const weeksRemaining = Math.ceil(daysRemaining / 7)
    return `Còn ${weeksRemaining} tuần mà mới đạt ${progress}%`
  } else {
    const monthsRemaining = Math.ceil(daysRemaining / 30)
    return `Còn ${monthsRemaining} tháng mà mới đạt ${progress}%`
  }
}

const handleCreateGoal = () => {
  goalModalMode.value = 'create'
  selectedGoal.value = null
  isGoalModalOpen.value = true
}

const handleCloseGoalModal = () => {
  isGoalModalOpen.value = false
  selectedGoal.value = null
}

const handleSaveSuccess = (data) => {
  console.log('Goal save success:', data)
  addToast({
    type: 'success',
    title: 'Thành công!',
    content: data.message
  }, 3000)
  
  refreshGoalsList()
}

const handleSaveError = (data) => {
  console.error('Goal save error:', data)
  
  addToast({
    type: 'error',
    title: 'Lỗi!',
    content: data.message
  }, 4000)
}

const refreshGoalsList = async () => {
  displayedGoals.value = []
  allGoals.value = []
  currentPage.value = 1
  hasMore.value = true
  await loadGoals()
}

const handleGoalDetail = (goal) => {
  selectedGoal.value = goal
  isDetailModalOpen.value = true
}

const handleCloseDetailModal = () => {
  isDetailModalOpen.value = false
  selectedGoal.value = null
}

const handleEditGoal = (goal) => {
  console.log('Editing goal:', goal)
  
  handleCloseDetailModal()
  
  selectedGoal.value = goal
  goalModalMode.value = 'edit'
  
  isGoalModalOpen.value = true
}

const handleDeleteGoal = async (goal) => {
  try {
    await financialGoalStore.deleteById(goal.id)
    
    console.log('Goal deleted successfully:', goal)
    
    addToast({
      type: 'success',
      title: 'Thành công!',
      content: 'Xóa mục tiêu thành công!'
    }, 3000)
    
    refreshGoalsList()
    
    handleCloseDetailModal()
  } catch (error) {
    console.error('Error deleting goal:', error)
    
    addToast({
      type: 'error',
      title: 'Lỗi!',
      content: 'Có lỗi xảy ra khi xóa mục tiêu!'
    }, 4000)
  }
}

const handleAddMoney = (goal) => {
  isDetailModalOpen.value = false
  selectedGoal.value = goal
  
  // If it's expired goal (activeTab === 2), show extend modal, otherwise show add money modal
  if (activeTab.value === 2) {
    isExtendModalOpen.value = true
  } else {
    isAddMoneyModalOpen.value = true
  }
}

const handleCloseAddMoneyModal = () => {
  isAddMoneyModalOpen.value = false
  selectedGoal.value = null
}

const handleCloseExtendModal = () => {
  isExtendModalOpen.value = false
  selectedGoal.value = null
}

const handleExtendSuccess = async (data) => {
  isExtendModalOpen.value = false
  
  addToast({
    type: 'success',
    title: 'Thành công!',
    content: data.message
  }, 3000)
  
  // Refresh goals list to show updated status
  await refreshGoalsList()
}

const handleExtendError = (data) => {
  addToast({
    type: 'error',
    title: 'Lỗi!',
    content: data.message
  }, 4000)
}

const handleDeposit = async (data) => {
  isAddMoneyModalOpen.value = false
  const oldTotalElements = totalElements.value
  await refreshGoalsList()
  
  if(totalElements.value < oldTotalElements){
    triggerCelebration(data.financialGoal.name)
  }

  addToast({
      type: 'success',
      title: 'Thành công!',
      content: `Đã nạp ${formatCurrencyWithSymbol(data.convertedAmount, data.currency, data.currencySymbol)} vào mục tiêu "${data.financialGoal.name}"!`
    }, 3000)
}

// Trigger celebration animation for completed goal
const triggerCelebration = (goalName) => {
  showConfetti.value = true
  
  // Show celebration toast
  addToast({
    type: 'success',
    title: '🎉 Chúc mừng!',
    content: `Bạn đã hoàn thành mục tiêu "${goalName}"! Thật tuyệt vời!`
  }, 5000)
  
  // Hide confetti after animation
  setTimeout(() => {
    showConfetti.value = false
  }, 4000)
}

const handleDepositDeleted = async (deposit) => {
  // Refresh the goals list to update current amounts
  await refreshGoalsList()
  
  addToast({
    type: 'success',
    title: 'Hoàn tác thành công!',
    content: `Đã hoàn tác giao dịch nạp ${formatCurrencyWithSymbol(deposit.convertedAmount, selectedGoal.value?.currency, selectedGoal.value?.currencySymbol)}!`
  }, 3000)
}

// Handle tab change
const handleTabChange = (tabIndex) => {
  activeTab.value = tabIndex
  displayedGoals.value = []
  allGoals.value = []
  currentPage.value = 1
  hasMore.value = true
  loadGoals(tabIndex)
}
</script>

<template>
  <div class="p-4">
    <!-- Toast Manager Component -->
    <ToastManager ref="toastManagerRef" />
    
    <!-- Header -->
    <div class="bg-surface rounded-2xl shadow-sm mb-6">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="py-0">
            <div class="flex space-x-8">
              <button 
                @click="handleTabChange(0)"
                class="relative py-3 text-sm font-medium transition-colors"
                :class="activeTab === 0 ? 'text-primary border-b-2 border-primary' : 'text-text-secondary hover:text-text'"
              >
                Chưa hoàn thành
                <span v-if="activeTab === 0" class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary"></span>
              </button>
              <button 
                @click="handleTabChange(1)"
                class="relative py-3 text-sm font-medium transition-colors"
                :class="activeTab === 1 ? 'text-primary border-b-2 border-primary' : 'text-text-secondary hover:text-text'"
              >
                Đã hoàn thành
                <span v-if="activeTab === 1" class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary"></span>
              </button>
              <button 
                @click="handleTabChange(2)"
                class="relative py-3 text-sm font-medium transition-colors"
                :class="activeTab === 2 ? 'text-primary border-b-2 border-primary' : 'text-text-secondary hover:text-text'"
              >
                Hết hạn
                <span v-if="activeTab === 2" class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary"></span>
              </button>
            </div>
      </div>
        </div>
        <button 
          @click="handleCreateGoal"
          class="bg-primary text-white font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'plus']" class="text-sm" />
          <span>Tạo mục tiêu</span>
        </button>
      </div>
      
      <!-- Tabs -->
      
    </div>

    <!-- Goals List -->
    <div class="space-y-4">
      <div 
        v-for="goal in displayedGoals" 
        :key="goal.id"
        class="bg-surface rounded-2xl shadow-sm overflow-hidden hover:shadow-md transition-shadow"
      >
        <div class="p-6">
          <!-- Goal Title -->
          <div class="flex items-center space-x-3 mb-4">
            <div class="bg-blue-50 p-2 rounded-lg">
              <font-awesome-icon :icon="['fas', goal.icon.replace('fa-', '')]" class="text-blue-600 text-lg" />
            </div>
            <h3 class="text-xl font-semibold text-text">{{ goal.name }}</h3>
          </div>

          <!-- Amount Progress -->
          <div class="mb-4">
            <div class="flex items-center space-x-2 mb-2">
              <span class="text-2xl">💰</span>
              <span class="text-text-secondary">Đã tiết kiệm:</span>
              <span class="font-semibold text-success">{{ formatCurrencyWithSymbol(goal.currentAmount, goal.currency, goal.currencySymbol) }}</span>
              <span class="text-text-secondary">/</span>
              <span class="font-semibold text-text">{{ formatCurrencyWithSymbol(goal.targetAmount, goal.currency, goal.currencySymbol) }}</span>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="mb-4">
            <div class="flex items-center space-x-3 mb-2">
              <span class="text-xl">📊</span>
              <span class="text-text-secondary">Tiến độ:</span>
              <div class="flex-1 bg-gray-200 rounded-full h-3 overflow-hidden">
                <div 
                  class="h-full bg-gradient-to-r from-primary to-success transition-all duration-500"
                  :style="{ width: `${getProgress(goal.currentAmount, goal.targetAmount)}%` }"
                ></div>
              </div>
              <span class="font-semibold text-primary text-sm min-w-[40px]">
                {{ getProgress(goal.currentAmount, goal.targetAmount) }}%
              </span>
            </div>
          </div>

          <!-- Deadline -->
          <div class="flex items-center space-x-2 mb-3">
            <span class="text-xl">📅</span>
            <span class="text-text-secondary">Hạn:</span>
            <span class="font-medium text-text">{{ formatDate(goal.deadline) }}</span>
          </div>

          <!-- Warning (if at risk) -->
          <div v-if="activeTab === 0 && isAtRisk(goal)" class="flex items-center space-x-2 mb-4 p-3 bg-warning/10 border border-warning/20 rounded-lg">
            <span class="text-xl">⚠️</span>
            <span class="text-warning font-medium">Cảnh báo:</span>
            <span class="text-warning text-sm">{{ getWarningMessage(goal) }}</span>
          </div>

          <!-- Description -->
          <div class="mb-4 text-text-secondary text-sm bg-gray-50 p-3 rounded-lg">
            {{ goal.interpretation || goal.description || 'Không có mô tả' }}
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-3">
            <button 
              @click="handleGoalDetail(goal)"
              class="bg-gray-100 text-text-secondary font-medium py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors flex items-center justify-center space-x-2"
              :class="activeTab === 1 || activeTab === 2 ? 'flex-1' : 'flex-1'"
            >
              <font-awesome-icon :icon="['fas', 'info-circle']" class="text-sm" />
              <span>Chi tiết</span>
            </button>
            <!-- Only show "Nạp thêm" for incomplete goals -->
            <button 
              v-if="activeTab === 0"
              @click="handleAddMoney(goal)"
              class="flex-1 bg-success text-white font-medium py-2 px-4 rounded-lg hover:bg-success/90 transition-colors flex items-center justify-center space-x-2"
            >
              <font-awesome-icon :icon="['fas', 'plus-circle']" class="text-sm" />
              <span>Nạp thêm</span>
            </button>
            <!-- Show "Gia hạn" for expired goals -->
            <button 
              v-if="activeTab === 2"
              @click="handleAddMoney(goal)"
              class="flex-1 bg-warning text-white font-medium py-2 px-4 rounded-lg hover:bg-warning/90 transition-colors flex items-center justify-center space-x-2"
            >
              <font-awesome-icon :icon="['fas', 'calendar-plus']" class="text-sm" />
              <span>Gia hạn</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="displayedGoals.length === 0" class="text-center py-12">
        <div class="bg-gray-100 p-6 rounded-full w-24 h-24 mx-auto mb-4 flex items-center justify-center">
          <font-awesome-icon :icon="['fas', 'bullseye']" class="text-text-secondary text-3xl" />
        </div>
        <h3 class="text-xl font-semibold text-text mb-2">{{ activeTab === 0 ? 'Chưa có mục tiêu nào' : activeTab === 1 ? 'Chưa có mục tiêu nào đã hoàn thành' : 'Chưa có mục tiêu nào đã hết hạn' }}</h3>
        <p class="text-text-secondary mb-6">{{ activeTab === 0 ? 'Hãy tạo mục tiêu tài chính đầu tiên của bạn' : activeTab === 1 ? 'Hãy hoàn thành mục tiêu tài chính đầu tiên của bạn' : 'Hãy hoàn thành mục tiêu tài chính đầu tiên của bạn' }}</p>
        <button
          v-if="activeTab === 0"
          @click="handleCreateGoal"
          class="bg-primary text-white font-medium px-6 py-3 rounded-lg hover:bg-primary/90 transition-colors inline-flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'plus']" class="text-sm" />
          <span>Tạo mục tiêu đầu tiên</span>
        </button>
        <button 
          v-if="activeTab === 1"
          @click="handleTabChange(0)"
          class="bg-primary text-white font-medium px-6 py-3 rounded-lg hover:bg-primary/90 transition-colors inline-flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'arrow-left']" class="text-sm" />
          <span>Quay lại để hoàn thành mục tiêu</span>
        </button>
        <button 
          v-if="activeTab === 2"
          @click="handleTabChange(0)"
          class="bg-primary text-white font-medium px-6 py-3 rounded-lg hover:bg-primary/90 transition-colors inline-flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'arrow-left']" class="text-sm" />
          <span>Quay lại để tạo mục tiêu mới</span>
        </button>
      </div>

      <!-- Load More Button -->
      <div v-if="hasMore && displayedGoals.length > 0" class="text-center py-6">
        <button 
          @click="loadMoreGoals"
          :disabled="isLoading"
          class="bg-surface text-text border border-gray-200 font-medium px-8 py-3 rounded-lg hover:bg-gray-50 transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2 mx-auto"
        >
          <div v-if="isLoading" class="animate-spin rounded-full h-4 w-4 border-2 border-text border-t-transparent"></div>
          <span>{{ isLoading ? 'Đang tải...' : 'Tải thêm mục tiêu' }}</span>
        </button>
      </div>
    </div>

    <!-- Goal Modal (Create/Edit) -->
    <GoalFormModal
      v-if="isGoalModalOpen"
      :is-open="isGoalModalOpen"
      :mode="goalModalMode"
      :goal="selectedGoal"
      @close="handleCloseGoalModal"
      @success="handleSaveSuccess"
      @error="handleSaveError"
    />

    <!-- Goal Detail Modal -->
    <GoalDetailModal 
      v-if="isDetailModalOpen"
      ref="goalDetailModalRef"
      :is-open="isDetailModalOpen"
      :goal="selectedGoal"
      :active-tab="activeTab"
      @close="handleCloseDetailModal"
      @edit="handleEditGoal"
      @delete="handleDeleteGoal"
      @add-money="handleAddMoney"
      @deposit-deleted="handleDepositDeleted"
    />

    <!-- Add Money Modal -->
    <AddMoneyModal 
      v-if="isAddMoneyModalOpen"
      :is-open="isAddMoneyModalOpen"
      :goal="selectedGoal"
      @close="handleCloseAddMoneyModal"
      @deposit="handleDeposit"
    />

    <!-- Extend Goal Modal -->
    <ExtendGoalModal 
      v-if="isExtendModalOpen"
      :is-open="isExtendModalOpen"
      :goal="selectedGoal"
      @close="handleCloseExtendModal"
      @success="handleExtendSuccess"
      @error="handleExtendError"
    />

    <!-- Confetti Animation -->
    <ConfettiAnimation :show="showConfetti" />
  </div>
</template>

<style scoped>
/* Custom progress bar animation */
.progress-bar {
  transition: width 0.5s ease-in-out;
}

/* Gradient text for success amounts */
.text-success {
  color: #059669;
}

/* Warning styling */
.text-warning {
  color: #D97706;
}

.bg-warning\/10 {
  background-color: rgba(217, 119, 6, 0.1);
}

.border-warning\/20 {
  border-color: rgba(217, 119, 6, 0.2);
}

/* Hover effects */
.hover\:shadow-md:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
</style>
