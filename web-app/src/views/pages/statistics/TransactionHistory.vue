<script setup>
import { ref } from 'vue'
import FilterOptions from '@components/FilterOptions.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faArrowUp,
  faArrowDown,
  faEye,
  faWallet,
  faBuildingColumns,
  faList
} from '@fortawesome/free-solid-svg-icons'

library.add(
  faArrowUp,
  faArrowDown,
  faEye,
  faWallet,
  faBuildingColumns,
  faList
)

// Sample data for transactions
const transactions = ref([
  {
    id: 1,
    type: 'income',
    amount: 5000000,
    category: 'Lương',
    account: 'Techcombank',
    date: '2024-04-01',
    note: 'Lương tháng 4'
  },
  {
    id: 2,
    type: 'expense',
    amount: 1500000,
    category: 'Ăn uống',
    account: 'Ví tiền mặt',
    date: '2024-04-02',
    note: 'Ăn uống trong tuần'
  }
])

// Modal state
const showModal = ref(false)
const selectedTransaction = ref(null)

// Methods
const handleFilterChange = (filters) => {
  console.log('Filters changed:', filters)
  // TODO: Apply filters to transactions
}

const handleFilterReset = () => {
  console.log('Filters reset')
  // TODO: Reset transactions to original state
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
</script>

<template>
  <div>
    <!-- Filters -->
    <FilterOptions
      :active-tab="'history'"
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
                    :icon="['fas', transaction.type === 'income' ? 'arrow-up' : 'arrow-down']"
                    :class="transaction.type === 'income' ? 'text-success' : 'text-red-500'"
                    class="mr-2"
                  />
                  <span :class="transaction.type === 'income' ? 'text-success' : 'text-red-500'">
                    {{ transaction.type === 'income' ? 'Thu nhập' : 'Chi tiêu' }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="transaction.type === 'income' ? 'text-success' : 'text-red-500'">
                  {{ formatCurrency(transaction.amount) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ transaction.category }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ transaction.account }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ formatDate(transaction.date) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ transaction.note }}
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
                :icon="['fas', selectedTransaction.type === 'income' ? 'arrow-up' : 'arrow-down']"
                :class="selectedTransaction.type === 'income' ? 'text-success' : 'text-red-500'"
                class="mr-2"
              />
              <span :class="selectedTransaction.type === 'income' ? 'text-success' : 'text-red-500'">
                {{ selectedTransaction.type === 'income' ? 'Thu nhập' : 'Chi tiêu' }}
              </span>
            </div>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Số tiền:</span>
            <span :class="[
              selectedTransaction.type === 'income' ? 'text-success' : 'text-red-500',
              'font-medium'
            ]">
              {{ formatCurrency(selectedTransaction.amount) }}
            </span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Danh mục:</span>
            <span>{{ selectedTransaction.category }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Tài khoản:</span>
            <span>{{ selectedTransaction.account }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Ngày:</span>
            <span>{{ formatDate(selectedTransaction.date) }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-gray-500">Ghi chú:</span>
            <span>{{ selectedTransaction.note }}</span>
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