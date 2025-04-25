<script setup>
import { computed } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'

const props = defineProps({
  isOpen: Boolean,
  limit: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'confirm'])

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN')
}

const getRepeatLabel = (repeatTime) => {
  const options = {
    '': 'Không lặp lại',
    'daily': 'Hàng ngày',
    'weekly': 'Hàng tuần',
    'monthly': 'Hàng tháng',
    'yearly': 'Hàng năm'
  }
  return options[repeatTime] || 'Không lặp lại'
}
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="$emit('close')"
    >
      <div 
        class="bg-white rounded-lg shadow-xl w-full max-w-md"
        @click.stop
      >
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-text">Xác nhận xóa hạn mức</h3>
        </div>

        <!-- Modal Content -->
        <div class="px-6 py-4">
          <div class="flex items-start space-x-4 mb-4">
            <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center">
              <font-awesome-icon 
                icon="chart-line"
                class="text-primary text-xl"
              />
            </div>
            <div class="flex-1">
              <h4 class="font-medium text-text">{{ limit?.name }}</h4>
              <div class="text-sm text-text-secondary space-y-1 mt-1">
                <p>Số tiền: {{ formatCurrency(limit?.amount) }}</p>
                <p>Thời gian: {{ formatDate(limit?.start_date) }} - {{ formatDate(limit?.end_date) }}</p>
                <p>Lặp lại: {{ getRepeatLabel(limit?.repeat_time) }}</p>
              </div>
            </div>
          </div>
          
          <div class="bg-danger/5 border border-danger/10 rounded-lg p-4">
            <div class="flex items-start space-x-3">
              <div class="mt-0.5">
                <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-danger" />
              </div>
              <div>
                <p class="text-text">Bạn có chắc chắn muốn xóa hạn mức này?</p>
                <p class="text-sm text-text-secondary mt-1">Hành động này không thể hoàn tác. Tất cả dữ liệu liên quan đến hạn mức này sẽ bị xóa vĩnh viễn.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
          <button 
            @click="$emit('close')"
            class="px-4 py-2 text-text-secondary hover:text-text"
          >
            Hủy
          </button>
          <button 
            @click="$emit('confirm')"
            class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger/90 flex items-center space-x-2"
          >
            <font-awesome-icon :icon="['fas', 'trash']" />
            <span>Xóa hạn mức</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template> 