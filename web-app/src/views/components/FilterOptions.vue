<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { faWallet, faBuildingColumns,faList,faChevronDown,faCalendar,faArrowUp,faArrowDown,faUtensils,faCar,faHome,faGamepad,faSearch,faCheck,faRotateLeft,faFilter,faAngleDown} from '@fortawesome/free-solid-svg-icons'
import SelectDropdown from '@/views/components/SelectDropdown.vue'
import { useDictionaryBucketPaymentStore } from '@stores/DictionaryBucketPaymentStore.js'
import { useDictionaryExpenseStore } from '@stores/DictionaryExpenseStore.js'
import { useDictionaryRevenueStore } from '@stores/DictionaryRevenueStore.js'


library.add(faWallet, faBuildingColumns,faList,faChevronDown,faCalendar,faArrowUp,faArrowDown,faUtensils,faCar,faHome,faGamepad,faSearch,faCheck,faRotateLeft,faFilter,faAngleDown)

const props = defineProps({
  showTimeRange: {
    type: Boolean,
    default: true
  },
  showAccount: {
    type: Boolean,
    default: true
  },
  showExpenseCategory: {
    type: Boolean,
    default: true
  },
  showRevenueCategory: {
    type: Boolean,
    default: true
  },
  showTransactionType: {
    type: Boolean,
    default: true
  },
  activeTab: {
    type: String,
    default: ''
  },
  isReset: {
    type: Boolean,
    default: false
  },
  defaultOpen: {
    type: Boolean,
    default: false
  },
  initialFilters: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['filter-change', 'filter-reset', 'apply-filter'])

// Time range options
const timeRanges = [
  { id: 'month', name: 'Theo tháng', icon: 'calendar', color: 'text-blue-500', pickerType: 'monthrange' },
  { id: 'year', name: 'Theo năm', icon: 'calendar', color: 'text-blue-500', pickerType: 'yearrange' },
  { id: 'date', name: 'Tùy chọn', icon: 'calendar', color: 'text-blue-500', pickerType: 'daterange' }
]

const transactionTypes = [
  { id: 'revenue', name: 'Thu nhập', icon: 'arrow-up', color: 'text-success' },
  { id: 'expense', name: 'Chi tiêu', icon: 'arrow-down', color: 'text-red-500' }
]

const categoryType = ref('expense');
const accounts = ref([]);
const expenseCategories = ref([]);
const revenueCategories = ref([]);

// Selected values - change to arrays for multiple select
const selectedTimeRange = ref('month')
const selectedAccount = ref(['all'])
const selectedExpenseCategory = ref(['all'])
const selectedRevenueCategory = ref(['all'])
const selectedTransactionType = ref('')
const customTimeRange = ref([new Date().toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)])

// Store original values for reset
const originalValues = {
  timeRange: 'month',
  transactionType: ['expense'],
  account: ['all'],
  expenseCategory: ['all'],
  revenueCategory: ['all'],
  categoryType: 'expense',
  customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
}


// Dropdown states
const isTimeRangeDropdownOpen = ref(false)
const isTransactionTypeDropdownOpen = ref(false)
const isAccountDropdownOpen = ref(false)
const isCategoryDropdownOpen = ref(false)
const isCategoryTypeDropdownOpen = ref(false)

const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const dictionaryExpenseStore = useDictionaryExpenseStore()
const dictionaryRevenueStore = useDictionaryRevenueStore()

// State for toggle filter options panel
const isFilterOpen = ref(props.defaultOpen)

const toggleFilter = () => {
  isFilterOpen.value = !isFilterOpen.value
}

onMounted(async() => {
  // Default state is closed
  isFilterOpen.value = props.defaultOpen
  
  // Apply initial filters if provided
  if (Object.keys(props.initialFilters).length > 0) {
    if (props.initialFilters.timeOption) {
      const timeRange = timeRanges.find(range => range.name === props.initialFilters.timeOption)
      if (timeRange) {
        selectedTimeRange.value = timeRange.id
      }
    }
    
    if (props.initialFilters.transactionType) {
      selectedTransactionType.value = props.initialFilters.transactionType
    }
    
    if (props.initialFilters.customTimeRange) {
      customTimeRange.value = props.initialFilters.customTimeRange
    }
    
    if (props.initialFilters.account) {
      selectedAccount.value = props.initialFilters.account
    }
    
    if (props.initialFilters.expenseCategory) {
      selectedExpenseCategory.value = props.initialFilters.expenseCategory
    }
    
    if (props.initialFilters.revenueCategory) {
      selectedRevenueCategory.value = props.initialFilters.revenueCategory
    }
  } else if(props.isReset){
    handleReset();
  }
  
  const allOption = { id: 'all', name: 'Tất cả danh mục', icon: 'list', color: 'text-gray-400' };
  expenseCategories.value = await dictionaryExpenseStore.getMyExpenseCategories();
  expenseCategories.value.unshift(allOption);
  revenueCategories.value = await dictionaryRevenueStore.getMyRevenueCategories();
  revenueCategories.value.unshift(allOption);
  accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments();
  
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// Computed properties
const selectedTimeRangeObj = computed(() => {
  return timeRanges.find(range => range.id === selectedTimeRange.value)
})

// Methods
const handleFilterChange = () => {
  emit('filter-change', {
    timeOption: selectedTimeRangeObj.value.name,
    account: selectedAccount.value,
    expenseCategory: selectedExpenseCategory.value,
    revenueCategory: selectedRevenueCategory.value,
    customTimeRange: customTimeRange.value,
    transactionType: selectedTransactionType.value,
  })
}

const applyFilter = () => {
  emit('apply-filter')
}

const handleReset = () => {
  selectedTimeRange.value = originalValues.timeRange
  selectedAccount.value = [...originalValues.account]
  selectedExpenseCategory.value = [...originalValues.expenseCategory]
  selectedRevenueCategory.value = [...originalValues.revenueCategory]
  customTimeRange.value = originalValues.customTimeRange
  selectedTransactionType.value = props.isReset ? '' : [...originalValues.transactionType]
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

  const transactionTypeDropdownEl = document.querySelector('.transaction-type-dropdown-container')
  if (transactionTypeDropdownEl && !transactionTypeDropdownEl.contains(event.target) && isTransactionTypeDropdownOpen.value) {
    isTransactionTypeDropdownOpen.value = false
  }
}

// Date picker format
const dateFormat = {
  date: {
    display: 'DD/MM/YYYY',
    value: 'YYYY-MM-DD'
  },
  month: {
    display: 'MM/YYYY',
    value: 'YYYY-MM'
  },
  year: {
    display: 'YYYY',
    value: 'YYYY'
  }
}

const toDateStr = (date) => date.toISOString().slice(0, 10)
const toMonthStr = (date) => date.toISOString().slice(0, 7)
const toYearStr = (date) => date.getFullYear().toString()

const updateCustomTimeRange = () => {
  const currentDate = new Date()
  customTimeRange.value = 
    selectedTimeRange.value === 'date' ? [toDateStr(currentDate), toDateStr(currentDate)] :
    selectedTimeRange.value === 'month' ? [toMonthStr(currentDate), toMonthStr(currentDate)] :
    selectedTimeRange.value === 'year' ? [toYearStr(currentDate), toYearStr(currentDate)] :
    null
}

// Watch for time range changes
watch(selectedTimeRange, () => {
  updateCustomTimeRange()
  handleFilterChange()
})


watch(selectedTransactionType, (newVal) => {
  if (newVal.includes('revenue') && !newVal.includes('expense')) {
    categoryType.value = 'revenue';
  } else if (newVal.includes('expense') && !newVal.includes('revenue')) {
    categoryType.value = 'expense';
  }
  handleFilterChange();
})
</script>

<template>
  <div class="bg-white rounded-lg p-4 mb-6 border border-gray-100">
    <!-- Header và nút toggle filter -->
    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center">
        <font-awesome-icon :icon="['fas', 'filter']" class="mr-2 text-primary" />
        <h3 class="font-medium text-text">Bộ lọc</h3>
      </div>
      <button 
        @click="toggleFilter" 
        class="text-text-secondary hover:text-text transition-colors"
      >
        <font-awesome-icon 
          :icon="['fas', 'angle-down']" 
          class="transition-transform" 
          :class="{ 'rotate-180': isFilterOpen }" 
        />
      </button>
    </div>

    <!-- Nội dung bộ lọc - chỉ hiển thị khi isFilterOpen = true -->
    <div v-if="isFilterOpen" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
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

          <!-- Custom Date Range -->
          <div class="mt-2">
            <el-date-picker
              v-model="customTimeRange"
              :type="selectedTimeRangeObj.pickerType"
              range-separator="Đến"
              :format="dateFormat[selectedTimeRangeObj.id].display"
              :value-format="dateFormat[selectedTimeRangeObj.id].value"
              @change="handleFilterChange"
              :clearable="false"
              class="w-full"
            />
          </div>
        </div>
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

      <div v-if="showTransactionType">
        <SelectDropdown
          v-model="selectedTransactionType"
          :options="transactionTypes"
          label="Loại giao dịch"
          placeholder="Chọn loại giao dịch"
          default-icon="list"
          @change="handleFilterChange"
        />
      </div>

      <!-- Category Filter -->
      <div v-if="showExpenseCategory">
        <!-- Tự động xác định loại danh mục -->
        <div class="hidden">
          Danh mục chi tiêu
        </div>

        <!-- Category Filter -->
        <SelectDropdown
          v-model="selectedExpenseCategory"
          :options="expenseCategories"
          :label="'Danh mục chi'"
          placeholder="Chọn danh mục"
          :show-search="true"
          :is-multiple="true"
          search-placeholder="Tìm kiếm danh mục..."
          default-icon="list"
          @change="handleFilterChange"
        />
      </div>

      <div v-if="showRevenueCategory">
        <!-- Tự động xác định loại danh mục -->
        <div class="hidden">
          Danh mục thu nhập
        </div>

        <!-- Category Filter -->
        <SelectDropdown
          v-model="selectedRevenueCategory"
          :options="revenueCategories"
          :label="'Danh mục thu'"
          placeholder="Chọn danh mục"
          :show-search="true"
          :is-multiple="true"
          search-placeholder="Tìm kiếm danh mục..."
          default-icon="list"
          @change="handleFilterChange"
        />
      </div>
    </div>

    <!-- Action buttons - chỉ hiển thị khi isFilterOpen = true -->
    <div v-if="isFilterOpen" class="flex justify-end gap-2 mt-4">
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