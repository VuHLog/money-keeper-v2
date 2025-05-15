<script setup>
import { defineProps, defineEmits } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'
import { formatCurrency } from '@/utils/formatters'
import { formatDateToVietnam } from '@/utils/DateUtil'
import Avatar from '@/views/components/Avatar.vue'

library.add(faExclamationTriangle)

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  transaction: {
    type: Object,
    required: true
  },
  transactionType: {
    type: String,
    required: true,
    validator: (value) => ['expense', 'revenue'].includes(value)
  }
})

const emit = defineEmits(['close', 'confirm'])

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<template>
  <div v-if="isOpen && transaction" class="fixed inset-0 z-50 overflow-y-auto">
    <!-- Backdrop -->
    <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity" @click="handleClose"></div>

    <!-- Modal -->
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="relative bg-white rounded-xl shadow-xl max-w-lg w-full mx-auto">
        <!-- Header -->
        <div class="p-6 text-center">
          <div class="w-16 h-16 bg-danger/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-2xl text-danger" />
          </div>
          <h3 class="text-lg font-medium text-text mb-2">
            Xác nhận xóa giao dịch {{ transactionType === 'revenue' ? 'thu' : 'chi' }}?
          </h3>
          <p class="text-text-secondary">
            Bạn có chắc chắn muốn xóa giao dịch này? Hành động này không thể hoàn tác.
          </p>
        </div>

        <!-- Transaction Details -->
        <div class="px-6 py-4 bg-gray-50 border-t border-b border-gray-100">
          <div class="flex items-start space-x-4">
            <!-- Category Icon -->
            <div class="flex-shrink-0">
              <Avatar 
                :src="transactionType === 'revenue' ? transaction.dictionaryRevenue?.iconUrl || 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/noInfo_vwxabr.svg' : transaction.dictionaryExpense?.iconUrl || 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/noInfo_vwxabr.svg'"
                :alt="transactionType === 'revenue' ? transaction.dictionaryRevenue?.name : transaction.dictionaryExpense?.name"
                size="m"
              />
            </div>

            <!-- Transaction Info -->
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-text">
                {{ transactionType === 'revenue' ? transaction.dictionaryRevenue?.name || 'Danh mục thu không xác định' : transaction.dictionaryExpense?.name || 'Danh mục chi không xác định' }}
              </p>
              <p class="text-sm text-text-secondary">
                {{ formatDateToVietnam(transactionType === 'revenue' ? transaction.revenueDate : transaction.expenseDate) }}
              </p>
              <p class="text-sm text-text-secondary mt-1">
                {{ transaction.interpretation || 'Không có ghi chú' }}
              </p>
            </div>

            <!-- Amount -->
            <div class="flex-shrink-0">
              <p :class="[
                transactionType === 'revenue' ? 'text-success' : 'text-danger',
                'text-sm font-medium'
              ]">
                {{ formatCurrency(transaction.amount) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="p-6 flex justify-end space-x-3">
          <button
            type="button"
            class="px-4 py-2 bg-gray-100 text-text-secondary rounded-lg hover:bg-gray-200 transition-colors"
            @click="handleClose"
          >
            Hủy
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger/90 transition-colors"
            @click="handleConfirm"
          >
            Xóa
          </button>
        </div>
      </div>
    </div>
  </div>
</template> 