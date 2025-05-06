<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faWallet, faLocationDot, faCalendarDays, faArrowUp, faArrowDown, faXmark, faChevronDown, faList } from '@fortawesome/free-solid-svg-icons'
import { useDictionaryExpenseStore } from '@/store/DictionaryExpenseStore'
import { useDictionaryRevenueStore } from '@/store/DictionaryRevenueStore'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { formatCurrency } from '@/utils/formatters'
import Avatar from '@/views/components/Avatar.vue'

library.add(faWallet, faLocationDot, faCalendarDays, faArrowUp, faArrowDown, faXmark, faChevronDown, faList)

const props = defineProps({
  transaction: {
    type: Object,
    default: null
  },
  isOpen: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'edit',
  }
})

const emit = defineEmits(['close', 'save'])

// Stores
const dictionaryExpenseStore = useDictionaryExpenseStore()
const dictionaryRevenueStore = useDictionaryRevenueStore()
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()

// Data for edit form
const categories = ref([])
const accounts = ref([])
const formData = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: '',
  location: '',
  event: '',
  person: '',
  note: ''
})

const errors = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: ''
})

// Load data when mounted
onMounted(async () => {
  if (props.transaction) {
    loadDataForTransaction(props.transaction)
  }
})

// Watch for transaction changes
watch(() => props.transaction, async (newTransaction) => {
  if (newTransaction) {
    loadDataForTransaction(newTransaction)
  }
}, { deep: true })

// Load categories and accounts based on transaction type
const loadDataForTransaction = async (transaction) => {
  try {
    accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments()
    
    if (transaction.type === 'revenue') {
      categories.value = await dictionaryRevenueStore.getMyRevenueCategoriesWithoutTransfer()
    } else {
      categories.value = await dictionaryExpenseStore.getMyExpenseCategoriesWithoutTransfer()
    }
    
    // Populate form data for edit mode
    populateFormData(transaction)
  } catch (error) {
    console.error('Error loading data:', error)
  }
}

// Populate form data from transaction
const populateFormData = (transaction) => {
  // Xác định categoryId và accountId
  let accountId, categoryId
  
  // Lấy accountId
  if (transaction.dictionaryBucketPaymentId) {
    accountId = transaction.dictionaryBucketPaymentId
  } else if (transaction.dictionaryBucketPayment?.id) {
    accountId = transaction.dictionaryBucketPayment.id
  } else {
    accountId = ''
  }
  
  // Lấy categoryId dựa trên loại giao dịch
  if (transaction.type === 'revenue') {
    if (transaction.dictionaryRevenueId) {
      categoryId = transaction.dictionaryRevenueId
    } else if (transaction.dictionaryRevenue?.id) {
      categoryId = transaction.dictionaryRevenue.id
    } else {
      categoryId = ''
    }
  } else {
    if (transaction.dictionaryExpenseId) {
      categoryId = transaction.dictionaryExpenseId
    } else if (transaction.dictionaryExpense?.id) {
      categoryId = transaction.dictionaryExpense.id
    } else {
      categoryId = ''
    }
  }
  
  // Populate form
  formData.value = {
    amount: transaction.amount || 0,
    categoryId,
    accountId,
    date: transaction.type === 'revenue' ? transaction.revenueDate : transaction.expenseDate,
    location: transaction.location || '',
    event: transaction.tripEvent || '',
    person: transaction.type === 'revenue' ? 
      (transaction.collectMoneyWho || '') : 
      (transaction.beneficiary || ''),
    note: transaction.interpretation || ''
  }
}

// Format currency for input
const formattedAmount = computed({
  get: () => {
    if (!formData.value.amount) return ''
    return formatCurrency(Number(formData.value.amount))
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

// Compute the selected category
const selectedCategory = computed(() => {
  if (!formData.value.categoryId) {
    return { name: 'Chọn danh mục', icon: 'list', color: 'text-gray-400' }
  }
  return categories.value.find(cat => cat.id === formData.value.categoryId)
})

// Compute the selected account
const selectedAccount = computed(() => {
  return accounts.value.find(acc => acc.id === formData.value.accountId)
})

// Add function to disable future dates
const disableFutureDates = (time) => {
  return time.getTime() > Date.now()
}

// Add state for dropdowns
const isCategoryDropdownOpen = ref(false)
const isAccountDropdownOpen = ref(false)
const categorySearchQuery = ref('')

// Add computed for filtered categories
const filteredCategories = computed(() => {
  if (!categorySearchQuery.value) return categories.value
  const query = categorySearchQuery.value.toLowerCase()
  return categories.value.filter(cat =>
    cat.name.toLowerCase().includes(query)
  )
})

// Validate form before submitting
const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: ''
  }

  // Validate amount
  if (!formData.value.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền'
    isValid = false
  }

  // Validate category
  if (!formData.value.categoryId) {
    errors.value.categoryId = 'Vui lòng chọn danh mục'
    isValid = false
  }

  // Validate account
  if (!formData.value.accountId) {
    errors.value.accountId = 'Vui lòng chọn tài khoản'
    isValid = false
  }

  // Validate date
  if (!formData.value.date) {
    errors.value.date = 'Vui lòng chọn ngày'
    isValid = false
  } else {
    const selectedDate = new Date(formData.value.date)
    const now = new Date()
    if (selectedDate > now) {
      errors.value.date = 'Ngày không được lớn hơn ngày hiện tại'
      isValid = false
    }
  }

  return isValid
}

// Tính toán tiêu đề modal
const modalTitle = computed(() => {
  if (!props.transaction) return 'Chỉnh sửa giao dịch';
  
  const transactionType = props.transaction.type === 'revenue' ? 'thu' : 'chi';
  return `Chỉnh sửa giao dịch ${transactionType}`;
})

// Handle close modal
const handleClose = () => {
  emit('close')
}

// Handle save button
const handleSave = () => {
  if (!validateForm()) return;
  
  const updatedTransaction = { ...props.transaction };
  
  // Common fields for both expense and revenue
  updatedTransaction.amount = formData.value.amount;
  updatedTransaction.location = formData.value.location;
  updatedTransaction.interpretation = formData.value.note;
  updatedTransaction.tripEvent = formData.value.event;
  updatedTransaction.dictionaryBucketPaymentId = formData.value.accountId;
  
  // Type specific fields
  if (updatedTransaction.transactionType === 'revenue') {
    updatedTransaction.revenueDate = formData.value.date;
    updatedTransaction.dictionaryRevenueId = formData.value.categoryId;
    updatedTransaction.collectMoneyWho = formData.value.person;
  } else {
    updatedTransaction.expenseDate = formData.value.date;
    updatedTransaction.dictionaryExpenseId = formData.value.categoryId;
    updatedTransaction.beneficiary = formData.value.person;
  }
  updatedTransaction.id = props.transaction.id
  updatedTransaction.transactionType = props.transaction.transactionType;
  emit('save', updatedTransaction)
}

// Đóng dropdown khi click outside
const handleClickOutside = (event) => {
  // Close category dropdown
  const categoryDropdownEl = document.querySelector('.category-dropdown-container')
  if (categoryDropdownEl && !categoryDropdownEl.contains(event.target) && isCategoryDropdownOpen.value) {
    isCategoryDropdownOpen.value = false
  }
  
  // Close account dropdown
  const accountDropdownEl = document.querySelector('.account-dropdown-container')
  if (accountDropdownEl && !accountDropdownEl.contains(event.target) && isAccountDropdownOpen.value) {
    isAccountDropdownOpen.value = false
  }
}

// Add and remove event listener when the modal is open/closed
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    document.addEventListener('mousedown', handleClickOutside)
  } else {
    document.removeEventListener('mousedown', handleClickOutside)
  }
})

// Cleanup on unmount
onMounted(() => {
  if (props.isOpen) {
    document.addEventListener('mousedown', handleClickOutside)
  }
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="handleClose">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-auto">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between sticky top-0 bg-white z-10">
        <h3 class="text-lg font-semibold text-text">{{ modalTitle }}</h3>
        <button @click="handleClose" class="text-text-secondary hover:text-text transition-colors">
          <font-awesome-icon :icon="['fas', 'xmark']" class="text-xl" />
        </button>
      </div>

      <!-- Modal Content - Edit Mode -->
      <div class="px-6 py-4">
        <form @submit.prevent="handleSave" class="space-y-6">
          <!-- Amount Input -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Số tiền <span class="text-danger">*</span>
            </label>
            <input v-model="formattedAmount" type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              :class=" [
                errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                formattedAmount ? 'bg-white' : 'bg-gray-50'
              ]" placeholder="0 ₫" />
            <p v-if="errors.amount" class="mt-1 text-sm text-danger">
              {{ errors.amount }}
            </p>
          </div>

          <!-- Category Selection -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Danh mục <span class="text-danger">*</span>
            </label>
            <div class="relative select-none category-dropdown-container">
              <div
                class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:!border-gray-200"
                :class=" [
                  isCategoryDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                  errors.categoryId ? 'border-danger/50' : ''
                ]" @click="isCategoryDropdownOpen = !isCategoryDropdownOpen">
                <div class="flex items-center flex-1">
                  <Avatar v-if="selectedCategory && selectedCategory.iconUrl" :src="selectedCategory.iconUrl"
                    :alt="selectedCategory.name" size="m" class="mr-2" />
                  <span>{{ selectedCategory ? selectedCategory.name : 'Chọn danh mục' }}</span>
                </div>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                  :class="{ 'rotate-180': isCategoryDropdownOpen }" />
              </div>

              <div v-if="isCategoryDropdownOpen"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg divide-y divide-gray-100">
                <!-- Search input -->
                <div class="p-2">
                  <input v-model="categorySearchQuery" type="text"
                    class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                    :class="categorySearchQuery ? 'bg-white' : 'bg-gray-50'" placeholder="Tìm kiếm danh mục..."
                    @click.stop />
                </div>

                <!-- Category list with max height -->
                <div class="max-h-48 overflow-y-auto py-1">
                  <div v-for="category in filteredCategories" :key="category.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{ 'bg-primary/5': category.id === formData.categoryId }"
                    @click="formData.categoryId = category.id; isCategoryDropdownOpen = false">
                    <Avatar :src="category.iconUrl" :alt="category.name" size="m" class="mr-2" />
                    <span>{{ category.name }}</span>
                  </div>
                  <div v-if="filteredCategories.length === 0" class="px-3 py-2 text-text-secondary text-sm">
                    Không tìm thấy danh mục
                  </div>
                </div>
              </div>
              <p v-if="errors.categoryId" class="mt-1 text-sm text-danger">
                {{ errors.categoryId }}
              </p>
            </div>
          </div>

          <!-- Account Selection -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Tài khoản <span class="text-danger">*</span>
            </label>
            <div class="relative select-none account-dropdown-container">
              <div
                class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:!border-gray-200"
                :class=" [
                  isAccountDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                  errors.accountId ? 'border-danger/50' : ''
                ]" @click="isAccountDropdownOpen = !isAccountDropdownOpen">
                <div class="flex items-center flex-1">
                  <template v-if="selectedAccount">
                    <Avatar :src="selectedAccount.iconUrl" :alt="selectedAccount.accountName" size="m" class="mr-2" />
                    <span>{{ selectedAccount.accountName }}</span>
                  </template>
                  <template v-else>
                    <font-awesome-icon :icon="['fas', 'wallet']" class="mr-2 text-lg text-gray-400" />
                    <span>Chọn tài khoản</span>
                  </template>
                </div>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                  :class="{ 'rotate-180': isAccountDropdownOpen }" />
              </div>

              <div v-if="isAccountDropdownOpen"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1">
                <div v-for="account in accounts" :key="account.id"
                  class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                  :class="{ 'bg-primary/5': account.id === formData.accountId }"
                  @click="formData.accountId = account.id; isAccountDropdownOpen = false">
                  <Avatar :src="account.iconUrl" :alt="account.accountName" size="m" class="mr-2" />
                  <span>{{ account.accountName }}</span>
                </div>
                <div v-if="accounts.length === 0" class="px-3 py-2 text-text-secondary text-sm">
                  Không có tài khoản
                </div>
              </div>
              <p v-if="errors.accountId" class="mt-1 text-sm text-danger">
                {{ errors.accountId }}
              </p>
            </div>
          </div>

          <!-- Date Input -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Ngày {{ transaction.type === 'revenue' ? 'thu' : 'chi' }} <span class="text-danger">*</span>
            </label>
            <div class="w-full">
              <el-date-picker v-model="formData.date" type="datetime" :format="'DD/MM/YYYY HH:mm:ss'"
                :value-format="'YYYY-MM-DD HH:mm:ss'" :placeholder="'Chọn ngày'" :disabled-date="disableFutureDates"
                class="date-picker-custom w-full" :class="{ 'error-date-picker': errors.date }"
                style="width: 100%;" />
              <p v-if="errors.date" class="mt-1 text-sm text-danger">
                {{ errors.date }}
              </p>
            </div>
          </div>

          <!-- Location Input -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Địa điểm
            </label>
            <input v-model="formData.location" type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              placeholder="Nhập địa điểm" />
          </div>

          <!-- Event/Trip Input -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Chuyến đi/Sự kiện
            </label>
            <input v-model="formData.event" type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              placeholder="Nhập tên chuyến đi hoặc sự kiện" />
          </div>

          <!-- Person Input (Thu từ ai / Chi cho ai) -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              {{ transaction.type === 'revenue' ? 'Thu từ ai' : 'Chi cho ai' }}
            </label>
            <input v-model="formData.person" type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              :placeholder="transaction.type === 'revenue' ? 'Nhập người/tổ chức thu tiền' : 'Nhập người/tổ chức nhận tiền'" />
          </div>

          <!-- Description -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Mô tả
            </label>
            <textarea v-model="formData.note" rows="3"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
              :class="formData.note ? 'bg-white' : 'bg-gray-50'" placeholder="Nhập mô tả"></textarea>
          </div>
        </form>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3 sticky bottom-0">
        <button 
          @click="handleClose" 
          class="px-4 py-2 text-text-secondary hover:text-text"
        >
          Hủy
        </button>
        <button 
          @click="handleSave" 
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
        >
          Cập nhật
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Override Element Plus date picker styles */
.date-picker-custom :deep(.el-input__wrapper) {
  @apply !bg-transparent !shadow-none !box-border !border !border-gray-100 !rounded-lg hover:!border-gray-200;
  height: 42px !important; /* Match height of other inputs */
}

.date-picker-custom :deep(.el-input__wrapper.is-focus) {
  @apply !border-primary/50 !ring-1 !ring-primary/20;
}

.error-date-picker :deep(.el-input__wrapper) {
  @apply !border-danger/50;
}

.error-date-picker :deep(.el-input__wrapper.is-focus) {
  @apply !border-danger !ring-1 !ring-danger/20;
}

/* Remove input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>