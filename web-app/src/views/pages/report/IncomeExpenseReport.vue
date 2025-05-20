<template>
  <div class="income-expense-report">
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
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo thu chi</h2>
        <div class="flex items-center space-x-4">
          <div class="flex items-center">
            <label for="currencyUnit" class="mr-2 text-sm text-gray-600">Đơn vị:</label>
            <select 
              id="currencyUnit" 
              v-model="currencyUnit"
              class="form-select rounded-lg border-gray-300 text-sm focus:border-primary focus:ring focus:ring-primary/20"
            >
              <option value="1">Đồng</option>
              <option value="1000">Nghìn đồng</option>
              <option value="1000000">Triệu đồng</option>
              <option value="1000000000">Tỷ đồng</option>
            </select>
          </div>
          <button 
            @click="exportExcel" 
            class="px-3 py-2 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition-colors flex items-center text-sm"
          >
            <font-awesome-icon :icon="['fas', 'file-excel']" class="mr-2" />
            Xuất Excel
          </button>
        </div>
      </div>
      
      <!-- Loading state -->
      <div v-if="loading" class="py-10">
        <div class="flex justify-center">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
        </div>
      </div>
      <!-- Transactions List -->
    <div class="mt-6 bg-white rounded-lg shadow-sm">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ngày
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Số tiền
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Loại
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Danh mục
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tài khoản
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Loại chuyển khoản
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Người thụ hưởng
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Nhận tiền từ
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tài khoản thụ hưởng
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tài khoản gửi tiền
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Chuyến đi/sự kiện
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Địa điểm
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ghi chú
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="transaction in transactions" :key="transaction.id">
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                {{ formatDateTime(transaction.date) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-end">
                <span :class="getTransactionTypeClass(transaction.transactionType)" >
                  {{ formatCurrency(transaction.amount) }}
                </span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span :class="getTransactionTypeClass(transaction.transactionType)">
                    {{ getTransactionType(transaction.transactionType) }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ getCategoryName(transaction) }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ getAccountName(transaction) }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.transferType === 'normal' ? 'Thông thường' : 'Chuyển khoản' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.beneficiary || 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.collectMoneyWho || 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.beneficiaryAccountName? transaction.beneficiaryAccountName : 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.senderAccountName? transaction.senderAccountName : 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.tripEvent || 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <span>{{ transaction.location || 'Không có thông tin' }}</span>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <span class="truncate block max-w-[200px]" :title="transaction.interpretation">
                  {{ transaction.interpretation || 'Không có ghi chú' }}
                </span>
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
      
      <!-- Summary section -->
      <div class="mt-4 pt-4 border-t border-gray-100">
        <div class="grid grid-cols-3 gap-4">
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng thu</div>
            <div class="text-lg font-medium text-green-600">{{ formatCurrency(totalRevenue) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng chi</div>
            <div class="text-lg font-medium text-red-500">{{ formatCurrency(totalExpense) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Chênh lệch</div>
            <div class="text-lg font-medium" :class="{'text-green-600': totalRevenue - totalExpense >= 0, 'text-red-500': totalRevenue - totalExpense < 0}">
              {{ formatCurrency(totalRevenue - totalExpense) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import FilterOptions from '@components/FilterOptions.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays, faEdit, faTrash, faChevronLeft, faChevronRight, faFileExcel } from '@fortawesome/free-solid-svg-icons'

library.add(faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays, faEdit, faTrash, faChevronLeft, faChevronRight, faFileExcel)
import { useTransactionHistoryStore } from '@stores/TransactionHistoryStore'
import { useReportStore } from '@stores/ReportStore'
import Avatar from '@/views/components/Avatar.vue'
import { formatCurrency as baseFormatCurrency } from '@/utils/formatters.js'

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
const reportStore = useReportStore()
const isReset = ref(false)

// Data
const transactions = ref([])
const totalExpense = ref(0)
const totalRevenue = ref(0)

// Thêm state cho đơn vị tiền tệ
const currencyUnit = ref('1')

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
  transactionHistoryStore.pagination.pageSize = 10
  await loadData()
})

const loadData = async () => {
  await transactionHistoryStore.getAllTransactionHistory(filters.value)
  transactions.value = transactionHistoryStore.transactionHistory
  totalExpense.value = await reportStore.getTotalExpense(filters.value)
  totalRevenue.value = await reportStore.getTotalRevenue(filters.value)
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
// Format datetime
const formatDateTime = (datetime) => {
  return new Date(datetime).toLocaleString('vi-VN')
}

// Get category name based on transactionType
const getCategoryName = (transaction) => {
  return transaction.categoryName || 'Danh mục không xác định'
}

const getAccountName = (transaction) => {
  return transaction.accountName || 'Tài khoản không xác định'
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

// Export Excel
const exportExcel = async () => {
  await transactionHistoryStore.exportExcel(excelFilters.value)
}

// Format tiền tệ theo đơn vị đã chọn
const formatCurrency = (amount) => {
  const unitValue = parseInt(currencyUnit.value)
  
  if (unitValue === 1) {
    // Đồng - sử dụng format mặc định
    return baseFormatCurrency(amount)
  } else {
    // Các đơn vị khác
    const convertedAmount = amount / unitValue
    
    // Xác định đơn vị hiển thị
    let unitSuffix = ''
    if (unitValue === 1000) unitSuffix = ' nghìn'
    else if (unitValue === 1000000) unitSuffix = ' triệu'
    else if (unitValue === 1000000000) unitSuffix = ' tỷ'
    
    // Format số với 2 chữ số thập phân và thêm đơn vị
    return new Intl.NumberFormat('vi-VN', { 
      style: 'decimal',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(convertedAmount) + unitSuffix + ' ₫'
  }
}
</script>

<style scoped>
.income-expense-report {
  @apply transition-all duration-300;
}

/* Ensure smooth transitions for filter changes */
.income-expense-report > div {
  @apply transition-opacity duration-300;
}
</style> 