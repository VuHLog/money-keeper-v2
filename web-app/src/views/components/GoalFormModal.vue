<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faWallet, faCalendarDays, faBullseye, faXmark, faChevronDown, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import { useFinancialGoalStore } from '@/store/FinancialGoalStore'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { formatCurrency } from '@/utils/formatters'
import { formatCurrencyWithSymbol } from '@/utils/formatters'
import Avatar from '@/views/components/Avatar.vue'
import { Currency } from "@constants/Currency.js"

library.add(faWallet, faCalendarDays, faBullseye, faXmark, faChevronDown, faExclamationTriangle)

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create', // 'create' or 'edit'
    validator: (value) => ['create', 'edit'].includes(value)
  },
  goal: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'save', 'success', 'error'])

// Stores
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const financialGoalStore = useFinancialGoalStore()
const currencies = ref(Currency)

// Data for form
const accounts = ref([])
const formData = ref({
  name: '',
  targetAmount: '',
  deadline: '',
  bucketPaymentId: '',
  interpretation: ''
})

const errors = ref({
  name: '',
  targetAmount: '',
  deadline: '',
  bucketPaymentId: ''
})

// Loading state
const isLoading = ref(false)

// Check if goal is completed (status = 1) - should not be editable
const isGoalCompleted = computed(() => {
  return props.mode === 'edit' && props.goal?.status === 1
})

// Load accounts when mounted
onMounted(async () => {
  try {
    accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments()
  } catch (error) {
    console.error('Error loading accounts:', error)
  }
})

// Populate form with goal data for editing
const populateFormWithGoal = (goal) => {
  console.log('Populating form with goal:', goal) // Debug log
  
  formData.value = {
    name: goal.name || '',
    targetAmount: goal.targetAmount || '',
    deadline: goal.deadline || '',
    // Extract bucketPaymentId from bucketPayment object or use direct field
    bucketPaymentId: goal.bucketPayment?.id || goal.bucketPaymentId || goal.accountId || '',
    interpretation: goal.interpretation || goal.description || ''
  }
  
  console.log('Form data populated:', formData.value) // Debug log
}

// Watch for goal changes to populate form in edit mode
watch(() => props.goal, (newGoal) => {
  if (newGoal && props.mode === 'edit') {
    populateFormWithGoal(newGoal)
  }
}, { immediate: true })

// Helper function to format currency based on currency code
const formatWithCurrency = (value, currencyCode, currencySymbol) => {
  if (value === null || value === undefined || value === '') return ''
  return formatCurrencyWithSymbol(Number(value), currencyCode, currencySymbol)
}

// Format currency for input based on selected account
const formattedAmount = computed({
  get: () => {
    if (!formData.value.targetAmount) return ''
    
    // Format using the currency of the selected account
    if (selectedAccount.value) {
      return formatWithCurrency(
        formData.value.targetAmount,
        selectedAccount.value.currency,
        selectedAccount.value.currencySymbol
      )
    }
    
    // Fallback to default VND format if no account is selected
    return formatCurrencyWithSymbol(Number(formData.value.targetAmount), 'VND', '₫')
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.value.targetAmount = numericValue ? Number(numericValue) : ''
  }
})

// Compute placeholder based on selected account currency
const amountPlaceholder = computed(() => {
  if (selectedAccount.value) {
    const symbol = selectedAccount.value.currencySymbol
    const currencyCode = selectedAccount.value.currency
    return currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
  }
  return '0 ₫' // Default placeholder
})

// Compute the selected account
const selectedAccount = computed(() => {
  return accounts.value.find(acc => acc.id === formData.value.bucketPaymentId)
})

// Add function to disable past dates
const disablePastDates = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000 // Allow today
}

// Add state for account dropdown
const isAccountDropdownOpen = ref(false)

// Reset form
const resetForm = () => {
  formData.value = {
    name: '',
    targetAmount: '',
    deadline: '',
    bucketPaymentId: '',
    interpretation: ''
  }
  errors.value = {
    name: '',
    targetAmount: '',
    deadline: '',
    bucketPaymentId: ''
  }
}

// Validate form before submitting
const validateForm = () => {
  let isValid = true
  errors.value = {
    name: '',
    targetAmount: '',
    deadline: '',
    bucketPaymentId: ''
  }

  // Validate name
  if (!formData.value.name.trim()) {
    errors.value.name = 'Vui lòng nhập tên mục tiêu'
    isValid = false
  } else if (formData.value.name.trim().length < 3) {
    errors.value.name = 'Tên mục tiêu phải có ít nhất 3 ký tự'
    isValid = false
  }

  // Validate target amount
  if (!formData.value.targetAmount) {
    errors.value.targetAmount = 'Vui lòng nhập số tiền cần đạt'
    isValid = false
  } else if (formData.value.targetAmount <= 0) {
    errors.value.targetAmount = 'Số tiền phải lớn hơn 0'
    isValid = false
  }

  // Validate deadline
  if (!formData.value.deadline) {
    errors.value.deadline = 'Vui lòng chọn hạn hoàn thành'
    isValid = false
  } else {
    const selectedDate = new Date(formData.value.deadline)
    const now = new Date()
    if (selectedDate <= now) {
      errors.value.deadline = 'Hạn hoàn thành phải sau ngày hiện tại'
      isValid = false
    }
  }

  // Validate account (only for create mode)
  if (props.mode === 'create' && !formData.value.bucketPaymentId) {
    errors.value.bucketPaymentId = 'Vui lòng chọn tài khoản'
    isValid = false
  }

  return isValid
}

// Handle close modal
const handleClose = () => {
  if (props.mode === 'create') {
    resetForm()
  }
  isLoading.value = false
  emit('close')
}

// Handle save button
const handleSave = async () => {
  if (!validateForm()) return
  
  isLoading.value = true
  
  try {
    const goalData = {
      name: formData.value.name.trim(),
      targetAmount: formData.value.targetAmount,
      deadline: formData.value.deadline,
      bucketPaymentId: formData.value.bucketPaymentId,
      interpretation: formData.value.interpretation.trim()
    }
    
    let response = null
    
    if (props.mode === 'create') {
      response = await financialGoalStore.createFinancialGoal(goalData)
    } else if (props.mode === 'edit' && props.goal) {
      response = await financialGoalStore.update(props.goal.id, goalData)
    }
    
    if (response) {
      emit('success', {
        mode: props.mode,
        goal: response,
        message: props.mode === 'create' ? 'Tạo mục tiêu thành công!' : 'Cập nhật mục tiêu thành công!'
      })
      
      if (props.mode === 'create') {
        resetForm()
      }
      emit('close')
    }
  } catch (error) {
    console.error('Error saving goal:', error)
    emit('error', {
      mode: props.mode,
      message: props.mode === 'create' ? 'Có lỗi xảy ra khi tạo mục tiêu!' : 'Có lỗi xảy ra khi cập nhật mục tiêu!'
    })
  } finally {
    isLoading.value = false
  }
}

// Đóng dropdown khi click outside
const handleClickOutside = (event) => {
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
    // Populate form if in edit mode and goal is provided
    if (props.mode === 'edit' && props.goal) {
      populateFormWithGoal(props.goal)
    }
  } else {
    document.removeEventListener('mousedown', handleClickOutside)
    if (props.mode === 'create') {
      resetForm()
    }
  }
})

// Cleanup on unmount
onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="handleClose">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-auto">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between sticky top-0 bg-white z-10">
        <div class="flex items-center space-x-3">
          <div class="bg-primary/10 p-2 rounded-lg">
            <font-awesome-icon :icon="['fas', 'bullseye']" class="text-primary text-lg" />
          </div>
          <h3 class="text-lg font-semibold text-text">
            {{ mode === 'create' ? 'Tạo mục tiêu mới' : 'Chỉnh sửa mục tiêu' }}
          </h3>
        </div>
        <button @click="handleClose" class="text-text-secondary hover:text-text transition-colors">
          <font-awesome-icon :icon="['fas', 'xmark']" class="text-xl" />
        </button>
      </div>

      <!-- Modal Content -->
      <div class="px-6 py-4">
        <!-- Warning for completed goals -->
        <div v-if="isGoalCompleted" class="mb-4 p-4 bg-warning/10 border border-warning/20 rounded-lg">
          <div class="flex items-center space-x-2">
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-warning" />
            <span class="text-warning font-medium">Cảnh báo</span>
          </div>
          <p class="text-warning text-sm mt-1">
            Mục tiêu này đã hoàn thành và không thể chỉnh sửa. Bạn chỉ có thể xem thông tin.
          </p>
        </div>
        
        <form @submit.prevent="handleSave" class="space-y-6">
          <!-- Goal Title -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Tên mục tiêu <span class="text-danger">*</span>
            </label>
            <input 
              v-model="formData.name" 
              type="text"
              :disabled="isGoalCompleted"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              :class="[
                errors.name ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                formData.name ? 'bg-white' : 'bg-gray-50',
                isGoalCompleted ? 'cursor-not-allowed bg-gray-100' : ''
              ]" 
              placeholder="Nhập tên mục tiêu (ví dụ: Du lịch Đà Nẵng, Mua xe máy...)" 
            />
            <p v-if="errors.name" class="mt-1 text-sm text-danger">
              {{ errors.name }}
            </p>
          </div>

          <!-- Target Amount -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Số tiền cần đạt <span class="text-danger">*</span>
            </label>
            <input 
              v-model="formattedAmount" 
              type="text"
              :disabled="isGoalCompleted"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
              :class="[
                errors.targetAmount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                formattedAmount ? 'bg-white' : 'bg-gray-50',
                isGoalCompleted ? 'cursor-not-allowed bg-gray-100' : ''
              ]" 
              :placeholder="amountPlaceholder" 
            />
            <p v-if="errors.targetAmount" class="mt-1 text-sm text-danger">
              {{ errors.targetAmount }}
            </p>
          </div>

          <!-- Deadline -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Hạn hoàn thành <span class="text-danger">*</span>
            </label>
            <div class="w-full">
              <el-date-picker 
                v-model="formData.deadline" 
                type="date" 
                :format="'DD/MM/YYYY'"
                :value-format="'YYYY-MM-DD'" 
                :placeholder="'Chọn ngày hoàn thành mục tiêu'" 
                :disabled-date="disablePastDates"
                :disabled="isGoalCompleted"
                class="date-picker-custom w-full" 
                :class="{ 'error-date-picker': errors.deadline }"
                style="width: 100%;" 
              />
              <p v-if="errors.deadline" class="mt-1 text-sm text-danger">
                {{ errors.deadline }}
              </p>
            </div>
          </div>

          <!-- Account Selection -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Chọn tài khoản <span class="text-danger">*</span>
            </label>
            <div class="relative select-none account-dropdown-container">
              <div
                class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg"
                :class="[
                  mode === 'edit' ? 'cursor-not-allowed bg-gray-100' : 'cursor-pointer hover:!border-gray-200',
                  isAccountDropdownOpen && mode === 'create' ? 'ring-1 ring-primary/20 border-primary/50' : '',
                  errors.bucketPaymentId ? 'border-danger/50' : ''
                ]" 
                @click="mode === 'create' && (isAccountDropdownOpen = !isAccountDropdownOpen)"
              >
                <div class="flex items-center flex-1">
                  <template v-if="selectedAccount">
                    <Avatar :src="selectedAccount.iconUrl" :alt="selectedAccount.accountName" size="m" class="mr-2" />
                    <span :class="mode === 'edit' ? 'text-gray-500' : ''">{{ selectedAccount.accountName }}</span>
                  </template>
                  <template v-else>
                    <font-awesome-icon :icon="['fas', 'wallet']" class="mr-2 text-lg text-gray-400" />
                    <span class="text-gray-400">Chọn tài khoản để tiết kiệm</span>
                  </template>
                </div>
                <font-awesome-icon 
                  v-if="mode === 'create'"
                  :icon="['fas', 'chevron-down']" 
                  class="text-gray-400 ml-2 transition-transform"
                  :class="{ 'rotate-180': isAccountDropdownOpen }" 
                />
                <span v-if="mode === 'edit'" class="text-xs text-gray-500 ml-2">(Không thể thay đổi)</span>
              </div>

              <div 
                v-if="isAccountDropdownOpen && mode === 'create'"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
              >
                <div 
                  v-for="account in accounts" 
                  :key="account.id"
                  class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                  :class="{ 'bg-primary/5': account.id === formData.bucketPaymentId }"
                  @click="formData.bucketPaymentId = account.id; isAccountDropdownOpen = false"
                >
                  <Avatar :src="account.iconUrl" :alt="account.accountName" size="m" class="mr-2" />
                  <div>
                    <div class="font-medium">{{ account.accountName }}</div>
                    <div class="text-xs text-text-secondary">
                      Số dư: {{ formatWithCurrency(account.balance, account.currency, account.currencySymbol) }}
                    </div>
                  </div>
                </div>
                <div v-if="accounts.length === 0" class="px-3 py-2 text-text-secondary text-sm">
                  Không có tài khoản
                </div>
              </div>
              <p v-if="errors.bucketPaymentId" class="mt-1 text-sm text-danger">
                {{ errors.bucketPaymentId }}
              </p>
            </div>
          </div>

          <!-- Description -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Mô tả
            </label>
            <textarea 
              v-model="formData.interpretation" 
              rows="3"
              :disabled="isGoalCompleted"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none transition-colors focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
              :class="[
                formData.interpretation && mode === 'create' ? 'bg-white' : mode === 'create' ? 'bg-gray-50' : '',
                isGoalCompleted ? 'cursor-not-allowed bg-gray-100' : ''
              ]" 
              :placeholder="mode === 'edit' ? '' : 'Mô tả chi tiết về mục tiêu của bạn...'"
            ></textarea>
          </div>
        </form>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3 sticky bottom-0">
        <button 
          @click="handleClose" 
          :disabled="isLoading"
          class="px-4 py-2 text-text-secondary hover:text-text transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Hủy
        </button>
        <button 
          @click="handleSave" 
          :disabled="isLoading"
          class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
        >
          <div v-if="isLoading" class="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></div>
          <span>
            {{ isGoalCompleted ? 'Đóng' : (mode === 'create' ? 'Tạo mục tiêu' : 'Cập nhật mục tiêu') }}
          </span>
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