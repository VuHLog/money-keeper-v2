<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'
import { useDictionaryBucketPaymentStore } from '@stores/DictionaryBucketPaymentStore.js'
import { useBankStore } from "@stores/BankStore.js"
import { AccountType } from "@constants/AccountType.js"
import Avatar from '@/views/components/Avatar.vue'
import Swal from 'sweetalert2'
import { Currency } from "@constants/Currency.js"

const props = defineProps({
  isOpen: Boolean,
})

const emit = defineEmits(['close', 'add'])
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore();
const bankStore = useBankStore()
const bankList = ref([]);
const accountTypes = ref(AccountType);

// Currency options
const currencies = ref(Currency)

const newAccount = ref({
  balance: "",
  creditLimit: "",
  accountName: "",
  accountType: "",
  interpretation: "",
  bankId: "",
  iconUrl: "",
  currencyCode: "VND",
  currencySymbol: "₫"
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

const bankSearchQuery = ref('')
const currencySearchQuery = ref('')
const errors = ref({
  accountName: '',
  balance: '',
  bankId: '',
  creditLimit: ''
})

const isTypeDropdownOpen = ref(false)
const isBankDropdownOpen = ref(false)
const isCurrencyDropdownOpen = ref(false)

const typeDropdownPosition = ref({ top: 0, left: 0, width: 0 })
const bankDropdownPosition = ref({ top: 0, left: 0, width: 0 })
const currencyDropdownPosition = ref({ top: 0, left: 0, width: 0 })

const updateTypeDropdownPosition = () => {
  const trigger = document.querySelector('.type-dropdown-container')
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
  const trigger = document.querySelector('.bank-dropdown-container')
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
  const trigger = document.querySelector('.currency-dropdown-container')
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

// Thêm và xóa event listener khi component được mount và unmount
onMounted(async () => {
  let accountTypeInit = accountTypes.value.find(t => t.name === "Tiền mặt");
  newAccount.value.accountType = accountTypeInit.name
  newAccount.value.iconUrl = accountTypeInit.icon
  newAccount.value.currencyCode = "VND"
  newAccount.value.currencySymbol = "₫"
  bankList.value = await bankStore.getBanks()
  document.addEventListener('mousedown', handleClickOutside)

  // Add window resize handler
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
  document.removeEventListener('mousedown', handleClickOutside)
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

const formattedBalance = computed({
  get: () => {
    if (!newAccount.value.balance) return ''
    return formatWithCurrency(newAccount.value.balance, newAccount.value.currencyCode)
  },
  set: (value) => {
    // Chỉ lấy số từ chuỗi đã format
    const numericValue = value.replace(/[^\d]/g, '')
    newAccount.value.balance = numericValue ? Number(numericValue) : ''
  }
})

const selectedAccountType = computed(() => {
  return accountTypes.value.find(t => t.name === newAccount.value.accountType)
})

const selectedBank = computed(() => {
  const bankId = newAccount.value?.bankId
  return bankList.value.find(bank => bank.id === bankId);
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

const balancePlaceholder = computed(() => {
  const symbol = getCurrencySymbol(newAccount.value.currencyCode)
  return newAccount.value.currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
})

const creditLimitPlaceholder = computed(() => {
  const symbol = getCurrencySymbol(newAccount.value.currencyCode)
  return newAccount.value.currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
})

const filteredBanks = computed(() => {
  if (!bankSearchQuery.value) return bankList.value
  const query = bankSearchQuery.value.toLowerCase()
  return bankList.value.filter(bank => bank.shortName.toLowerCase().includes(query) || bank.name.toLowerCase().includes(query))
})

const filteredCurrencies = computed(() => {
  if (!currencySearchQuery.value) return currencies.value
  const query = currencySearchQuery.value.toLowerCase()
  return currencies.value.filter(currency => 
    currency.code.toLowerCase().includes(query) || 
    currency.name.toLowerCase().includes(query)
  )
})

const handleBankSelect = (bank) => {
  if (newAccount.value) {
    newAccount.value.bankId = bank.id
  }
  isBankDropdownOpen.value = false
  bankSearchQuery.value = ''
}

const handleTypeSelect = (type) => {
  if (newAccount.value) {
    newAccount.value.accountType = type.name
    newAccount.value.iconUrl = type.icon
    console.log(newAccount.value.accountType, newAccount.value.iconUrl);
    
    // Reset bank selection when changing type
    if (!['Tài khoản ngân hàng', 'Thẻ tín dụng'].includes(type.name)) {
      newAccount.value.bankId = ''
    }
  }
  isTypeDropdownOpen.value = false
}

const handleCurrencySelect = (currency) => {
  if (newAccount.value) {
    newAccount.value.currencyCode = currency.code
    newAccount.value.currencySymbol = currency.symbol
  }
  isCurrencyDropdownOpen.value = false
}

const validateForm = () => {
  let isValid = true
  errors.value = {
    accountName: '',
    balance: '',
    bankId: '',
    creditLimit: ''
  }

  // Validate name
  if (!newAccount.value.accountName.trim()) {
    errors.value.accountName = 'Vui lòng nhập tên tài khoản'
    isValid = false
  } else if (newAccount.value.accountName.trim().length < 3) {
    errors.value.accountName = 'Tên tài khoản phải có ít nhất 3 ký tự'
    isValid = false
  }

  // Validate balance
  if (newAccount.value.balance === '') {
    errors.value.balance = 'Vui lòng nhập số dư ban đầu'
    isValid = false
  } else if (Number(newAccount.value.balance) < 0) {
    errors.value.balance = 'Số dư ban đầu không được âm'
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
  newAccount.value = {
    accountName: '',
    accountType: 'Tiền mặt',
    iconUrl: "https://res.cloudinary.com/cloud1412/image/upload/v1741273907/cash_ifor6b.png",
    balance: '',
    interpretation: '',
    bankId: '',
    creditLimit: '',
    currencyCode: 'VND',
    currencySymbol: '₫'
  }
  errors.value = {
    accountName: '',
    balance: '',
    bankId: '',
    creditLimit: ''
  }
  bankSearchQuery.value = ''
  currencySearchQuery.value = ''
  isTypeDropdownOpen.value = false
  isBankDropdownOpen.value = false
  isCurrencyDropdownOpen.value = false
  emit('close')
}

const handleAdd = async () => {
  if (!validateForm()) {
    return
  }

  try {
    await dictionaryBucketPaymentStore.createBucketPayment({...newAccount.value, currency: newAccount.value.currencyCode});
    emit('add')
    Swal.fire({
      title: "Thành công",
      text: "Bạn đã thêm tài khoản thành công!",
      icon: "success",
    });
    handleClose()
  } catch (error) {
    console.error('Error adding account:', error)
    Swal.fire({
      title: "Thất bại",
      text: "Bạn đã thêm tài khoản không thành công!",
      icon: "error",
    });
    router.push("/account")
  } finally {
    newAccount.value = {
      accountName: '',
      accountType: 'Tiền mặt',
      balance: '',
      interpretation: '',
      bankId: '',
      creditLimit: '',
      iconUrl: '',
      currencyCode: 'VND'
    }
    errors.value = {
      accountName: '',
      balance: '',
      bankId: '',
      creditLimit: ''
    }
    bankSearchQuery.value = ''
    currencySearchQuery.value = ''
    isTypeDropdownOpen.value = false
    isBankDropdownOpen.value = false
    isCurrencyDropdownOpen.value = false
  }
}

// Function để đóng tất cả dropdown khi click ra ngoài
const handleClickOutside = (event) => {
  // Xử lý dropdown loại tài khoản
  const typeDropdownEl = document.querySelector('.type-dropdown-container')
  if (typeDropdownEl && !typeDropdownEl.contains(event.target) && isTypeDropdownOpen.value) {
    isTypeDropdownOpen.value = false
  }

  // Xử lý dropdown ngân hàng
  const bankDropdownEl = document.querySelector('.bank-dropdown-container')
  if (bankDropdownEl && !bankDropdownEl.contains(event.target) && isBankDropdownOpen.value) {
    isBankDropdownOpen.value = false
  }

  // Xử lý dropdown tiền tệ
  const currencyDropdownEl = document.querySelector('.currency-dropdown-container')
  if (currencyDropdownEl && !currencyDropdownEl.contains(event.target) && isCurrencyDropdownOpen.value) {
    isCurrencyDropdownOpen.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click="handleClose">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4" @click.stop>
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h3 class="text-lg font-semibold text-text">Thêm tài khoản mới</h3>
          <button @click="handleClose" class="text-text-secondary hover:text-text">
            <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
          </button>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4">
          <form @submit.prevent="handleAdd" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Số dư ban đầu <span class="text-danger">*</span>
              </label>
              <input v-model="formattedBalance" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                :class="[
                  errors.balance ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50'
                ]" :placeholder="balancePlaceholder" min="0" />
              <p v-if="errors.balance" class="mt-1 text-sm text-danger">
                {{ errors.balance }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tên tài khoản <span class="text-danger">*</span>
              </label>
              <input v-model="newAccount.accountName" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                :class="[
                  errors.accountName ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50'
                ]" placeholder="Nhập tên tài khoản" />
              <p v-if="errors.accountName" class="mt-1 text-sm text-danger">
                {{ errors.accountName }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Loại tài khoản <span class="text-danger">*</span>
              </label>
              <div class="relative type-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="{ 'ring-1 ring-primary/20 border-primary/50': isTypeDropdownOpen }"
                  @click="isTypeDropdownOpen = !isTypeDropdownOpen">
                  <div class="flex items-center flex-1">
                    <Avatar :src="selectedAccountType?.icon" :alt="selectedAccountType?.name" size="sm" class="mr-2" />
                    <span>{{ selectedAccountType?.name }}</span>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isTypeDropdownOpen }" />
                </div>

                <div v-if="isTypeDropdownOpen"
                  class="fixed z-[60] w-[calc(100%-2rem)] max-w-md mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 max-h-64 overflow-y-auto"
                  :style="{
                    top: typeDropdownPosition.top + 'px',
                    left: typeDropdownPosition.left + 'px',
                    width: typeDropdownPosition.width + 'px'
                  }"
                >
                  <div v-for="type in accountTypes" :key="type.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{ 'bg-primary/5': type.name === newAccount?.accountType }"
                    @click="handleTypeSelect(type)">
                    <Avatar :src="type.icon" :alt="type.name" size="m" class="mr-2" />
                    <span>{{ type.name }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="select-none">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Loại tiền tệ <span class="text-danger">*</span>
              </label>
              <div class="relative currency-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="{ 'ring-1 ring-primary/20 border-primary/50': isCurrencyDropdownOpen }"
                  @click="isCurrencyDropdownOpen = !isCurrencyDropdownOpen">
                  <div class="flex items-center flex-1">
                    <span>{{ selectedCurrency?.name }}</span>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isCurrencyDropdownOpen }" />
                </div>

                <div v-if="isCurrencyDropdownOpen"
                  class="fixed z-[60] w-[calc(100%-2rem)] max-w-md mt-1 bg-white border border-gray-200 rounded-lg shadow-lg divide-y divide-gray-100"
                  :style="{
                    top: currencyDropdownPosition.top + 'px',
                    left: currencyDropdownPosition.left + 'px',
                    width: currencyDropdownPosition.width + 'px'
                  }"
                >
                  <div class="p-2">
                    <input v-model="currencySearchQuery" type="text"
                      class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                      placeholder="Tìm kiếm tiền tệ..." @click.stop />
                  </div>
                  <div class="max-h-48 overflow-y-auto py-1">
                    <div v-for="currency in filteredCurrencies" :key="currency.code"
                      class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                      :class="{ 'bg-primary/5': currency.code === newAccount.currencyCode }"
                      @click="handleCurrencySelect(currency)">
                      <span class="mr-2 font-bold">{{ currency.symbol }}</span>
                      <span>{{ currency.name }}</span>
                    </div>
                    <div v-if="filteredCurrencies.length === 0" class="px-3 py-2 text-text-secondary text-sm">
                      Không tìm thấy loại tiền tệ
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="showBankField">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Ngân hàng <span class="text-danger">*</span>
              </label>
              <div class="relative bank-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="{ 'ring-1 ring-primary/20 border-primary/50': isBankDropdownOpen }"
                  @click="isBankDropdownOpen = !isBankDropdownOpen">
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
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isBankDropdownOpen }" />
                </div>

                <div v-if="isBankDropdownOpen"
                  class="fixed z-[60] w-[calc(100%-2rem)] max-w-md mt-1 bg-white border border-gray-200 rounded-lg shadow-lg divide-y divide-gray-100"
                  :style="{
                    top: bankDropdownPosition.top + 'px',
                    left: bankDropdownPosition.left + 'px',
                    width: bankDropdownPosition.width + 'px'
                  }"
                >
                  <div class="p-2">
                    <input v-model="bankSearchQuery" type="text"
                      class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                      placeholder="Tìm kiếm ngân hàng..." @click.stop />
                  </div>
                  <div class="max-h-48 overflow-y-auto py-1">
                    <div v-for="bank in filteredBanks" :key="bank.id"
                      class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                      :class="{ 'bg-primary/5': bank.id === newAccount.value?.bankId }" @click="handleBankSelect(bank)">
                      <Avatar :src="bank.logo" :alt="bank.shortName" size="m" class="mr-2" />
                      <span>{{ bank.name }}</span>
                    </div>
                    <div v-if="filteredBanks.length === 0" class="px-3 py-2 text-text-secondary text-sm">
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
              <input v-model="formattedCreditLimit" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                :class="[
                  errors.creditLimit ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50'
                ]" :placeholder="creditLimitPlaceholder" min="0" />
              <p v-if="errors.creditLimit" class="mt-1 text-sm text-danger">
                {{ errors.creditLimit }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Diễn giải
              </label>
              <input v-model="newAccount.interpretation" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                placeholder="Nhập diễn giải cho tài khoản" />
            </div>
          </form>
        </div>

        <!-- Modal Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
          <button @click="handleClose" class="px-4 py-2 text-text-secondary hover:text-text">
            Hủy
          </button>
          <button @click="handleAdd" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
            Thêm tài khoản
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>