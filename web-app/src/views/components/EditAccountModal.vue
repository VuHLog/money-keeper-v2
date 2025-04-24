<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'

const props = defineProps({
  isOpen: Boolean,
  account: {
    type: Object,
    default: null
  },
  accountTypes: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['close', 'update'])

const bankList = [
  { 
    id: 'vcb', 
    name: 'Vietcombank',
    icon: 'building-columns',
    color: 'text-[#1e4d2b]'
  },
  { 
    id: 'tcb', 
    name: 'Techcombank',
    icon: 'building-columns',
    color: 'text-[#f53b47]'
  },
  { 
    id: 'mb', 
    name: 'MB Bank',
    icon: 'building-columns',
    color: 'text-[#1414b3]'
  },
  { 
    id: 'acb', 
    name: 'ACB',
    icon: 'building-columns',
    color: 'text-[#0066b3]'
  },
  { 
    id: 'bidv', 
    name: 'BIDV',
    icon: 'building-columns',
    color: 'text-[#1e589d]'
  },
  { 
    id: 'vtb', 
    name: 'VietinBank',
    icon: 'building-columns',
    color: 'text-[#00529c]'
  },
  { 
    id: 'other', 
    name: 'Ngân hàng khác',
    icon: 'building',
    color: 'text-gray-400'
  }
]

const editingAccount = ref(null)
const newAccount = ref({
  name: '',
  type: 'cash',
  balance: '',
  description: '',
  bankId: '',
  creditLimit: ''
})

const errors = ref({
  name: '',
  balance: '',
  bankId: '',
  creditLimit: ''
})

const isConfirmingClose = ref(false)
const isTypeDropdownOpen = ref(false)
const isBankDropdownOpen = ref(false)
const bankSearchQuery = ref('')

// Watch for account changes to update form
watch(() => props.account, (newVal) => {
  if (newVal) {
    editingAccount.value = newVal
    newAccount.value = {
      name: newVal.name,
      type: newVal.type,
      balance: newVal.balance,
      description: newVal.description || '',
      bankId: newVal.bankId || '',
      creditLimit: newVal.creditLimit || ''
    }
  }
}, { immediate: true })

const selectedBank = computed(() => {
  return bankList.find(bank => bank.id === newAccount.value.bankId) || { id: '', name: 'Chọn ngân hàng' }
})

const showBankField = computed(() => {
  return ['bank', 'credit'].includes(newAccount.value.type)
})

const showCreditLimitField = computed(() => {
  return newAccount.value.type === 'credit'
})

const formattedCreditLimit = computed({
  get: () => {
    if (!newAccount.value.creditLimit) return ''
    return formatCurrency(Number(newAccount.value.creditLimit))
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    newAccount.value.creditLimit = numericValue ? Number(numericValue) : ''
  }
})

const formattedBalance = computed({
  get: () => {
    if (!newAccount.value.balance) return ''
    return formatCurrency(Number(newAccount.value.balance))
  },
  set: (value) => {
    // Chỉ lấy số từ chuỗi đã format
    const numericValue = value.replace(/[^\d]/g, '')
    newAccount.value.balance = numericValue ? Number(numericValue) : ''
  }
})

const selectedAccountType = computed(() => {
  return props.accountTypes.find(t => t.value === newAccount.value.type)
})

const hasChanges = computed(() => {
  if (!editingAccount.value) return false
  
  const basicFieldsChanged = 
    editingAccount.value.name !== newAccount.value.name.trim() ||
    editingAccount.value.type !== newAccount.value.type ||
    editingAccount.value.balance !== Number(newAccount.value.balance) ||
    editingAccount.value.description !== newAccount.value.description.trim()

  const bankFieldChanged = 
    (editingAccount.value.bankId || '') !== (newAccount.value.bankId || '')

  const creditLimitChanged = 
    (editingAccount.value.creditLimit || '') !== (newAccount.value.creditLimit || '')

  return basicFieldsChanged || bankFieldChanged || creditLimitChanged
})

const validateForm = () => {
  let isValid = true
  errors.value = {
    name: '',
    balance: '',
    bankId: '',
    creditLimit: ''
  }

  // Validate name
  if (!newAccount.value.name.trim()) {
    errors.value.name = 'Vui lòng nhập tên tài khoản'
    isValid = false
  } else if (newAccount.value.name.trim().length < 3) {
    errors.value.name = 'Tên tài khoản phải có ít nhất 3 ký tự'
    isValid = false
  }

  // Validate balance
  if (newAccount.value.balance === '') {
    errors.value.balance = 'Vui lòng nhập số dư ban đầu'
    isValid = false
  }

  // Validate bank selection
  if (showBankField.value && !newAccount.value.bankId) {
    errors.value.bankId = 'Vui lòng chọn ngân hàng'
    isValid = false
  }

  // Validate credit limit
  if (showCreditLimitField.value) {
    if (!newAccount.value.creditLimit) {
      errors.value.creditLimit = 'Vui lòng nhập hạn mức tín dụng'
      isValid = false
    } else if (Number(newAccount.value.creditLimit) <= 0) {
      errors.value.creditLimit = 'Hạn mức tín dụng phải lớn hơn 0'
      isValid = false
    }
  }

  return isValid
}

const handleClose = () => {
  if (hasChanges.value && !isConfirmingClose.value) {
    isConfirmingClose.value = true
    return
  }
  resetAndClose()
}

const resetAndClose = () => {
  editingAccount.value = null
  newAccount.value = {
    name: '',
    type: 'cash',
    balance: '',
    description: '',
    bankId: '',
    creditLimit: ''
  }
  errors.value = {
    name: '',
    balance: '',
    bankId: '',
    creditLimit: ''
  }
  isConfirmingClose.value = false
  isTypeDropdownOpen.value = false
  isBankDropdownOpen.value = false
  emit('close')
}

const handleUpdate = () => {
  if (!validateForm()) {
    return
  }

  if (!hasChanges.value) {
    resetAndClose()
    return
  }

  emit('update', {
    ...editingAccount.value,
    name: newAccount.value.name.trim(),
    type: newAccount.value.type,
    balance: Number(newAccount.value.balance),
    description: newAccount.value.description.trim(),
    bankId: showBankField.value ? newAccount.value.bankId : undefined,
    creditLimit: showCreditLimitField.value ? Number(newAccount.value.creditLimit) : undefined
  })

  resetAndClose()
}

const continueEditing = () => {
  isConfirmingClose.value = false
}

const filteredBanks = computed(() => {
  if (!bankSearchQuery.value) return bankList
  const query = bankSearchQuery.value.toLowerCase()
  return bankList.filter(bank => bank.name.toLowerCase().includes(query))
})

// Function để đóng tất cả dropdown khi click ra ngoài
const handleClickOutside = (event) => {
  // Xử lý dropdown loại tài khoản
  const typeDropdownEl = document.querySelector('.edit-type-dropdown-container')
  if (typeDropdownEl && !typeDropdownEl.contains(event.target) && isTypeDropdownOpen.value) {
    isTypeDropdownOpen.value = false
  }
  
  // Xử lý dropdown ngân hàng
  const bankDropdownEl = document.querySelector('.edit-bank-dropdown-container')
  if (bankDropdownEl && !bankDropdownEl.contains(event.target) && isBankDropdownOpen.value) {
    isBankDropdownOpen.value = false
  }
}

// Thêm và xóa event listener khi component được mount và unmount
onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="handleClose"
    >
      <div 
        class="bg-white rounded-lg shadow-xl w-full max-w-md relative"
        @click.stop
      >
        <!-- Confirmation Dialog for Unsaved Changes -->
        <div 
          v-if="isConfirmingClose"
          class="absolute inset-0 bg-white rounded-lg z-10 flex flex-col"
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
          <h3 class="text-lg font-semibold text-text">Chỉnh sửa tài khoản</h3>
          <button 
            @click="handleClose"
            class="text-text-secondary hover:text-text"
          >
            <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
          </button>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4 max-h-[calc(100vh-16rem)] overflow-y-auto">
          <form @submit.prevent="handleUpdate" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Số dư ban đầu <span class="text-danger">*</span>
              </label>
              <input 
                v-model="formattedBalance"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[

                  errors.balance ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  formattedBalance ? 'bg-white' : 'bg-gray-50'
                ]"
                placeholder="0 ₫"
              />
              <p v-if="errors.balance" class="mt-1 text-sm text-danger">
                {{ errors.balance }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tên tài khoản <span class="text-danger">*</span>
              </label>
              <input 
                v-model="newAccount.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[

                  errors.name ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  newAccount.name ? 'bg-white' : 'bg-gray-50'
                ]"
                placeholder="Nhập tên tài khoản"
              />
              <p v-if="errors.name" class="mt-1 text-sm text-danger">
                {{ errors.name }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Loại tài khoản <span class="text-danger">*</span>
              </label>
              <div class="relative edit-type-dropdown-container">
                <!-- Selected Value Display -->
                <div 
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[

                    {'ring-1 ring-primary/20 border-primary/50': isTypeDropdownOpen},
                    errors.type ? 'border-danger/50' : ''
                  ]"
                  @click="isTypeDropdownOpen = !isTypeDropdownOpen"
                >
                  <div class="flex items-center flex-1">
                    <font-awesome-icon 
                      :icon="['fas', selectedAccountType.icon]" 
                      :class="selectedAccountType.color"
                      class="mr-2"
                    />
                    <span>{{ selectedAccountType.label }}</span>
                  </div>
                  <font-awesome-icon 
                    :icon="['fas', 'chevron-down']" 
                    class="text-gray-400 ml-2 transition-transform"
                    :class="{'rotate-180': isTypeDropdownOpen}"
                  />
                </div>

                <!-- Dropdown Options -->
                <div 
                  v-if="isTypeDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 max-h-48 overflow-y-auto"
                >
                  <div 
                    v-for="type in accountTypes" 
                    :key="type.value"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{'bg-primary/5': type.value === newAccount.type}"
                    @click="() => {
                      newAccount.type = type.value;
                      isTypeDropdownOpen = false;
                    }"
                  >
                    <font-awesome-icon 
                      :icon="['fas', type.icon]" 
                      :class="type.color"
                      class="mr-2"
                    />
                    <span>{{ type.label }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="showBankField">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Ngân hàng <span class="text-danger">*</span>
              </label>
              <div class="relative edit-bank-dropdown-container">
                <div 
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[
                    {'ring-1 ring-primary/20 border-primary/50': isBankDropdownOpen},
                    errors.bankId ? 'border-danger/50' : ''
                  ]"
                  @click="isBankDropdownOpen = !isBankDropdownOpen"
                >
                  <div class="flex items-center flex-1">
                    <font-awesome-icon 
                      v-if="selectedBank.icon"
                      :icon="['fas', selectedBank.icon]" 
                      :class="selectedBank.color"
                      class="mr-2"
                    />
                    <span>{{ selectedBank.name }}</span>
                  </div>
                  <font-awesome-icon 
                    :icon="['fas', 'chevron-down']" 
                    class="text-gray-400 ml-2 transition-transform"
                    :class="{'rotate-180': isBankDropdownOpen}"
                  />
                </div>

                <div 
                  v-if="isBankDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg divide-y divide-gray-100"
                >
                  <div class="p-2">
                    <!-- Bank search input -->
                    <input
                      v-model="bankSearchQuery"
                      type="text"
                      class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                      :class="bankSearchQuery ? 'bg-white' : 'bg-gray-50'"
                      placeholder="Tìm kiếm ngân hàng..."
                      @click.stop
                    />
                  </div>
                  <div class="max-h-48 overflow-y-auto py-1">
                    <div 
                      v-for="bank in filteredBanks" 
                      :key="bank.id"
                      class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                      :class="{'bg-primary/5': bank.id === newAccount.bankId}"
                      @click="() => {
                        newAccount.bankId = bank.id;
                        isBankDropdownOpen = false;
                        bankSearchQuery = '';
                      }"
                    >
                      <font-awesome-icon 
                        :icon="['fas', bank.icon]" 
                        :class="bank.color"
                        class="mr-2"
                      />
                      <span>{{ bank.name }}</span>
                    </div>
                    <div 
                      v-if="filteredBanks.length === 0" 
                      class="px-3 py-2 text-text-secondary text-sm"
                    >
                      Không tìm thấy ngân hàng
                    </div>
                  </div>
                </div>
              </div>
              <p v-if="errors.bankId" class="mt-1 text-sm text-danger">
                {{ errors.bankId }}
              </p>
            </div>

            <div v-if="showCreditLimitField">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Hạn mức tín dụng <span class="text-danger">*</span>
              </label>
              <input 
                v-model="formattedCreditLimit"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[

                  errors.creditLimit ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  formattedCreditLimit ? 'bg-white' : 'bg-gray-50'
                ]"
                placeholder="0 ₫"
              />
              <p v-if="errors.creditLimit" class="mt-1 text-sm text-danger">
                {{ errors.creditLimit }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Diễn giải
              </label>
              <input 
                v-model="newAccount.description"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                :class="newAccount.description ? 'bg-white' : 'bg-gray-50'"
                placeholder="Nhập diễn giải cho tài khoản"
              />
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
            @click="handleUpdate"
            class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90"
            :disabled="!hasChanges"
          >
            Cập nhật
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>