<script setup>
import { ref, computed, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'

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

const emit = defineEmits(['close', 'transfer'])

const transferData = ref({
  amount: '',
  fromAccount: null,
  toAccount: null,
  description: ''
})

const errors = ref({
  amount: '',
  fromAccount: '',
  toAccount: ''
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
    return formatCurrency(Number(transferData.value.amount))
  },
  set: (value) => {
    // Chỉ lấy số từ chuỗi đã format
    const numericValue = value.replace(/[^\d]/g, '')
    transferData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: '',
    fromAccount: '',
    toAccount: ''
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

  return isValid
}

const handleClose = () => {
  transferData.value = {
    amount: '',
    fromAccount: null,
    toAccount: null,
    description: ''
  }
  errors.value = {
    amount: '',
    fromAccount: '',
    toAccount: ''
  }
  emit('close')
}

const handleTransfer = () => {
  if (!validateForm()) {
    return
  }

  emit('transfer', {
    amount: Number(transferData.value.amount),
    fromAccount: transferData.value.fromAccount,
    toAccount: transferData.value.toAccount,
    description: transferData.value.description.trim()
  })

  handleClose()
}

const getAccountIcon = (type) => {
  switch (type) {
    case 'cash':
      return 'money-bill-wave'
    case 'bank':
      return 'university'
    case 'credit':
      return 'credit-card'
    default:
      return 'wallet'
  }
}

const getAccountColor = (type) => {
  switch (type) {
    case 'cash':
      return 'text-success'
    case 'bank':
      return 'text-primary'
    case 'credit':
      return 'text-warning'
    default:
      return 'text-text-secondary'
  }
}
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
              <input 
                v-model="formattedTransferAmount"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20"
                :class="[
                  errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50'
                ]"
                placeholder="0 ₫"
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
              <div class="relative">
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
                        <font-awesome-icon 
                          :icon="['fas', getAccountIcon(transferData.fromAccount.type)]"
                          :class="[getAccountColor(transferData.fromAccount.type)]"
                        />
                        <span>{{ transferData.fromAccount.name }} ({{ formatCurrency(transferData.fromAccount.balance) }})</span>
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
                    <font-awesome-icon 
                      :icon="['fas', getAccountIcon(account.type)]"
                      :class="[getAccountColor(account.type)]"
                      class="mr-2"
                    />
                    <span>{{ account.name }} ({{ formatCurrency(account.balance) }})</span>
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
              <div class="relative">
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
                        <font-awesome-icon 
                          :icon="['fas', getAccountIcon(transferData.toAccount.type)]"
                          :class="[getAccountColor(transferData.toAccount.type)]"
                        />
                        <span>{{ transferData.toAccount.name }} ({{ formatCurrency(transferData.toAccount.balance) }})</span>
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
                    <font-awesome-icon 
                      :icon="['fas', getAccountIcon(account.type)]"
                      :class="[getAccountColor(account.type)]"
                      class="mr-2"
                    />
                    <span>{{ account.name }} ({{ formatCurrency(account.balance) }})</span>
                  </div>
                </div>
              </div>
              <p v-if="errors.toAccount" class="mt-1 text-sm text-danger">
                {{ errors.toAccount }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Diễn giải
              </label>
              <input 
                v-model="transferData.description"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
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