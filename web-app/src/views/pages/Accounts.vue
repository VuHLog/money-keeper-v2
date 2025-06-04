<script setup>
import { ref, computed, onMounted } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faWallet,
  faCreditCard,
  faMoneyBillWave,
  faUniversity,
  faEllipsisVertical,
  faPlus,
  faPen,
  faTrash,
  faExchangeAlt,
  faTimes,
  faChevronDown,
  faExclamationTriangle,
  faChevronLeft,
  faChevronRight,
  faSearch
} from '@fortawesome/free-solid-svg-icons'
import { formatCurrency, formatCurrencyWithSymbol } from '@/utils/formatters'
import AddAccountModal from '@components/AddAccountModal.vue'
import EditAccountModal from '@components/EditAccountModal.vue'
import DeleteAccountModal from '@components/DeleteAccountModal.vue'
import TransferModal from '@components/TransferModal.vue'
import Avatar from '@/views/components/Avatar.vue'
import { useDictionaryBucketPaymentStore } from '@stores/DictionaryBucketPaymentStore.js'
import { useCurrencyStore } from '@stores/CurrencyStore.js'

library.add(
  faWallet,
  faCreditCard,
  faMoneyBillWave,
  faUniversity,
  faEllipsisVertical,
  faPlus,
  faPen,
  faTrash,
  faExchangeAlt,
  faTimes,
  faChevronDown,
  faExclamationTriangle,
  faChevronLeft,
  faChevronRight,
  faSearch
)

const dictionaryBucketPaymentStore = useDictionaryBucketPaymentStore()
const currencyStore = useCurrencyStore()
const accounts = ref([]);
const myAllAccounts = ref([]);
const isAddModalOpen = ref(false)
const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const isTransferModalOpen = ref(false)
const editingAccount = ref(null)
const deletingAccount = ref(null)
const selectedAccount = ref(null)
const totalBalance = ref(0)
const searchQuery = ref('')

// Computed properties for pagination
const paginationInfo = computed(() => {
  const start = ((dictionaryBucketPaymentStore.pagination.pageNumber - 1) * dictionaryBucketPaymentStore.pagination.pageSize) + 1
  const end = Math.min(start + dictionaryBucketPaymentStore.pagination.pageSize - 1, dictionaryBucketPaymentStore.pagination.totalElements)
  return {
    start,
    end,
    total: dictionaryBucketPaymentStore.pagination.totalElements,
    currentPage: dictionaryBucketPaymentStore.pagination.pageNumber,
    totalPages: dictionaryBucketPaymentStore.pagination.totalPages
  }
})

onMounted(async () => {
  myAllAccounts.value = await dictionaryBucketPaymentStore.getMyBucketPayments();
  await loadData();
  totalBalance.value = await dictionaryBucketPaymentStore.getTotalBalance();
})

const loadData = async () => {
  await dictionaryBucketPaymentStore.getMyBucketPaymentsPagination();
  accounts.value = dictionaryBucketPaymentStore.accounts;
}

const handlePageChange = async (newPage) => {
  dictionaryBucketPaymentStore.pagination.pageNumber = newPage
  await loadData()
}

const handleSearch = async () => {
  dictionaryBucketPaymentStore.pagination.search = searchQuery.value
  dictionaryBucketPaymentStore.pagination.pageNumber = 1 // Reset về trang đầu
  await loadData()
  totalBalance.value = await dictionaryBucketPaymentStore.getTotalBalance(searchQuery.value);
}

const handleTransfer = (account) => {
  selectedAccount.value = account
  isTransferModalOpen.value = true
}

const handleEdit = (account) => {
  editingAccount.value = account
  isEditModalOpen.value = true
}

const handleDelete = (account) => {
  deletingAccount.value = account
  isDeleteModalOpen.value = true
}

const handleAddAccount = async () => {
  await loadData();
  totalBalance.value = await dictionaryBucketPaymentStore.getTotalBalance(searchQuery.value);
}

const handleUpdateAccount = async () => {
  await loadData();
}

const handleConfirmDelete = async () => {
  await dictionaryBucketPaymentStore.deleteById(deletingAccount.value.id);
  await loadData();
  totalBalance.value = await dictionaryBucketPaymentStore.getTotalBalance(searchQuery.value);
  deletingAccount.value = null
  isDeleteModalOpen.value = false
}

const handleTransferConfirm = async (data) => {
  // Reload dữ liệu từ server để đảm bảo tính nhất quán
  await loadData();
  totalBalance.value = await dictionaryBucketPaymentStore.getTotalBalance(searchQuery.value);
  isTransferModalOpen.value = false
}
</script>

<template>
  <div class="w-full h-full p-4">
    <div class="bg-surface w-full rounded-lg h-full">
      <!-- Account Header -->
      <div class="w-full px-4 sm:px-6 py-5 border-b border-gray-100">
        <!-- First row: Total Balance and Add Button -->
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0 mb-4">
          <!-- Total Balance - Stack on mobile -->
          <div class="flex items-center space-x-3 w-full sm:w-auto">
            <font-awesome-icon :icon="['fas', 'wallet']" class="text-xl sm:text-2xl text-primary" />
            <p class="text-base sm:text-lg text-text-secondary">
              Tổng số dư: 
              <span class="font-semibold text-text block sm:inline mt-1 sm:mt-0 text-xl">
                {{ formatCurrency(totalBalance) }}
              </span>
            </p>
          </div>
          <!-- Add Button - Full width on mobile -->
          <button 
            @click="isAddModalOpen = true"
            class="w-full sm:w-auto bg-primary text-white px-4 sm:px-4 py-2 rounded-lg flex items-center justify-center space-x-2 sm:space-x-3 hover:bg-primary/90"
          >
            <font-awesome-icon :icon="['fas', 'plus']" class="text-lg" />
            <span class="text-base">Thêm tài khoản</span>
          </button>
        </div>

        <!-- Second row: Search -->
        <div class="flex items-center space-x-2 justify-end">
          <div class="relative flex-1 max-w-md">
            <input
              v-model="searchQuery"
              @keyup.enter="handleSearch"
              type="text"
              placeholder="Tìm kiếm theo tên tài khoản..."
              class="w-full px-4 py-2 border border-gray-200 rounded-lg text-text placeholder-gray-400 focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
            />
            <button
              @click="handleSearch"
              class="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-primary"
            >
              <font-awesome-icon :icon="['fas', 'search']" />
            </button>
          </div>
          <button
            v-if="searchQuery"
            @click="searchQuery = ''; handleSearch()"
            class="px-3 py-2 text-sm text-gray-600 hover:text-gray-800 border border-gray-200 rounded-lg hover:bg-gray-50"
          >
            Xóa bộ lọc
          </button>
        </div>
      </div>

      <!-- Account List -->
      <div class="w-full divide-y divide-gray-100">
        <!-- Empty state when no accounts -->
        <div v-if="accounts.length === 0" class="w-full px-4 sm:px-6 py-12 text-center">
          <div class="mx-auto w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
            <font-awesome-icon :icon="['fas', 'wallet']" class="text-4xl text-gray-400" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">
            {{ searchQuery ? 'Không tìm thấy tài khoản' : 'Chưa có tài khoản nào' }}
          </h3>
          <p class="text-gray-500 mb-6">
            {{ searchQuery ? `Không có tài khoản nào khớp với "${searchQuery}"` : 'Hãy thêm tài khoản đầu tiên để bắt đầu quản lý tài chính' }}
          </p>
          <button
            v-if="!searchQuery"
            @click="isAddModalOpen = true"
            class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
          >
            <font-awesome-icon :icon="['fas', 'plus']" class="mr-2" />
            Thêm tài khoản đầu tiên
          </button>
        </div>

        <!-- Account items -->
        <div 
          v-for="account in accounts" 
          :key="account.id"
          class="w-full px-4 sm:px-6 py-4 sm:py-6 hover:bg-gray-50/50"
        >
          <!-- Stack on mobile, row on desktop -->
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between w-full space-y-4 sm:space-y-0">
            <!-- Left section -->
            <div class="flex items-start space-x-4 sm:space-x-6">
              <div class="w-12 h-12 sm:w-14 sm:h-14 rounded-full bg-gray-100 flex items-center justify-center flex-shrink-0">
                <Avatar
                  :src="account.iconUrl"
                  :alt="account.accountName"
                  size="sm"
                />
              </div>
              <div class="flex flex-col">
                <h3 class="text-lg text-start sm:text-lg font-medium text-text">{{ account.accountName }}</h3>
                <p class="text-sm text-start text-text-secondary">{{ account.interpretation }}</p>
              </div>
            </div>

            <!-- Right section - Stack below on mobile -->
            <div class="flex items-center justify-between sm:justify-end w-full sm:w-auto space-x-4">
              <p :class="[
                account.balance >= 0 ? 'text-success' : 'text-danger', 
                'font-semibold text-lg sm:text-lg'
              ]">
                {{ formatCurrencyWithSymbol(account.balance, account.currency, account.currencySymbol) }}
              </p>

              <!-- Action buttons -->
              <div class="flex items-center space-x-2 sm:space-x-4">
                <button 
                  @click="handleTransfer(account)"
                  class="p-2 sm:p-3 text-text-secondary hover:text-text hover:bg-gray-100 rounded-lg"
                  title="Chuyển khoản"
                >
                  <font-awesome-icon :icon="['fas', 'exchange-alt']" class="text-lg sm:text-xl" />
                </button>
                <button 
                  @click="handleEdit(account)"
                  class="p-2 sm:p-3 text-text-secondary hover:text-text hover:bg-gray-100 rounded-lg"
                  title="Chỉnh sửa"
                >
                  <font-awesome-icon :icon="['fas', 'pen']" class="text-lg sm:text-xl" />
                </button>
                <button 
                  @click="handleDelete(account)"
                  class="p-2 sm:p-3 text-text-secondary hover:text-danger hover:bg-gray-100 rounded-lg"
                  title="Xóa"
                >
                  <font-awesome-icon :icon="['fas', 'trash']" class="text-lg sm:text-xl" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination section - only show if there are accounts -->
      <div v-if="accounts.length > 0" class="mt-4 flex justify-between items-center px-6 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ paginationInfo.total }} tài khoản
        </div>

        <!-- Pagination controls -->
        <div class="flex space-x-2">
          <!-- Previous button -->
          <button @click="handlePageChange(paginationInfo.currentPage - 1)" :disabled="paginationInfo.currentPage === 1"
            class="px-3 py-1 rounded-lg text-sm" :class="[
              paginationInfo.currentPage === 1
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]">
            <font-awesome-icon :icon="['fas', 'chevron-left']" />
          </button>

          <!-- Page numbers -->
          <div v-for="pageNumber in paginationInfo.totalPages" :key="pageNumber">
            <button v-if="
              pageNumber === 1 ||
              pageNumber === paginationInfo.totalPages ||
              (pageNumber >= paginationInfo.currentPage - 1 && pageNumber <= paginationInfo.currentPage + 1)
            " @click="handlePageChange(pageNumber)" class="px-3 py-1 rounded-lg text-sm" :class="[
                pageNumber === paginationInfo.currentPage
                  ? 'bg-primary text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
              ]">
              {{ pageNumber }}
            </button>

            <!-- Ellipsis -->
            <span v-if="
              (pageNumber === 1 && paginationInfo.currentPage - 2 > 1) ||
              (pageNumber === paginationInfo.totalPages - 1 && paginationInfo.currentPage + 2 < paginationInfo.totalPages)
            " class="px-2 py-1 text-gray-500">
              ...
            </span>
          </div>

          <!-- Next button -->
          <button @click="handlePageChange(paginationInfo.currentPage + 1)"
            :disabled="paginationInfo.currentPage === paginationInfo.totalPages" class="px-3 py-1 rounded-lg text-sm"
            :class="[
              paginationInfo.currentPage === paginationInfo.totalPages
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]">
            <font-awesome-icon :icon="['fas', 'chevron-right']" />
          </button>
        </div>
      </div>
    </div>

    <!-- Keep existing modals -->
    <AddAccountModal
      :is-open="isAddModalOpen"
      @close="isAddModalOpen = false"
      @add="handleAddAccount"
    />

    <EditAccountModal
      v-if="isEditModalOpen"
      :is-open="isEditModalOpen"
      :account="editingAccount"
      @close="isEditModalOpen = false"
      @update="handleUpdateAccount"
    />

    <DeleteAccountModal
      :is-open="isDeleteModalOpen"
      :account="deletingAccount"
      @close="isDeleteModalOpen = false"
      @confirm="handleConfirmDelete"
    />

    <TransferModal
      :is-open="isTransferModalOpen"
      :initial-from-account="selectedAccount"
      :accounts="myAllAccounts"
      @close="isTransferModalOpen = false"
      @transfer="handleTransferConfirm"
    />
  </div>
</template>