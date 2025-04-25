<script setup>
import { ref, computed } from 'vue'
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
  faExclamationTriangle
} from '@fortawesome/free-solid-svg-icons'
import { formatCurrency } from '@/utils/formatters'
import AddAccountModal from '@components/AddAccountModal.vue'
import EditAccountModal from '@components/EditAccountModal.vue'
import DeleteAccountModal from '@components/DeleteAccountModal.vue'
import TransferModal from '@components/TransferModal.vue'

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
  faExclamationTriangle
)

// Mock data - sẽ được thay thế bằng dữ liệu từ API
const accounts = ref([
  {
    id: 1,
    name: 'Tiền mặt',
    icon: 'money-bill-wave',
    balance: 5000000,
    type: 'cash',
    description: 'Tiền mặt cá nhân'
  },
  {
    id: 2,
    name: 'Techcombank',
    icon: 'university',
    balance: 15000000,
    type: 'bank',
    description: 'Tài khoản ngân hàng chính'
  },
  {
    id: 3,
    name: 'Thẻ tín dụng BIDV',
    icon: 'credit-card',
    balance: -2000000,
    type: 'credit',
    description: 'Thẻ tín dụng cho chi tiêu'
  }
])

const accountTypes = [
  { 
    value: 'cash', 
    label: 'Tiền mặt',
    icon: 'money-bill-wave',
    color: 'text-success'
  },
  { 
    value: 'bank', 
    label: 'Tài khoản ngân hàng',
    icon: 'university',
    color: 'text-primary'
  },
  { 
    value: 'credit', 
    label: 'Thẻ tín dụng',
    icon: 'credit-card',
    color: 'text-warning'
  },
  { 
    value: 'investment', 
    label: 'Tài khoản đầu tư',
    icon: 'chart-line',
    color: 'text-info'
  },
  { 
    value: 'other', 
    label: 'Khác',
    icon: 'wallet',
    color: 'text-text-secondary'
  }
]

const isAddModalOpen = ref(false)
const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const isTransferModalOpen = ref(false)
const editingAccount = ref(null)
const deletingAccount = ref(null)
const selectedAccount = ref(null)

const totalBalance = computed(() => {
  return accounts.value.reduce((sum, account) => sum + account.balance, 0)
})

const getAccountIcon = (type) => {
  const accountType = accountTypes.find(t => t.value === type)
  return accountType ? accountType.icon : 'wallet'
}

const getAccountColor = (type) => {
  const accountType = accountTypes.find(t => t.value === type)
  return accountType ? accountType.color : 'text-text-secondary'
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

const handleAddAccount = (newAccount) => {
  accounts.value.push({
    id: accounts.value.length + 1,
    name: newAccount.name,
    type: newAccount.type,
    balance: newAccount.balance,
    icon: getAccountIcon(newAccount.type),
    description: newAccount.description
  })
}

const handleUpdateAccount = (updatedAccount) => {
  const index = accounts.value.findIndex(acc => acc.id === updatedAccount.id)
  if (index !== -1) {
    accounts.value[index] = updatedAccount
  }
}

const handleConfirmDelete = () => {
  const index = accounts.value.findIndex(acc => acc.id === deletingAccount.value.id)
  if (index !== -1) {
    accounts.value.splice(index, 1)
  }
  deletingAccount.value = null
  isDeleteModalOpen.value = false
}

const handleTransferConfirm = (data) => {
  const fromIndex = accounts.value.findIndex(acc => acc.id === data.fromAccount.id)
  const toIndex = accounts.value.findIndex(acc => acc.id === data.toAccount.id)

  if (fromIndex !== -1 && toIndex !== -1) {
    accounts.value[fromIndex].balance -= data.amount
    accounts.value[toIndex].balance += data.amount
  }

  isTransferModalOpen.value = false
}
</script>

<template>
  <div class="w-full h-full p-4">
    <div class="bg-surface w-full rounded-lg h-full">
      <!-- Account Header -->
      <div class="w-full px-4 sm:px-6 py-5 border-b border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0">
        <!-- Total Balance - Stack on mobile -->
        <div class="flex items-center space-x-3 w-full sm:w-auto">
          <font-awesome-icon :icon="['fas', 'wallet']" class="text-xl sm:text-2xl text-primary" />
          <p class="text-base sm:text-lg text-text-secondary">
            Tổng số dư: 
            <span class="font-semibold text-text block sm:inline mt-1 sm:mt-0">
              {{ formatCurrency(totalBalance) }}
            </span>
          </p>
        </div>
        <!-- Add Button - Full width on mobile -->
        <button 
          @click="isAddModalOpen = true"
          class="w-full sm:w-auto bg-primary text-white px-4 sm:px-8 py-3 rounded-lg flex items-center justify-center space-x-2 sm:space-x-3 hover:bg-primary/90"
        >
          <font-awesome-icon :icon="['fas', 'plus']" class="text-lg" />
          <span class="text-base">Thêm tài khoản</span>
        </button>
      </div>

      <!-- Account List -->
      <div class="w-full divide-y divide-gray-100">
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
                <font-awesome-icon 
                  :icon="['fas', getAccountIcon(account.type)]"
                  :class="[getAccountColor(account.type), 'text-xl sm:text-2xl']"
                />
              </div>
              <div class="flex flex-col">
                <h3 class="text-lg text-start sm:text-xl font-medium text-text">{{ account.name }}</h3>
                <p class="text-sm text-start text-text-secondary">{{ account.description }}</p>
              </div>
            </div>

            <!-- Right section - Stack below on mobile -->
            <div class="flex items-center justify-between sm:justify-end w-full sm:w-auto space-x-4">
              <p :class="[
                account.balance >= 0 ? 'text-success' : 'text-danger', 
                'font-semibold text-lg sm:text-xl'
              ]">
                {{ formatCurrency(account.balance) }}
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
    </div>

    <!-- Keep existing modals -->
    <AddAccountModal
      :is-open="isAddModalOpen"
      :account-types="accountTypes"
      @close="isAddModalOpen = false"
      @add="handleAddAccount"
    />

    <EditAccountModal
      :is-open="isEditModalOpen"
      :account="editingAccount"
      :account-types="accountTypes"
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
      :accounts="accounts"
      :initial-from-account="selectedAccount"
      @close="isTransferModalOpen = false"
      @transfer="handleTransferConfirm"
    />
  </div>
</template>