<script setup>
import { ref, computed, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useExpenseLimitStore } from '@stores/ExpenseLimitStore.js'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { formatCurrency, formatCurrencyWithSymbol } from '@/utils/formatters'
import { faTimes, faChevronDown, faCalendar, faList, faUtensils, faShoppingBag, faHome, faTaxi, faTshirt, faHeartbeat, faGraduationCap, faRepeat, faClock, faCalendarDay, faCalendarWeek, faCalendarAlt } from '@fortawesome/free-solid-svg-icons'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import Swal from 'sweetalert2'
import SelectDropdown from '@components/SelectDropdown.vue'
import { formatDate } from '@utils/DateUtil'
import { Currency } from "@constants/Currency.js"


library.add(faTimes, faChevronDown, faCalendar, faList, faUtensils, faShoppingBag, faHome, faTaxi, faTshirt, faHeartbeat, faGraduationCap, faRepeat, faClock, faCalendarDay, faCalendarWeek, faCalendarAlt)

const expenseLimitStore = useExpenseLimitStore()
const currencies = ref(Currency)
const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  categories: {
    type: Array,
    required: true
  },
  accounts: {
    type: Array,
    required: true
  },
  limit: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'submit'])

const formData = reactive({
  name: '',
  category_ids: [],
  account_ids: '',
  amount: '',
  start_date: '',
  end_date: '',
  repeat_time: ''
})

const errors = ref({
  name: '',
  category_ids: '',
  account_ids: '',
  amount: '',
  start_date: '',
  end_date: ''
})

// Add helper function to format date consistently
const formatDateForForm = (dateValue) => {
  if (!dateValue) return '';
  
  // Nếu đã là chuỗi, xử lý chuỗi
  if (typeof dateValue === 'string') {
    // Nếu đã có định dạng đầy đủ với thời gian, trả về luôn
    if (dateValue.includes(' ')) return dateValue;
    // Nếu chỉ có ngày, thêm thời gian
    return `${dateValue} 00:00:00`;
  }
  
  // Nếu là đối tượng Date, chuyển sang chuỗi định dạng yyyy-mm-dd hh:mm:ss
  if (dateValue instanceof Date) {
    const year = dateValue.getFullYear();
    // Tháng bắt đầu từ 0, nên cần +1 và đảm bảo luôn có 2 chữ số
    const month = String(dateValue.getMonth() + 1).padStart(2, '0');
    const day = String(dateValue.getDate()).padStart(2, '0');
    return `${year}-${month}-${day} 00:00:00`;
  }
  
  // Trả về rỗng nếu không xử lý được
  return '';
}

// Xử lý sự kiện khi người dùng chọn ngày
const handleStartDateChange = (date) => {
  formData.start_date = formatDateForForm(date);
}

const handleEndDateChange = (date) => {
  formData.end_date = formatDateForForm(date);
}

// Helper function to format currency based on currency code
const formatWithCurrency = (value, currencyCode, currencySymbol) => {
  if (value === null || value === undefined || value === '') return ''
  const numberValue = Number(value)
  
  // Format the number with thousand separators
  const formattedValue = new Intl.NumberFormat().format(numberValue).replace(/,/g, '.')
  
  // Return with the proper currency symbol
  if (currencyCode === 'VND') {
    return `${formattedValue} ${currencySymbol || '₫'}`
  } else {
    return `${currencySymbol || ''}${formattedValue}`
  }
}

// Get selected account
const selectedAccount = computed(() => {
  return props.accounts.find(acc => acc.id === formData.account_ids)
})

// Format amount with currency based on selected account
const formattedAmount = computed({
  get: () => {
    if (!formData.amount) return ''
    
    // Format using the currency of the selected account
    if (selectedAccount.value) {
      return formatWithCurrency(
        formData.amount,
        selectedAccount.value.currency,
        selectedAccount.value.currencySymbol
      )
    }
    
    // Fallback to default VND format if no account is selected
    return formatCurrency(Number(formData.amount))
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.amount = numericValue ? Number(numericValue) : ''
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

// Update watch for limit prop
watch(() => props.limit, (newLimit) => {
  if (newLimit) {
    formData.name = newLimit.name || '';
    formData.category_ids = Array.isArray(newLimit.categories) 
      ? newLimit.categories.map(cat => cat.id)
      : [];
    formData.account_ids = newLimit.bucketPayments.id || '';
    formData.amount = newLimit.amount || '';
    formData.start_date = formatDateForForm(newLimit.startDate);
    formData.end_date = formatDateForForm(newLimit.endDate);
    formData.repeat_time = newLimit.repeatTime || '';
  }
}, { immediate: true, deep: true })

// Repeat options dropdown
const isRepeatDropdownOpen = ref(false)

const repeatOptions = [
  { value: 'Không lặp lại', label: 'Không lặp lại', icon: 'repeat', color: 'text-gray-400' },
  { value: 'Hàng ngày', label: 'Hàng ngày', icon: 'clock', color: 'text-blue-500' },
  { value: 'Hàng tuần', label: 'Hàng tuần', icon: 'calendar-day', color: 'text-green-500' },
  { value: 'Hàng tháng', label: 'Hàng tháng', icon: 'calendar-week', color: 'text-purple-500' },
  { value: 'Hàng năm', label: 'Hàng năm', icon: 'calendar-alt', color: 'text-red-500' }
]

const selectedRepeatOption = computed(() => {
  return repeatOptions.find(opt => opt.value === formData.repeat_time) || repeatOptions[3]
})

const handleRepeatSelect = (option) => {
  formData.repeat_time = option.value
  isRepeatDropdownOpen.value = false
}

const toggleRepeatDropdown = (event) => {
  event.stopPropagation()
  isRepeatDropdownOpen.value = !isRepeatDropdownOpen.value
}

// Add isConfirmingClose ref
const isConfirmingClose = ref(false)

// Update close modal
const handleClose = () => {
  // Normalize values for comparison
  const formStartDate = normalizeDateForComparison(formData.start_date);
  const formEndDate = normalizeDateForComparison(formData.end_date);
  const propsStartDate = normalizeDateForComparison(props.limit.startDate);
  const propsEndDate = normalizeDateForComparison(props.limit.endDate);
  
  // Convert amount to string for comparison to avoid type issues
  const formAmount = String(formData.amount);
  const propsAmount = String(props.limit.amount);
  
  // Kiểm tra xem form có thay đổi không
  const hasChanges = 
    formData.name !== props.limit.name ||
    !isArrayEqual(formData.category_ids, Array.isArray(props.limit.categories) 
      ? props.limit.categories.map(cat => cat.id)
      : []) ||
    formData.account_ids !== props.limit.bucketPayments.id ||
    formAmount !== propsAmount ||
    formStartDate !== propsStartDate ||
    formEndDate !== propsEndDate ||
    formData.repeat_time !== props.limit.repeatTime

  if (hasChanges && !isConfirmingClose.value) {
    isConfirmingClose.value = true
    return
  }
  resetAndClose()
}

const continueEditing = () => {
  isConfirmingClose.value = false
}

const resetAndClose = () => {
  resetForm()
  isRepeatDropdownOpen.value = false
  isConfirmingClose.value = false
  emit('close')
}

// Add helper function to compare arrays
const isArrayEqual = (arr1, arr2) => {
  if (!Array.isArray(arr1) || !Array.isArray(arr2)) return false
  if (arr1.length !== arr2.length) return false
  return arr1.every((item, index) => item === arr2[index])
}

// Add reset form function
const resetForm = () => {
  if (!props.limit) return

  formData.name = props.limit.name || '';
  formData.category_ids = Array.isArray(props.limit.categories)
    ? props.limit.categories.map(cat => cat.id)
    : [];
  formData.account_ids = props.limit.bucketPayments.id || '';
  formData.amount = props.limit.amount || '';
  formData.start_date = formatDateForForm(props.limit.startDate);
  formData.end_date = formatDateForForm(props.limit.endDate);
  formData.repeat_time = props.limit.repeatTime || '';
  
  errors.value = {
    name: '',
    category_ids: '',
    account_ids: '',
    amount: '',
    start_date: '',
    end_date: ''
  }
}

// Update click outside handler
const handleClickOutside = (event) => {
  const repeatDropdownEl = document.querySelector('.repeat-dropdown-container')
  const modalContentEl = document.querySelector('.modal-content')
  const dialogEl = document.querySelector('.confirmation-dialog')

  // Close repeat dropdown if click is outside
  if (repeatDropdownEl && !repeatDropdownEl.contains(event.target) && isRepeatDropdownOpen.value) {
    isRepeatDropdownOpen.value = false
    return
  }

  // Close modal if click is outside modal content and dialog
  if (!modalContentEl?.contains(event.target) && !dialogEl?.contains(event.target)) {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

const handleSubmit = async () => {
  if (!validateForm()) return
  
  // Tạo object data để submit
  const submitData = {
    id: props.limit.id,
    amount: Number(formData.amount),
    name: formData.name,
    categoriesId: formData.category_ids.join(','),
    bucketPaymentIds: formData.account_ids,
    startDate: formatDate(formData.start_date),
    endDate: formData.end_date ? formatDate(formData.end_date) : null,
    repeatTime: formData.repeat_time,
  }

  submitData.startDateLimit = (expenseLimitStore.getCurrentStartDate(submitData));
  submitData.endDateLimit = (expenseLimitStore.getEndDate(submitData.startDateLimit, submitData.repeatTime, submitData.endDate || null));

  try{
    // Gọi API cập nhật
    let response = await expenseLimitStore.updateExpenseLimit(submitData);
    await emit('submit', response)
    resetAndClose()
  } catch (error) {
    if(error.response?.data?.code === 9002){
      errors.value.end_date = "Ngày kết thúc phải lớn hơn " + error.response.data.message;
    } else {
      console.log(error);
    }
  }
}

// Update validation
const validateForm = () => {
  let isValid = true
  errors.value = {
    name: '',
    category_ids: '',
    account_ids: '',
    amount: '',
    start_date: '',
    end_date: ''
  }

  if (!formData.name.trim()) {
    errors.value.name = 'Vui lòng nhập tên hạn mức'
    isValid = false
  }

  if (!formData.category_ids.length || (formData.category_ids.length === 1 && formData.category_ids[0] === 'all')) {
    errors.value.category_ids = 'Vui lòng chọn ít nhất một danh mục'
    isValid = false
  }

  if (!formData.account_ids) {
    errors.value.account_ids = 'Vui lòng chọn ít nhất một tài khoản'
    isValid = false
  }

  if (!formData.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền'
    isValid = false
  }

  if (!formData.start_date) {
    errors.value.start_date = 'Vui lòng chọn ngày bắt đầu'
    isValid = false
  }

  if (formData.start_date && formData.end_date) {
    const start = new Date(formData.start_date)
    const end = new Date(formData.end_date)
    if (end < start) {
      errors.value.end_date = 'Ngày kết thúc phải sau ngày bắt đầu'
      isValid = false
    }
  }

  if (formData.end_date) {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const end = new Date(formData.end_date)
    if (end < today) {
      errors.value.end_date = 'Ngày kết thúc phải lớn hơn hoặc bằng ngày hiện tại'
      isValid = false
    }
  }

  if (formData.repeat_time === 'Không lặp lại' && !formData.end_date) {
        errors.value.end_date = 'Vui lòng chọn ngày kết thúc'
        isValid = false
    }

  return isValid
}

// Add helper to normalize dates for comparison
const normalizeDateForComparison = (dateStr) => {
  if (!dateStr) return '';
  
  // If the date string contains time part (space followed by digits), extract only the date part
  if (dateStr.includes(' ')) {
    return dateStr.split(' ')[0].trim();
  }
  
  // Handle date strings in format YYYY-MM-DD or DD/MM/YYYY
  return dateStr.trim();
}
</script>

<template>
    <Teleport to="body">
      <div 
        v-if="show"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="handleClose"
      >
        <div 
          class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 relative modal-content"
          @click.stop
        >
          <!-- Confirmation Dialog for Unsaved Changes -->
          <div 
            v-if="isConfirmingClose"
            class="absolute inset-0 bg-white rounded-lg z-10 flex flex-col confirmation-dialog"
            @click.stop
          >
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-semibold text-text">Xác nhận hủy chỉnh sửa</h3>
            </div>
            
            <div class="px-6 py-4 flex-1">
              <div class="bg-warning/5 border border-warning/10 rounded-lg p-4">
                <div class="flex items-start space-x-3">
                  <div class="mt-0.5">
                    <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-warning" />
                  </div>
                  <div>
                    <p class="text-text">Bạn có thay đổi chưa được lưu</p>
                    <p class="text-sm text-text-secondary mt-1">Các thay đổi sẽ bị mất nếu bạn đóng form mà không lưu.</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
              <button 
                @click="continueEditing"
                class="px-4 py-2 text-text-secondary hover:text-text"
              >
                Tiếp tục chỉnh sửa
              </button>
              <button 
                @click="resetAndClose"
                class="px-4 py-2 bg-warning text-white rounded-lg hover:bg-warning/90"
              >
                Hủy thay đổi
              </button>
            </div>
          </div>

          <!-- Modal Header -->
          <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
            <h3 class="text-lg font-semibold text-text">Chỉnh sửa hạn mức</h3>
            <button 
              @click="handleClose"
              class="text-text-secondary hover:text-text"
            >
              <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
            </button>
          </div>
  
          <!-- Modal Content -->
          <div class="px-6 py-4">
            <form @submit.prevent="handleSubmit" class="space-y-4">
              <!-- Số tiền -->
              <div>
                <label class="block text-sm font-medium text-text-secondary mb-1">
                  Số tiền <span class="text-danger">*</span>
                </label>
                <input 
                  v-model="formattedAmount"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                  :class="[
                    errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                    formattedAmount ? 'bg-white' : 'bg-gray-50'
                  ]"
                  :placeholder="amountPlaceholder"
                />
                <p v-if="errors.amount" class="mt-1 text-sm text-danger">
                  {{ errors.amount }}
                </p>
              </div>
  
              <!-- Tên hạn mức -->
              <div>
                <label class="block text-sm font-medium text-text-secondary mb-1">
                  Tên hạn mức <span class="text-danger">*</span>
                </label>
                <input 
                  v-model="formData.name"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                  :class="[
                    errors.name ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                    formData.name ? 'bg-white' : 'bg-gray-50'
                  ]"
                  placeholder="Nhập tên hạn mức"
                />
                <p v-if="errors.name" class="mt-1 text-sm text-danger">
                  {{ errors.name }}
                </p>
              </div>
  
              <!-- Danh mục -->
              <SelectDropdown
                v-model="formData.category_ids"
                :options="categories"
                label="Danh mục"
                placeholder="Chọn danh mục"
                :required="true"
                :error="errors.category_ids"
                :show-search="true"
                :is-multiple="true"
                search-placeholder="Tìm kiếm danh mục..."
                default-icon="list"
              />
  
              <!-- Tài khoản -->
              <SelectDropdown
                v-model="formData.account_ids"
                :options="accounts"
                label="Tài khoản"
                placeholder="Chọn tài khoản"
                :required="true"
                :error="errors.account_ids"
                :is-multiple="false"
                default-icon="wallet"
              />
              
              <!-- Lặp lại -->
              <div>
                <label class="block text-sm font-medium text-text-secondary mb-1">
                  Lặp lại
                </label>
                <div class="relative select-none repeat-dropdown-container">
                  <div 
                    class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                    :class="[
                      isRepeatDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : ''
                    ]"
                    @click="toggleRepeatDropdown($event)"
                  >
                    <div class="flex items-center flex-1">
                      <font-awesome-icon 
                        :icon="['fas', selectedRepeatOption.icon]" 
                        :class="selectedRepeatOption.color"
                        class="mr-2"
                      />
                      <span>{{ selectedRepeatOption.label }}</span>
                    </div>
                    <font-awesome-icon 
                      :icon="['fas', 'chevron-down']" 
                      class="text-gray-400 ml-2 transition-transform"
                      :class="{'rotate-180': isRepeatDropdownOpen}"
                    />
                  </div>
  
                  <div 
                    v-if="isRepeatDropdownOpen"
                    class="absolute z-[101] w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 max-h-40 overflow-y-auto"
                    @click.stop
                  >
                    <div 
                      v-for="option in repeatOptions" 
                      :key="option.value"
                      class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                      :class="{'bg-primary/5': option.value === formData.repeat_time}"
                      @click="handleRepeatSelect(option)"
                    >
                      <font-awesome-icon 
                        :icon="['fas', option.icon]" 
                        :class="option.color"
                        class="mr-2"
                      />
                      <span>{{ option.label }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Thời gian -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Ngày bắt đầu <span class="text-danger">*</span>
                  </label>
                  <div class="w-full">
                    <el-date-picker
                      v-model="formData.start_date"
                      type="date"
                      :format="'DD/MM/YYYY'"
                      :placeholder="'Chọn ngày'"
                      class="date-picker-custom w-full"
                      :class="{'error-date-picker': errors.start_date}"
                      style="width: 100%;"
                      @change="handleStartDateChange"
                    />
                  </div>
                  <p v-if="errors.start_date" class="mt-1 text-sm text-danger">
                    {{ errors.start_date }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Ngày kết thúc
                  </label>
                  <div class="w-full">
                    <el-date-picker
                      v-model="formData.end_date"
                      type="date"
                      :format="'DD/MM/YYYY'"
                      :placeholder="'Chọn ngày'"
                      class="date-picker-custom w-full"
                      :class="{'error-date-picker': errors.end_date}"
                      style="width: 100%;"
                      @change="handleEndDateChange"
                    />
                  </div>
                  <p v-if="errors.end_date" class="mt-1 text-sm text-danger">
                    {{ errors.end_date }}
                  </p>
                </div>
              </div>
            </form>
          </div>
  
          <!-- Modal Footer -->
          <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
            <button 
              @click="handleClose"
              class="px-4 py-2 text-text-secondary hover:text-text"
            >
              Hủy
            </button>
            <button 
              @click="handleSubmit"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
            >
              Cập nhật
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </template>

<style scoped>
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

/* Override Element Plus date picker styles */
.date-picker-custom :deep(.el-input__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
  box-sizing: border-box !important;
  border: 1px solid rgb(243 244 246) !important;
  border-radius: 0.5rem !important;
  height: 42px !important; /* Match height of other inputs */
}

.date-picker-custom :deep(.el-input__wrapper:hover) {
  border-color: rgb(229 231 235) !important;
}

.date-picker-custom :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(99, 102, 241, 0.5) !important;
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.2) !important;
}

.error-date-picker :deep(.el-input__wrapper) {
  border-color: rgba(239, 68, 68, 0.5) !important;
}

.error-date-picker :deep(.el-input__wrapper.is-focus) {
  border-color: rgb(239, 68, 68) !important;
  box-shadow: 0 0 0 1px rgba(239, 68, 68, 0.2) !important;
}

/* Remove input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>