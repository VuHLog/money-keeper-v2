<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faBullseye, 
  faXmark, 
  faEllipsisVertical,
  faWallet,
  faCalendarDays,
  faFileText,
  faPlusCircle,
  faEdit,
  faTrash,
  faClock,
  faExclamationTriangle,
  faChevronLeft,
  faChevronRight,
  faUndo
} from '@fortawesome/free-solid-svg-icons'
import { formatCurrencyWithSymbol } from '@/utils/formatters'
import Avatar from '@/views/components/Avatar.vue'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import { useFinancialGoalStore } from '@/store/FinancialGoalStore'
import { useExpenseRegularStore } from '@/store/ExpenseRegularStore'

library.add(
  faBullseye,
  faXmark,
  faEllipsisVertical,
  faWallet,
  faCalendarDays,
  faFileText,
  faPlusCircle,
  faEdit,
  faTrash,
  faClock,
  faExclamationTriangle,
  faChevronLeft,
  faChevronRight,
  faUndo
)

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  goal: {
    type: Object,
    default: null
  },
  activeTab: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['close', 'edit', 'delete', 'addMoney', 'deposit-deleted'])

// Store
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const financialGoalStore = useFinancialGoalStore()
const expenseRegularStore = useExpenseRegularStore()

// Data
const accounts = ref([])

// Dropdown menu state
const isMenuOpen = ref(false)

// Delete confirmation modal state
const isDeleteModalOpen = ref(false)

// Deposit history data and pagination
const depositHistory = ref([])
const depositPagination = ref({
  pageNumber: 1,
  pageSize: 2,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false
})
const isLoadingDeposits = ref(false)
const deletingDepositId = ref(null)
const currentAmount = ref(0)

// Load accounts when component mounts
onMounted(async () => {
  try {
    accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments()
    currentAmount.value = props.goal.currentAmount
  } catch (error) {
    console.error('Error loading accounts:', error)
  }
})

// Load deposit history from API
const loadDepositHistory = async (pageNumber = 1) => {
  if (!props.goal?.id || isLoadingDeposits.value) return
  
  isLoadingDeposits.value = true
  try {
    const response = await financialGoalStore.getDipositHistoryByFinancialGoalId(
      props.goal.id,
      pageNumber,
      depositPagination.value.pageSize,
      'DESC',
      ''
    )
    
    if (response) {
      // Map the response to match our expected format
      depositHistory.value = response.content.map(deposit => ({
        id: deposit.id,
        date: deposit.expenseDate,
        amount: deposit.convertedAmount,
        note: deposit.interpretation || deposit.note || ''
      }))
      
      // Update pagination info
      depositPagination.value = {
        pageNumber: response.pageable.pageNumber + 1, // API uses 0-based, UI uses 1-based
        pageSize: response.pageable.pageSize,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
        hasNext: !response.last,
        hasPrevious: !response.first
      }
    }
  } catch (error) {
    console.error('Error loading deposit history:', error)
    depositHistory.value = []
  } finally {
    isLoadingDeposits.value = false
  }
}

// Handle pagination
const handlePreviousPage = () => {
  if (depositPagination.value.hasPrevious) {
    loadDepositHistory(depositPagination.value.pageNumber - 1)
  }
}

const handleNextPage = () => {
  if (depositPagination.value.hasNext) {
    loadDepositHistory(depositPagination.value.pageNumber + 1)
  }
}

// Calculate progress percentage
const getProgress = (current, target) => {
  return Math.round((current / target) * 100)
}

// Format date to Vietnamese format
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Calculate remaining amount
const remainingAmount = computed(() => {
  if (!props.goal) return 0
  return Math.max(0, props.goal.targetAmount - currentAmount.value)
})

// Check if goal is completed
const isCompleted = computed(() => {
  if (!props.goal) return false
  return currentAmount.value >= props.goal.targetAmount
})

// Check if goal status is completed (status = 1)
const isStatusCompleted = computed(() => {
  if (!props.goal) return false
  return props.goal.status === 1
})

// Handle close modal
const handleClose = () => {
  isMenuOpen.value = false
  emit('close')
}

// Handle menu actions
const handleEdit = () => {
  isMenuOpen.value = false
  emit('edit', props.goal)
}

const handleDelete = () => {
  isMenuOpen.value = false
  isDeleteModalOpen.value = true
}

const handleConfirmDelete = () => {
  isDeleteModalOpen.value = false
  emit('delete', props.goal)
}

const handleCancelDelete = () => {
  isDeleteModalOpen.value = false
}

const handleAddMoney = () => {
  emit('addMoney', props.goal)
}

// Close menu when clicking outside
const handleClickOutside = (event) => {
  const menuEl = document.querySelector('.goal-menu-dropdown')
  if (menuEl && !menuEl.contains(event.target) && isMenuOpen.value) {
    isMenuOpen.value = false
  }
}

// Add event listener when menu is open
watch(() => isMenuOpen.value, (isOpen) => {
  if (isOpen) {
    document.addEventListener('mousedown', handleClickOutside)
  } else {
    document.removeEventListener('mousedown', handleClickOutside)
  }
})

// Watch for modal open/close and goal changes to load deposit history
watch(() => [props.isOpen, props.goal?.id], ([isOpen, goalId]) => {
  if (isOpen && goalId) {
    // Reset pagination and load first page when opening modal or goal changes
    depositPagination.value.pageNumber = 1
    loadDepositHistory(1)
  }
}, { immediate: true })

// Get account info - now uses real data from FinancialGoalResponse.bucketPayment
const getAccountInfo = (goal) => {
  if (!goal) {
    return {
      name: 'Ví tiết kiệm',
      iconUrl: 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/wallet_icon.svg'
    }
  }
  
  // Use bucketPayment from FinancialGoalResponse if available
  if (goal.bucketPayment) {
    return {
      name: goal.bucketPayment.accountName || goal.bucketPayment.name || 'Ví tiết kiệm',
      iconUrl: goal.bucketPayment.iconUrl || 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/wallet_icon.svg'
    }
  }
  
  // Fallback: find account from store by bucketPaymentId or accountId (backward compatibility)
  const accountId = goal.bucketPaymentId || goal.accountId
  if (accountId && accounts.value.length) {
    const account = accounts.value.find(acc => acc.id === accountId)
    if (account) {
      return {
        name: account.accountName,
        iconUrl: account.iconUrl
      }
    }
  }
  
  // Default fallback
  return {
    name: 'Ví tiết kiệm',
    iconUrl: 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/wallet_icon.svg'
  }
}

// Refresh deposit history (can be called from parent)
const refreshDepositHistory = () => {
  if (props.goal?.id) {
    loadDepositHistory(depositPagination.value.pageNumber)
  }
}

// Handle delete deposit (revert transaction)
const handleDeleteDeposit = async (deposit) => {
  if (deletingDepositId.value) {
    return
  }
  
  deletingDepositId.value = deposit.id
  
  try {
    await expenseRegularStore.deleteExpenseRegular(deposit.id)
    
    // Refresh deposit history after successful deletion
    await loadDepositHistory(depositPagination.value.pageNumber)
    
    // If current page is empty and not the first page, go to previous page
    if (depositHistory.value.length === 0 && depositPagination.value.pageNumber > 1) {
      await loadDepositHistory(depositPagination.value.pageNumber - 1)
    }

    debugger
    currentAmount.value = currentAmount.value - deposit.amount
    
    // Emit event to parent to refresh goal data
    emit('deposit-deleted', deposit)
    
  } catch (error) {
    console.error('Error deleting deposit:', error)
    alert('Có lỗi xảy ra khi hoàn tác giao dịch!')
  } finally {
    deletingDepositId.value = null
  }
}

// Expose methods for parent component
defineExpose({
  refreshDepositHistory
})
</script>

<template>
  <div v-if="isOpen && goal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="handleClose">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-auto">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between sticky top-0 bg-white z-10">
        <div class="flex items-center space-x-3 flex-1">
          <div class="bg-blue-50 p-2 rounded-lg">
            <font-awesome-icon :icon="['fas', goal.icon.replace('fa-', '')]" class="text-blue-600 text-lg" />
          </div>
          <h3 class="text-lg font-semibold text-text">{{ goal.name }}</h3>
        </div>
        
        <button @click="handleClose" class="ml-3 text-text-secondary hover:text-text transition-colors">
          <font-awesome-icon :icon="['fas', 'xmark']" class="text-xl" />
        </button>
      </div>

      <!-- Modal Content -->
      <div class="px-6 py-6 space-y-6">
        <!-- Goal Information -->
        <div class="bg-gray-50 rounded-lg p-4 space-y-3">
          <!-- Target Amount -->
          <div class="flex items-center space-x-3">
            <span class="text-2xl">💰</span>
            <span class="text-text-secondary">Mục tiêu:</span>
            <span class="font-semibold text-text text-lg">{{ formatCurrencyWithSymbol(goal.targetAmount, goal.currency || 'VND', goal.currencySymbol || '₫') }}</span>
          </div>
          
          <!-- Deadline -->
          <div class="flex items-center space-x-3">
            <span class="text-2xl">📅</span>
            <span class="text-text-secondary">Hạn:</span>
            <span class="font-medium text-text">{{ formatDate(goal.deadline) }}</span>
          </div>
          
          <!-- Account -->
          <div class="flex items-center space-x-3">
            <span class="text-2xl">💼</span>
            <span class="text-text-secondary">Ví:</span>
            <div class="flex items-center space-x-2">
              <Avatar :src="getAccountInfo(goal).iconUrl" :alt="getAccountInfo(goal).name" size="s" />
              <span class="font-medium text-text">{{ getAccountInfo(goal).name }}</span>
            </div>
          </div>
          
          <!-- Description -->
          <div class="flex items-start space-x-3" v-if="goal.interpretation">
            <span class="text-2xl">🔖</span>
            <span class="text-text-secondary">Mô tả:</span>
            <span class="text-text">{{ goal.interpretation }}</span>
          </div>
        </div>

        <!-- Progress Section -->
        <div class="bg-gradient-to-r from-primary/5 to-success/5 rounded-lg p-4 space-y-4">
          <!-- Progress Bar -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <span class="text-text-secondary">Tiến độ:</span>
              <span class="font-semibold text-primary">{{ getProgress(currentAmount, goal.targetAmount) }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
              <div 
                class="h-full bg-gradient-to-r from-primary to-success transition-all duration-500"
                :style="{ width: `${getProgress(currentAmount, goal.targetAmount)}%` }"
              ></div>
            </div>
          </div>
          
          <!-- Amount Info -->
          <div class="grid grid-cols-2 gap-4">
            <div class="text-center">
              <div class="text-text-secondary text-sm">Đã nạp</div>
              <div class="font-semibold text-success text-lg">{{ formatCurrencyWithSymbol(currentAmount, goal.currency, goal.currencySymbol) }}</div>
            </div>
            <div class="text-center">
              <div class="text-text-secondary text-sm">Còn thiếu</div>
              <div class="font-semibold text-warning text-lg">{{ formatCurrencyWithSymbol(remainingAmount, goal.currency, goal.currencySymbol) }}</div>
            </div>
          </div>
        </div>

        <!-- Add Money Button -->
        <div class="flex justify-center" v-if="!isCompleted && !isStatusCompleted && activeTab !== 2">
          <button 
            @click="handleAddMoney"
            class="bg-success text-white font-medium px-6 py-3 rounded-lg hover:bg-success/90 transition-colors flex items-center space-x-2"
          >
            <font-awesome-icon :icon="['fas', 'plus-circle']" class="text-lg" />
            <span>Nạp thêm tiền</span>
          </button>
        </div>

        <!-- Completion Badge -->
        <div v-if="isCompleted" class="flex justify-center">
          <div class="bg-success/10 border border-success/20 rounded-lg px-6 py-3 text-center">
            <div class="text-success font-semibold text-lg mb-1">🎉 Hoàn thành mục tiêu!</div>
            <div class="text-success text-sm">Chúc mừng bạn đã đạt được mục tiêu của mình</div>
          </div>
        </div>

        <!-- Deposit History -->
        <div class="border-t border-gray-200 pt-6">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-lg font-semibold text-text flex items-center space-x-2">
              <span>Lịch sử nạp</span>
            </h4>
            
            <!-- Pagination Info -->
            <div v-if="depositPagination.totalElements > 0" class="text-sm text-text-secondary">
              Trang {{ depositPagination.pageNumber }} / {{ depositPagination.totalPages }}
              ({{ depositPagination.totalElements }} giao dịch)
            </div>
          </div>
          
          <!-- Loading State -->
          <div v-if="isLoadingDeposits" class="text-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-2 border-primary border-t-transparent mx-auto mb-2"></div>
            <p class="text-text-secondary text-sm">Đang tải...</p>
          </div>
          
          <!-- Deposit List -->
          <div v-else-if="depositHistory.length > 0" class="space-y-3">
            <div 
              v-for="deposit in depositHistory" 
              :key="deposit.id"
              class="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg group hover:bg-gray-100 transition-colors"
            >
              <div class="bg-primary/10 p-2 rounded-lg">
                <font-awesome-icon :icon="['fas', 'clock']" class="text-primary text-sm" />
              </div>
              <div class="flex-1">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-text">{{ formatCurrencyWithSymbol(deposit.amount, goal.currency, goal.currencySymbol) }}</span>
                  <div class="flex items-center space-x-2">
                    <span class="text-text-secondary text-sm">{{ formatDate(deposit.date) }}</span>
                    <!-- Undo Button - Only show for incomplete goals -->
                    <button 
                      v-if="!isStatusCompleted && activeTab !== 2"
                      @click="handleDeleteDeposit(deposit)"
                      :disabled="deletingDepositId === deposit.id"
                      class="opacity-0 group-hover:opacity-100 transition-opacity p-1.5 rounded-lg hover:bg-danger/10 text-danger hover:text-danger/80 disabled:opacity-50 disabled:cursor-not-allowed"
                      :class="{ 'opacity-100': deletingDepositId === deposit.id }"
                      title="Hoàn tác giao dịch này"
                    >
                      <div v-if="deletingDepositId === deposit.id" class="animate-spin rounded-full h-3 w-3 border border-danger border-t-transparent"></div>
                      <font-awesome-icon v-else :icon="['fas', 'undo']" class="text-xs" />
                    </button>
                  </div>
                </div>
                <div class="text-text-secondary text-sm" v-if="deposit.note">
                  {{ deposit.note }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- Empty State -->
          <div v-else-if="!isLoadingDeposits" class="text-center py-8 text-text-secondary">
            <div class="bg-gray-100 p-4 rounded-full w-16 h-16 mx-auto mb-3 flex items-center justify-center">
              <font-awesome-icon :icon="['fas', 'clock']" class="text-xl" />
            </div>
            <p>Chưa có lịch sử nạp tiền</p>
          </div>
          
          <!-- Pagination Controls -->
          <div v-if="depositPagination.totalPages > 1" class="flex items-center justify-center space-x-4 mt-4 pt-4 border-t border-gray-100">
            <!-- Previous Button -->
            <button 
              @click="handlePreviousPage"
              :disabled="!depositPagination.hasPrevious || isLoadingDeposits"
              class="flex items-center justify-center w-10 h-10 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :class="{ 'hover:border-primary': depositPagination.hasPrevious && !isLoadingDeposits }"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" class="text-sm" />
            </button>
            
            <!-- Page Info -->
            <span class="text-sm text-text-secondary px-4">
              {{ depositPagination.pageNumber }} / {{ depositPagination.totalPages }}
            </span>
            
            <!-- Next Button -->
            <button 
              @click="handleNextPage"
              :disabled="!depositPagination.hasNext || isLoadingDeposits"
              class="flex items-center justify-center w-10 h-10 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :class="{ 'hover:border-primary': depositPagination.hasNext && !isLoadingDeposits }"
            >
              <font-awesome-icon :icon="['fas', 'chevron-right']" class="text-sm" />
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3 sticky bottom-0">
        <!-- Only show Edit button for incomplete goals (not completed and not expired) -->
        <button 
          v-if="!isStatusCompleted && activeTab !== 2"
          @click="handleEdit" 
          class="px-4 py-2 bg-warning text-white rounded-lg hover:bg-warning/90 transition-colors flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'edit']" class="text-sm" />
          <span>Sửa</span>
        </button>
        <button 
          @click="handleDelete" 
          class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger/90 transition-colors flex items-center space-x-2"
        >
          <font-awesome-icon :icon="['fas', 'trash']" class="text-sm" />
          <span>Xoá</span>
        </button>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="isDeleteModalOpen" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]" @click.self="handleCancelDelete">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200">
          <div class="flex items-center space-x-3">
            <div class="bg-danger/10 p-2 rounded-lg">
              <font-awesome-icon :icon="['fas', 'trash']" class="text-danger text-lg" />
            </div>
            <h3 class="text-lg font-semibold text-text">Xác nhận xóa mục tiêu</h3>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4">
          <p class="text-text-secondary mb-4">
            Bạn có chắc chắn muốn xóa mục tiêu <strong class="text-text">"{{ goal?.name }}"</strong> không?
          </p>
          <div class="bg-warning/10 border border-warning/20 rounded-lg p-3 mb-4">
            <div class="flex items-center space-x-2">
              <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-warning" />
              <span class="text-warning text-sm font-medium">Cảnh báo</span>
            </div>
            <p class="text-warning text-sm mt-1">
              Hành động này không thể hoàn tác. Tất cả dữ liệu liên quan đến mục tiêu này sẽ bị xóa vĩnh viễn.
            </p>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <div class="text-sm text-text-secondary space-y-1">
              <div>• Số tiền đã tiết kiệm: <span class="font-medium text-text">{{ formatCurrencyWithSymbol(currentAmount || 0, goal?.currency, goal?.currencySymbol) }}</span></div>
              <div>• Mục tiêu: <span class="font-medium text-text">{{ formatCurrencyWithSymbol(goal?.targetAmount || 0, goal?.currency, goal?.currencySymbol) }}</span></div>
              <div>• Lịch sử giao dịch: <span class="font-medium text-text">{{ depositPagination.totalElements }} giao dịch</span></div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
          <button 
            @click="handleCancelDelete" 
            class="px-4 py-2 text-text-secondary hover:text-text transition-colors"
          >
            Hủy
          </button>
          <button 
            @click="handleConfirmDelete" 
            class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger/90 transition-colors flex items-center space-x-2"
          >
            <font-awesome-icon :icon="['fas', 'trash']" class="text-sm" />
            <span>Xác nhận xóa</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom styling for progress section */
.text-success {
  color: #059669;
}

.bg-success {
  background-color: #059669;
}

.bg-success\/90 {
  background-color: rgba(5, 150, 105, 0.9);
}

.bg-success\/10 {
  background-color: rgba(5, 150, 105, 0.1);
}

.border-success\/20 {
  border-color: rgba(5, 150, 105, 0.2);
}

.text-warning {
  color: #D97706;
}

.bg-warning {
  background-color: #D97706;
}

.bg-warning\/90 {
  background-color: rgba(217, 119, 6, 0.9);
}

.text-danger {
  color: #DC2626;
}

.bg-danger {
  background-color: #DC2626;
}

.bg-danger\/90 {
  background-color: rgba(220, 38, 38, 0.9);
}

.bg-danger\/10 {
  background-color: rgba(220, 38, 38, 0.1);
}

.bg-warning\/10 {
  background-color: rgba(217, 119, 6, 0.1);
}

.border-warning\/20 {
  border-color: rgba(217, 119, 6, 0.2);
}

/* Hover effects for undo button */
.text-danger\/80 {
  color: rgba(220, 38, 38, 0.8);
}

.hover\:bg-danger\/10:hover {
  background-color: rgba(220, 38, 38, 0.1);
}

.hover\:text-danger\/80:hover {
  color: rgba(220, 38, 38, 0.8);
}

/* Dropdown animation */
.goal-menu-dropdown div {
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 