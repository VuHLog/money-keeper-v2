<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

const props = defineProps({
  notification: {
    type: Object,
    required: true
  },
  duration: {
    type: Number,
    default: 5000
  },
  index: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['close'])

const isVisible = ref(false)
const timeoutId = ref(null)
const router = useRouter()

const getTypeColor = (type) => {
  switch (type) {
    case 'expense':
      return 'border-red-500'
    case 'revenue':
      return 'border-green-500'
    case 'expense limit':
      return 'border-orange-500'
    case 'success':
      return 'border-green-500'
    case 'error':
      return 'border-red-500'
    default:
      return 'border-primary'
  }
}

const getIcon = (type) => {
  switch (type) {
    case 'expense':
      return ['fas', 'arrow-down']
    case 'revenue':
      return ['fas', 'arrow-up']
    case 'expense limit':
      return ['fas', 'exclamation-triangle']
    case 'success':
      return ['fas', 'check-circle']
    case 'error':
      return ['fas', 'times-circle']
    default:
      return ['fas', 'bell']
  }
}

const getIconColor = (type) => {
  switch (type) {
    case 'expense':
      return 'text-red-500'
    case 'revenue':
      return 'text-green-500'
    case 'expense limit':
      return 'text-orange-500'
    case 'success':
      return 'text-green-500'
    case 'error':
      return 'text-red-500'
    default:
      return 'text-primary'
  }
}

const handleClose = () => {
  isVisible.value = false
  if (timeoutId.value) {
    clearTimeout(timeoutId.value)
  }
  // Đợi animation kết thúc
  setTimeout(() => {
    emit('close')
  }, 300)
}

const handleClick = () => {
  if (props.notification.href) {
    router.push('/' + props.notification.href)
  }
  handleClose()
}

onMounted(() => {
  // Hiển thị toast với animation sau một khoảng thời gian nhỏ
  nextTick(() => {
    setTimeout(() => {
      isVisible.value = true
    }, 50)
  })

  // Tự động ẩn toast sau khoảng thời gian duration
  if (props.duration > 0) {
    timeoutId.value = setTimeout(() => {
      handleClose()
    }, props.duration)
  }
})

onUnmounted(() => {
  if (timeoutId.value) {
    clearTimeout(timeoutId.value)
  }
})
</script>

<template>
  <div 
    class="fixed flex flex-col items-end z-50 transition-transform transform"
    :style="{
      bottom: `${20 + props.index * 120}px`, 
      right: '20px',
      opacity: isVisible ? 1 : 0,
      transform: isVisible ? 'translateX(0)' : 'translateX(100%)'
    }"
  >
    <div 
      class="max-w-sm w-full bg-white shadow-lg rounded-lg overflow-hidden transition-all cursor-pointer"
      :class="[getTypeColor(props.notification.type), 'border-l-4']"
      @click="handleClick"
    >
      <div class="relative p-4">
        <div class="flex items-start">
          <div class="flex-shrink-0 mr-3">
            <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="[`bg-${getIconColor(props.notification.type).split('-')[1]}-50`]">
              <FontAwesomeIcon :icon="getIcon(props.notification.type)" :class="getIconColor(props.notification.type)" />
            </div>
          </div>
          <div class="flex-1 pt-0.5">
            <h3 class="text-sm font-medium text-gray-900">
              {{ props.notification.title }}
            </h3>
            <div class="mt-1 text-sm text-gray-500" v-html="props.notification.content"></div>
          </div>
        </div>
          <button 
          class="absolute top-2 right-2 text-gray-400 hover:text-gray-600 bg-white rounded-full p-1 hover:bg-gray-100 w-8 h-8 flex items-center justify-center"
            @click.stop="handleClose"
          >
          <FontAwesomeIcon icon="times" size="lg" />
          </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from,
.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style> 