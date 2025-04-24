<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { formatCurrency } from '@/utils/formatters'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import TransactionDetailModal from '@/views/components/TransactionDetailModal.vue'
import Swal from 'sweetalert2'
import { 
  faWallet, 
  faUtensils, 
  faShoppingBag,
  faCalendar,
  faMapMarkerAlt,
  faPlane,
  faUser,
  faStickyNote,
  faArrowDown,
  faReceipt,
  faChevronRight,
  faEye,
  faPen,
  faHome,
  faTaxi,
  faTshirt,
  faHeartbeat,
  faGraduationCap
} from '@fortawesome/free-solid-svg-icons'

library.add(
  faWallet, 
  faUtensils, 
  faShoppingBag,
  faCalendar,
  faMapMarkerAlt,
  faPlane,
  faUser,
  faStickyNote,
  faArrowDown,
  faReceipt,
  faChevronRight,
  faEye,
  faPen,
  faHome,
  faTaxi,
  faTshirt,
  faHeartbeat,
  faGraduationCap
)

// Trạng thái cho chế độ chỉnh sửa
const isEditMode = ref(false)
const editingTransactionId = ref(null)

// Form data
const formData = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: new Date(),
  location: '',
  event: '',
  paidTo: '',
  note: ''
})

const errors = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: ''
})

// Format amount with currency
const formattedAmount = computed({
  get: () => {
    if (!formData.value.amount) return ''
    return formatCurrency(Number(formData.value.amount))
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

// Format date function
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getDate()}/${d.getMonth() + 1}/${d.getFullYear()}`
}

const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: ''
  }

  if (!formData.value.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền'
    isValid = false
  }

  if (!formData.value.categoryId) {
    errors.value.categoryId = 'Vui lòng chọn danh mục'
    isValid = false
  }

  if (!formData.value.accountId) {
    errors.value.accountId = 'Vui lòng chọn tài khoản'
    isValid = false
  }
  
  if (!formData.value.date) {
    errors.value.date = 'Vui lòng chọn ngày'
    isValid = false
  }

  return isValid
}

const handleSubmit = () => {
  if (!validateForm()) return
  
  if (isEditMode.value) {
    // Xử lý chỉnh sửa giao dịch
    updateTransaction()
  } else {
    // Xử lý thêm mới giao dịch
    addNewTransaction()
  }
}

// Hàm thêm mới giao dịch
const addNewTransaction = () => {
  // Tạo một đối tượng giao dịch mới từ form data
  const newTransaction = {
    id: Date.now(), // Sử dụng timestamp làm ID tạm thời
    title: formData.value.note ? formData.value.note.substring(0, 30) : 'Giao dịch chi mới',
    category: selectedCategory.value.name,
    account: selectedAccount.value.name,
    date: formatDate(formData.value.date),
    amount: formData.value.amount,
    location: formData.value.location || '',
    event: formData.value.event || '',
    paidTo: formData.value.paidTo || '',
    note: formData.value.note || ''
  }
  
  // Thêm giao dịch mới vào đầu danh sách lịch sử
  recentTransactions.value.unshift(newTransaction)
  
  // Giới hạn hiển thị 5 giao dịch gần nhất
  if (recentTransactions.value.length > 5) {
    recentTransactions.value = recentTransactions.value.slice(0, 5)
  }
  
  // Hiển thị thông báo thành công
  Swal.fire({
    icon: 'success',
    title: 'Thành công!',
    text: 'Thêm giao dịch chi thành công!',
    toast: true,
    position: 'bottom-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
      toast.addEventListener('mouseenter', Swal.stopTimer)
      toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
  })
  
  // Reset form sau khi thêm thành công
  resetForm()
}

// Hàm cập nhật giao dịch
const updateTransaction = () => {
  // Tìm vị trí của giao dịch cần cập nhật trong mảng
  const index = recentTransactions.value.findIndex(t => t.id === editingTransactionId.value)
  
  if (index !== -1) {
    // Cập nhật thông tin giao dịch
    const updatedTransaction = {
      ...recentTransactions.value[index],
      title: formData.value.note ? formData.value.note.substring(0, 30) : 'Giao dịch chi đã cập nhật',
      category: selectedCategory.value.name,
      account: selectedAccount.value.name,
      date: formatDate(formData.value.date),
      amount: formData.value.amount,
      location: formData.value.location || '',
      event: formData.value.event || '',
      paidTo: formData.value.paidTo || '',
      note: formData.value.note || ''
    }
    
    // Xóa giao dịch cũ và thêm giao dịch đã cập nhật vào đầu mảng
    recentTransactions.value.splice(index, 1)
    recentTransactions.value.unshift(updatedTransaction)
    
    // Hiển thị thông báo thành công
    Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      text: 'Cập nhật giao dịch chi thành công!',
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
      }
    })
    
    // Reset form và chuyển về chế độ thêm mới
    resetForm()
  }
}

// Hàm xử lý khi nhấn nút "Chi tiết"
const handleViewDetail = (transaction) => {
  selectedTransaction.value = transaction
  isDetailModalOpen.value = true
}

// Hàm xử lý khi nhấn nút "Sửa"
const handleEditTransaction = (transaction) => {
  // Chuyển sang chế độ chỉnh sửa
  isEditMode.value = true
  editingTransactionId.value = transaction.id
  
  // Tìm category và account id từ danh sách dựa trên tên
  const categoryId = categories.value.find(cat => cat.name === transaction.category)?.id
  const accountId = accounts.value.find(acc => acc.name === transaction.account)?.id
  
  // Cập nhật form data với thông tin giao dịch
  formData.value = {
    amount: transaction.amount,
    categoryId: categoryId || '',
    accountId: accountId || '',
    date: new Date(transaction.date.split('/').reverse().join('-')), // Chuyển string ngày thành Date object
    location: transaction.location || '',
    event: transaction.event || '',
    paidTo: transaction.paidTo || '',
    note: transaction.note || ''
  }
  
  // Cuộn lên đầu để nhìn thấy form
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Recent transactions
const recentTransactions = ref([
  {
    id: 1,
    title: 'Ăn trưa',
    category: 'Ăn uống',
    account: 'Ví tiền',
    date: '25/3/2024',
    amount: 50000,
    location: 'Nhà hàng ABC',
    event: '',
    paidTo: '',
    note: 'Bữa trưa với đồng nghiệp'
  },
  {
    id: 2,
    title: 'Mua sắm cuối tuần',
    category: 'Mua sắm',
    account: 'Tài khoản ngân hàng',
    date: '20/3/2024',
    amount: 350000,
    location: 'Siêu thị XYZ', 
    event: '',
    paidTo: 'Siêu thị XYZ',
    note: 'Mua đồ ăn và nhu yếu phẩm'
  }
])

// Update categories data to include icons and colors
const categories = ref([
  { id: 1, name: 'Ăn uống', icon: 'utensils', color: 'text-red-500' },
  { id: 2, name: 'Mua sắm', icon: 'shopping-bag', color: 'text-purple-500' },
  { id: 3, name: 'Nhà cửa', icon: 'home', color: 'text-blue-500' },
  { id: 4, name: 'Di chuyển', icon: 'taxi', color: 'text-yellow-500' },
  { id: 5, name: 'Quần áo', icon: 'tshirt', color: 'text-pink-500' },
  { id: 6, name: 'Sức khỏe', icon: 'heartbeat', color: 'text-green-500' },
  { id: 7, name: 'Giáo dục', icon: 'graduation-cap', color: 'text-indigo-500' }
])

// Update accounts data to include icons and colors
const accounts = ref([
  { id: 1, name: 'Ví tiền', icon: 'wallet', color: 'text-yellow-500' },
  { id: 2, name: 'Tài khoản ngân hàng', icon: 'building-columns', color: 'text-blue-500' }
])

// Add computed properties for selected items
const selectedCategory = computed(() => {
  if (!formData.value.categoryId) {
    return { name: 'Chọn danh mục', icon: 'list', color: 'text-gray-400' }
  }
  return categories.value.find(cat => cat.id === formData.value.categoryId)
})

const selectedAccount = computed(() => {
  if (!formData.value.accountId) {
    return { name: 'Chọn tài khoản', icon: 'wallet', color: 'text-gray-400' }
  }
  return accounts.value.find(acc => acc.id === formData.value.accountId)
})

// Add state for dropdowns
const isCategoryDropdownOpen = ref(false)
const isAccountDropdownOpen = ref(false)

// Function để đóng tất cả dropdown khi click ra ngoài
const handleClickOutside = (event) => {
  // Xử lý dropdown danh mục
  const categoryDropdownEl = document.querySelector('.category-dropdown-container')
  if (categoryDropdownEl && !categoryDropdownEl.contains(event.target) && isCategoryDropdownOpen.value) {
    isCategoryDropdownOpen.value = false
  }
  
  // Xử lý dropdown tài khoản
  const accountDropdownEl = document.querySelector('.account-dropdown-container')
  if (accountDropdownEl && !accountDropdownEl.contains(event.target) && isAccountDropdownOpen.value) {
    isAccountDropdownOpen.value = false
  }
}

// Thêm và xóa event listener khi component được mount và unmount
onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// Biến cho modal chi tiết
const isDetailModalOpen = ref(false)
const selectedTransaction = ref(null)

// Hàm reset form sau khi thêm thành công
const resetForm = () => {
  formData.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: new Date(),
    location: '',
    event: '',
    paidTo: '',
    note: ''
  }
  
  errors.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: ''
  }
  
  // Nếu đang ở chế độ chỉnh sửa, chuyển về chế độ thêm mới
  isEditMode.value = false
  editingTransactionId.value = null
}

// Hàm hủy chỉnh sửa
const cancelEdit = () => {
  resetForm()
}
</script>

<template>
  <div class="p-4">
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- Form bên trái -->
      <div class="lg:col-span-7">
        <div class="bg-surface rounded-2xl shadow-sm">
          <!-- Tiêu đề form -->
          <div class="px-6 pt-6 pb-2 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-text">
              {{ isEditMode ? 'Chỉnh sửa giao dịch chi' : 'Thêm giao dịch chi mới' }}
            </h2>
          </div>
          
          <form @submit.prevent="handleSubmit" class="space-y-6 px-6 py-4">
            <!-- Amount Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Số tiền <span class="text-danger">*</span>
              </label>
              <input 
                v-model="formattedAmount"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[
                  errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  formattedAmount ? 'bg-white' : 'bg-gray-50'
                ]"
                placeholder="0 ₫"
              />
              <p v-if="errors.amount" class="mt-1 text-sm text-danger">
                {{ errors.amount }}
              </p>
            </div>

            <!-- Category Selection -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Danh mục <span class="text-danger">*</span>
              </label>
              <div class="relative select-none category-dropdown-container">
                <div 
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[
                    isCategoryDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                    errors.categoryId ? 'border-danger/50' : ''
                  ]"
                  @click="isCategoryDropdownOpen = !isCategoryDropdownOpen"
                >
                  <div class="flex items-center flex-1">
                    <font-awesome-icon 
                      :icon="['fas', selectedCategory.icon]" 
                      :class="selectedCategory.color"
                      class="mr-2"
                    />
                    <span>{{ selectedCategory.name }}</span>
                  </div>
                  <font-awesome-icon 
                    :icon="['fas', 'chevron-down']" 
                    class="text-gray-400 ml-2 transition-transform"
                    :class="{'rotate-180': isCategoryDropdownOpen}"
                  />
                </div>

                <div 
                  v-if="isCategoryDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                >
                  <div 
                    v-for="category in categories" 
                    :key="category.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{'bg-primary/5': category.id === formData.categoryId}"
                    @click="formData.categoryId = category.id; isCategoryDropdownOpen = false"
                  >
                    <font-awesome-icon 
                      :icon="['fas', category.icon]" 
                      :class="category.color"
                      class="mr-2"
                    />
                    <span>{{ category.name }}</span>
                  </div>
                </div>
                <p v-if="errors.categoryId" class="mt-1 text-sm text-danger">
                  {{ errors.categoryId }}
                </p>
              </div>
            </div>

            <!-- Account Selection -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Tài khoản <span class="text-danger">*</span>
              </label>
              <div class="relative select-none account-dropdown-container">
                <div 
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="[
                    isAccountDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                    errors.accountId ? 'border-danger/50' : ''
                  ]"
                  @click="isAccountDropdownOpen = !isAccountDropdownOpen"
                >
                  <div class="flex items-center flex-1">
                    <font-awesome-icon 
                      :icon="['fas', selectedAccount.icon]" 
                      :class="selectedAccount.color"
                      class="mr-2"
                    />
                    <span>{{ selectedAccount.name }}</span>
                  </div>
                  <font-awesome-icon 
                    :icon="['fas', 'chevron-down']" 
                    class="text-gray-400 ml-2 transition-transform"
                    :class="{'rotate-180': isAccountDropdownOpen}"
                  />
                </div>

                <div 
                  v-if="isAccountDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                >
                  <div 
                    v-for="account in accounts" 
                    :key="account.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{'bg-primary/5': account.id === formData.accountId}"
                    @click="formData.accountId = account.id; isAccountDropdownOpen = false"
                  >
                    <font-awesome-icon 
                      :icon="['fas', account.icon]" 
                      :class="account.color"
                      class="mr-2"
                    />
                    <span>{{ account.name }}</span>
                  </div>
                </div>
                <p v-if="errors.accountId" class="mt-1 text-sm text-danger">
                  {{ errors.accountId }}
                </p>
              </div>
            </div>

            <!-- Date Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Ngày ghi chi <span class="text-danger">*</span>
              </label>
              <div class="w-full">
                <el-date-picker
                  v-model="formData.date"
                  type="datetime"
                  :format="'DD/MM/YYYY HH:MM'"
                  :placeholder="'Chọn ngày'"
                  class="date-picker-custom w-full"
                  :class="{'error-date-picker': errors.date}"
                  style="width: 100%;"
                />
                <p v-if="errors.date" class="mt-1 text-sm text-danger">
                  {{ errors.date }}
                </p>
              </div>
            </div>

            <!-- Location Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Địa điểm
              </label>
              <input 
                v-model="formData.location"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập địa điểm"
              />
            </div>

            <!-- Event/Trip Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Chuyến đi/Sự kiện
              </label>
              <input 
                v-model="formData.event"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập tên chuyến đi hoặc sự kiện"
              />
            </div>

            <!-- Paid To Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Chi cho ai/đơn vị nào
              </label>
              <input 
                v-model="formData.paidTo"
                type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập người/tổ chức nhận tiền"
              />
            </div>

            <!-- Description -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Mô tả
              </label>
              <textarea
                v-model="formData.note"
                rows="3"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                :class="formData.note ? 'bg-white' : 'bg-gray-50'"
                placeholder="Nhập mô tả"
              ></textarea>
            </div>

            <!-- Submit Button -->
            <div class="flex justify-end space-x-3">
              <!-- Nút hủy chỉ hiển thị khi đang ở chế độ sửa -->
              <button
                v-if="isEditMode"
                type="button"
                class="bg-gray-100 text-text-secondary font-medium px-6 py-2 rounded-lg hover:bg-gray-200 transition-colors"
                @click="cancelEdit"
              >
                Hủy
              </button>
              
              <button
                type="submit"
                class="bg-primary text-white font-medium px-6 py-2 rounded-lg hover:bg-primary/90 transition-colors"
              >
                {{ isEditMode ? 'Cập nhật' : 'Lưu' }}
              </button>
            </div>
          </form>
        </div>
      </div>
      
      <!-- Lịch sử bên phải -->
      <div class="lg:col-span-5">
        <div class="bg-surface rounded-2xl shadow-sm p-6">
          <h2 class="text-lg font-semibold text-text mb-4">Lịch sử ghi chi gần đây</h2>
          <div class="space-y-4 divide-y divide-gray-100">
            <div v-for="transaction in recentTransactions" :key="transaction.id" class="pt-4 first:pt-0">
              <div class="flex flex-col space-y-2">
                <!-- Thông tin giao dịch -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="bg-danger/10 p-3 rounded-lg flex-shrink-0">
                      <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-danger" />
                    </div>
                    <div>
                      <p class="font-medium text-text">{{ transaction.title }}</p>
                      <div class="flex items-center text-sm text-text-secondary">
                        <span>{{ transaction.category }}</span>
                        <span class="mx-1">•</span>
                        <span>{{ transaction.date }}</span>
                      </div>
                    </div>
                  </div>
                  <p class="font-medium text-danger">
                    {{ formatCurrency(transaction.amount) }}
                  </p>
                </div>

                <!-- Nút tùy chọn -->
                <div class="flex justify-end space-x-2">
                  <button 
                    @click="() => handleViewDetail(transaction)"
                    class="text-primary bg-primary/5 hover:bg-primary/10 rounded px-2 py-1 flex items-center text-sm space-x-1 transition-colors"
                  >
                    <font-awesome-icon :icon="['fas', 'eye']" class="text-xs" />
                    <span>Chi tiết</span>
                  </button>
                  <button 
                    @click="() => handleEditTransaction(transaction)"
                    class="text-warning bg-warning/5 hover:bg-warning/10 rounded px-2 py-1 flex items-center text-sm space-x-1 transition-colors"
                  >
                    <font-awesome-icon :icon="['fas', 'pen']" class="text-xs" />
                    <span>Sửa</span>
                  </button>
                </div>
              </div>
            </div>
            
            <!-- Hiển thị khi không có giao dịch -->
            <div v-if="recentTransactions.length === 0" class="py-8 flex flex-col items-center justify-center text-center">
              <div class="bg-gray-100 p-4 rounded-full mb-3">
                <font-awesome-icon :icon="['fas', 'receipt']" class="text-text-secondary text-xl" />
              </div>
              <p class="text-text-secondary">Chưa có giao dịch chi nào</p>
            </div>
          </div>
          
          <!-- Nút xem tất cả -->
          <div class="mt-4 pt-4 border-t border-gray-100">
            <router-link to="/history" class="text-primary hover:text-primary/80 flex items-center justify-center space-x-1">
              <span>Xem tất cả</span>
              <font-awesome-icon :icon="['fas', 'chevron-right']" class="text-sm" />
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal chi tiết giao dịch -->
    <TransactionDetailModal 
      :isOpen="isDetailModalOpen" 
      :transaction="selectedTransaction" 
      @close="isDetailModalOpen = false"
    />
  </div>
</template>

<style scoped>
/* Override Element Plus date picker styles */
.date-picker-custom :deep(.el-input__wrapper) {
  @apply !bg-transparent !shadow-none !box-border !border !border-gray-100 !rounded-lg hover:!border-gray-200;
  height: 42px !important; /* Match height of other inputs */
}

.date-picker-custom :deep(.el-input__wrapper.is-focus) {
  @apply !border-primary/50 !ring-1 !ring-primary/20;
}

.error-date-picker :deep(.el-input__wrapper) {
  @apply !border-danger/50;
}

.error-date-picker :deep(.el-input__wrapper.is-focus) {
  @apply !border-danger !ring-1 !ring-danger/20;
}

/* Remove input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>