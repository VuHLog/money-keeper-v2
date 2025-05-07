<script setup>
import { ref, onMounted, computed } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import Charts from '@/views/components/Charts.vue'
import { formatCurrency } from '@/utils/formatters'
import { useDashboardStore } from '@/store/DashboardStore'
import { useTransactionHistoryStore } from '@/store/TransactionHistoryStore'
import Avatar from '@/views/components/Avatar.vue'
import { formatDateToVietnam } from '@/utils/DateUtil'
import Swal from 'sweetalert2'

// Khởi tạo store
const dashboardStore = useDashboardStore()
const transactionHistoryStore = useTransactionHistoryStore()

// Thay thế các biến giả lập bằng dữ liệu thực từ API
const totalBalance = ref(0)
const totalIncome = ref(0)
const totalExpense = ref(0)
const totalSavings = computed(() => totalIncome.value - totalExpense.value)
const isLoading = ref(true)

// Recent transactions
const recentTransactions = ref([])

// Lấy dữ liệu dashboard từ API
const fetchDashboardData = async () => {
  try {
    isLoading.value = true
    
    // Gọi API lấy tổng số dư
    const balanceData = await dashboardStore.getMyTotalBalance()
    totalBalance.value = balanceData || 0
    
    // Gọi API lấy tổng thu nhập/chi tiêu tháng này
    const transactionData = await dashboardStore.getTotalExpenseRevenueThisMonth()
    
    if (transactionData) {
      totalIncome.value = transactionData.totalRevenue || 0
      totalExpense.value = transactionData.totalExpense || 0
    }
    
    // Lấy dữ liệu giao dịch gần đây
    await fetchRecentTransactions()
    
  } catch (error) {
    console.error('Lỗi khi lấy dữ liệu dashboard:', error)
    Swal.fire({
      icon: 'error',
      title: 'Lỗi!',
      text: 'Không thể tải dữ liệu dashboard',
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: 3000
    })
  } finally {
    isLoading.value = false
  }
}

// Lấy dữ liệu giao dịch gần đây
const fetchRecentTransactions = async () => {
  try {
    // Lưu lại giá trị pageSize hiện tại
    const currentPageSize = transactionHistoryStore.pagination.pageSize
    
    // Thiết lập kích thước trang nhỏ tạm thời để chỉ lấy vài giao dịch gần đây nhất
    transactionHistoryStore.pagination.pageSize = 5
    transactionHistoryStore.pagination.pageNumber = 1
    
    // Gọi API lấy giao dịch gần đây nhất
    const response = await transactionHistoryStore.getAllTransactionHistory({
      timeOption: '',
      transactionType: 'all',
      account: [],
      expenseCategory: [],
      revenueCategory: [],
      customTimeRange: null
    })
    
    // Kiểm tra xem transactionHistory đã tồn tại chưa trước khi gọi slice
    if (transactionHistoryStore.transactionHistory && Array.isArray(transactionHistoryStore.transactionHistory)) {
      recentTransactions.value = transactionHistoryStore.transactionHistory.slice(0, 5)
    } else {
      console.warn('transactionHistory không phải là mảng hoặc chưa được định nghĩa')
      recentTransactions.value = []
    }
    
    // Khôi phục lại giá trị pageSize ban đầu
    transactionHistoryStore.pagination.pageSize = currentPageSize
  } catch (error) {
    console.error('Lỗi khi lấy giao dịch gần đây:', error)
    recentTransactions.value = []
  }
}

// Xác định icon và màu sắc cho loại giao dịch
const getTransactionIcon = (type) => {
  if (type === 'expense') return 'arrow-down'
  if (type === 'revenue') return 'arrow-up'
  return 'exchange-alt'
}

const getTransactionIconClass = (type) => {
  if (type === 'expense') return 'text-danger'
  if (type === 'revenue') return 'text-success'
  return 'text-primary'
}

const getTransactionBgClass = (type) => {
  if (type === 'expense') return 'bg-danger/10'
  if (type === 'revenue') return 'bg-success/10'
  return 'bg-primary/10'
}

const getTransactionTextClass = (type) => {
  if (type === 'expense') return 'text-danger'
  if (type === 'revenue') return 'text-success'
  return 'text-primary'
}

const formatAmountWithSign = (amount, type) => {
  const formatted = formatCurrency(amount)
  if (type === 'expense') return '-' + formatted
  if (type === 'revenue') return '+' + formatted
  return formatted
}

// Gọi hàm fetch dữ liệu khi component được mount
onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="p-4 h-full">
    <div class="space-y-6">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Balance Card -->
        <router-link to="/accounts" class="block hover:no-underline">
          <div class="bg-surface rounded-xl p-4 shadow-sm transition-all duration-300 hover:shadow-md hover:translate-y-[-5px] cursor-pointer">
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <p class="text-text-secondary text-sm">Tổng số dư</p>
                <p v-if="!isLoading" class="text-2xl font-semibold text-text mt-1">{{ formatCurrency(totalBalance) }}</p>
                <p v-else class="text-2xl font-semibold text-text-secondary mt-1 animate-pulse">đang tải...</p>
              </div>
              <div class="bg-primary/10 p-3 rounded-lg flex-shrink-0">
                <font-awesome-icon :icon="['fas', 'wallet']" class="text-primary text-xl" />
              </div>
            </div>
          </div>
        </router-link>

        <!-- Income Card -->
        <router-link to="/transaction-history?transactionType=revenue&timeOption=month&month=current" class="block hover:no-underline">
          <div class="bg-surface rounded-xl p-4 shadow-sm transition-all duration-300 hover:shadow-md hover:translate-y-[-5px] cursor-pointer">
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <p class="text-text-secondary text-sm">Thu nhập tháng này</p>
                <p v-if="!isLoading" class="text-2xl font-semibold text-success mt-1">{{ formatCurrency(totalIncome) }}</p>
                <p v-else class="text-2xl font-semibold text-text-secondary mt-1 animate-pulse">đang tải...</p>
              </div>
              <div class="bg-success/10 p-3 rounded-lg flex-shrink-0">
                <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-success text-xl" />
              </div>
            </div>
          </div>
        </router-link>

        <!-- Expense Card -->
        <router-link to="/transaction-history?transactionType=expense&timeOption=month&month=current" class="block hover:no-underline">
          <div class="bg-surface rounded-xl p-4 shadow-sm transition-all duration-300 hover:shadow-md hover:translate-y-[-5px] cursor-pointer">
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <p class="text-text-secondary text-sm">Chi tiêu tháng này</p>
                <p v-if="!isLoading" class="text-2xl font-semibold text-danger mt-1">{{ formatCurrency(totalExpense) }}</p>
                <p v-else class="text-2xl font-semibold text-text-secondary mt-1 animate-pulse">đang tải...</p>
              </div>
              <div class="bg-danger/10 p-3 rounded-lg flex-shrink-0">
                <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-danger text-xl" />
              </div>
            </div>
          </div>
        </router-link>

        <!-- Savings Card -->
        <div class="bg-surface rounded-xl p-4 shadow-sm transition-all duration-300 hover:shadow-md hover:translate-y-[-5px]">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-text-secondary text-sm">Tiết kiệm tháng này</p>
              <p v-if="!isLoading" class="text-2xl font-semibold text-primary mt-1">{{ formatCurrency(totalSavings) }}</p>
              <p v-else class="text-2xl font-semibold text-text-secondary mt-1 animate-pulse">đang tải...</p>
            </div>
            <div class="bg-primary/10 p-3 rounded-lg flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'piggy-bank']" class="text-primary text-xl" />
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <Charts />

      <!-- Recent Transactions -->
      <div>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-text">Giao dịch gần đây</h3>
          <router-link to="/transaction-history" class="text-primary hover:text-primary/80 text-sm">
            Xem tất cả
          </router-link>
        </div>
        <div class="bg-surface rounded-xl shadow-sm">
          <div class="divide-y divide-gray-100">
            <!-- Hiển thị các giao dịch gần đây -->
            <div v-for="transaction in recentTransactions" :key="transaction.id"
                class="p-4 flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div :class="[getTransactionBgClass(transaction.transactionType), 'p-3 rounded-lg flex-shrink-0']">
                  <font-awesome-icon :icon="['fas', getTransactionIcon(transaction.transactionType)]" 
                    :class="getTransactionIconClass(transaction.transactionType)" />
                </div>
                <div class="flex-1">
                  <p class="font-medium flex text-text">
                    <Avatar v-if="transaction.categoryIconUrl" 
                            :src="transaction.categoryIconUrl" 
                            :alt="transaction.categoryName" 
                            size="m" 
                            class="mr-2" />
                    {{ transaction.categoryName || 'Không có danh mục' }}
                  </p>
                  <div class="flex items-center text-sm text-text-secondary">
                    <span>{{ transaction.accountName || 'Không có tài khoản' }}</span>
                    <span class="mx-1">•</span>
                    <span>{{ formatDateToVietnam(transaction.date) }}</span>
                  </div>
                </div>
              </div>
              <p :class="[getTransactionTextClass(transaction.transactionType), 'font-medium flex-shrink-0']">
                {{ formatAmountWithSign(transaction.amount, transaction.transactionType) }}
              </p>
            </div>
            
            <!-- Hiển thị khi không có dữ liệu -->
            <div v-if="recentTransactions.length === 0"
              class="py-8 flex flex-col items-center justify-center text-center">
              <div class="bg-gray-100 p-4 rounded-full mb-3">
                <font-awesome-icon :icon="['fas', 'receipt']" class="text-text-secondary text-xl" />
              </div>
              <p class="text-text-secondary">Chưa có giao dịch nào</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-white {
  background-color: #ffffff;
}
</style>