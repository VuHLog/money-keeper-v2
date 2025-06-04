<script setup>
import { ref, computed, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faMoneyBill, faXmark } from '@fortawesome/free-solid-svg-icons'
import { formatCurrencyWithSymbol } from '@/utils/formatters'
import { useFinancialGoalStore } from '@/store/FinancialGoalStore'

library.add(faMoneyBill, faXmark)

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  goal: {
    type: Object,
    default: null
  }
})

const financialGoalStore = useFinancialGoalStore()
const emit = defineEmits(['close', 'deposit'])

// Data for form (simplified - removed accounts)
const formData = ref({
  amount: '',
  note: ''
})

const errors = ref({
  amount: ''
})

// Helper function to format currency based on currency code
const formatWithCurrency = (value, currencyCode, currencySymbol) => {
  if (value === null || value === undefined || value === '') return ''
  return formatCurrencyWithSymbol(Number(value), currencyCode, currencySymbol)
}

// Format currency for input based on selected account
const formattedAmount = computed({
  get: () => {
    if (!formData.value.amount) return ''
    
    // Format using the goal's currency
    return formatWithCurrency(
      formData.value.amount,
      props.goal?.currency || 'VND',
      props.goal?.currencySymbol || '₫'
    )
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

// Compute placeholder based on goal's currency
const amountPlaceholder = computed(() => {
  if (props.goal?.currencySymbol && props.goal?.currency) {
    const symbol = props.goal.currencySymbol
    const currencyCode = props.goal.currency
    return currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
  }
  return '0 ₫' // Default placeholder
})

// Reset form
const resetForm = () => {
  formData.value = {
    amount: '',
    note: ''
  }
  errors.value = {
    amount: ''
  }
}

// Validate form before submitting
const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: ''
  }

  // Validate amount
  if (!formData.value.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền nạp'
    isValid = false
  } else if (formData.value.amount <= 0) {
    errors.value.amount = 'Số tiền phải lớn hơn 0'
    isValid = false
  }

  return isValid
}

// Handle close modal
const handleClose = () => {
  resetForm()
  emit('close')
}

// Handle deposit button
const handleDeposit = async () => {
  if (!validateForm()) return
  
  const depositData = {
    financialGoalId: props.goal.id,
    amount: formData.value.amount,
    dictionaryBucketPaymentId: props.goal.bucketPayment.id,
    interpretation: formData.value.note.trim(),
  }

  let data = null
  try {
    data = await financialGoalStore.createDeposit(depositData.financialGoalId, depositData)
  } catch (error) {
    if(error.response?.data?.code === 10002){
        errors.value.amount = "Tài khoản không đủ số dư";
    }
  }
  
  emit('deposit', data)
  resetForm()
}

// Simplified watch - just reset form when modal closes
watch(() => props.isOpen, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})
</script>

<template>
  <div v-if="isOpen && goal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="handleClose">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="bg-success/10 p-2 rounded-lg">
            <font-awesome-icon :icon="['fas', 'money-bill']" class="text-success text-lg" />
          </div>
          <h3 class="text-lg font-semibold text-text">
            💸 Nạp tiền vào "{{ goal.name || goal.title }}"
          </h3>
        </div>
        <button @click="handleClose" class="text-text-secondary hover:text-text transition-colors">
          <font-awesome-icon :icon="['fas', 'xmark']" class="text-xl" />
        </button>
      </div>

      <!-- Modal Content -->
      <div class="px-6 py-4">
        <form @submit.prevent="handleDeposit" class="space-y-4">
          <!-- Amount -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Số tiền nạp <span class="text-danger">*</span>
            </label>
            <input 
              v-model="formattedAmount" 
              type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
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

          <!-- Note -->
          <div>
            <label class="text-start block text-sm font-medium text-text-secondary mb-1">
              Ghi chú (tuỳ chọn)
            </label>
            <input 
              v-model="formData.note" 
              type="text"
              class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
              :class="formData.note ? 'bg-white' : 'bg-gray-50'" 
              placeholder="Thêm ghi chú cho lần nạp này..."
            />
          </div>
        </form>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
        <button 
          @click="handleClose" 
          class="px-4 py-2 text-text-secondary hover:text-text transition-colors"
        >
          Hủy
        </button>
        <button 
          @click="handleDeposit" 
          class="px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors"
        >
          Xác nhận nạp
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Success color utilities */
.text-success {
  color: #059669;
}

.bg-success {
  background-color: #059669;
}

.bg-success\/90 {
  background-color: rgba(5, 150, 105, 0.9);
}

.bg-success\/10 {
  background-color: rgba(5, 150, 105, 0.1);
}

/* Danger color utilities */
.text-danger {
  color: #DC2626;
}

.bg-danger\/10 {
  background-color: rgba(220, 38, 38, 0.1);
}

.border-danger\/20 {
  border-color: rgba(220, 38, 38, 0.2);
}

.border-danger\/50 {
  border-color: rgba(220, 38, 38, 0.5);
}

/* Remove input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>