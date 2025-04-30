<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faChevronDown, faSearch, faList, faWallet, faBuildingColumns, faUtensils, faCar, faHome, faGamepad, faCheck } from '@fortawesome/free-solid-svg-icons'
import Avatar from "@components/Avatar.vue"

library.add(faChevronDown, faSearch, faList, faWallet, faBuildingColumns, faUtensils, faCar, faHome, faGamepad, faCheck)

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
const isOnMounted = ref(false)

onMounted(() => {
  if(props.isMultiple && (props.modelValue.length === 0 || props.modelValue[0] === 'all')) {
    isAllSelected.value = true
    handleSelectAll()
  }
  document.addEventListener('mousedown', handleClickOutside)
  isOnMounted.value = true
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// For single select
const selectedOption = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// For multiple select
const selectedValues = computed({
  get: () => {
    if (props.isMultiple) {
      if(Array.isArray(props.modelValue)) {
        if(props.modelValue.length === 0 || props.modelValue[0] === 'all') {
          return []
        } else {
          return props.modelValue
        }
      } else {
        return []
      }
    }
    return []
  },
  set: (value) => emit('update:modelValue', value)
})

const isAllSelected = computed(() => {
  if (!props.isMultiple) return false
  
  // Kiểm tra xem tất cả các tùy chọn (trừ 'all') có được chọn không
  const optionIds = props.options
    .filter(opt => opt.id !== 'all')
    .map(opt => opt.id)
  
  // Nếu không có tùy chọn nào, luôn trả về false
  if (optionIds.length === 0) return false
  
  // Trả về true nếu tất cả các tùy chọn đều được chọn
  return optionIds.length > 0 && 
    optionIds.every(id => selectedValues.value.includes(id)) &&
    selectedValues.value.length === optionIds.length
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
    
    // Nếu tùy chọn không có trong danh sách đã chọn
    if (index === -1) {
      newValues.push(option.id)
    } else {
      // Nếu tùy chọn đã được chọn, xóa nó khỏi danh sách
      newValues.splice(index, 1)
    }
    
    selectedValues.value = newValues
    emit('change', selectedValues.value)
  } else {
    // Xử lý cho single select
    selectedOption.value = option.id
    isDropdownOpen.value = false
    searchQuery.value = ''
    emit('change', option)
  }
}

const handleSelectAll = () => {
  if (!props.isMultiple) return
  
  // Lấy tất cả các ID tùy chọn (trừ 'all')
  const allOptionIds = props.options
    .filter(opt => opt.id !== 'all')
    .map(opt => opt.id)
  
  if (isAllSelected.value) {
    // Nếu đã chọn tất cả, bỏ chọn tất cả (mảng rỗng)
    selectedValues.value = []
  } else {
    // Nếu chưa chọn tất cả, chọn tất cả các tùy chọn
    selectedValues.value = [...allOptionIds]
  }
  
  if(isOnMounted.value){
    emit('change', selectedValues.value)
  }
}

// Click outside handler
const handleClickOutside = (event) => {
  const dropdownEl = event.target.closest('.dropdown-container')
  if (!dropdownEl && isDropdownOpen.value) {
    isDropdownOpen.value = false
    searchQuery.value = ''
  }
}
</script>

<!-- SelectDropdown.vue -->
<template>
  <div>
    <label v-if="label" class="block text-sm font-medium text-text-secondary mb-1">
      {{ label }} <span v-if="required" class="text-danger">*</span>
    </label>
    <div class="relative select-none dropdown-container">
      <div 
        class="flex items-center w-full px-3 py-2 border border-gray-200 rounded-lg cursor-pointer hover:border-gray-300 h-[42px]"
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
            <!-- Phần hiển thị các mục đã chọn trong trường hợp multiple select -->
            <div v-else class="flex items-center flex-wrap gap-1">
              <div v-for="value in selectedValues" :key="value" class="flex items-center">
                <Avatar 
                  v-if="options.find(opt => opt.id === value)?.iconUrl"
                  :src="options.find(opt => opt.id === value)?.iconUrl"
                  :alt="options.find(opt => opt.id === value)?.name || options.find(opt => opt.id === value)?.accountName"
                  size="m"
                  class="mr-2"
                />
                <span class="mr-2">{{ options.find(opt => opt.id === value)?.name || options.find(opt => opt.id === value)?.accountName }}</span>
              </div>
            </div>
          </template>
          <template v-else>
            <Avatar 
              v-if="selectedOption && options.find(opt => opt.id === selectedOption)?.iconUrl"
              :src="options.find(opt => opt.id === selectedOption)?.iconUrl"
              :alt="options.find(opt => opt.id === selectedOption)?.name || options.find(opt => opt.id === selectedOption)?.accountName"
              size="m"
              class="mr-2"
            />
            <span>{{ selectedOption ? (options.find(opt => opt.id === selectedOption)?.name || options.find(opt => opt.id === selectedOption)?.accountName || placeholder) : placeholder }}</span>
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
        class="absolute z-[100] w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 dropdown-list"
        :style="{ maxHeight: 'auto', overflowY: 'visible' }"
      >
        <!-- Search input -->
        <div v-if="showSearch" class="px-3 py-2 border-b border-gray-100">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="searchPlaceholder"
              class="w-full pl-8 pr-3 py-1 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
              @click.stop
            />
            <font-awesome-icon 
              :icon="['fas', 'search']" 
              class="absolute left-2 top-1/2 -translate-y-1/2 text-gray-400"
            />
          </div>
        </div>

        <!-- Options list -->
        <div class="max-h-60 overflow-y-auto options-container">
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
            <Avatar 
              v-if="option.iconUrl"
              :src="option.iconUrl"
              :alt="option.name || option.accountName"
              size="m"
              class="mr-2"
            />
            <span>{{ option.name || option.accountName }}</span>
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
  scrollbar-color: #cbd5e1 #f1f5f9;
  scrollbar-width: thin;
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

/* Ensure consistent height for all dropdowns */
.dropdown-container .flex-wrap {
  max-height: 24px;
  overflow: hidden;
}
</style>