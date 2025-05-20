<template>
  <div class="spending-limit-report">
    <FilterOptions
      :show-transaction-type="false"
      :show-time-range="false"
      :show-revenue-category="false"
      :default-open="false"
      @filter-change="handleFilterChange" 
      @filter-reset="handleFilterReset"
      @apply-filter="handleApplyFilter"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo kiểm soát hạn mức chi tiêu</h2>
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
      <div v-else-if="paginatedSpendingLimits.length === 0" class="py-10 text-center text-gray-500">
        Không có dữ liệu hạn mức chi tiêu phù hợp với bộ lọc
      </div>
      
      <!-- Data table -->
      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-gray-50 sticky top-0">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên hạn mức</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tài khoản áp dụng</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Danh mục</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hạn mức</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đã chi tiêu</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Còn lại</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tiến độ</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thời gian</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="(limit, index) in paginatedSpendingLimits" 
              :key="index"
              class="hover:bg-gray-50 transition-colors"
            >
            <td class="px-4 py-3 text-sm text-gray-900">{{ limit.limitName }}</td>
            <td class="px-4 py-3 text-sm text-gray-900">{{ limit.account }}</td>
              <td class="px-4 py-3 text-sm text-gray-900 w-[300px]">
                <div class="line-clamp-3">
                  {{ limit.category }}
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600 text-end">{{ formatAmount(limit.limitAmount) }}</td>
              <td class="px-4 py-3 whitespace-nowrap text-sm font-medium text-red-500 text-end">{{ formatAmount(limit.spentAmount) }}</td>
              <td 
                class="px-4 py-3 whitespace-nowrap text-sm font-medium text-end"
                :class="{
                  'text-green-600': limit.remainingAmount > 0,
                  'text-red-500': limit.remainingAmount <= 0
                }"
              >
                {{ formatAmount(limit.remainingAmount) }}
              </td>
              <td class="px-4 py-3 text-sm">
                <span 
                  class="px-2 py-1 text-xs rounded-full"
                  :class="{
                    'bg-green-50 text-green-600': limit.status === 'Tốt',
                    'bg-yellow-50 text-yellow-600': limit.status === 'Cảnh báo',
                    'bg-red-50 text-red-600': limit.status === 'Vượt hạn mức'
                  }"
                >
                  {{ limit.status }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-end">
                <div class="w-full">
                  <div 
                    :class="{
                      'text-green-500': limit.percentUsed < 70,
                      'text-yellow-500': limit.percentUsed >= 70 && limit.percentUsed < 100,
                      'text-red-500': limit.percentUsed >= 100
                    }"
                  >{{ limit.percentUsed }}%</div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm">
                Từ {{ formatDate(limit.startDateLimit) }} đến {{ formatDate(limit.endDateLimit) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div v-if="paginatedSpendingLimits.length > 0" class="mt-4 flex justify-between items-center px-4 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ pagination.totalElements }} hạn mức
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
        <div class="grid grid-cols-3 gap-4">
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng hạn mức</div>
            <div class="text-lg font-medium text-gray-800">{{ formatAmount(totalLimit) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng đã chi tiêu</div>
            <div class="text-lg font-medium text-red-500">{{ formatAmount(totalSpent) }}</div>
          </div>
          <div class="bg-blue-50 p-3 rounded-lg">
            <div class="text-sm text-gray-600">Tổng còn lại</div>
            <div 
              class="text-lg font-medium"
              :class="{
                'text-green-600': totalRemaining > 0,
                'text-red-500': totalRemaining <= 0
              }"
            >
              {{ formatAmount(totalRemaining) }}
            </div>
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
import { faFileExcel, faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons';
import { useExpenseLimitStore } from '@/store/ExpenseLimitStore.js';

// Register Font Awesome icons
library.add(faFileExcel, faChevronLeft, faChevronRight);

// Store
const expenseLimitStore = useExpenseLimitStore();

// Data state
const spendingLimits = ref([]);
const spendingLimitsNoPagination = ref([]);
const loading = ref(true);
const filters = ref({
  timeOption: 'Theo tháng',
  customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
});

// Pagination state - connect to store pagination
const pagination = computed(() => {
  return {
    currentPage: expenseLimitStore.pagination.pageNumber,
    pageSize: expenseLimitStore.pagination.pageSize,
    totalPages: expenseLimitStore.pagination.totalPages,
    totalElements: expenseLimitStore.pagination.totalElements
  };
});

// Computed properties for pagination
const paginatedSpendingLimits = computed(() => {
  return spendingLimits.value;
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
    expenseLimitStore.pagination.pageNumber = newPage;
    await fetchData();
  }
};

// Get real data from API
const fetchData = async () => {
  loading.value = true;
  
  try {
    await expenseLimitStore.getExpenseLimitsPagination();
    spendingLimitsNoPagination.value = await expenseLimitStore.getExpenseLimits();

    // Transform data to match the format needed for the report
    spendingLimits.value = expenseLimitStore.expenseLimits.map(limit => {
      // Calculate spent amount and remaining amount
      const spentAmount = limit.spentAmount || 0;
      const remainingAmount = limit.amount - spentAmount;
      
      // Calculate percentage used
      const percentUsed = limit.amount ? Math.round((spentAmount / limit.amount) * 100) : 0;
      
      // Determine status based on percentage
      let status = 'Tốt';
      if (percentUsed >= 100) {
        status = 'Vượt hạn mức';
      } else if (percentUsed >= 70) {
        status = 'Cảnh báo';
      }
      
      return {
        limitName: limit.name,
        account: limit.bucketPayments?.accountName,
        category: limit.categories.map(category => category.name).join(', '),
        limitAmount: limit.amount,
        spentAmount: spentAmount,
        remainingAmount: remainingAmount,
        status: status,
        percentUsed: percentUsed,
        startDateLimit: limit.startDateLimit,
        endDateLimit: limit.endDateLimit
      };
    });
  } catch (error) {
    console.error('Error fetching spending limit data:', error);
    spendingLimits.value = [];
  } finally {
    loading.value = false;
  }
};

// Handle filter changes
const handleFilterChange = (newFilters) => {
  filters.value = { ...filters.value, ...newFilters };
  console.log(filters.value);
};

const handleApplyFilter = async () => {
  // Handle expense categories
  if (filters.value.expenseCategory && filters.value.expenseCategory.length > 0 && filters.value.expenseCategory[0] !== 'all') {
    expenseLimitStore.categoriesId = filters.value.expenseCategory.join(',');
  } else {
    expenseLimitStore.categoriesId = null;
  }
  
  // Handle account filter
  if (filters.value.account && filters.value.account.length > 0 && filters.value.account[0] !== 'all') {
    expenseLimitStore.bucketPaymentIds = filters.value.account.join(',');
  } else {
    expenseLimitStore.bucketPaymentIds = null;
  }
  await fetchData();
};

const handleFilterReset = () => {
  filters.value = {
    timeOption: 'Theo tháng',
    customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
  };
  
  // Reset store filters
  expenseLimitStore.categoriesId = null;
  expenseLimitStore.bucketPaymentIds = null;
  expenseLimitStore.resetPagination();
  
  fetchData();
};

// Computed properties for summary
const totalLimit = computed(() => {
  return spendingLimitsNoPagination.value.reduce((sum, limit) => sum + limit.amount, 0);
});

const totalSpent = computed(() => {
  return spendingLimitsNoPagination.value.reduce((sum, limit) => sum + limit.spentAmount, 0);
});

const totalRemaining = computed(() => {
  return totalLimit.value - totalSpent.value;
});

// Add formatDate function
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  // Check if the date is in yyyy-mm-dd format
  const dateParts = dateString.split('-');
  if (dateParts.length === 3) {
    // Convert to dd/mm/yyyy format
    return `${dateParts[2]}/${dateParts[1]}/${dateParts[0]}`;
  }
  
  return dateString; // Return original if not in expected format
};

// Format helpers
const formatAmount = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Export to Excel - implement later with real API
const exportExcel = async () => {
  try {
    await expenseLimitStore.exportExcelForReportExpenseLimit();
  } catch (error) {
    console.error('Error exporting to Excel:', error);
  }
};

// Lifecycle hooks
onMounted(() => {
  
  fetchData();
});
</script>

<style scoped>
.spending-limit-report {
  @apply transition-all duration-300;
}

/* Ensure smooth transitions for filter changes */
.spending-limit-report > div {
  @apply transition-opacity duration-300;
}
</style> 