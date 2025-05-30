<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'
import { AccountType } from "@constants/AccountType.js"
import { Currency } from "@constants/Currency.js"
import { useDictionaryBucketPaymentStore } from '@stores/DictionaryBucketPaymentStore.js'
import { useBankStore } from "@stores/BankStore.js"
import Avatar from '@/views/components/Avatar.vue'
import { useCurrencyStore } from '@stores/CurrencyStore.js'


const props = defineProps({
  isOpen: Boolean,
  account: {
    type: Object,
    default: null
  },
})

const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore();
const bankStore = useBankStore()
const currencyStore = useCurrencyStore()
const emit = defineEmits(['close', 'update'])
const accountTypes = ref(AccountType)
const currencies = ref(Currency)

const bankList = ref([])

const editingAccount = ref(null)
const newAccount = ref({
  name: '',
  type: 'cash',
  balance: '',
  initialBalance: '',
  description: '',
  bankId: '',
  creditLimit: '',
  currencyCode: 'VND',
  currencySymbol: '₫'
})

// Helper function to format currency based on currency code
const formatWithCurrency = (value, currencyCode) => {
  if (!value) return ''
  const numberValue = Number(value)
  
  // Get the currency symbol
  const currency = currencies.value.find(c => c.code === currencyCode) || currencies.value[0]
  const symbol = currency.symbol
  
  // Format the number with thousand separators
  const formattedValue = new Intl.NumberFormat().format(numberValue).replace(/,/g, '.')
  
  // Return with the proper currency symbol
  if (currencyCode === 'VND') {
    return `${formattedValue} ${symbol}`
  } else {
    return `${symbol}${formattedValue}`
  }
}

// Helper to get currency symbol
const getCurrencySymbol = (currencyCode) => {
  const currency = currencies.value.find(c => c.code === currencyCode) || currencies.value[0]
  return currency.symbol
}

const errors = ref({
  name: '',
  bankId: '',
  creditLimit: ''
})

const isConfirmingClose = ref(false)
const isTypeDropdownOpen = ref(false)
const isBankDropdownOpen = ref(false)
const isCurrencyDropdownOpen = ref(false)

const bankSearchQuery = ref('')
const currencySearchQuery = ref('')

const typeDropdownPosition = ref({ top: 0, left: 0, width: 0 })
const bankDropdownPosition = ref({ top: 0, left: 0, width: 0 })
const currencyDropdownPosition = ref({ top: 0, left: 0, width: 0 })

const updateTypeDropdownPosition = () => {
  const trigger = document.querySelector('.edit-type-dropdown-container')
  if (trigger) {
    const rect = trigger.getBoundingClientRect()
    typeDropdownPosition.value = {
      top: rect.bottom + window.scrollY,
      left: rect.left + window.scrollX,
      width: rect.width
    }
  }
}

const updateBankDropdownPosition = () => {
  const trigger = document.querySelector('.edit-bank-dropdown-container')
  if (trigger) {
    const rect = trigger.getBoundingClientRect()
    bankDropdownPosition.value = {
      top: rect.bottom + window.scrollY,
      left: rect.left + window.scrollX,
      width: rect.width
    }
  }
}

const updateCurrencyDropdownPosition = () => {
  const trigger = document.querySelector('.edit-currency-dropdown-container')
  if (trigger) {
    const rect = trigger.getBoundingClientRect()
    currencyDropdownPosition.value = {
      top: rect.bottom + window.scrollY,
      left: rect.left + window.scrollX,
      width: rect.width
    }
  }
}

watch(isTypeDropdownOpen, (newVal) => {
  if (newVal) {
    nextTick(() => {
      updateTypeDropdownPosition()
    })
  }
})

watch(isBankDropdownOpen, (newVal) => {
  if (newVal) {
    nextTick(() => {
      updateBankDropdownPosition()
    })
  }
})

watch(isCurrencyDropdownOpen, (newVal) => {
  if (newVal) {
    nextTick(() => {
      updateCurrencyDropdownPosition()
    })
  }
})

// Add window resize handler
onMounted(async () => {
  // await currencyStore.getExchangeRate("EUR", 'VND', 1)
  bankList.value = await bankStore.getBanks()
  accountTypes.value = AccountType
  if(props.account){
    editingAccount.value = props.account
    newAccount.value = {
      accountName: props.account.accountName,
      accountType: props.account.accountType,
      balance: props.account.balance,
      initialBalance: props.account.initialBalance || props.account.balance,
      interpretation: props.account.interpretation || '',
      bankId: props.account.bank?.id || '',
      creditLimit: props.account.creditLimit || '',
      iconUrl: props.account.iconUrl || '',
      currencyCode: props.account.currency || 'VND',
      currencySymbol: props.account.currencySymbol || '₫'
    }
    
    // Log for debugging
    console.log('Account loaded in mounted:', props.account)
    console.log('Initial balance in mounted:', newAccount.value.initialBalance)
  }
  window.addEventListener('resize', () => {
    if (isTypeDropdownOpen.value) {
      updateTypeDropdownPosition()
    }
    if (isBankDropdownOpen.value) {
      updateBankDropdownPosition()
    }
    if (isCurrencyDropdownOpen.value) {
      updateCurrencyDropdownPosition()
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', () => {
    if (isTypeDropdownOpen.value) {
      updateTypeDropdownPosition()
    }
    if (isBankDropdownOpen.value) {
      updateBankDropdownPosition()
    }
    if (isCurrencyDropdownOpen.value) {
      updateCurrencyDropdownPosition()
    }
  })
})

// Watch for account changes to update form
watch(() => props.account, (newVal) => {
  if (newVal) {
    editingAccount.value = newVal
    newAccount.value = {
      accountName: newVal.accountName,
      accountType: newVal.accountType,
      balance: newVal.balance,
      initialBalance: newVal.initialBalance || newVal.balance,
      interpretation: newVal.interpretation || '',
      bankId: newVal.bank?.id || '',
      creditLimit: newVal.creditLimit || '',
      iconUrl: newVal.iconUrl || '',
      currencyCode: newVal.currency || 'VND',
      currencySymbol: newVal.currencySymbol || '₫'
    }
    
    // Log for debugging
    console.log('Account loaded:', newVal)
    console.log('Initial balance:', newAccount.value.initialBalance)
  }
}, { immediate: true })

const selectedBank = computed(() => {
  return bankList.value.find(bank => bank.id === newAccount.value.bankId)
})

const selectedCurrency = computed(() => {
  return currencies.value.find(c => c.code === newAccount.value.currencyCode)
})

const showBankField = computed(() => {
  return ['Tài khoản ngân hàng', 'Thẻ tín dụng'].includes(newAccount.value.accountType)
})

const showCreditLimitField = computed(() => {
  return newAccount.value.accountType === 'Thẻ tín dụng'
})

const balancePlaceholder = computed(() => {
  const symbol = getCurrencySymbol(newAccount.value.currencyCode)
  return newAccount.value.currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
})

const creditLimitPlaceholder = computed(() => {
  const symbol = getCurrencySymbol(newAccount.value.currencyCode)
  return newAccount.value.currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
})

const filteredCurrencies = computed(() => {
  if (!currencySearchQuery.value) return currencies.value
  const query = currencySearchQuery.value.toLowerCase()
  return currencies.value.filter(currency => 
    currency.code.toLowerCase().includes(query) || 
    currency.name.toLowerCase().includes(query)
  )
})

const formattedCreditLimit = computed({
  get: () => {
    if (!newAccount.value.creditLimit) return ''
    return formatWithCurrency(newAccount.value.creditLimit, newAccount.value.currencyCode)
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    newAccount.value.creditLimit = numericValue ? Number(numericValue) : ''
  }
})

const formattedBalance = computed({
  get: () => {
    // First check if newAccount.value exists to avoid errors
    if (!newAccount.value) return ''
    
    // Try to use initialBalance, fall back to balance if needed
    const balanceValue = newAccount.value.initialBalance !== undefined && newAccount.value.initialBalance !== null
      ? newAccount.value.initialBalance
      : newAccount.value.balance
      
    // Return empty string if no value is found
    if (balanceValue === undefined || balanceValue === null) return ''
    
    // Format with currency
    return formatWithCurrency(balanceValue, newAccount.value.currencyCode)
  },
  set: (value) => {
    // Chỉ lấy số từ chuỗi đã format
    const numericValue = value.replace(/[^\d]/g, '')
    newAccount.value.initialBalance = numericValue ? Number(numericValue) : ''
  }
})

const selectedAccountType = computed(() => {
  return accountTypes.value.find(t => t.name === newAccount.value.accountType)
})

const hasChanges = computed(() => {
  if (!editingAccount.value) return false
  
  const basicFieldsChanged = 
    editingAccount.value.accountName !== newAccount.value.accountName.trim() ||
    editingAccount.value.accountType !== newAccount.value.accountType ||
    editingAccount.value.interpretation !== newAccount.value.interpretation.trim() ||
    editingAccount.value.iconUrl !== newAccount.value.iconUrl ||
    (editingAccount.value.currency || 'VND') !== newAccount.value.currencyCode

  const bankFieldChanged = 
    ((editingAccount.value.bankId || editingAccount.value.bank?.id) || '') !== (newAccount.value.bankId || '')

  const creditLimitChanged = 
    (editingAccount.value.creditLimit || '') !== (newAccount.value.creditLimit || '')

  return basicFieldsChanged || bankFieldChanged || creditLimitChanged
})

const validateForm = () => {
  let isValid = true
  errors.value = {
    accountName: '',
    accountType: '',
    interpretation: '',
    iconUrl: '',
    bankId: '',
    creditLimit: ''
  }

  // Validate name
  if (!newAccount.value.accountName.trim()) {
    errors.value.accountName = 'Vui lòng nhập tên tài khoản'
    isValid = false
  } else if (newAccount.value.accountName.trim().length < 3) {
    errors.value.name = 'Tên tài khoản phải có ít nhất 3 ký tự'
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
    accountName: '',
    accountType: 'cash',
    balance: '',
    initialBalance: '',
    interpretation: '',
    bankId: '',
    creditLimit: '',
    iconUrl: '',
    currencyCode: 'VND'
  }
  errors.value = {
    accountName: '',
    bankId: '',
    creditLimit: ''
  }
  isConfirmingClose.value = false
  isTypeDropdownOpen.value = false
  isBankDropdownOpen.value = false
  isCurrencyDropdownOpen.value = false
  currencySearchQuery.value = ''
  emit('close')
}

const handleCurrencySelect = (currency) => {
  if (newAccount.value) {
    newAccount.value.currencyCode = currency.code
    newAccount.value.currencySymbol = currency.symbol
  }
  isCurrencyDropdownOpen.value = false
  currencySearchQuery.value = ''
}

const handleUpdate = async () => {
  if (!validateForm()) {
    return
  }

  if (!hasChanges.value) {
    resetAndClose()
    return
  }
  try {
    let data = {
      ...editingAccount.value,
      accountName: newAccount.value.accountName.trim(),
      accountType: newAccount.value.accountType,
      balance: Number(newAccount.value.balance),
      initialBalance: Number(newAccount.value.initialBalance),
      interpretation: newAccount.value.interpretation.trim(),
      iconUrl: newAccount.value.iconUrl,
      bankId: showBankField.value ? newAccount.value.bankId : undefined,
      creditLimit: showCreditLimitField.value ? Number(newAccount.value.creditLimit) : undefined,
      currency: newAccount.value.currencyCode,
      currencySymbol: newAccount.value.currencySymbol
    };
    await dictionaryBucketPaymentStore.update(data.id, data);
    emit('update', {
      ...editingAccount.value,
      accountName: newAccount.value.accountName.trim(),
      accountType: newAccount.value.accountType,
      balance: Number(newAccount.value.balance),
      initialBalance: Number(newAccount.value.initialBalance),
      interpretation: newAccount.value.interpretation.trim(),
      iconUrl: newAccount.value.iconUrl,
      bankId: showBankField.value ? newAccount.value.bankId : undefined,
      creditLimit: showCreditLimitField.value ? Number(newAccount.value.creditLimit) : undefined,
      currency: newAccount.value.currencyCode
    })
  } catch (err) {
    console.log(err)
  }

  resetAndClose()
}

const continueEditing = () => {
  isConfirmingClose.value = false
}

const filteredBanks = computed(() => {
  if (!bankSearchQuery.value) return bankList.value
  const query = bankSearchQuery.value.toLowerCase()
  return bankList.value.filter(bank => bank.shortName.toLowerCase().includes(query) || bank.name.toLowerCase().includes(query))
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
  
  // Xử lý dropdown tiền tệ
  const currencyDropdownEl = document.querySelector('.edit-currency-dropdown-container')
  if (currencyDropdownEl && !currencyDropdownEl.contains(event.target) && isCurrencyDropdownOpen.value) {
    isCurrencyDropdownOpen.value = false
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
                disabled
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors cursor-not-allowed bg-gray-100"
                :class="[
                  errors.balance ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50'
                ]"
                :placeholder="balancePlaceholder"
              />
              <!-- Add debug info during development -->
              <p v-if="false" class="mt-1 text-xs text-gray-500">
                Raw value: {{ newAccount?.initialBalance || 'none' }} ({{ typeof newAccount?.initialBalance }})
              </p>
              <p class="mt-1 text-xs text-gray-500 italic">
                Số dư ban đầu không thể thay đổi sau khi tài khoản đã được tạo
              </p>
              <p v-if="errors.balance" class="mt-1 text-sm text-danger">
                {{ errors.balance }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tên tài khoản <span class="text-danger">*</span>
              </label>
              <input 
                v-model="newAccount.accountName"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[

                  errors.accountName ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  newAccount.accountName ? 'bg-white' : 'bg-gray-50'
                ]"
                placeholder="Nhập tên tài khoản"
              />
              <p v-if="errors.accountName" class="mt-1 text-sm text-danger">
                {{ errors.accountName }}
              </p>
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Loại tài khoản
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
                    <Avatar :src="selectedAccountType?.icon" :alt="selectedAccountType?.name" size="sm" class="mr-2" />
                    <span>{{ selectedAccountType?.name }}</span>
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
                  :style="{
                    position: 'fixed',
                    top: `${typeDropdownPosition.top}px`,
                    left: `${typeDropdownPosition.left}px`,
                    width: `${typeDropdownPosition.width}px`,
                    maxHeight: '200px',
                    overflowY: 'auto',
                    zIndex: 50
                  }"
                  class="bg-white border border-gray-300 rounded-md shadow-lg"
                >
                  <div 
                    v-for="type in accountTypes" 
                    :key="type.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{'bg-primary/5': type.name === newAccount.accountType}"
                    @click="() => {
                      newAccount.accountType = type.name;
                      newAccount.iconUrl = type.icon;
                      isTypeDropdownOpen = false;
                    }"
                  >
                    <Avatar :src="type.icon" :alt="type.name" size="m" class="mr-2" />
                    <span>{{ type.name }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Currency Dropdown -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Loại tiền tệ <span class="text-danger">*</span>
              </label>
              <div class="relative edit-currency-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-not-allowed bg-gray-100"
                >
                  <div class="flex items-center flex-1">
                    <span>{{ selectedCurrency?.name }}</span>
                  </div>
                  <font-awesome-icon
                    :icon="['fas', 'chevron-down']"
                    class="text-gray-400 ml-2"
                  />
                </div>
              </div>
              <p class="mt-1 text-xs text-gray-500 italic">
                Loại tiền tệ không thể thay đổi sau khi tài khoản đã được tạo
              </p>
            </div>

            <div v-if="showBankField">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Ngân hàng
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
                    <template v-if="selectedBank">
                      <Avatar :src="selectedBank.logo" :alt="selectedBank.shortName" size="sm" class="mr-2" />
                      <span>{{ selectedBank.name }}</span>
                    </template>
                    <template v-else>
                      <font-awesome-icon :icon="['fas', 'building']" class="mr-2 text-lg text-gray-400" />
                      <span>Chọn ngân hàng</span>
                    </template>
                  </div>
                  <font-awesome-icon 
                    :icon="['fas', 'chevron-down']" 
                    class="text-gray-400 ml-2 transition-transform"
                    :class="{'rotate-180': isBankDropdownOpen}"
                  />
                </div>

                <div
                  v-if="isBankDropdownOpen"
                  :style="{
                    position: 'fixed',
                    top: `${bankDropdownPosition.top}px`,
                    left: `${bankDropdownPosition.left}px`,
                    width: `${bankDropdownPosition.width}px`,
                    maxHeight: '200px',
                    overflowY: 'auto',
                    zIndex: 50
                  }"
                  class="bg-white border border-gray-300 rounded-md shadow-lg"
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
                    <Avatar :src="bank.logo" :alt="bank.shortName" size="m" class="mr-2" />
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
                :placeholder="creditLimitPlaceholder"
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
                v-model="newAccount.interpretation"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                :class="newAccount.interpretation ? 'bg-white' : 'bg-gray-50'"
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