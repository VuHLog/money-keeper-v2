<script setup>
import { ref, computed, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'
import { ElScrollbar } from 'element-plus'
import { useExpenseLimitStore } from '@/store/ExpenseLimitStore'
import Avatar from '@/views/components/Avatar.vue'

const props = defineProps({
  isOpen: Boolean,
  limit: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const expenseLimitStore = useExpenseLimitStore()
const details = ref([])

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN')
}

watch(() => props.limit, async (newLimit) => {
  if (newLimit && props.isOpen) {
    const response = await expenseLimitStore.getExpenseLimitDetailByExpenseLimitId(
      newLimit.id,
      newLimit.startDateLimit + " 00:00:00",
      newLimit.endDateLimit + " 23:59:59"
    )
    details.value = response || []
  }
}, { immediate: true })

const totalSpent = computed(() => {
  return details.value.reduce((sum, item) => sum + (item.amount || 0), 0)
})

const percentageUsed = computed(() => {
  if (!props.limit) return 0
  return Math.round((totalSpent.value / props.limit.amount) * 100)
})
</script>

<template>
  <Teleport to="body">
    <!-- Overlay layer -->
    <div v-if="isOpen" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 overflow-y-auto"
         @click.self="$emit('close')">
      <!-- Modal content -->
      <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl my-8">
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200 sticky top-0 bg-white z-10">
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
          <div class="bg-gray-50 rounded-lg p-4 mb-6 sticky top-[73px] z-10">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h4 class="font-medium text-text">{{ limit?.name }}</h4>
                <p class="text-sm text-text-secondary mt-1">
                  {{ formatDate(limit?.startDateLimit) }} - {{ formatDate(limit?.endDateLimit) }}
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
              <div class="absolute h-full left-0 top-0 rounded-full transition-all duration-300" :class="{
                'bg-danger': percentageUsed > 100,
                'bg-warning': percentageUsed >= 80 && percentageUsed <= 100,
                'bg-success': percentageUsed < 80
              }" :style="{ width: `${Math.min(percentageUsed, 100)}%` }"></div>
            </div>
          </div>

          <!-- Danh sách giao dịch với ElScrollbar -->
          <ElScrollbar height="calc(70vh - 250px)" class="custom-scrollbar">
            <div class="space-y-2 px-2">
              <div v-for="(item, idx) in details" :key="idx"
                class="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
                <div class="flex items-center space-x-3 min-w-0">
                  <div class="flex-shrink-0 w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center">
                    <Avatar :src="item.iconUrl" :alt="item.name" size="m" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="font-medium text-text truncate">{{ item.name }}</div>
                    <div class="text-sm text-text-secondary">{{ formatDate(item.expenseDate) }}</div>
                  </div>
                </div>
                <div class="text-right flex-shrink-0 ml-4">
                  <div class="font-medium text-text">{{ formatCurrency(item.amount) }}</div>
                  <div class="text-sm text-text-secondary">{{ item.bucketPaymentName }}</div>
                </div>
              </div>
            </div>
          </ElScrollbar>
        </div>

        <!-- Modal Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end sticky bottom-0 z-10">
          <button @click="$emit('close')" class="px-4 py-2 text-text-secondary hover:text-text">
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

/* Thêm style cho modal container */
.modal-container {
  max-height: 90vh;
  margin: auto;
}
</style> 