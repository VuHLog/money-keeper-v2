<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency, formatCurrencyWithSymbol } from '@/utils/formatters'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import Avatar from '@/views/components/Avatar.vue'
import { getVietnamDateTime } from '@/utils/DateUtil'
import { useExpenseRegularStore } from '@/store/ExpenseRegularStore'
import Swal from 'sweetalert2'
import { Currency } from "@constants/Currency.js"

const props = defineProps({
  isOpen: Boolean,
  accounts: {
    type: Array,
    required: true
  },
  initialFromAccount: {
    type: Object,
    default: null
  }
})

const expenseRegularStore = useExpenseRegularStore()
const currencies = ref(Currency)

const emit = defineEmits(['close', 'transfer'])

const transferData = ref({
  amount: '',
  fromAccount: null,
  toAccount: null,
  interpretation: '',
  transferDate: getVietnamDateTime()
})

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

const errors = ref({
  amount: '',
  fromAccount: '',
  toAccount: '',
  transferDate: ''
})

const isFromAccountDropdownOpen = ref(false)
const isToAccountDropdownOpen = ref(false)

// Watch for initial account changes
watch(() => props.initialFromAccount, (newVal) => {
  if (newVal) {
    transferData.value.fromAccount = newVal
  }
}, { immediate: true })

const formattedTransferAmount = computed({
  get: () => {
    if (!transferData.value.amount) return ''
    // Format using the currency of the selected "from" account
    if (transferData.value.fromAccount) {
      return formatWithCurrency(
        transferData.value.amount,
        transferData.value.fromAccount.currency,
        transferData.value.fromAccount.currencySymbol
      )
    }
    // Fallback to default VND format if no account is selected
    return formatCurrency(Number(transferData.value.amount))
  },
  set: (value) => {
    // Chỉ lấy số từ chuỗi đã format
    const numericValue = value.replace(/[^\d]/g, '')
    transferData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

// Compute placeholder based on selected account currency
const amountPlaceholder = computed(() => {
  if (transferData.value.fromAccount) {
    const symbol = transferData.value.fromAccount.currencySymbol
    const currencyCode = transferData.value.fromAccount.currency
    return currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
  }
  return '0 ₫' // Default placeholder
})

const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: '',
    fromAccount: '',
    toAccount: '',
    transferDate: ''
  }

  // Validate amount
  if (!transferData.value.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền chuyển khoản'
    isValid = false
  } else if (Number(transferData.value.amount) <= 0) {
    errors.value.amount = 'Số tiền chuyển khoản phải lớn hơn 0'
    isValid = false
  } else if (transferData.value.fromAccount && Number(transferData.value.amount) > transferData.value.fromAccount.balance) {
    errors.value.amount = 'Số dư không đủ để thực hiện giao dịch'
    isValid = false
  }

  // Validate accounts
  if (!transferData.value.fromAccount) {
    errors.value.fromAccount = 'Vui lòng chọn tài khoản chuyển'
    isValid = false
  }

  if (!transferData.value.toAccount) {
    errors.value.toAccount = 'Vui lòng chọn tài khoản nhận'
    isValid = false
  }

  if (transferData.value.fromAccount && transferData.value.toAccount && 
      transferData.value.fromAccount.id === transferData.value.toAccount.id) {
    errors.value.toAccount = 'Tài khoản nhận phải khác tài khoản chuyển'
    isValid = false
  }

  // Validate transfer date
  if (!transferData.value.transferDate) {
    errors.value.transferDate = 'Vui lòng chọn ngày và giờ chuyển khoản'
    isValid = false
  }

  return isValid
}

const handleClose = () => {
  transferData.value = {
    amount: '',
    fromAccount: null,
    toAccount: null,
    interpretation: '',
    transferDate: getVietnamDateTime()
  }
  errors.value = {
    amount: '',
    fromAccount: '',
    toAccount: '',
    transferDate: ''
  }
  emit('close')
}

const handleTransfer = async () => {
  if (!validateForm()) {
    return
  }

  try{
    let data = {
      amount: Number(transferData.value.amount),
      dictionaryBucketPaymentId: transferData.value.fromAccount.id,
      beneficiaryAccountId: transferData.value.toAccount.id,
      interpretation: transferData.value.interpretation.trim(),
      expenseDate: transferData.value.transferDate
    }
    await expenseRegularStore.createTransfer(data)
    emit('transfer', {
      ...data,
      fromAccount: transferData.value.fromAccount,
      toAccount: transferData.value.toAccount
    })
    Swal.fire({
        title: "Thành công",
        text: "Bạn đã thêm chuyển khoản thành công!",
        icon: "success",
      });
    handleClose()
  } catch (error) {
    console.error('Error transferring funds:', error)
    Swal.fire({
      title: "Lỗi",
      text: "Lỗi khi chuyển khoản!",
      icon: "error",
    });
  }

}

// Function để đóng tất cả dropdown khi click ra ngoài
const handleClickOutside = (event) => {
  // Xử lý dropdown tài khoản nguồn
  const fromDropdownEl = document.querySelector('.from-account-dropdown')
  if (fromDropdownEl && !fromDropdownEl.contains(event.target) && isFromAccountDropdownOpen.value) {
    isFromAccountDropdownOpen.value = false
  }
  
  // Xử lý dropdown tài khoản đích
  const toDropdownEl = document.querySelector('.to-account-dropdown')
  if (toDropdownEl && !toDropdownEl.contains(event.target) && isToAccountDropdownOpen.value) {
    isToAccountDropdownOpen.value = false
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
        class="bg-white rounded-lg shadow-xl w-full max-w-md"
        @click.stop
      >
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h3 class="text-lg font-semibold text-text">Chuyển khoản</h3>
          <button 
            @click="handleClose"
            class="text-text-secondary hover:text-text"
          >
            <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
          </button>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4">
          <form @submit.prevent="handleTransfer" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Số tiền chuyển khoản <span class="text-danger">*</span>
              </label>
              <!-- For Amount Input -->
              <input 
                v-model="formattedTransferAmount"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[
                  errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  formattedTransferAmount ? 'bg-white' : 'bg-gray-50'
                ]"
                :placeholder="amountPlaceholder"
              />
              <p v-if="errors.amount" class="mt-1 text-sm text-danger">
                {{ errors.amount }}
              </p>
            </div>

            <!-- From Account -->
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tài khoản chuyển <span class="text-danger">*</span>
              </label>
              <div class="relative from-account-dropdown select-none">
                <div 
                  class="w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[

                    {'ring-1 ring-primary/20 border-primary/50': isFromAccountDropdownOpen},
                    errors.fromAccount ? 'border-danger/50' : ''
                  ]"
                  @click="isFromAccountDropdownOpen = !isFromAccountDropdownOpen"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center space-x-2">
                      <div v-if="transferData.fromAccount" class="flex items-center space-x-2">
                        <Avatar :src="transferData.fromAccount.iconUrl" :alt="transferData.fromAccount.accountName" size="m" />
                        <span>{{ transferData.fromAccount.accountName }} ({{ formatCurrencyWithSymbol(transferData.fromAccount.balance, transferData.fromAccount.currency, transferData.fromAccount.currencySymbol) }})</span>
                      </div>
                      <span v-else class="text-text-secondary">Chọn tài khoản chuyển</span>
                    </div>
                    <font-awesome-icon 
                      :icon="['fas', 'chevron-down']" 
                      class="text-gray-400 transition-transform"
                      :class="{'rotate-180': isFromAccountDropdownOpen}"
                    />
                  </div>
                </div>
                <!-- Dropdown Options -->
                <div 
                  v-if="isFromAccountDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 max-h-48 overflow-y-auto"
                >
                  <div 
                    v-for="account in accounts" 
                    :key="account.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="[

                      {'bg-primary/5': account.id === transferData.fromAccount?.id},
                      {'opacity-50 cursor-not-allowed': account.id === transferData.toAccount?.id}
                    ]"
                    @click="() => {
                      if (account.id !== transferData.toAccount?.id) {
                        transferData.fromAccount = account;
                        isFromAccountDropdownOpen = false;
                      }
                    }"
                  >
                    <Avatar :src="account.iconUrl" :alt="account.accountName" size="m" />
                    <span>{{ account.accountName }} ({{ formatCurrencyWithSymbol(account.balance, account.currency, account.currencySymbol) }})</span>
                  </div>
                </div>
              </div>
              <p v-if="errors.fromAccount" class="mt-1 text-sm text-danger">
                {{ errors.fromAccount }}
              </p>
            </div>

            <!-- To Account -->
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tài khoản nhận <span class="text-danger">*</span>
              </label>
              <div class="relative to-account-dropdown select-none">
                <div 
                  class="w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[

                    {'ring-1 ring-primary/20 border-primary/50': isToAccountDropdownOpen},
                    errors.toAccount ? 'border-danger/50' : ''
                  ]"
                  @click="isToAccountDropdownOpen = !isToAccountDropdownOpen"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center space-x-2">
                      <div v-if="transferData.toAccount" class="flex items-center space-x-2">
                        <Avatar :src="transferData.toAccount.iconUrl" :alt="transferData.toAccount.accountName" size="m" />
                        <span>{{ transferData.toAccount.accountName }} ({{ formatCurrency(transferData.toAccount.balance) }})</span>
                      </div>
                      <span v-else class="text-text-secondary">Chọn tài khoản nhận</span>
                    </div>
                    <font-awesome-icon 
                      :icon="['fas', 'chevron-down']" 
                      class="text-gray-400 transition-transform"
                      :class="{'rotate-180': isToAccountDropdownOpen}"
                    />
                  </div>
                </div>
                <!-- Dropdown Options -->
                <div 
                  v-if="isToAccountDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 max-h-48 overflow-y-auto"
                >
                  <div 
                    v-for="account in accounts" 
                    :key="account.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="[

                      {'bg-primary/5': account.id === transferData.toAccount?.id},
                      {'opacity-50 cursor-not-allowed': account.id === transferData.fromAccount?.id}
                    ]"
                    @click="() => {
                      if (account.id !== transferData.fromAccount?.id) {
                        transferData.toAccount = account;
                        isToAccountDropdownOpen = false;
                      }
                    }"
                  >
                    <Avatar :src="account.iconUrl" :alt="account.accountName" size="m" />
                    <span>{{ account.accountName }} ({{ formatCurrency(account.balance) }})</span>
                  </div>
                </div>
              </div>
              <p v-if="errors.toAccount" class="mt-1 text-sm text-danger">
                {{ errors.toAccount }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Ngày chuyển khoản <span class="text-danger">*</span>
              </label>
              <div class="date-picker-container">
                <el-date-picker
                  v-model="transferData.transferDate"
                  type="datetime"
                  format="DD/MM/YYYY HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="'Chọn ngày và giờ chuyển khoản'"
                  :clearable="false"
                  class="w-full"
                  :class="[
                    errors.transferDate ? 'border-danger/50' : 'border-gray-100',
                    transferData.transferDate ? 'bg-white' : 'bg-gray-50'
                  ]"
                />
              </div>
              <p v-if="errors.transferDate" class="mt-1 text-sm text-danger">
                {{ errors.transferDate }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Diễn giải
              </label>
              <!-- For Description Input -->
              <input 
                v-model="transferData.interpretation"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                :class="transferData.interpretation ? 'bg-white' : 'bg-gray-50'"
                placeholder="Nhập diễn giải cho giao dịch"
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
            @click="handleTransfer"
            class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 flex items-center space-x-2"
          >
            <font-awesome-icon :icon="['fas', 'exchange-alt']" />
            <span>Chuyển khoản</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
:deep(.el-input__wrapper) {
  background-color: transparent;
  box-shadow: none !important;
  padding: 0;
}

:deep(.el-input__inner) {
  height: 38px;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

:deep(.el-input) {
  --el-input-border-color: var(--el-border-color);
  --el-input-hover-border-color: var(--el-border-color-hover);
  --el-input-focus-border-color: var(--el-color-primary);
}

.date-picker-container {
  @apply w-full;
}

.date-picker-container :deep(.el-date-editor) {
  @apply w-full;
}
</style>