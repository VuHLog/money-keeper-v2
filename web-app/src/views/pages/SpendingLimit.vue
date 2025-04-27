<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDictionaryExpenseStore } from '@/store/DictionaryExpenseStore'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import FilterOptions from '@/views/components/FilterOptions.vue'
import AddSpendingLimitModal from '../components/AddSpendingLimitModal.vue'
import EditSpendingLimitModal from '../components/EditSpendingLimitModal.vue'
import DeleteSpendingLimitModal from '../components/DeleteSpendingLimitModal.vue'
import SpendingLimitDetailModal from '../components/SpendingLimitDetailModal.vue'
import { useExpenseLimitStore } from '@/store/ExpenseLimitStore.js'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import Avatar from '@/views/components/Avatar.vue'
import { faPlus, faEdit, faTrash, faUtensils, faCar, faHome, faGamepad, faInfoCircle, faCoffee } from '@fortawesome/free-solid-svg-icons'
import Swal from 'sweetalert2'

library.add(faPlus, faEdit, faTrash, faUtensils, faCar, faHome, faGamepad, faInfoCircle, faCoffee)

const expenseLimitStore = useExpenseLimitStore()
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const dictionaryExpenseStore = useDictionaryExpenseStore()
const spendingLimits = ref([])
const categories = ref([])
const accounts = ref([])
onMounted(async () => {
  spendingLimits.value = await expenseLimitStore.getExpenseLimits()
  categories.value = await dictionaryExpenseStore.getMyExpenseCategoriesWithoutTransfer()
  accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments()
})

// Modal state
const showAddModal = ref(false)
const showEditModal = ref(false)
const selectedLimit = ref(null)
const showDeleteModal = ref(false)
const limitToDelete = ref(null)
const showDetailModal = ref(false)
const selectedLimitForDetail = ref(null)

// Methods
const handleFilterChange = (filters) => {
  console.log('Filters changed:', filters)
  // TODO: Apply filters to spending limits
}

const handleFilterReset = () => {
  console.log('Filters reset')
  // TODO: Reset spending limits to original state
}

const openAddLimitModal = () => {
  showAddModal.value = true
}

const editLimit = (limit) => {
  selectedLimit.value = limit
  showEditModal.value = true
}

const deleteLimit = (limit) => {
  limitToDelete.value = limit
  showDeleteModal.value = true
}

const handleConfirmDelete = () => {
  const index = spendingLimits.value.findIndex(item => item.id === limitToDelete.value.id)
  if (index !== -1) {
    expenseLimitStore.deleteExpenseLimit(limitToDelete.value.id)
    spendingLimits.value.splice(index, 1)

    // Hiển thị thông báo thành công
    Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      text: 'Xóa hạn mức chi thành công!',
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
      }
    })
  }

  // Đóng modal và reset state
  showDeleteModal.value = false
  limitToDelete.value = null
}

const handleAddLimit = async (formData) => {
  // Thêm vào đầu danh sách
  spendingLimits.value.unshift(formData)
  spendingLimits.value = spendingLimits.value.sort((a, b) => new Date(b.startDateLimit) - new Date(a.startDateLimit))

  // Đóng modal
  showAddModal.value = false

  // Hiển thị thông báo thành công
  Swal.fire({
    title: "Thành công",
    text: "Bạn đã thêm hạn mức chi thành công!",
    icon: "success",
  });
}

const handleEditLimit = (formData) => {
  const index = spendingLimits.value.findIndex(limit => limit.id === formData.id)

  if (index !== -1) {
    // Cập nhật vào danh sách
    spendingLimits.value[index] = formData
    spendingLimits.value = spendingLimits.value.sort((a, b) => new Date(b.startDateLimit) - new Date(a.startDateLimit))

    // Đóng modal
    showEditModal.value = false
    selectedLimit.value = null

    // Hiển thị thông báo thành công
    Swal.fire({
      title: "Thành công",
      text: "Bạn đã cập nhật hạn mức chi thành công!",
      icon: "success",
    });
  }
}

// Format currency
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

// Thêm computed property để tính toán số tiền đã chi
const calculateSpentAmount = (limit) => {
  // TODO: Thay thế bằng dữ liệu thực từ API
  // Giả lập dữ liệu chi tiêu
  return Math.floor(Math.random() * limit.amount * 1.2) // Tạm thời random để demo
}

// Thêm hàm format phần trăm
const formatPercentage = (spent, total) => {
  const percentage = (spent / total) * 100
  return Math.min(Math.round(percentage), 999) // Giới hạn tối đa 999%
}

// Thêm hàm tính toán style cho progress bar
const getProgressBarStyle = (spent, total) => {
  const percentage = (spent / total) * 100
  let color = 'var(--color-success)'

  if (percentage > 100) {
    color = 'var(--color-danger)'
  } else if (percentage >= 80) {
    color = 'var(--color-warning)'
  }

  return {
    width: `${Math.min(percentage, 100)}%`,
    backgroundColor: color
  }
}

// Thêm hàm lấy class cho status badge
const getStatusClass = (spent, total) => {
  const percentage = (spent / total) * 100
  let colorClass = 'bg-success/10 text-success'

  if (percentage > 100) {
    colorClass = 'bg-danger/10 text-danger'
  } else if (percentage >= 80) {
    colorClass = 'bg-warning/10 text-warning'
  }

  return ['text-xs font-medium px-2 py-0.5 rounded-full', colorClass]
}

const openDetailModal = (limit) => {
  selectedLimitForDetail.value = limit
  showDetailModal.value = true
}
</script>

<template>
  <div class="p-4">
    <!-- Filter Options -->
    <FilterOptions :show-time-range="false" :show-transaction-type="false" :show-account="true" :show-category-expense="true"
      @filter-change="handleFilterChange" @filter-reset="handleFilterReset" />

    <!-- Spending Limits Table -->
    <div class="mt-6 bg-white rounded-lg shadow-sm p-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium">Danh sách hạn mức chi</h2>
        <button @click="openAddLimitModal"
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/20">
          <font-awesome-icon :icon="['fas', 'plus']" class="mr-2" />
          Thêm hạn mức
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tên hạn mức
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thời gian
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Danh mục
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tài khoản
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Hạn mức
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Đã chi tiêu
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Lặp lại
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="limit in spendingLimits" :key="limit.id">
              <td class="px-6 py-4 whitespace-nowrap">
                {{ limit.name }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex flex-col">
                  <span class="text-sm">Từ: {{ limit.startDateLimit }}</span>
                  <span class="text-sm">Đến: {{ limit.endDateLimit }}</span>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex items-center space-x-2">
                  <!-- Hiển thị danh mục đầu tiên -->
                  <div v-if="limit.categories?.length > 0" class="flex items-center bg-gray-50 rounded-lg px-2 py-1">
                    <Avatar :src="limit.categories[0].iconUrl" :alt="limit.categories[0].name" size="m" class="mr-2" />
                    {{ limit.categories[0].name }}
                  </div>
                  <!-- Hiển thị số lượng danh mục còn lại -->
                  <div v-if="limit.categories?.length > 1" class="text-sm text-gray-500">
                    + {{ limit.categories.length - 1 }} danh mục khác
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex items-center space-x-2">
                  <!-- Hiển thị tài khoản đầu tiên -->
                  <div v-if="limit.bucketPayments?.length > 0"
                    class="flex items-center bg-gray-50 rounded-lg px-2 py-1">
                    <Avatar :src="limit.bucketPayments[0].iconUrl" :alt="limit.bucketPayments[0].name" size="m"
                      class="mr-2" />
                    {{ limit.bucketPayments[0].accountName }}
                  </div>
                  <!-- Hiển thị số lượng tài khoản còn lại -->
                  <div v-if="limit.bucketPayments?.length > 1" class="text-sm text-gray-500">
                    + {{ limit.bucketPayments.length - 1 }} tài khoản khác
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ formatCurrency(limit.amount) }}
              </td>
              <td class="px-6 py-4">
                <div class="space-y-2">
                  <!-- Số tiền đã chi -->
                  <div class="flex items-center justify-between">
                    <span class="text-sm font-medium">{{ formatCurrency(calculateSpentAmount(limit)) }}</span>
                    <div class="flex items-center space-x-2">
                      <span :class="getStatusClass(calculateSpentAmount(limit), limit.amount)">
                        {{ formatPercentage(calculateSpentAmount(limit), limit.amount) }}%
                        <span v-if="calculateSpentAmount(limit) > limit.amount" class="ml-1">(Bội chi)</span>
                      </span>
                      <button @click.stop="openDetailModal(limit)"
                        class="p-1 text-text-secondary hover:text-text rounded-full hover:bg-gray-100"
                        title="Xem chi tiết">
                        <font-awesome-icon :icon="['fas', 'info-circle']" />
                      </button>
                    </div>
                  </div>

                  <!-- Progress bar container -->
                  <div class="relative w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                    <!-- Progress bar background -->
                    <div class="absolute inset-0 bg-gray-50"></div>

                    <!-- Progress bar indicator -->
                    <div class="absolute h-full left-0 top-0 rounded-full transition-all duration-300 ease-in-out"
                      :style="getProgressBarStyle(calculateSpentAmount(limit), limit.amount)"></div>

                    <!-- Limit indicator line -->
                    <div class="absolute h-full w-0.5 bg-gray-400" style="left: 100%; transform: translateX(-50%);">
                    </div>
                  </div>

                  <!-- Legend -->
                  <div class="flex items-center justify-between text-xs text-gray-500">
                    <span>0 ₫</span>
                    <span>{{ formatCurrency(limit.amount) }}</span>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ limit.repeatTime }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <button @click="editLimit(limit)" class="text-primary hover:text-primary/80 mr-2">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                </button>
                <button @click="deleteLimit(limit)" class="text-red-500 hover:text-red-600">
                  <font-awesome-icon :icon="['fas', 'trash']" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <AddSpendingLimitModal :show="showAddModal" :categories="categories" :accounts="accounts"
      @close="showAddModal = false" @submit="handleAddLimit" />

    <EditSpendingLimitModal v-if="selectedLimit" :show="showEditModal" :categories="categories" :accounts="accounts"
      :limit="selectedLimit" @close="showEditModal = false" @submit="handleEditLimit" />

    <DeleteSpendingLimitModal :is-open="showDeleteModal" :limit="limitToDelete" @close="showDeleteModal = false"
      @confirm="handleConfirmDelete" />

    <SpendingLimitDetailModal :is-open="showDetailModal" :limit="selectedLimitForDetail"
      @close="showDetailModal = false" />
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

/* Form styles */
.form-group {
  @apply space-y-1;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-label.required::after {
  content: "*";
  @apply text-red-500 ml-1;
}

.form-input-wrapper {
  @apply relative;
}

.form-input,
.form-select {
  @apply w-full px-3 py-2 border border-gray-200 rounded-lg text-text placeholder-gray-400;
  @apply focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50;
  @apply disabled:bg-gray-100 disabled:cursor-not-allowed;
}

.form-input-icon {
  @apply absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none;
}

.btn {
  @apply px-4 py-2 rounded-lg text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2;
}

.btn-primary {
  @apply bg-primary text-white hover:bg-primary/90 focus:ring-primary/20;
}

.btn-secondary {
  @apply bg-gray-100 text-gray-700 hover:bg-gray-200 focus:ring-gray-200;
}

/* Thêm animation cho progress bar */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

/* Progress bar colors */
:root {
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;
}
</style>