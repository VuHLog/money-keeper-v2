<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { faWallet, faBuildingColumns,faList,faChevronDown,faCalendar,faArrowUp,faArrowDown,faUtensils,faCar,faHome,faGamepad,faSearch,faCheck,faRotateLeft} from '@fortawesome/free-solid-svg-icons'
import SelectDropdown from '@/views/components/SelectDropdown.vue'
import { useDictionaryBucketPaymentStore } from '@stores/DictionaryBucketPaymentStore.js'
import { useDictionaryExpenseStore } from '@stores/DictionaryExpenseStore.js'
import { useDictionaryRevenueStore } from '@stores/DictionaryRevenueStore.js'


library.add(faWallet, faBuildingColumns,faList,faChevronDown,faCalendar,faArrowUp,faArrowDown,faUtensils,faCar,faHome,faGamepad,faSearch,faCheck,faRotateLeft)

const props = defineProps({
  showTimeRange: {
    type: Boolean,
    default: true
  },
  showTransactionType: {
    type: Boolean,
    default: true
  },
  showAccount: {
    type: Boolean,
    default: true
  },
  showCategory: {
    type: Boolean,
    default: false
  },
  showExpenseCategory: {
    type: Boolean,
    default: true
  },
  showRevenueCategory: {
    type: Boolean,
    default: true
  },
  activeTab: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['filter-change', 'filter-reset', 'apply-filter'])

// Time range options
const timeRanges = [
  { id: 'day', name: 'Theo ngày', icon: 'calendar', color: 'text-blue-500', pickerType: 'date' },
  { id: 'month', name: 'Theo tháng', icon: 'calendar', color: 'text-blue-500', pickerType: 'month' },
  { id: 'year', name: 'Theo năm', icon: 'calendar', color: 'text-blue-500', pickerType: 'year' },
  { id: 'custom', name: 'Tùy chọn', icon: 'calendar', color: 'text-blue-500', pickerType: 'daterange' }
]

// Transaction type options
const transactionTypes = [
  { id: 'all', name: 'Tất cả', icon: 'list', color: 'text-gray-400' },
  { id: 'income', name: 'Thu nhập', icon: 'arrow-up', color: 'text-success' },
  { id: 'expense', name: 'Chi tiêu', icon: 'arrow-down', color: 'text-red-500' }
]

// Loại danh mục thu nhập/chi tiêu
const categoryTypes = [
  { id: 'expense', name: 'Danh mục chi tiêu', icon: 'arrow-down', color: 'text-red-500' },
  { id: 'revenue', name: 'Danh mục thu nhập', icon: 'arrow-up', color: 'text-success' }
]

const accounts = ref([]);
const expenseCategories = ref([]);
const revenueCategories = ref([]);
const categoryType = ref('expense'); // Mặc định chọn danh mục chi tiêu

// Computed để lấy danh mục theo loại đã chọn
const categories = computed(() => {
  // Thêm option "all" vào đầu danh sách
  const allOption = { id: 'all', name: 'Tất cả danh mục', icon: 'list', color: 'text-gray-400' };
  
  if (categoryType.value === 'expense' || selectedTransactionType.value.includes('expense')) {
    return [allOption, ...expenseCategories.value];
  } else if (categoryType.value === 'revenue' || selectedTransactionType.value.includes('income')) {
    return [allOption, ...revenueCategories.value];
  }
  
  // Mặc định trả về danh sách rỗng với option "all"
  return [allOption];
});

// Selected values - change to arrays for multiple select
const selectedTimeRange = ref('month')
const selectedTransactionType = ref(['all'])
const selectedAccount = ref(['all'])
const selectedCategory = ref(['all'])
const customDateRange = ref([null, null])

// Set default date based on current time
const currentDate = new Date()
const selectedDate = ref(
  selectedTimeRange.value === 'day' ? currentDate :
  selectedTimeRange.value === 'month' ? new Date(currentDate.getFullYear(), currentDate.getMonth(), 1) :
  selectedTimeRange.value === 'year' ? new Date(currentDate.getFullYear(), 0, 1) :
  null
)

// Store original values for reset
const originalValues = {
  timeRange: 'month',
  transactionType: ['all'],
  account: ['all'],
  category: ['all'],
  categoryType: 'expense',
  date: selectedDate.value,
  customDateRange: [null, null]
}

const categorySearch = ref('')

// Dropdown states
const isTimeRangeDropdownOpen = ref(false)
const isTransactionTypeDropdownOpen = ref(false)
const isAccountDropdownOpen = ref(false)
const isCategoryDropdownOpen = ref(false)
const isCategoryTypeDropdownOpen = ref(false)

const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const dictionaryExpenseStore = useDictionaryExpenseStore()
const dictionaryRevenueStore = useDictionaryRevenueStore()
onMounted(async() => {
  expenseCategories.value = await dictionaryExpenseStore.getMyExpenseCategories();
  revenueCategories.value = await dictionaryRevenueStore.getMyRevenueCategories();
  accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments();
  
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// Computed properties
const showCustomDateRange = computed(() => selectedTimeRange.value === 'custom')
const showDatePicker = computed(() => selectedTimeRange.value !== 'custom')
const selectedTimeRangeObj = computed(() => {
  return timeRanges.find(range => range.id === selectedTimeRange.value)
})

// Methods
const handleFilterChange = () => {
  emit('filter-change', {
    timeRange: selectedTimeRange.value,
    transactionType: selectedTransactionType.value,
    account: selectedAccount.value,
    category: selectedCategory.value,
    customDateRange: customDateRange.value,
    selectedDate: selectedDate.value
  })
}

const applyFilter = () => {
  emit('apply-filter')
}

const handleReset = () => {
  selectedTimeRange.value = originalValues.timeRange
  selectedTransactionType.value = [...originalValues.transactionType]
  selectedAccount.value = [...originalValues.account]
  selectedCategory.value = [...originalValues.category]
  selectedDate.value = originalValues.date
  customDateRange.value = originalValues.customDateRange
  emit('filter-reset')
}

// Watch for tab changes to reset filters
watch(() => props.activeTab, () => {
  handleReset()
})

// Function để đóng tất cả dropdown khi click ra ngoài
const handleClickOutside = (event) => {
  // Xử lý dropdown thời gian
  const timeRangeDropdownEl = document.querySelector('.time-range-dropdown-container')
  if (timeRangeDropdownEl && !timeRangeDropdownEl.contains(event.target) && isTimeRangeDropdownOpen.value) {
    isTimeRangeDropdownOpen.value = false
  }
  
  // Xử lý dropdown loại giao dịch
  const transactionTypeDropdownEl = document.querySelector('.transaction-type-dropdown-container')
  if (transactionTypeDropdownEl && !transactionTypeDropdownEl.contains(event.target) && isTransactionTypeDropdownOpen.value) {
    isTransactionTypeDropdownOpen.value = false
  }
  
  // Xử lý dropdown tài khoản
  const accountDropdownEl = document.querySelector('.account-dropdown-container')
  if (accountDropdownEl && !accountDropdownEl.contains(event.target) && isAccountDropdownOpen.value) {
    isAccountDropdownOpen.value = false
  }
  
  // Xử lý dropdown danh mục
  const categoryDropdownEl = document.querySelector('.category-dropdown-container')
  if (categoryDropdownEl && !categoryDropdownEl.contains(event.target) && isCategoryDropdownOpen.value) {
    isCategoryDropdownOpen.value = false
  }

  // Xử lý dropdown loại danh mục
  const categoryTypeDropdownEl = document.querySelector('.category-type-dropdown-container')
  if (categoryTypeDropdownEl && !categoryTypeDropdownEl.contains(event.target) && isCategoryTypeDropdownOpen.value) {
    isCategoryTypeDropdownOpen.value = false
  }
}

// Date picker format
const dateFormat = {
  date: 'DD/MM/YYYY',
  month: 'MM/YYYY',
  year: 'YYYY',
  daterange: 'DD/MM/YYYY'
}

// Date picker placeholder
const datePlaceholder = {
  date: 'Chọn ngày',
  month: 'Chọn tháng',
  year: 'Chọn năm',
  daterange: 'Chọn khoảng thời gian'
}

// Method to update selected date when time range changes
const updateSelectedDate = () => {
  const currentDate = new Date()
  selectedDate.value = 
    selectedTimeRange.value === 'day' ? currentDate :
    selectedTimeRange.value === 'month' ? new Date(currentDate.getFullYear(), currentDate.getMonth(), 1) :
    selectedTimeRange.value === 'year' ? new Date(currentDate.getFullYear(), 0, 1) :
    null
}

// Watch for time range changes
watch(selectedTimeRange, () => {
  updateSelectedDate()
  handleFilterChange()
})

// Watch for transaction type changes to update category type
watch(selectedTransactionType, (newVal) => {
  if (newVal.includes('income') && !newVal.includes('expense') && !newVal.includes('all')) {
    categoryType.value = 'revenue';
  } else if (newVal.includes('expense') && !newVal.includes('income') && !newVal.includes('all')) {
    categoryType.value = 'expense';
  }
  // Reset danh mục đã chọn khi thay đổi loại giao dịch
  selectedCategory.value = ['all'];
  handleFilterChange();
})

// Watch for category type changes
watch(categoryType, () => {
  // Reset danh mục đã chọn khi thay đổi loại danh mục
  selectedCategory.value = ['all'];
  handleFilterChange();
})
</script>

<template>
  <div class="bg-white rounded-lg p-4 mb-6 border border-gray-100">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Time Range Filter -->
      <div v-if="showTimeRange">
        <label class="block text-sm font-medium text-text-secondary mb-1">
          Thời gian
        </label>
        <div class="relative select-none time-range-dropdown-container">
          <div 
            class="flex items-center w-full px-3 py-2 border border-gray-200 rounded-lg cursor-pointer hover:border-gray-300"
            :class="[
              isTimeRangeDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : ''
            ]"
            @click="isTimeRangeDropdownOpen = !isTimeRangeDropdownOpen"
          >
            <div class="flex items-center flex-1">
              <font-awesome-icon 
                :icon="['fas', selectedTimeRangeObj.icon]" 
                :class="selectedTimeRangeObj.color"
                class="mr-2"
              />
              <span>{{ selectedTimeRangeObj.name }}</span>
            </div>
            <font-awesome-icon 
              :icon="['fas', 'chevron-down']" 
              class="text-gray-400 ml-2 transition-transform"
              :class="{'rotate-180': isTimeRangeDropdownOpen}"
            />
          </div>

          <div 
            v-if="isTimeRangeDropdownOpen"
            class="absolute z-[100] w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
          >
            <div 
              v-for="range in timeRanges" 
              :key="range.id"
              class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
              :class="{'bg-primary/5': range.id === selectedTimeRange}"
              @click="selectedTimeRange = range.id; isTimeRangeDropdownOpen = false; handleFilterChange()"
            >
              <font-awesome-icon 
                :icon="['fas', range.icon]" 
                :class="range.color"
                class="mr-2"
              />
              <span>{{ range.name }}</span>
            </div>
          </div>

          <!-- Date Picker -->
          <div v-if="showDatePicker" class="mt-2">
            <el-date-picker
              v-model="selectedDate"
              :type="selectedTimeRangeObj.pickerType"
              :format="dateFormat[selectedTimeRangeObj.pickerType]"
              :placeholder="datePlaceholder[selectedTimeRangeObj.pickerType]"
              @change="handleFilterChange"
              class="w-full"
              :clearable="false"
            />
          </div>

          <!-- Custom Date Range -->
          <div v-if="showCustomDateRange" class="mt-2">
            <el-date-picker
              v-model="customDateRange"
              type="daterange"
              range-separator="Đến"
              start-placeholder="Từ ngày"
              end-placeholder="Đến ngày"
              format="DD/MM/YYYY"
              value-format="YYYY-MM-DD"
              @change="handleFilterChange"
              class="w-full"
            />
          </div>
        </div>
      </div>

      <!-- Transaction Type Filter -->
      <div v-if="showTransactionType">
        <SelectDropdown
          v-model="selectedTransactionType"
          :options="transactionTypes"
          label="Loại giao dịch"
          placeholder="Chọn loại giao dịch"
          :is-multiple="true"
          default-icon="list"
          @change="handleFilterChange"
        />
      </div>

      <!-- Account Filter -->
      <div v-if="showAccount">
        <SelectDropdown
          v-model="selectedAccount"
          :options="accounts"
          label="Tài khoản"
          placeholder="Chọn tài khoản"
          :is-multiple="true"
          default-icon="wallet"
          @change="handleFilterChange"
        />
      </div>

      <!-- Category Filter -->
      <div v-if="showExpenseCategory || showRevenueCategory">
        <!-- Tự động xác định loại danh mục -->
        <div class="hidden">
          {{ showExpenseCategory && !showRevenueCategory ? categoryType = 'expense' : 
             !showExpenseCategory && showRevenueCategory ? categoryType = 'revenue' : 
             categoryType = categoryType }}
        </div>

        <!-- Category Filter -->
        <SelectDropdown
          v-model="selectedCategory"
          :options="categories"
          :label="'Danh mục' + (showExpenseCategory ? ' chi' : showRevenueCategory ? ' thu' : '')"
          placeholder="Chọn danh mục"
          :show-search="true"
          :is-multiple="true"
          search-placeholder="Tìm kiếm danh mục..."
          default-icon="list"
          @change="handleFilterChange"
        />
      </div>
    </div>

    <!-- Action buttons -->
    <div class="flex justify-end gap-2 mt-4">
      <button
        @click="handleReset"
        class="px-4 py-2 text-sm font-medium text-text-secondary bg-gray-100 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-200"
      >
        <font-awesome-icon :icon="['fas', 'rotate-left']" class="mr-2" />
        Đặt lại
      </button>
      <button
        @click="applyFilter"
        class="px-4 py-2 text-sm font-medium text-white bg-primary rounded-lg hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary/20"
      >
        <font-awesome-icon :icon="['fas', 'check']" class="mr-2" />
        Áp dụng
      </button>
    </div>
  </div>
</template>

<style scoped>
/* Override Element Plus date picker styles */
:deep(.el-date-editor) {
  @apply !w-full !h-[42px] !border-gray-200 hover:!border-gray-300 focus:!border-primary/50 focus:!ring-1 focus:!ring-primary/20 !bg-white;
}

:deep(.el-date-editor .el-input__wrapper) {
  @apply !bg-white !shadow-none !box-border !border !border-gray-200 !rounded-lg hover:!border-gray-300;
}

:deep(.el-date-editor .el-input__wrapper.is-focus) {
  @apply !border-primary/50 !ring-1 !ring-primary/20;
}

:deep(.el-input__inner) {
  @apply !text-text !text-base !font-normal;
}

:deep(.el-input__inner::placeholder) {
  @apply !text-gray-500 !opacity-100;
}

:deep(.el-date-editor .el-range-input) {
  @apply !text-text !text-base !font-normal;
}

:deep(.el-date-editor .el-range-separator) {
  @apply !text-gray-500;
}

:deep(.el-date-editor .el-range-input::placeholder) {
  @apply !text-gray-500 !opacity-100;
}

:deep(.el-date-editor:not(.el-range-editor) .el-input__wrapper) {
  @apply !px-3 !py-2;
}

:deep(.el-date-editor .el-input__prefix),
:deep(.el-date-editor .el-input__suffix),
:deep(.el-date-editor .el-input__prefix-inner),
:deep(.el-date-editor .el-input__suffix-inner) {
  @apply !text-gray-500;
}

:deep(.el-date-editor .el-input__prefix-inner i),
:deep(.el-date-editor .el-input__suffix-inner i) {
  @apply !text-gray-500;
}

:deep(.el-date-editor .el-input__prefix-inner .el-icon),
:deep(.el-date-editor .el-input__suffix-inner .el-icon) {
  @apply !text-gray-500;
}

:deep(.el-date-editor .el-input__prefix-inner .el-icon svg),
:deep(.el-date-editor .el-input__suffix-inner .el-icon svg) {
  @apply !text-gray-500;
}

:deep(.el-date-editor .el-input__prefix-inner .el-icon svg path),
:deep(.el-date-editor .el-input__suffix-inner .el-input__prefix-inner .el-icon svg path) {
  @apply !fill-gray-500;
}

/* Custom scrollbar for category list */
:deep(.max-h-60) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

:deep(.max-h-60::-webkit-scrollbar) {
  width: 6px;
}

:deep(.max-h-60::-webkit-scrollbar-track) {
  background: #f1f5f9;
  border-radius: 3px;
}

:deep(.max-h-60::-webkit-scrollbar-thumb) {
  background-color: #cbd5e1;
  border-radius: 3px;
}
</style>