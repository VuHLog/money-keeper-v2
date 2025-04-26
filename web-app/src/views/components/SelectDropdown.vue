<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faChevronDown,
  faSearch,
  faList,
  faWallet,
  faBuildingColumns,
  faUtensils,
  faCar,
  faHome,
  faGamepad,
  faCheck
} from '@fortawesome/free-solid-svg-icons'

library.add(
  faChevronDown,
  faSearch,
  faList,
  faWallet,
  faBuildingColumns,
  faUtensils,
  faCar,
  faHome,
  faGamepad,
  faCheck
)

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  options: {
    type: Array,
    required: true
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'Chọn'
  },
  required: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  showSearch: {
    type: Boolean,
    default: false
  },
  searchPlaceholder: {
    type: String,
    default: 'Tìm kiếm...'
  },
  defaultIcon: {
    type: String,
    default: 'list'
  },
  isMultiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isDropdownOpen = ref(false)
const searchQuery = ref('')

// For single select
const selectedOption = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// For multiple select
const selectedValues = computed({
  get: () => props.isMultiple ? (Array.isArray(props.modelValue) ? props.modelValue : []) : [],
  set: (value) => emit('update:modelValue', value)
})

const isAllSelected = computed(() => {
  if (!props.isMultiple) return false
  const filteredOptionsIds = filteredOptions.value.map(opt => opt.id)
  return selectedValues.value.length === filteredOptionsIds.length &&
         filteredOptionsIds.every(id => selectedValues.value.includes(id))
})

// Filtered options based on search
const filteredOptions = computed(() => {
  if (!searchQuery.value) return props.options.filter(opt => opt.id !== 'all')
  const query = searchQuery.value.toLowerCase()
  return props.options.filter(opt => 
    opt.id !== 'all' && opt.name.toLowerCase().includes(query)
  )
})

const isSelected = (id) => {
  if (props.isMultiple) {
    return selectedValues.value.includes(id)
  }
  return selectedOption.value === id
}

const handleSelect = (option) => {
  if (props.isMultiple) {
    const newValues = [...selectedValues.value]
    const index = newValues.indexOf(option.id)
    
    if (option.id === 'all') {
      // Nếu chọn "Tất cả", xóa hết các giá trị khác
      selectedValues.value = ['all']
    } else {
      // Nếu chọn giá trị khác
      // 1. Xóa giá trị "all" nếu có
      const allIndex = newValues.indexOf('all')
      if (allIndex !== -1) {
        newValues.splice(allIndex, 1)
      }

      // 2. Toggle giá trị được chọn
      if (index === -1) {
        newValues.push(option.id)
      } else {
        newValues.splice(index, 1)
      }

      // 3. Nếu không còn giá trị nào, thêm lại "all"
      if (newValues.length === 0) {
        newValues.push('all')
      }

      selectedValues.value = newValues
    }
    emit('change', selectedValues.value)
  } else {
    selectedOption.value = option.id
    isDropdownOpen.value = false
    searchQuery.value = ''
    emit('change', option)
  }
}

const handleSelectAll = () => {
  if (!props.isMultiple) return
  
  if (isAllSelected.value || selectedValues.value.includes('all')) {
    // Nếu đang chọn tất cả, bỏ chọn tất cả và chọn "all"
    selectedValues.value = ['all']
  } else {
    // Nếu chưa chọn tất cả, chọn tất cả các giá trị (trừ "all")
    selectedValues.value = filteredOptions.value.map(opt => opt.id)
  }
  emit('change', selectedValues.value)
}

// Click outside handler
const handleClickOutside = (event) => {
  const dropdownEl = event.target.closest('.dropdown-container')
  if (!dropdownEl && isDropdownOpen.value) {
    isDropdownOpen.value = false
    searchQuery.value = ''
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<!-- SelectDropdown.vue -->
<template>
  <div>
    <label v-if="label" class="block text-sm font-medium text-text-secondary mb-1">
      {{ label }} <span v-if="required" class="text-danger">*</span>
    </label>
    <div class="relative select-none dropdown-container">
      <div 
        class="flex items-center w-full px-3 py-2 border border-gray-200 rounded-lg cursor-pointer hover:border-gray-300"
        :class="[
          isDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
          error ? 'border-danger/50' : ''
        ]"
        @click="isDropdownOpen = !isDropdownOpen"
      >
        <div class="flex items-center flex-1">
          <template v-if="isMultiple">
            <div v-if="selectedValues.length === 0" class="flex items-center">
              <font-awesome-icon 
                :icon="['fas', defaultIcon]"
                class="text-gray-400 mr-2"
              />
              <span>{{ placeholder }}</span>
            </div>
            <div v-else-if="isAllSelected" class="flex items-center">
              <font-awesome-icon 
                :icon="['fas', defaultIcon]"
                class="text-gray-400 mr-2"
              />
              <span>Tất cả {{ label?.toLowerCase() }}</span>
            </div>
            <div v-else class="flex items-center flex-wrap gap-1">
              <div v-for="value in selectedValues" :key="value" class="flex items-center">
                <font-awesome-icon 
                  :icon="['fas', options.find(opt => opt.id === value)?.icon || defaultIcon]"
                  :class="options.find(opt => opt.id === value)?.color || 'text-gray-400'"
                  class="mr-1"
                />
                <span class="mr-2">{{ options.find(opt => opt.id === value)?.name }}</span>
              </div>
            </div>
          </template>
          <template v-else>
            <font-awesome-icon 
              :icon="['fas', selectedOption ? (options.find(opt => opt.id === selectedOption)?.icon || defaultIcon) : defaultIcon]"
              :class="selectedOption ? (options.find(opt => opt.id === selectedOption)?.color || 'text-gray-400') : 'text-gray-400'"
              class="mr-2"
            />
            <span>{{ selectedOption ? (options.find(opt => opt.id === selectedOption)?.name || placeholder) : placeholder }}</span>
          </template>
        </div>
        <font-awesome-icon 
          :icon="['fas', 'chevron-down']" 
          class="text-gray-400 ml-2 transition-transform"
          :class="{'rotate-180': isDropdownOpen}"
        />
      </div>

      <div 
        v-if="isDropdownOpen"
        class="absolute z-[100] w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
      >
        <!-- Search input -->
        <div v-if="showSearch" class="px-3 py-2 border-b border-gray-100">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="searchPlaceholder"
              class="w-full pl-8 pr-3 py-1 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
            />
            <font-awesome-icon 
              :icon="['fas', 'search']" 
              class="absolute left-2 top-1/2 -translate-y-1/2 text-gray-400"
            />
          </div>
        </div>

        <!-- Options list -->
        <div class="max-h-60 overflow-y-auto">
          <!-- All option for multiple select -->
          <div 
            v-if="isMultiple"
            class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
            :class="{'bg-primary/5': isAllSelected}"
            @click="handleSelectAll"
          >
            <font-awesome-icon 
              :icon="['fas', defaultIcon]"
              class="text-gray-400 mr-2"
            />
            <span>Tất cả {{ label?.toLowerCase() }}</span>
            <font-awesome-icon 
              v-if="isAllSelected"
              :icon="['fas', 'check']"
              class="text-primary ml-auto"
            />
          </div>

          <!-- Options -->
          <div 
            v-for="option in filteredOptions" 
            :key="option.id"
            class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
            :class="{'bg-primary/5': isSelected(option.id)}"
            @click="handleSelect(option)"
          >
            <font-awesome-icon 
              :icon="['fas', option.icon]" 
              :class="option.color"
              class="mr-2"
            />
            <span>{{ option.name }}</span>
            <font-awesome-icon 
              v-if="isMultiple && isSelected(option.id)"
              :icon="['fas', 'check']"
              class="text-primary ml-auto"
            />
          </div>
        </div>
      </div>
      <p v-if="error" class="mt-1 text-sm text-danger">
        {{ error }}
      </p>
    </div>
  </div>
</template>


<style scoped>
/* Custom scrollbar for options list */
.max-h-60 {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.max-h-60::-webkit-scrollbar {
  width: 6px;
}

.max-h-60::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.max-h-60::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}
</style> 