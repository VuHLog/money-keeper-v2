<script setup>
import { ref, computed, watch } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faCalendarDays, faXmark, faCalendarPlus } from '@fortawesome/free-solid-svg-icons'
import { useFinancialGoalStore } from '@/store/FinancialGoalStore'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'

library.add(faCalendarDays, faXmark, faCalendarPlus)

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

const emit = defineEmits(['close', 'success', 'error'])

// Store
const financialGoalStore = useFinancialGoalStore()

// Form data
const newDeadline = ref('')
const isSubmitting = ref(false)
const errors = ref({
  deadline: ''
})

// Get today's date in YYYY-MM-DD format
const getTodayDate = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Format date for display
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Calculate days between two dates
const calculateDaysDifference = (date1, date2) => {
  const oneDay = 24 * 60 * 60 * 1000 // hours*minutes*seconds*milliseconds
  const firstDate = new Date(date1)
  const secondDate = new Date(date2)
  
  return Math.round((secondDate - firstDate) / oneDay)
}

// Computed properties
const minDate = computed(() => {
  if (!props.goal) return getTodayDate()
  
  // Minimum date should be after the current deadline
  const currentDeadline = new Date(props.goal.deadline)
  const tomorrow = new Date(currentDeadline)
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  const year = tomorrow.getFullYear()
  const month = String(tomorrow.getMonth() + 1).padStart(2, '0')
  const day = String(tomorrow.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const currentDeadlineFormatted = computed(() => {
  return props.goal ? formatDate(props.goal.deadline) : ''
})

const newDeadlineFormatted = computed(() => {
  return newDeadline.value ? formatDate(newDeadline.value) : ''
})

const extensionDays = computed(() => {
  if (!props.goal || !newDeadline.value) return 0
  return calculateDaysDifference(props.goal.deadline, newDeadline.value)
})

// Disable dates before current deadline
const disablePastDates = (time) => {
  if (!props.goal) return true
  const currentDeadline = new Date(props.goal.deadline)
  return time.getTime() <= currentDeadline.getTime()
}

// Validation
const validateForm = () => {
  errors.value = { deadline: '' }
  let isValid = true

  if (!newDeadline.value) {
    errors.value.deadline = 'Vui lòng chọn ngày gia hạn mới'
    isValid = false
  } else {
    const selectedDate = new Date(newDeadline.value)
    const currentDeadline = new Date(props.goal.deadline)
    
    if (selectedDate <= currentDeadline) {
      errors.value.deadline = 'Ngày gia hạn phải sau ngày hết hạn hiện tại'
      isValid = false
    }
  }

  return isValid
}

// Handle form submission
const handleSubmit = async () => {
  if (!validateForm()) return

  isSubmitting.value = true
  try {
    // Update goal deadline
    const updatedGoal = {
      ...props.goal,
      deadline: newDeadline.value
    }
    
    await financialGoalStore.updateDeadline(props.goal.id, updatedGoal)
    
    emit('success', {
      message: `Đã gia hạn mục tiêu "${props.goal.name}" đến ${newDeadlineFormatted.value}`,
      goal: updatedGoal
    })
    
    handleClose()
  } catch (error) {
    console.error('Error extending goal:', error)
    emit('error', {
      message: 'Có lỗi xảy ra khi gia hạn mục tiêu'
    })
  } finally {
    isSubmitting.value = false
  }
}

// Handle close modal
const handleClose = () => {
  newDeadline.value = ''
  errors.value = { deadline: '' }
  emit('close')
}

// Watch for modal open to reset form
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    newDeadline.value = ''
    errors.value = { deadline: '' }
  }
})
</script>

<template>
  <div v-if="isOpen && goal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="handleClose">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="bg-warning/10 p-2 rounded-lg">
            <font-awesome-icon :icon="['fas', 'calendar-plus']" class="text-warning text-lg" />
          </div>
          <h3 class="text-lg font-semibold text-text">Gia hạn mục tiêu</h3>
        </div>
        <button @click="handleClose" class="text-text-secondary hover:text-text transition-colors">
          <font-awesome-icon :icon="['fas', 'xmark']" class="text-xl" />
        </button>
      </div>

      <!-- Modal Content -->
      <div class="px-6 py-6 space-y-6">
        <!-- Goal Info -->
        <div class="bg-gray-50 rounded-lg p-4">
          <h4 class="font-semibold text-text mb-2">{{ goal.name }}</h4>
          <div class="text-sm text-text-secondary space-y-1">
            <div>Hạn hiện tại: <span class="font-medium text-text">{{ currentDeadlineFormatted }}</span></div>
            <div>Mục tiêu: <span class="font-medium text-text">{{ goal.targetAmount?.toLocaleString() }} {{ goal.currencySymbol }}</span></div>
            <div>Đã tiết kiệm: <span class="font-medium text-text">{{ goal.currentAmount?.toLocaleString() }} {{ goal.currencySymbol }}</span></div>
          </div>
        </div>

        <!-- Date Picker -->
        <div>
          <label class="block text-sm font-medium text-text-secondary mb-2">
            Chọn ngày hết hạn mới <span class="text-danger">*</span>
          </label>
          <div class="w-full">
            <el-date-picker 
              v-model="newDeadline" 
              type="date" 
              :format="'DD/MM/YYYY'"
              :value-format="'YYYY-MM-DD'" 
              :placeholder="'Chọn ngày gia hạn mới'" 
              :disabled-date="disablePastDates"
              class="date-picker-custom w-full" 
              :class="{ 'error-date-picker': errors.deadline }"
              style="width: 100%;" 
            />
            <p v-if="errors.deadline" class="mt-1 text-sm text-danger">
              {{ errors.deadline }}
            </p>
          </div>
        </div>

        <!-- Extension Info -->
        <div v-if="newDeadline && !errors.deadline" class="bg-success/5 border border-success/20 rounded-lg p-4">
          <div class="flex items-center space-x-2 mb-2">
            <font-awesome-icon :icon="['fas', 'calendar-plus']" class="text-success" />
            <span class="font-medium text-success">Thông tin gia hạn</span>
          </div>
          <div class="text-sm text-text-secondary space-y-1">
            <div>Ngày hết hạn mới: <span class="font-medium text-text">{{ newDeadlineFormatted }}</span></div>
            <div>Số ngày gia hạn: <span class="font-medium text-success">{{ extensionDays }} ngày</span></div>
          </div>
        </div>

        <!-- Warning -->
        <div class="bg-warning/10 border border-warning/20 rounded-lg p-4">
          <div class="flex items-center space-x-2 mb-2">
            <font-awesome-icon :icon="['fas', 'calendar-days']" class="text-warning" />
            <span class="font-medium text-warning">Lưu ý</span>
          </div>
          <p class="text-warning text-sm">
            Sau khi gia hạn, mục tiêu sẽ chuyển từ trạng thái "Hết hạn" sang "Chưa hoàn thành" và bạn có thể tiếp tục nạp tiền.
          </p>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
        <button 
          @click="handleClose"
          :disabled="isSubmitting"
          class="px-4 py-2 text-text-secondary hover:text-text transition-colors disabled:opacity-50"
        >
          Hủy
        </button>
        <button 
          @click="handleSubmit"
          :disabled="isSubmitting || !newDeadline || !!errors.deadline"
          class="px-4 py-2 bg-warning text-white rounded-lg hover:bg-warning/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
        >
          <div v-if="isSubmitting" class="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></div>
          <font-awesome-icon v-else :icon="['fas', 'calendar-plus']" class="text-sm" />
          <span>{{ isSubmitting ? 'Đang gia hạn...' : 'Gia hạn' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Override Element Plus date picker styles */
.date-picker-custom :deep(.el-input__wrapper) {
  @apply !bg-transparent !shadow-none !box-border !border !border-gray-100 !rounded-lg hover:!border-gray-200;
  height: 42px !important; /* Match height of other inputs */
}

.date-picker-custom :deep(.el-input__wrapper.is-focus) {
  @apply !border-warning/50 !ring-1 !ring-warning/20;
}

.error-date-picker :deep(.el-input__wrapper) {
  @apply !border-danger/50;
}

.error-date-picker :deep(.el-input__wrapper.is-focus) {
  @apply !border-danger !ring-1 !ring-danger/20;
}

.text-warning {
  color: #D97706;
}

.bg-warning {
  background-color: #D97706;
}

.bg-warning\/90 {
  background-color: rgba(217, 119, 6, 0.9);
}

.bg-warning\/10 {
  background-color: rgba(217, 119, 6, 0.1);
}

.border-warning\/20 {
  border-color: rgba(217, 119, 6, 0.2);
}

.focus\:border-warning\/50:focus {
  border-color: rgba(217, 119, 6, 0.5);
}

.focus\:ring-warning\/20:focus {
  ring-color: rgba(217, 119, 6, 0.2);
}

.text-success {
  color: #059669;
}

.bg-success\/5 {
  background-color: rgba(5, 150, 105, 0.05);
}

.border-success\/20 {
  border-color: rgba(5, 150, 105, 0.2);
}

.text-danger {
  color: #DC2626;
}

.border-danger\/50 {
  border-color: rgba(220, 38, 38, 0.5);
}

.focus\:border-danger:focus {
  border-color: #DC2626;
}

.focus\:ring-danger\/20:focus {
  ring-color: rgba(220, 38, 38, 0.2);
}
</style> 