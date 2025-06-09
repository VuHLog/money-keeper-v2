<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useDictionaryRevenueStore } from '@/store/DictionaryRevenueStore'
import { useDictionaryBucketPaymentStore } from '@/store/DictionaryBucketPaymentStore'
import { formatCurrency, formatCurrencyWithSymbol } from '@/utils/formatters'
import { ElDatePicker } from 'element-plus'
import 'element-plus/theme-chalk/el-date-picker.css'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import TransactionDetailModal from '@/views/components/TransactionDetailModal.vue'
import Avatar from '@components/Avatar.vue'
import { getVietnamDateTime, formatDateToVietnam } from '@/utils/DateUtil'
import { useRevenueRegularStore } from '@/store/RevenueRegularStore'
import { faWallet, faGift, faChartLine, faCalendar, faMapMarkerAlt, faPlane, faUser, faStickyNote, faArrowUp, faReceipt, faChevronRight, faEye, faPen, faTrash } from '@fortawesome/free-solid-svg-icons'
import DeleteTransactionModal from '@/views/components/DeleteTransactionModal.vue'
import ToastManager from '@/views/components/ToastManager.vue'
import { Currency } from "@constants/Currency.js"
library.add(faWallet, faGift, faChartLine, faCalendar, faMapMarkerAlt, faPlane, faUser, faStickyNote, faArrowUp, faReceipt, faChevronRight, faEye, faPen, faTrash)

const dictionaryRevenueStore = useDictionaryRevenueStore()
const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const revenueRegularStore = useRevenueRegularStore()
const currencies = ref(Currency)
const isEditMode = ref(false)
const editingTransactionId = ref(null)
const categories = ref([])
const accounts = ref([])
// Recent transactions
const recentTransactions = ref([])

// Reference to ToastManager component
const toastManagerRef = ref(null)

onMounted(async () => {
  categories.value = await dictionaryRevenueStore.getMyRevenueCategoriesWithoutTransfer()
  accounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments();
  revenueRegularStore.pagination.pageSize = 4
  recentTransactions.value = await revenueRegularStore.getAllRevenueRegularPagination()

  // add and remove event listener when component is mounted and unmounted
  document.addEventListener('mousedown', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// Form data
const formData = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: getVietnamDateTime(),
  location: '',
  event: '',
  receivedFrom: '',
  note: ''
})

const errors = ref({
  amount: '',
  categoryId: '',
  accountId: '',
  date: ''
})

// Helper function to format currency based on currency code
const formatWithCurrency = (value, currencyCode, currencySymbol) => {
  if (value === null || value === undefined || value === '') return ''
  const numberValue = Number(value)
  
  // Format the number with thousand separators
  const formattedValue = new Intl.NumberFormat().format(numberValue).replace(/,/g, '.')
  
  // Return with the proper currency symbol
  if (currencyCode === 'VND') {
    return `${formattedValue} ${currencySymbol || '₫'}`
  } else {
    return `${currencySymbol || ''}${formattedValue}`
  }
}

// Format amount with currency based on selected account
const formattedAmount = computed({
  get: () => {
    if (!formData.value.amount) return ''
    
    // Format using the currency of the selected account
    if (selectedAccount.value) {
      return formatWithCurrency(
        formData.value.amount,
        selectedAccount.value.currency,
        selectedAccount.value.currencySymbol
      )
    }
    
    // Fallback to default VND format if no account is selected
    return formatCurrency(Number(formData.value.amount))
  },
  set: (value) => {
    const numericValue = value.replace(/[^\d]/g, '')
    formData.value.amount = numericValue ? Number(numericValue) : ''
  }
})

// Compute placeholder based on selected account currency
const amountPlaceholder = computed(() => {
  if (selectedAccount.value) {
    const symbol = selectedAccount.value.currencySymbol
    const currencyCode = selectedAccount.value.currency
    return currencyCode === 'VND' ? `0 ${symbol}` : `${symbol}0`
  }
  return '0 ₫' // Default placeholder
})

// Add function to disable future dates
const disableFutureDates = (time) => {
  return time.getTime() > Date.now()
}

const validateForm = () => {
  let isValid = true
  errors.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: ''
  }

  // Validate amount
  if (!formData.value.amount) {
    errors.value.amount = 'Vui lòng nhập số tiền'
    isValid = false
  }

  // Validate category
  if (!formData.value.categoryId) {
    errors.value.categoryId = 'Vui lòng chọn danh mục'
    isValid = false
  }

  // Validate account
  if (!formData.value.accountId) {
    errors.value.accountId = 'Vui lòng chọn tài khoản'
    isValid = false
  }

  // Validate date
  if (!formData.value.date) {
    errors.value.date = 'Vui lòng chọn ngày ghi thu'
    isValid = false
  } else {
    const selectedDate = new Date(formData.value.date)
    const now = new Date()
    if (selectedDate > now) {
      errors.value.date = 'Ngày ghi thu không được lớn hơn ngày hiện tại'
      isValid = false
    }
  }

  return isValid
}

const handleSubmit = () => {
  if (!validateForm()) return

  if (isEditMode.value) {
    updateTransaction()
  } else {
    addNewTransaction()
  }
}

// function to reset form after successful addition
const resetForm = () => {
  formData.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: getVietnamDateTime(),
    location: '',
    event: '',
    receivedFrom: '',
    note: ''
  }

  errors.value = {
    amount: '',
    categoryId: '',
    accountId: '',
    date: ''
  }

  // if in edit mode, switch to add new mode
  isEditMode.value = false
  editingTransactionId.value = null
}

const addNewTransaction = async () => {
  const newTransaction = {
    amount: formData.value.amount,
    location: formData.value.location || '',
    interpretation: formData.value.note || '',
    revenueDate: formData.value.date,
    dictionaryBucketPaymentId: selectedAccount.value.id,
    dictionaryRevenueId: selectedCategory.value.id,
    tripEvent: formData.value.event || '',
    collectMoneyWho: formData.value.receivedFrom || '',
  }

  try {
    const response = await revenueRegularStore.createRevenueRegular(newTransaction)

    recentTransactions.value = await revenueRegularStore.getAllRevenueRegularPagination()


    // Use the toast manager directly
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'success',
        title: 'Thành công!',
        content: 'Thêm giao dịch thu thành công!'
      })
    }
    
    //reset form after successful addition
    resetForm()
  } catch (error) {
    // Show error toast
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Lỗi!',
        content: 'Thêm giao dịch thu thất bại!'
      })
    }
  }
}

// function to handle view detail
const handleViewDetail = (transaction) => {
  selectedTransaction.value = transaction
  isDetailModalOpen.value = true
}

// function to handle edit transaction
const handleEditTransaction = (transaction) => {
  if (transaction.transferType === 'transfer') return;

  // switch to edit mode
  isEditMode.value = true
  editingTransactionId.value = transaction.id

  // find category and account id from list based on id
  let accountId, categoryId;
  if(transaction.dictionaryBucketPaymentId){
    accountId = transaction.dictionaryBucketPaymentId
  }else accountId = accounts.value.find(acc => acc.id === transaction.dictionaryBucketPayment.id)?.id

  if(transaction.dictionaryRevenueId){
    categoryId = transaction.dictionaryRevenueId
  } else {
    categoryId = null
  }

  // update form data with transaction information
  formData.value = {
    amount: transaction.convertedAmount,
    categoryId: categoryId || '',
    accountId: accountId || '',
    date: transaction.revenueDate,
    location: transaction.location || '',
    event: transaction.tripEvent || '',
    receivedFrom: transaction.collectMoneyWho || '',
    note: transaction.interpretation || ''
  }

  // scroll to top when edit
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Add computed properties for selected items
const selectedCategory = computed(() => {
  if (!formData.value.categoryId) {
    return { name: 'Chọn danh mục', icon: 'list', color: 'text-gray-400' }
  }
  return categories.value.find(cat => cat.id === formData.value.categoryId)
})

const selectedAccount = computed(() => {
  return accounts.value.find(acc => acc.id === formData.value.accountId)
})

// Add state for dropdowns
const isCategoryDropdownOpen = ref(false)
const isAccountDropdownOpen = ref(false)

// close dropdown when click outside
const handleClickOutside = (event) => {
  // close category dropdown when click outside
  const categoryDropdownEl = document.querySelector('.category-dropdown-container')
  if (categoryDropdownEl && !categoryDropdownEl.contains(event.target) && isCategoryDropdownOpen.value) {
    isCategoryDropdownOpen.value = false
  }

  // close account dropdown when click outside
  const accountDropdownEl = document.querySelector('.account-dropdown-container')
  if (accountDropdownEl && !accountDropdownEl.contains(event.target) && isAccountDropdownOpen.value) {
    isAccountDropdownOpen.value = false
  }
}
const isDetailModalOpen = ref(false)
const selectedTransaction = ref(null)

// update transaction
const updateTransaction = async () => {
  // find the index of the transaction to update in the array
  const index = recentTransactions.value.findIndex(t => t.id === editingTransactionId.value)

  if (index !== -1) {
    // update transaction information
    try {
      const updatedTransaction = {
        ...recentTransactions.value[index],
        amount: formData.value.amount,
        location: formData.value.location || '',
        interpretation: formData.value.note || '',
        revenueDate: formData.value.date,
        dictionaryBucketPaymentId: selectedAccount.value.id,
        dictionaryRevenueId: selectedCategory.value.id,
        tripEvent: formData.value.event || '',
        collectMoneyWho: formData.value.receivedFrom || '',
      }

      await revenueRegularStore.updateRevenueRegular(editingTransactionId.value, updatedTransaction);

      recentTransactions.value = await revenueRegularStore.getAllRevenueRegularPagination()

      // show success message using the toast manager
      if (toastManagerRef.value) {
        toastManagerRef.value.addToast({
          type: 'success',
          title: 'Thành công!',
          content: 'Cập nhật giao dịch thu thành công!'
        })
      }

      resetForm()
    } catch (error) {
      console.error(error)
      // Show error toast
      if (toastManagerRef.value) {
        toastManagerRef.value.addToast({
          type: 'error',
          title: 'Lỗi!',
          content: 'Cập nhật giao dịch thu thất bại!'
        })
      }
    }
  }
}

// cancel edit
const cancelEdit = () => {
  resetForm()
}

// Add new ref for search
const categorySearchQuery = ref('')

// Add computed for filtered categories
const filteredCategories = computed(() => {
  if (!categorySearchQuery.value) return categories.value
  const query = categorySearchQuery.value.toLowerCase()
  return categories.value.filter(cat =>
    cat.name.toLowerCase().includes(query)
  )
})

// Add refs for delete modal
const isDeleteModalOpen = ref(false)
const deletingTransaction = ref(null)

// Update delete handler
const handleDeleteTransaction = (transaction) => {
  if (transaction.transferType === 'transfer') return;
  deletingTransaction.value = transaction
  isDeleteModalOpen.value = true
}

// Add confirm delete handler
const handleConfirmDelete = async () => {
  try {
    await revenueRegularStore.deleteRevenueRegular(deletingTransaction.value.id)
    
    recentTransactions.value = await revenueRegularStore.getAllRevenueRegularPagination()

    // Show success toast
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'success',
        title: 'Thành công!',
        content: 'Xóa giao dịch thu thành công!'
      })
    }

    // Reset state
    deletingTransaction.value = null
    isDeleteModalOpen.value = false
  } catch (error) {
    console.error(error)
    // Show error toast
    if (toastManagerRef.value) {
      toastManagerRef.value.addToast({
        type: 'error',
        title: 'Lỗi!',
        content: 'Xóa giao dịch thu thất bại!'
      })
    }
  }
}
</script>

<template>
  <div class="p-4">
    <!-- Add ToastManager component -->
    <ToastManager ref="toastManagerRef" />
    
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <div class="lg:col-span-7">
        <div class="bg-surface rounded-2xl shadow-sm">
          <div class="px-6 pt-6 pb-2 border-b border-gray-100">
            <h2 class="text-lg font-semibold text-text">
              {{ isEditMode ? 'Chỉnh sửa giao dịch thu' : 'Thêm giao dịch thu mới' }}
            </h2>
          </div>

          <form @submit.prevent="handleSubmit" class="space-y-6 px-6 py-4">
            <!-- Amount Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Số tiền <span class="text-danger">*</span>
              </label>
              <input v-model="formattedAmount" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                :class="[
                  errors.amount ? 'border-danger/50 focus:border-danger focus:ring-danger/20' : 'border-gray-100 focus:border-primary/50',
                  formattedAmount ? 'bg-white' : 'bg-gray-50'
                ]" :placeholder="amountPlaceholder" />
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
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:!border-gray-200"
                  :class="[
                    isCategoryDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                    errors.categoryId ? 'border-danger/50' : ''
                  ]" @click="isCategoryDropdownOpen = !isCategoryDropdownOpen">
                  <div class="flex items-center flex-1">
                    <Avatar v-if="selectedCategory.iconUrl" :src="selectedCategory.iconUrl"
                      :alt="selectedCategory.name" size="m" class="mr-2" />
                    <span>{{ selectedCategory.name }}</span>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isCategoryDropdownOpen }" />
                </div>

                <div v-if="isCategoryDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg divide-y divide-gray-100">
                  <!-- Search input -->
                  <div class="p-2">
                    <input v-model="categorySearchQuery" type="text"
                      class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                      :class="categorySearchQuery ? 'bg-white' : 'bg-gray-50'" placeholder="Tìm kiếm danh mục..."
                      @click.stop />
                  </div>

                  <!-- Category list with max height -->
                  <div class="max-h-48 overflow-y-auto py-1">
                    <div v-for="category in filteredCategories" :key="category.id"
                      class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                      :class="{ 'bg-primary/5': category.id === formData.categoryId }"
                      @click="formData.categoryId = category.id; isCategoryDropdownOpen = false">
                      <Avatar :src="category.iconUrl" :alt="category.name" size="m" />
                      <span>{{ category.name }}</span>
                    </div>
                    <div v-if="filteredCategories.length === 0" class="px-3 py-2 text-text-secondary text-sm">
                      Không tìm thấy danh mục
                    </div>
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
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:!border-gray-200"
                  :class="[
                    isAccountDropdownOpen ? 'ring-1 ring-primary/20 border-primary/50' : '',
                    errors.accountId ? 'border-danger/50' : ''
                  ]" @click="isAccountDropdownOpen = !isAccountDropdownOpen">
                  <div class="flex items-center flex-1">
                    <template v-if="selectedAccount">
                      <Avatar :src="selectedAccount.iconUrl" :alt="selectedAccount.accountName" size="m" />
                      <span>{{ selectedAccount.accountName }}</span>
                    </template>
                    <template v-else>
                      <font-awesome-icon :icon="['fas', 'wallet']" class="mr-2 text-lg text-gray-400" />
                      <span>Chọn tài khoản</span>
                    </template>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isAccountDropdownOpen }" />
                </div>

                <div v-if="isAccountDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1">
                  <div v-for="account in accounts" :key="account.id"
                    class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                    :class="{ 'bg-primary/5': account.id === formData.accountId }"
                    @click="formData.accountId = account.id; isAccountDropdownOpen = false">
                    <Avatar :src="account.iconUrl" :alt="account.accountName" size="m" />
                    <span>{{ account.accountName }}</span>
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
                Ngày ghi thu <span class="text-danger">*</span>
              </label>
              <div class="w-full">
                <el-date-picker v-model="formData.date" type="datetime" :format="'DD/MM/YYYY HH:mm:ss'"
                  :value-format="'YYYY-MM-DD HH:mm:ss'" :placeholder="'Chọn ngày'" :disabled-date="disableFutureDates"
                  class="date-picker-custom w-full" :class="{ 'error-date-picker': errors.date }"
                  style="width: 100%;" />
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
              <input v-model="formData.location" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập địa điểm" />
            </div>

            <!-- Event/Trip Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Chuyến đi/Sự kiện
              </label>
              <input v-model="formData.event" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập tên chuyến đi hoặc sự kiện" />
            </div>

            <!-- Received From Input -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Nhận từ ai
              </label>
              <input v-model="formData.receivedFrom" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors"
                placeholder="Nhập người/tổ chức nhận tiền" />
            </div>

            <!-- Description -->
            <div>
              <label class="text-start block text-sm font-medium text-text-secondary mb-1">
                Mô tả
              </label>
              <textarea v-model="formData.note" rows="3"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50 transition-colors"
                :class="formData.note ? 'bg-white' : 'bg-gray-50'" placeholder="Nhập mô tả"></textarea>
            </div>

            <!-- Submit Button -->
            <div class="flex justify-end space-x-3">
              <!-- Nút hủy chỉ hiển thị khi đang ở chế độ sửa -->
              <button v-if="isEditMode" type="button"
                class="bg-gray-100 text-text-secondary font-medium px-6 py-2 rounded-lg hover:bg-gray-200 transition-colors"
                @click="cancelEdit">
                Hủy
              </button>

              <button type="submit"
                class="bg-primary text-white font-medium px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors">
                {{ isEditMode ? 'Cập nhật' : 'Lưu' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- history section -->
      <div class="lg:col-span-5">
        <div class="bg-surface rounded-2xl shadow-sm p-6">
          <h2 class="text-lg font-semibold text-text mb-4">Lịch sử ghi thu gần đây</h2>
          <div class="space-y-4 divide-y divide-gray-100">
            <div v-for="transaction in recentTransactions" :key="transaction.id" class="pt-4 first:pt-0">
              <div class="flex flex-col space-y-2">
                <!-- transaction information -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="bg-success/10 p-3 rounded-lg flex-shrink-0">
                      <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-success" />
                    </div>
                    <div>
                      <p class="font-medium flex text-text">
                        <Avatar :src="transaction?.dictionaryRevenue?.iconUrl || 'https://res.cloudinary.com/cloud1412/image/upload/v1747283065/noInfo_vwxabr.svg'" :alt="transaction?.dictionaryRevenue?.name || 'Không có thông tin'"
                          size="m" class="mr-2" />
                        {{ transaction?.dictionaryRevenue?.name || 'Không xác định' }}
                      </p>
                      <div class="flex flex-col justify-center text-sm text-text-secondary">
                        <span>{{ transaction.transferType === 'transfer' ? 'Nhận ' +
                          transaction?.dictionaryRevenue?.name?.toLowerCase() || 'Không xác định' + ' từ ' +
                          transaction.senderAccount.accountName :
                          transaction?.dictionaryRevenue?.name || 'Không xác định' }}</span>
                        <span>{{ formatDateToVietnam(transaction.revenueDate) }}</span>
                      </div>
                    </div>
                  </div>
                  <p class="font-medium text-success">
                    {{ transaction.currency === 'VND' ?
                      formatCurrencyWithSymbol(transaction.convertedAmount, transaction.currency, transaction.currencySymbol) :
                      `${formatCurrencyWithSymbol(transaction.convertedAmount, transaction.currency, transaction.currencySymbol)} ~ ${formatCurrencyWithSymbol(transaction.amount, 'VND', '₫')}`}}
                  </p>
                </div>

                <!-- option button -->
                <div class="flex justify-end space-x-2">
                  <button @click="() => handleViewDetail(transaction)"
                    class="text-primary bg-primary/5 hover:bg-primary/10 rounded px-2 py-1 flex items-center text-sm space-x-1 transition-colors">
                    <font-awesome-icon :icon="['fas', 'eye']" class="text-xs" />
                    <span>Chi tiết</span>
                  </button>
                  <button
                    @click="() => handleEditTransaction(transaction)"
                    class="text-warning bg-warning/5 hover:bg-warning/10 rounded px-2 py-1 flex items-center text-sm space-x-1 transition-colors"
                    :class="{ 'opacity-50 cursor-not-allowed': transaction.transferType === 'transfer' }">
                    <font-awesome-icon :icon="['fas', 'pen']" class="text-xs" />
                    <span>Sửa</span>
                  </button>
                  <button
                    @click="() => handleDeleteTransaction(transaction)"
                    class="text-danger bg-danger/5 hover:bg-danger/10 rounded px-2 py-1 flex items-center text-sm space-x-1 transition-colors"
                    :class="{ 'opacity-50 cursor-not-allowed': transaction.transferType === 'transfer' }">
                    <font-awesome-icon :icon="['fas', 'trash']" class="text-xs" />
                    <span>Xóa</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- show when no transaction -->
            <div v-if="recentTransactions.length === 0"
              class="py-8 flex flex-col items-center justify-center text-center">
              <div class="bg-gray-100 p-4 rounded-full mb-3">
                <font-awesome-icon :icon="['fas', 'receipt']" class="text-text-secondary text-xl" />
              </div>
              <p class="text-text-secondary">Chưa có giao dịch thu nào</p>
            </div>
          </div>

          <!-- show all button -->
          <div class="mt-4 pt-4 border-t border-gray-100">
            <router-link to="/transaction-history?transactionType=revenue"
              class="text-primary hover:text-primary/80 flex items-center justify-center space-x-1">
              <span>Xem tất cả</span>
              <font-awesome-icon :icon="['fas', 'chevron-right']" class="text-sm" />
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- detail transaction modal -->
    <TransactionDetailModal v-if="isDetailModalOpen" :isOpen="isDetailModalOpen" :transaction="selectedTransaction" :transactionType="'revenue'"
      @close="isDetailModalOpen = false" />

    <!-- Add delete modal -->
    <DeleteTransactionModal
      v-if="isDeleteModalOpen"
      :is-open="isDeleteModalOpen"
      :transaction="deletingTransaction"
      :transaction-type="'revenue'"
      @close="isDeleteModalOpen = false"
      @confirm="handleConfirmDelete"
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