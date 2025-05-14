<script setup>
import { ref, computed, onMounted } from 'vue'
import FilterOptions from '@components/FilterOptions.vue'
import TransactionActionModal from '@components/TransactionActionModal.vue'
import TransactionDetailModal from '@components/TransactionDetailModal.vue'
import DeleteTransactionModal from '@components/DeleteTransactionModal.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays, faEdit, faTrash, faChevronLeft, faChevronRight, faFileExcel } from '@fortawesome/free-solid-svg-icons'

library.add(faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays, faEdit, faTrash, faChevronLeft, faChevronRight, faFileExcel)
import { useTransactionHistoryStore } from '@stores/TransactionHistoryStore'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import { useExpenseRegularStore } from '@/store/ExpenseRegularStore'
import { useRevenueRegularStore } from '@/store/RevenueRegularStore'
import Avatar from '@/views/components/Avatar.vue'
import ToastManager from '@/views/components/ToastManager.vue'

// Reference to ToastManager component
const toastManagerRef = ref(null)

const filters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: [],
  revenueCategory: [],
  customTimeRange: null
})

const excelFilters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: [],
  revenueCategory: [],
  customTimeRange: null
})

// Add new refs to hold initial filters
const initialFilters = ref({})

const transactionHistoryStore = useTransactionHistoryStore()
const expenseRegularStore = useExpenseRegularStore()
const revenueRegularStore = useRevenueRegularStore()
const isReset = ref(false)

// Data
const transactions = ref([])

// Computed properties for pagination
const paginationInfo = computed(() => {
  const start = ((transactionHistoryStore.pagination.pageNumber - 1) * transactionHistoryStore.pagination.pageSize) + 1
  const end = Math.min(start + transactionHistoryStore.pagination.pageSize - 1, transactionHistoryStore.pagination.totalElements)
  return {
    start,
    end,
    total: transactionHistoryStore.pagination.totalElements,
    currentPage: transactionHistoryStore.pagination.pageNumber,
    totalPages: transactionHistoryStore.pagination.totalPages
  }
})

// Modal states
const showActionModal = ref(false)
const showDetailModal = ref(false)
const showDeleteModal = ref(false)
const selectedTransaction = ref(null)
const modalMode = ref('view') // 'view', 'edit', 'delete'
const deletingTransaction = ref(null)

// Load data
onMounted(async () => {
  // Check for URL query parameters
  const urlParams = new URLSearchParams(window.location.search)
  const transactionType = urlParams.get('transactionType')
  const timeOption = urlParams.get('timeOption')
  const month = urlParams.get('month')

  if(transactionType || timeOption || month) {
    isReset.value = false
  }else{
    isReset.value = true
  }
  
  // Apply URL parameters to filters if they exist
  if (transactionType) {
    filters.value.transactionType = transactionType
    initialFilters.value.transactionType = transactionType
  }
  
  if (timeOption === 'month') {
    // In FilterOptions, the selectedTimeRange should be 'month' (id)
    // but the emitted timeOption will be 'Theo tháng' (name)
    initialFilters.value.timeOption = 'Theo tháng'
    filters.value.timeOption = 'Theo tháng'
    
    // If month=current, set customTimeRange to current month
    if (month === 'current') {
      const now = new Date()
      const currentMonth = now.getMonth() + 1 // JavaScript months are 0-indexed
      const currentYear = now.getFullYear()
      const monthString = `${currentYear}-${currentMonth.toString().padStart(2, '0')}` // Format YYYY-MM
      filters.value.customTimeRange = [monthString, monthString] // Format as array for date picker
      initialFilters.value.customTimeRange = [monthString, monthString]
    }
  }
  await loadData()
})

const loadData = async () => {
  await transactionHistoryStore.getAllTransactionHistory(filters.value)
  transactions.value = transactionHistoryStore.transactionHistory
}

// Methods
const handleFilterChange = (filterOptions) => {
  filters.value = {
    ...filters.value,
    ...filterOptions
  }
  console.log('Filters changed:', filters.value)
}

const handleFilterReset = async () => {
  // Reset filters
  filters.value = {
    timeOption: '',
    transactionType: 'all',
    account: [],
    customTimeRange: null,
    expenseCategory: [],
    revenueCategory: [],
  }
  
  // Reset URL params
  const currentPath = window.location.pathname
  window.history.replaceState({}, '', currentPath)
  initialFilters.value = {}
  
  transactionHistoryStore.resetPagination()
  await loadData()
}

const handleApplyFilter = async () => {
  excelFilters.value = filters.value
  transactionHistoryStore.resetPagination()
  await loadData()
}

const handlePageChange = async (newPage) => {
  transactionHistoryStore.pagination.pageNumber = newPage
  await loadData()
}

const openDetailModal = async (transaction) => {
  try {
    // Mở modal trước với dữ liệu hiện tại để tránh delay giao diện
    selectedTransaction.value = { ...transaction };
    showDetailModal.value = true;
    
    // Sau đó mới gọi API để lấy dữ liệu chi tiết
    let detailedTransaction = null;
    
    // Lấy dữ liệu chi tiết theo loại giao dịch
    if (transaction.transactionType === 'expense') {
      // Cho chi tiêu
      detailedTransaction = await expenseRegularStore.getExpenseRegularById(transaction.id);
    } else if (transaction.transactionType === 'revenue') {
      // Cho thu nhập
      detailedTransaction = await revenueRegularStore.getRevenueRegularById(transaction.id);
    }
    
    // Chỉ cập nhật dữ liệu chi tiết nếu modal vẫn đang mở và API trả về dữ liệu hợp lệ
    if (showDetailModal.value && detailedTransaction) {
      selectedTransaction.value = detailedTransaction;
      selectedTransaction.value.transactionType = transaction.transactionType; 
    }
    
  } catch (error) {
    console.error('Lỗi khi tải chi tiết giao dịch:', error);
    
    // Hiển thị thông báo lỗi với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Cảnh báo',
        content: 'Không thể tải đầy đủ chi tiết giao dịch'
      })
    }
  }
}

const closeDetailModal = () => {
  showDetailModal.value = false;
  // Đảm bảo xóa dữ liệu sau khi modal đóng để tránh lỗi khi mở lại
  // Đợi một chút để animation đóng hoàn thành
  setTimeout(() => {
    selectedTransaction.value = null;
  }, 300);
}

const openTransactionModal = (transaction, mode = 'view') => {
  // Nếu là chế độ xem chi tiết, sử dụng TransactionDetailModal
  if (mode === 'view') {
    openDetailModal(transaction)
    return
  }

  selectedTransaction.value = transaction
  modalMode.value = mode
  showActionModal.value = true
}

const closeTransactionModal = () => {
  selectedTransaction.value = null
  showActionModal.value = false
}

const handleEditTransaction = async (transaction) => {
  try {
    let detailedTransaction = null;
    
    // Gọi API để lấy thông tin chi tiết đầy đủ theo loại giao dịch
    if (transaction.transactionType === 'expense') {
      // Cho chi tiêu
      detailedTransaction = await expenseRegularStore.getExpenseRegularById(transaction.id);
    } else if (transaction.transactionType === 'revenue') {
      // Cho thu nhập
      detailedTransaction = await revenueRegularStore.getRevenueRegularById(transaction.id);
    }
    
    // Nếu lấy được dữ liệu chi tiết, sử dụng dữ liệu đó
    if (detailedTransaction) {
      selectedTransaction.value = detailedTransaction;
    } else {
      // Nếu không lấy được, sử dụng dữ liệu có sẵn
      selectedTransaction.value = transaction;
    }

    selectedTransaction.value.transactionType = transaction.transactionType;
    
    // Mở modal chỉnh sửa
    modalMode.value = 'edit';
    showActionModal.value = true;
    
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết giao dịch để chỉnh sửa:', error);
    
    // Nếu có lỗi, vẫn tiếp tục với dữ liệu hiện có
    selectedTransaction.value = transaction;
    modalMode.value = 'edit';
    showActionModal.value = true;
    
    // Hiển thị thông báo lỗi với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Cảnh báo',
        content: 'Không thể tải đầy đủ chi tiết giao dịch để chỉnh sửa'
      })
    }
  }
}

const handleDeleteTransaction = async (transaction) => {
  if (transaction.transferType === 'transfer') return;
  
  try {
    // Sau đó mới gọi API để lấy dữ liệu chi tiết
    let detailedTransaction = null;
    
    // Gọi API để lấy thông tin chi tiết đầy đủ theo loại giao dịch
    if (transaction.transactionType === 'expense') {
      // Cho chi tiêu
      detailedTransaction = await expenseRegularStore.getExpenseRegularById(transaction.id);
    } else if (transaction.transactionType === 'revenue') {
      // Cho thu nhập
      detailedTransaction = await revenueRegularStore.getRevenueRegularById(transaction.id);
    }
    
    // Nếu lấy được dữ liệu chi tiết, cập nhật modal
    selectedTransaction.value = detailedTransaction;
    deletingTransaction.value = detailedTransaction;
    selectedTransaction.value.transactionType = transaction.transactionType;
    deletingTransaction.value.transactionType = transaction.transactionType;
    showDeleteModal.value = true;
  } catch (error) {
    console.error('Lỗi khi tải chi tiết giao dịch để xóa:', error);
    
    // Hiển thị thông báo lỗi với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Cảnh báo',
        content: 'Không thể tải đầy đủ chi tiết giao dịch'
      })
    }
  }
}

const confirmDeleteTransaction = async () => {
  try {
    if (!selectedTransaction.value) return;
    
    // Xóa giao dịch dựa vào loại giao dịch (expense hoặc revenue)
    if (selectedTransaction.value.transactionType === 'expense') {
      await expenseRegularStore.deleteExpenseRegular(selectedTransaction.value.id);
    } else if (selectedTransaction.value.transactionType === 'revenue') {
      await revenueRegularStore.deleteRevenueRegular(selectedTransaction.value.id);
    }
    
    // Cập nhật lại dữ liệu
    await loadData();
    
    // Hiển thị thông báo thành công với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'success',
        title: 'Thành công!',
        content: 'Xóa giao dịch thành công!'
      })
    }
    
    // Đóng modal
    showDeleteModal.value = false;
    selectedTransaction.value = null;
    deletingTransaction.value = null;
  } catch (error) {
    console.error('Lỗi khi xóa giao dịch:', error);
    
    // Hiển thị thông báo lỗi với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Lỗi!',
        content: 'Xóa giao dịch thất bại!'
      })
    }
  }
}

const handleSaveTransaction = async (transaction) => {
  try {
    // Xử lý lưu thông tin giao dịch đã chỉnh sửa
    console.log('Lưu giao dịch:', transaction)

    // Gọi API cập nhật giao dịch từ store
    if(transaction.transactionType === 'expense') {
      await expenseRegularStore.updateExpenseRegular(transaction.id, transaction)
    } else if (transaction.transactionType === 'revenue') {
      await revenueRegularStore.updateRevenueRegular(transaction.id, transaction)
    }
    
    // Cập nhật lại dữ liệu
    await loadData()
    
    // Hiển thị thông báo thành công với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'success',
        title: 'Thành công!',
        content: 'Cập nhật giao dịch thành công!'
      })
    }
    
    // Đóng modal
    closeTransactionModal()
  } catch (error) {
    console.error(error)
    // Hiển thị thông báo lỗi với ToastManager
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Lỗi!',
        content: 'Cập nhật giao dịch thất bại!'
      })
    }
  }
}

// Format currency
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
// Format datetime
const formatDateTime = (datetime) => {
  return new Date(datetime).toLocaleString('vi-VN')
}

// Get category name based on transactionType
const getCategoryName = (transaction) => {
  return transaction.categoryName || 'Không có danh mục'
}

const getAccountName = (transaction) => {
  return transaction.accountName || 'Không có tài khoản'
}
// Xác định loại giao dịch
const getTransactionType = (type) => {
  switch (type?.toLowerCase()) {
    case 'expense':
      return 'Chi tiêu'
    case 'revenue':
      return 'Thu nhập'
    case 'transfer':
      return 'Chuyển khoản'
    default:
      return 'Không xác định'
  }
}

// Xác định class cho loại giao dịch
const getTransactionTypeClass = (type) => {
  switch (type?.toLowerCase()) {
    case 'expense':
      return 'text-danger'
    case 'revenue':
      return 'text-success'
    case 'transfer':
      return 'text-primary'
    default:
      return 'text-gray-500'
  }
}

// Xác định icon cho loại giao dịch
const getTransactionTypeIcon = (type) => {
  switch (type?.toLowerCase()) {
    case 'expense':
      return 'arrow-down'
    case 'revenue':
      return 'arrow-up'
    case 'transfer':
      return 'exchange-alt'
    default:
      return 'question'
  }
}

// Export Excel
const exportExcel = async () => {
  await transactionHistoryStore.exportExcel(excelFilters.value)
}
</script>

<template>
  <div class="p-4">
    <!-- Add ToastManager component -->
    <ToastManager ref="toastManagerRef" />
    
    <!-- Filters -->
    <FilterOptions
      :is-reset="isReset" 
      :show-expense-category="filters.transactionType === 'expense'"
      :show-revenue-category="filters.transactionType === 'revenue'"
      :default-open="false"
      :initial-filters="initialFilters"
      @filter-change="handleFilterChange"
      @filter-reset="handleFilterReset"
      @apply-filter="handleApplyFilter"
    />

    <!-- Header section with filters and export button -->
    <div class="flex justify-end items-center mb-4">
      <!-- Export Excel button -->
      <button
        class="flex items-center space-x-2 px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors duration-200"
        @click="exportExcel"
      >
        <font-awesome-icon :icon="['fas', 'file-excel']" />
        <span>Xuất Excel</span>
      </button>
    </div>

    <!-- Transactions List -->
    <div class="mt-6 bg-white rounded-lg shadow-sm">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Loại
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Số tiền
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Danh mục
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tài khoản
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ngày
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ghi chú
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="transaction in transactions" :key="transaction.id">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div :class="[
                    'p-2 rounded-lg flex-shrink-0 mr-2 flex items-center justify-center',
                    transaction.transactionType === 'expense' ? 'bg-danger/10' : 
                    transaction.transactionType === 'revenue' ? 'bg-success/10' : 
                    'bg-primary/10'
                  ]">
                    <font-awesome-icon 
                      :icon="['fas', getTransactionTypeIcon(transaction.transactionType)]"
                      :class="getTransactionTypeClass(transaction.transactionType)"
                    />
                  </div>
                  <span :class="getTransactionTypeClass(transaction.transactionType)">
                    {{ getTransactionType(transaction.transactionType) }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getTransactionTypeClass(transaction.transactionType)">
                  {{ formatCurrency(transaction.amount) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <Avatar v-if="transaction.categoryIconUrl" :src="transaction.categoryIconUrl" size="m" class="mr-2" />
                  <span>{{ getCategoryName(transaction) }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <Avatar v-if="transaction.bucketPaymentIconUrl" :src="transaction.bucketPaymentIconUrl" size="m" class="mr-2" />
                  <span>{{ getAccountName(transaction) }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ formatDateTime(transaction.date) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="truncate block max-w-[200px]" :title="transaction.interpretation">
                  {{ transaction.interpretation || 'Không có ghi chú' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <div class="flex justify-end gap-2">
                  <button
                    @click="openTransactionModal(transaction, 'view')"
                    class="text-primary hover:text-primary/80"
                    title="Xem chi tiết"
                  >
                    <font-awesome-icon :icon="['fas', 'eye']" />
                  </button>
                  <button
                    @click="transaction.transferType !== 'transfer' && handleEditTransaction(transaction)"
                    class="text-warning hover:text-warning/80"
                    :class="{'opacity-50 cursor-not-allowed': transaction.transferType === 'transfer'}"
                    :title="transaction.transferType === 'transfer' ? 'Không thể sửa giao dịch chuyển khoản' : 'Sửa giao dịch'"
                  >
                    <font-awesome-icon :icon="['fas', 'edit']" />
                  </button>
                  <button
                    @click="transaction.transferType !== 'transfer' && handleDeleteTransaction(transaction)"
                    class="text-danger hover:text-danger/80"
                    :class="{'opacity-50 cursor-not-allowed': transaction.transferType === 'transfer'}"
                    :title="transaction.transferType === 'transfer' ? 'Không thể xóa giao dịch chuyển khoản' : 'Xóa giao dịch'"
                  >
                    <font-awesome-icon :icon="['fas', 'trash']" />
                  </button>
                </div>
              </td>
            </tr>
            
            <!-- Hiển thị khi không có dữ liệu -->
            <tr v-if="transactions.length === 0">
              <td colspan="7" class="px-6 py-8 text-center text-gray-500">
                <div class="flex flex-col items-center justify-center">
                  <font-awesome-icon :icon="['fas', 'wallet']" class="text-3xl mb-2 text-gray-300" />
                  <p>Không có giao dịch nào</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div class="mt-4 flex justify-between items-center px-6 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ paginationInfo.total }} giao dịch
        </div>

        <!-- Pagination controls -->
        <div class="flex space-x-2">
          <!-- Previous button -->
          <button 
            @click="handlePageChange(paginationInfo.currentPage - 1)" 
            :disabled="paginationInfo.currentPage === 1"
            class="px-3 py-1 rounded-lg text-sm" 
            :class=" [
              paginationInfo.currentPage === 1 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]"
          >
            <font-awesome-icon :icon="['fas', 'chevron-left']" />
          </button>
          
          <!-- Page numbers -->
          <template v-for="pageNumber in paginationInfo.totalPages">
            <button 
              v-if="
                pageNumber === 1 || 
                pageNumber === paginationInfo.totalPages || 
                (pageNumber >= paginationInfo.currentPage - 1 && pageNumber <= paginationInfo.currentPage + 1)
              " 
              @click="handlePageChange(pageNumber)" 
              class="px-3 py-1 rounded-lg text-sm" 
              :class=" [
                pageNumber === paginationInfo.currentPage 
                  ? 'bg-primary text-white' 
                  : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
              ]"
              :key="pageNumber"
            >
              {{ pageNumber }}
            </button>
            
            <!-- Ellipsis -->
            <span 
              v-if="
                (pageNumber === 1 && paginationInfo.currentPage - 2 > 1) || 
                (pageNumber === paginationInfo.totalPages - 1 && paginationInfo.currentPage + 2 < paginationInfo.totalPages)
              " 
              class="px-2 py-1 text-gray-500"
              :key="`ellipsis-${pageNumber}`"
            >
              ...
            </span>
          </template>
          
          <!-- Next button -->
          <button 
            @click="handlePageChange(paginationInfo.currentPage + 1)" 
            :disabled="paginationInfo.currentPage === paginationInfo.totalPages"
            class="px-3 py-1 rounded-lg text-sm" 
            :class=" [
              paginationInfo.currentPage === paginationInfo.totalPages 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]"
          >
            <font-awesome-icon :icon="['fas', 'chevron-right']" />
          </button>
        </div>
      </div>
    </div>

    <!-- Sử dụng component TransactionActionModal -->
    <TransactionActionModal
      :transaction="selectedTransaction"
      :isOpen="showActionModal"
      :mode="modalMode"
      @close="closeTransactionModal"
      @edit="transaction => handleEditTransaction(transaction)"
      @save="transaction => handleSaveTransaction(transaction)"
    />

    <!-- Sử dụng component TransactionDetailModal -->
    <TransactionDetailModal
      :transaction="selectedTransaction"
      :isOpen="showDetailModal"
      :transactionType="selectedTransaction?.transactionType || 'expense'"
      @close="closeDetailModal"
    />

    <!-- Sử dụng component DeleteTransactionModal -->
    <DeleteTransactionModal
      :transaction="selectedTransaction"
      :transactionType="selectedTransaction?.transactionType || 'expense'"
      :isOpen="showDeleteModal"
      @close="closeTransactionModal"
      @confirm="confirmDeleteTransaction"
    />
  </div>
</template>

<style scoped>
.table-container {
  @apply overflow-x-auto;
}

table {
  @apply min-w-full divide-y divide-gray-200;
}

th {
  @apply px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider;
}

td {
  @apply px-6 py-4 whitespace-nowrap;
}

/* Thêm độ dài tối đa cho cột ghi chú để tránh làm hỏng layout */
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>