<script setup>
import { ref, computed } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faWallet, faCalendar, faTimes, faMapMarkerAlt, faPlane, faUser, faStickyNote, faArrowUp, faArrowDown, faTags } from '@fortawesome/free-solid-svg-icons'
import { formatCurrency } from '@/utils/formatters'
import Avatar from '@components/Avatar.vue'
library.add(faWallet, faCalendar, faTimes, faMapMarkerAlt, faPlane, faUser, faStickyNote, faArrowUp, faArrowDown, faTags)

const props = defineProps({
  isOpen: Boolean,
  transaction: {
    type: Object,
    default: null
  },
  transactionType: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}

const formatDate = (dateString) => {
  if (!dateString) return ''

  // Nếu dateString đã là chuỗi định dạng, trả về luôn
  if (typeof dateString === 'string') return dateString

  // Nếu là đối tượng Date, định dạng nó
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Xác định loại giao dịch (thu/chi) để hiển thị đúng màu sắc và biểu tượng
const isExpense = computed(() => {
  if (!props.transaction) return false
  if (!props.transactionType) return false

  return props.transactionType === 'expense'
})

// Hàm lấy URL icon an toàn
const getIconUrl = computed(() => {
  if (!props.transaction) return ''
  
  if (props.transactionType === 'revenue' && props.transaction.dictionaryRevenue) {
    return props.transaction.dictionaryRevenue.iconUrl || ''
  } else if (props.transactionType === 'expense' && props.transaction.dictionaryExpense) {
    return props.transaction.dictionaryExpense.iconUrl || ''
  }
  
  return ''
})

// Hàm lấy tên danh mục an toàn
const getCategoryName = computed(() => {
  if (!props.transaction) return ''
  
  if (props.transactionType === 'revenue' && props.transaction.dictionaryRevenue) {
    return props.transaction.dictionaryRevenue.name || ''
  } else if (props.transactionType === 'expense' && props.transaction.dictionaryExpense) {
    return props.transaction.dictionaryExpense.name || ''
  }
  
  return ''
})
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen && transaction"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click="handleClose">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-lg" @click.stop>
        <!-- Header -->
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h3 class="text-lg font-semibold text-text">Chi tiết giao dịch</h3>
          <button @click="handleClose" class="text-text-secondary hover:text-text transition-colors">
            <font-awesome-icon :icon="['fas', 'times']" class="text-xl" />
          </button>
        </div>

        <!-- Nội dung -->
        <div class="p-6 max-h-[60vh] overflow-y-auto">
          <!-- Tiêu đề và số tiền -->
          <div class="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-end">
            <div class="mt-2 sm:mt-0 text-xl font-semibold" :class="isExpense ? 'text-danger' : 'text-success'">
              <font-awesome-icon :icon="['fas', isExpense ? 'arrow-down' : 'arrow-up']" class="mr-2" />
              {{ formatCurrency(transaction.amount) }}
            </div>
          </div>

          <!-- Thông tin chi tiết -->
          <div class="space-y-4">
            <!-- Danh mục -->
            <div class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'tags']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Danh mục</p>
                <p class="font-medium flex text-text">
                  <Avatar v-if="getIconUrl" :src="getIconUrl" :alt="getCategoryName" 
                    size="m" class="mr-2" />
                  {{ getCategoryName }}
                </p>
              </div>
            </div>

            <!-- Tài khoản -->
            <div class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'wallet']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Tài khoản</p>
                <p class="font-medium flex text-text">
                  <Avatar v-if="transaction.dictionaryBucketPayment?.iconUrl" :src="transaction.dictionaryBucketPayment?.iconUrl" :alt="transaction.dictionaryBucketPayment?.accountName" 
                    size="m" class="mr-2" />
                    {{ transaction.dictionaryBucketPayment?.accountName || 'Không có' }}
                </p>
                <!-- Hiển thị số dư sau giao dịch -->
                <p v-if="transaction.balance" class="text-sm text-text-secondary mt-1">
                  Số dư sau giao dịch: <span class="font-medium">{{ formatCurrency(transaction.balance) }}</span>
                </p>
              </div>
            </div>

            <!-- Ngày tháng -->
            <div class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'calendar']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Ngày giao dịch</p>
                <p class="font-medium">{{ transactionType === 'revenue' ? formatDate(transaction.revenueDate) : formatDate(transaction.expenseDate) }}</p>
              </div>
            </div>

            <!-- Địa điểm (nếu có) -->
            <div v-if="transaction.location" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'map-marker-alt']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Địa điểm</p>
                <p class="font-medium">{{ transaction.location }}</p>
              </div>
            </div>

            <!-- Chuyến đi/Sự kiện (nếu có) -->
            <div v-if="transaction.tripEvent" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'plane']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Chuyến đi/Sự kiện</p>
                <p class="font-medium">{{ transaction.tripEvent }}</p>
              </div>
            </div>

            <!-- Nhận từ ai/Chi cho ai (nếu có) -->
            <div v-if="transaction.beneficiaryAccount || transaction.senderAccount" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'user']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">{{ isExpense ? 'Chi cho tài khoản' : 'Nhận từ tài khoản' }}</p>
                <p class="font-medium">{{ isExpense ? transaction.beneficiaryAccount.accountName : transaction.senderAccount.accountName }}</p>
              </div>
            </div>

            <div v-if="transaction.beneficiary || transaction.collectMoneyWho" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'user']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">{{ isExpense ? 'Chi cho' : 'Nhận từ ' }}</p>
                <p class="font-medium">{{ isExpense ? transaction.beneficiary : transaction.collectMoneyWho}}</p>
              </div>
            </div>

          
            <div v-if="transaction.benecificary || transaction.collectMoneyWho" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'user']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">{{ isExpense ? 'Chi cho' : 'Nhận từ ' }}</p>
                <p class="font-medium">{{ isExpense ? transaction.benecificary : transaction.collectMoneyWho}}</p>
              </div>
            </div>


            <!-- Mô tả (nếu có) -->
            <div v-if="transaction.interpretation" class="flex items-start">
              <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0 mr-3">
                <font-awesome-icon :icon="['fas', 'sticky-note']" class="text-primary" />
              </div>
              <div>
                <p class="text-sm text-text-secondary">Mô tả</p>
                <p class="font-medium whitespace-pre-wrap">{{ transaction.interpretation }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end">
          <button @click="handleClose"
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg font-medium transition-colors">
            Đóng
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>