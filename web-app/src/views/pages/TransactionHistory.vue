<script setup>
import { ref, computed, onMounted } from 'vue'
import FilterOptions from '@components/FilterOptions.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays } from '@fortawesome/free-solid-svg-icons'

library.add(faArrowUp, faArrowDown, faEye, faWallet, faBuildingColumns, faList, faLocationDot, faCalendarDays)

const filters = ref({})

// Updated transaction data structure based on expense and revenue tables
const transactions = ref([
  {
    id: 'rev-001',
    type: 'revenue',
    amount: 5000000,
    balance: 5000000,
    created_date: '2024-04-01T10:30:00',
    revenue_date: '2024-04-01T09:00:00',
    interpretation: 'Lương tháng 4',
    location: 'Công ty',
    modified_date: '2024-04-01T10:30:00',
    transfer_type: 'banking',
    dictionary_bucket_payment_id: 'bucket-001',
    dictionary_revenue_id: 'rev-cat-001',
    sender_account_id: 'acc-001',
    expense_regular_id: null,
    collect_money_who: 'Ngân hàng',
    trip_event: null
  },
  {
    id: 'exp-001',
    type: 'expense',
    amount: 1500000,
    balance: 3500000,
    created_date: '2024-04-02T14:20:00',
    expense_date: '2024-04-02T12:30:00',
    interpretation: 'Ăn trưa với đồng nghiệp',
    location: 'Nhà hàng ABC',
    modified_date: '2024-04-02T14:20:00',
    transfer_type: 'cash',
    beneficiary_account_id: null,
    dictionary_bucket_payment_id: 'bucket-002',
    dictionary_expense_id: 'exp-cat-001',
    beneficiary: 'Nhà hàng ABC',
    trip_event: null
  }
])

// Modal state
const showModal = ref(false)
const selectedTransaction = ref(null)

// Methods
const handleFilterChange = (filterOptions) => {
  filters.value = filterOptions
  console.log('Filters changed:', filterOptions)
  // Thực hiện lọc dữ liệu theo filters
}

const handleFilterReset = () => {
  filters.value = {}
  console.log('Filters reset')
  // Reset dữ liệu về mặc định
}

const openTransactionModal = (transaction) => {
  selectedTransaction.value = transaction
  showModal.value = true
}

const closeTransactionModal = () => {
  selectedTransaction.value = null
  showModal.value = false
}

// Format currency
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

// Format date
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN')
}

// Format datetime
const formatDateTime = (datetime) => {
  return new Date(datetime).toLocaleString('vi-VN')
}

// Get transaction date based on type
const getTransactionDate = (transaction) => {
  return transaction.type === 'revenue' ? transaction.revenue_date : transaction.expense_date
}

// Get category name based on dictionary_revenue_id or dictionary_expense_id
const getCategoryName = (transaction) => {
  // Thực tế sẽ lấy tên danh mục từ store hoặc API
  if (transaction.type === 'revenue') {
    return 'Danh mục thu: ' + transaction.dictionary_revenue_id
  } else {
    return 'Danh mục chi: ' + transaction.dictionary_expense_id
  }
}

// Get transaction account info
const getAccountInfo = (transaction) => {
  if (transaction.type === 'revenue') {
    return transaction.sender_account_id || 'N/A'
  } else {
    return transaction.beneficiary_account_id || transaction.beneficiary || 'N/A'
  }
}

// Get transaction related person
const getRelatedPerson = (transaction) => {
  return transaction.type === 'revenue' ? transaction.collect_money_who : transaction.beneficiary
}
</script>

<template>
  <div class="p-4">
    <!-- Filters -->
    <FilterOptions
      :active-tab="'history'"
      :is-reset="true"
      :show-expense-category="filters.transactionType === 'expense'"
      :show-revenue-category="filters.transactionType === 'revenue'"
      @filter-change="handleFilterChange"
      @filter-reset="handleFilterReset"
    />

    <!-- Transactions List -->
    <div class="mt-6">
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
                  <font-awesome-icon 
                    :icon="['fas', transaction.type === 'revenue' ? 'arrow-up' : 'arrow-down']"
                    :class="transaction.type === 'revenue' ? 'text-success' : 'text-red-500'"
                    class="mr-2"
                  />
                  <span :class="transaction.type === 'revenue' ? 'text-success' : 'text-red-500'">
                    {{ transaction.type === 'revenue' ? 'Thu nhập' : 'Chi tiêu' }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="transaction.type === 'revenue' ? 'text-success' : 'text-red-500'">
                  {{ formatCurrency(transaction.amount) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ getCategoryName(transaction) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ getAccountInfo(transaction) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ formatDateTime(getTransactionDate(transaction)) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ transaction.interpretation }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <button
                  @click="openTransactionModal(transaction)"
                  class="text-primary hover:text-primary/80"
                >
                  <font-awesome-icon :icon="['fas', 'eye']" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Transaction Detail Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeTransactionModal"
    >
      <div class="bg-white rounded-lg p-6 max-w-lg w-full mx-4">
        <h3 class="text-lg font-medium mb-4">Chi tiết giao dịch</h3>
        
        <div v-if="selectedTransaction" class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-gray-500">Loại giao dịch:</span>
            <div class="flex items-center">
              <font-awesome-icon 
                :icon="['fas', selectedTransaction.type === 'revenue' ? 'arrow-up' : 'arrow-down']"
                :class="selectedTransaction.type === 'revenue' ? 'text-success' : 'text-red-500'"
                class="mr-2"
              />
              <span :class="selectedTransaction.type === 'revenue' ? 'text-success' : 'text-red-500'">
                {{ selectedTransaction.type === 'revenue' ? 'Thu nhập' : 'Chi tiêu' }}
              </span>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Số tiền:</span>
            <span :class="[selectedTransaction.type === 'revenue' ? 'text-success' : 'text-red-500', 'font-medium']">
              {{ formatCurrency(selectedTransaction.amount) }}
            </span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Số dư:</span>
            <span class="font-medium">
              {{ formatCurrency(selectedTransaction.balance) }}
            </span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Danh mục:</span>
            <span>{{ getCategoryName(selectedTransaction) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Hình thức chuyển khoản:</span>
            <span>{{ selectedTransaction.transfer_type }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Tài khoản:</span>
            <span>{{ getAccountInfo(selectedTransaction) }}</span>
          </div>

          <div v-if="getRelatedPerson(selectedTransaction)" class="flex items-center justify-between">
            <span class="text-gray-500">
              {{ selectedTransaction.type === 'revenue' ? 'Người thu tiền:' : 'Người nhận:' }}
            </span>
            <span>{{ getRelatedPerson(selectedTransaction) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">
              {{ selectedTransaction.type === 'revenue' ? 'Ngày thu:' : 'Ngày chi:' }}
            </span>
            <span>{{ formatDateTime(getTransactionDate(selectedTransaction)) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Ngày tạo:</span>
            <span>{{ formatDateTime(selectedTransaction.created_date) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Ngày chỉnh sửa:</span>
            <span>{{ formatDateTime(selectedTransaction.modified_date) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Địa điểm:</span>
            <div class="flex items-center">
              <font-awesome-icon :icon="['fas', 'location-dot']" class="mr-2 text-primary" />
              <span>{{ selectedTransaction.location || 'Không có' }}</span>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Ghi chú:</span>
            <span>{{ selectedTransaction.interpretation }}</span>
          </div>

          <div v-if="selectedTransaction.trip_event" class="flex items-center justify-between">
            <span class="text-gray-500">Sự kiện/Chuyến đi:</span>
            <span>{{ selectedTransaction.trip_event }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">ID:</span>
            <span class="text-xs text-gray-500">{{ selectedTransaction.id }}</span>
          </div>
        </div>

        <div class="mt-6 flex justify-end">
          <button
            @click="closeTransactionModal"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
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

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
}

.modal-content {
  @apply bg-white rounded-lg p-6 max-w-lg w-full mx-4;
}
</style>