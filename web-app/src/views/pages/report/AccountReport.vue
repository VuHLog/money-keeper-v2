<template>
  <div class="account-report">
    <FilterOptions 
      :show-expense-category="false"
      :show-revenue-category="false"
      :show-transaction-type="false"
      :default-open="false"
      @filter-change="handleFilterChange" 
      @filter-reset="handleFilterReset"
      @apply-filter="handleApplyFilter"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo theo tài khoản</h2>
        <button 
          @click="exportExcel" 
          class="px-3 py-2 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition-colors flex items-center text-sm"
        >
          <font-awesome-icon :icon="['fas', 'file-excel']" class="mr-2" />
          Xuất Excel
        </button>
      </div>
      
      <!-- Loading state -->
      <div v-if="loading" class="py-10">
        <div class="flex justify-center">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
        </div>
      </div>
      
      <!-- No data state -->
      <div v-else-if="paginatedAccounts.length === 0" class="py-10 text-center text-gray-500">
        Không có dữ liệu tài khoản phù hợp với bộ lọc
      </div>
      
      <!-- Data table -->
      <div v-else class="overflow-auto max-h-[500px]">
        <table class="min-w-full">
          <thead class="bg-gray-50 sticky top-0">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tài khoản</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số dư ban đầu</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tổng thu</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tổng chi</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số dư hiện tại</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Chênh lệch</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="(account, index) in paginatedAccounts" 
              :key="index"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 text-sm text-gray-900">
                <div class="flex items-center">
                  <font-awesome-icon :icon="['fas', 'wallet']" class="mr-2 text-primary" />
                  {{ account.accountName }}
                </div>
              </td>
              <td class="px-4 py-3 text-end whitespace-nowrap text-sm text-gray-600">{{ formatAmount(account.initialBalance) }}</td>
              <td class="px-4 py-3 text-end whitespace-nowrap text-sm font-medium text-green-600">{{ formatAmount(account.totalRevenue) }}</td>
              <td class="px-4 py-3 text-end whitespace-nowrap text-sm font-medium text-red-500">{{ formatAmount(account.totalExpense) }}</td>
              <td class="px-4 py-3 text-end whitespace-nowrap text-sm font-medium text-blue-600">{{ formatAmount(account.balance) }}</td>
              <td 
                class="px-4 py-3 text-end whitespace-nowrap text-sm font-medium"
                :class="{
                  'text-red-500': account.disparity < 0,
                  'text-green-600': account.disparity >= 0
                }"
              >
                {{ formatAmount(account.disparity) }}
                <span class="text-xs ml-1">
                  ({{ account.initialBalance !== 0 ? ((account.disparity / account.initialBalance) * 100).toFixed(1) : 0 }}%)
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div v-if="accounts.length > 0" class="mt-4 flex justify-between items-center px-4 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ accounts.length }} tài khoản
        </div>

        <!-- Pagination controls -->
        <div class="flex space-x-2">
          <!-- Previous button -->
          <button 
            @click="handlePageChange(pagination.currentPage - 1)" 
            :disabled="pagination.currentPage === 1"
            class="px-3 py-1 rounded-lg text-sm" 
            :class="[
              pagination.currentPage === 1 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]"
          >
            <font-awesome-icon :icon="['fas', 'chevron-left']" />
          </button>
          
          <!-- Page numbers -->
          <div v-for="pageNumber in pagination.totalPages" :key="pageNumber" class="inline-block">
            <button 
              v-if="
                pageNumber === 1 || 
                pageNumber === pagination.totalPages || 
                (pageNumber >= pagination.currentPage - 1 && pageNumber <= pagination.currentPage + 1)
              " 
              @click="handlePageChange(pageNumber)" 
              class="px-3 py-1 rounded-lg text-sm" 
              :class="[
                pageNumber === pagination.currentPage 
                  ? 'bg-primary text-white' 
                  : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
              ]"
            >
              {{ pageNumber }}
            </button>
            
            <!-- Ellipsis -->
            <span 
              v-if="
                (pageNumber === 1 && pagination.currentPage - 2 > 1) || 
                (pageNumber === pagination.totalPages - 1 && pagination.currentPage + 2 < pagination.totalPages)
              " 
              class="px-2 py-1 text-gray-500"
            >
              ...
            </span>
          </div>
          
          <!-- Next button -->
          <button 
            @click="handlePageChange(pagination.currentPage + 1)" 
            :disabled="pagination.currentPage === pagination.totalPages"
            class="px-3 py-1 rounded-lg text-sm" 
            :class="[
              pagination.currentPage === pagination.totalPages 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
            ]"
          >
            <font-awesome-icon :icon="['fas', 'chevron-right']" />
          </button>
        </div>
      </div>
      
      <!-- Summary section -->
      <div class="mt-4 pt-4 border-t border-gray-100">
        <div class="grid grid-cols-4 gap-4">
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng số dư ban đầu</div>
            <div class="text-lg font-medium text-gray-800">{{ formatAmount(totalInitialBalance) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng thu</div>
            <div class="text-lg font-medium text-green-600">{{ formatAmount(totalIncome) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng chi</div>
            <div class="text-lg font-medium text-red-500">{{ formatAmount(totalExpense) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng số dư hiện tại</div>
            <div class="text-lg font-medium text-blue-600">{{ formatAmount(totalCurrentBalance) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import FilterOptions from '@/views/components/FilterOptions.vue';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faFileExcel, faWallet, faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons';
import { useReportStore } from '@/store/ReportStore';

// Register Font Awesome icons
library.add(faFileExcel, faWallet, faChevronLeft, faChevronRight);

// Store
const reportStore = useReportStore();

// Data state
const accounts = ref([]);
const loading = ref(true);
const filters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: [],
  revenueCategory: [],
  customTimeRange: null
});
const excelFilters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: [],
  revenueCategory: [],
  customTimeRange: null
})

// Pagination state
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  totalPages: 1,
  totalElements: 0
});

// Computed properties for pagination
const paginatedAccounts = computed(() => {
  return accounts.value;
});

const paginationInfo = computed(() => {
  const start = ((pagination.value.currentPage - 1) * pagination.value.pageSize) + 1;
  const end = Math.min(start + pagination.value.pageSize - 1, pagination.value.totalElements);
  return {
    start,
    end,
    total: pagination.value.totalElements
  };
});

// Handle page change
const handlePageChange = async (newPage) => {
  if (newPage >= 1 && newPage <= pagination.value.totalPages) {
    pagination.value.currentPage = newPage;
    await loadData();
  }
};

// Computed properties for summary
const totalInitialBalance = ref(0);

const totalIncome = ref(0);

const totalExpense = ref(0);

const totalCurrentBalance = ref(0);

// Load data from API
const loadData = async () => {
  loading.value = true;
  
  try {
    const response = await reportStore.getReportBucketPayment({
      ...filters.value,
    }, {
      pageNumber: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    });
    if (response) {
      accounts.value = response.content;
      pagination.value.totalElements = response.totalElements;
      pagination.value.totalPages = response.totalPages;
    }

    // Get total summary
    const totalResponse = await reportStore.getReportTotalBucketPayment(filters.value);
    if (totalResponse) {
      totalInitialBalance.value = totalResponse.totalInitialBalance;
      totalIncome.value = totalResponse.totalRevenue;
      totalExpense.value = totalResponse.totalExpense;
      totalCurrentBalance.value = totalResponse.totalBalance;
    }

  } catch (error) {
    console.error('Error fetching account data:', error);
    accounts.value = [];
  } finally {
    loading.value = false;
  }
};

// Handle filter changes
const handleFilterChange = (filterOptions) => {
  filters.value = {
    ...filters.value,
    ...filterOptions
  }
  console.log('Filters changed:', filters.value)
}

const handleFilterReset = async () => {
  filters.value = {
    timeOption: '',
    transactionType: 'all',
    account: [],
    customTimeRange: null,
    expenseCategory: [],
    revenueCategory: [],
  }
  pagination.value.currentPage = 1;
  await loadData();
}


// Format helpers
const formatAmount = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Export to Excel
const exportExcel = async () => {
  try {
    await reportStore.exportExcelForReportBucketPayment(excelFilters.value);
  } catch (error) {
    console.error('Error exporting to Excel:', error);
  }
};

// Lifecycle hooks
onMounted(async () => {
  await loadData();
});

const handleApplyFilter = async () => {
  excelFilters.value = filters.value;
  pagination.value.currentPage = 1;
  await loadData();
}
</script>

<style scoped>
.account-report {
  @apply transition-all duration-300;
}

/* Ensure smooth transitions for filter changes */
.account-report > div {
  @apply transition-opacity duration-300;
}
</style> 