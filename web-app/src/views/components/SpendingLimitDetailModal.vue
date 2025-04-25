<script setup>
import { computed } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'
import { ElScrollbar } from 'element-plus'

const props = defineProps({
  isOpen: Boolean,
  limit: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN')
}

// TODO: Thay thế bằng dữ liệu thực từ API
const mockTransactions = computed(() => {
  if (!props.limit) return []
  return [
    {
      id: 1,
      description: 'Ăn trưa',
      amount: 50000,
      date: '2024-04-10',
      category: { name: 'Ăn uống', icon: 'utensils', color: 'text-orange-500' }
    },
    {
      id: 2,
      description: 'Cafe sáng',
      amount: 35000,
      date: '2024-04-10',
      category: { name: 'Ăn uống', icon: 'coffee', color: 'text-orange-500' }
    },
    {
      id: 3,
      description: 'Ăn tối',
      amount: 150000,
      date: '2024-04-09',
      category: { name: 'Ăn uống', icon: 'utensils', color: 'text-orange-500' }
    }
  ]
})

const totalSpent = computed(() => {
  return mockTransactions.value.reduce((sum, transaction) => sum + transaction.amount, 0)
})

const percentageUsed = computed(() => {
  if (!props.limit) return 0
  return Math.round((totalSpent.value / props.limit.amount) * 100)
})
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="$emit('close')"
    >
      <div 
        class="bg-white rounded-lg shadow-xl w-full max-w-2xl"
        @click.stop
      >
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-text">Chi tiết hạn mức chi</h3>
            <button @click="$emit('close')" class="text-text-secondary hover:text-text">
              <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
            </button>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4">
          <!-- Thông tin hạn mức -->
          <div class="bg-gray-50 rounded-lg p-4 mb-6">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h4 class="font-medium text-text">{{ limit?.name }}</h4>
                <p class="text-sm text-text-secondary mt-1">
                  {{ formatDate(limit?.start_date) }} - {{ formatDate(limit?.end_date) }}
                </p>
              </div>
              <div class="text-right">
                <div class="text-sm text-text-secondary">Đã chi tiêu</div>
                <div class="text-lg font-semibold text-text">
                  {{ formatCurrency(totalSpent) }} / {{ formatCurrency(limit?.amount) }}
                </div>
              </div>
            </div>

            <!-- Progress bar -->
            <div class="relative w-full h-2 bg-gray-200 rounded-full overflow-hidden">
              <div 
                class="absolute h-full left-0 top-0 rounded-full transition-all duration-300"
                :class="{
                  'bg-danger': percentageUsed > 100,
                  'bg-warning': percentageUsed >= 80 && percentageUsed <= 100,
                  'bg-success': percentageUsed < 80
                }"
                :style="{ width: `${Math.min(percentageUsed, 100)}%` }"
              ></div>
            </div>
          </div>

          <!-- Danh sách giao dịch với ElScrollbar -->
          <ElScrollbar height="400px" class="custom-scrollbar">
            <div class="space-y-2 px-2">
              <div v-for="transaction in mockTransactions" :key="transaction.id"
                   class="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
                <div class="flex items-center space-x-3 min-w-0">
                  <div class="flex-shrink-0 w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center">
                    <font-awesome-icon 
                      :icon="['fas', transaction.category.icon]"
                      :class="transaction.category.color"
                    />
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="font-medium text-text truncate">{{ transaction.description }}</div>
                    <div class="text-sm text-text-secondary">{{ formatDate(transaction.date) }}</div>
                  </div>
                </div>
                <div class="text-right flex-shrink-0 ml-4">
                  <div class="font-medium text-text">{{ formatCurrency(transaction.amount) }}</div>
                  <div class="text-sm text-text-secondary">{{ transaction.category.name }}</div>
                </div>
              </div>
            </div>
          </ElScrollbar>
        </div>

        <!-- Modal Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end">
          <button 
            @click="$emit('close')"
            class="px-4 py-2 text-text-secondary hover:text-text"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* Custom scrollbar styles */
.custom-scrollbar :deep(.el-scrollbar__bar) {
  z-index: 10;
}

.custom-scrollbar :deep(.el-scrollbar__wrap) {
  margin-right: -8px !important;
  margin-bottom: -8px !important;
}

.custom-scrollbar :deep(.el-scrollbar__thumb) {
  background-color: rgba(144, 147, 153, 0.3);
}

.custom-scrollbar :deep(.el-scrollbar__thumb:hover) {
  background-color: rgba(144, 147, 153, 0.5);
}

/* Thêm style cho container */
.custom-scrollbar :deep(.el-scrollbar__view) {
  width: 100%;
  min-width: 0;
}
</style> 