<template>
  <div class="spending-limit-report">
    <FilterOptions 
      @filter-change="handleFilterChange" 
      @filter-reset="handleFilterReset"
      @apply-filter="fetchData"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo kiểm soát hạn mức chi tiêu</h2>
        <button 
          @click="exportToExcel" 
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
      <div v-else class="overflow-auto max-h-[500px]">
        <table class="min-w-full">
          <thead class="bg-gray-50 sticky top-0">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Danh mục</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hạn mức</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đã chi tiêu</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Còn lại</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tiến độ</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="(limit, index) in paginatedSpendingLimits" 
              :key="index"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 text-sm text-gray-900">
                <div class="flex items-center">
                  <div class="w-2 h-2 rounded-full mr-2 bg-primary"></div>
                  {{ limit.category }}
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">{{ formatAmount(limit.limitAmount) }}</td>
              <td class="px-4 py-3 whitespace-nowrap text-sm font-medium text-red-500">{{ formatAmount(limit.spentAmount) }}</td>
              <td 
                class="px-4 py-3 whitespace-nowrap text-sm font-medium"
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
              <td class="px-4 py-3 text-sm">
                <div class="w-full bg-gray-200 rounded-full h-2.5">
                  <div 
                    class="h-2.5 rounded-full" 
                    :class="{
                      'bg-green-500': limit.percentUsed < 70,
                      'bg-yellow-500': limit.percentUsed >= 70 && limit.percentUsed < 100,
                      'bg-red-500': limit.percentUsed >= 100
                    }"
                    :style="{ width: Math.min(limit.percentUsed, 100) + '%' }"
                  ></div>
                </div>
                <div class="text-xs text-gray-500 mt-1">{{ limit.percentUsed }}%</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div v-if="spendingLimits.length > 0" class="mt-4 flex justify-between items-center px-4 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ spendingLimits.length }} hạn mức
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

// Register Font Awesome icons
library.add(faFileExcel, faChevronLeft, faChevronRight);

// Data state
const spendingLimits = ref([]);
const loading = ref(true);
const filters = ref({
  timeOption: 'Theo tháng',
  account: ['all'],
  expenseCategory: ['all'],
  customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
});

// Pagination state
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  totalPages: 1
});

// Computed properties for pagination
const paginatedSpendingLimits = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return spendingLimits.value.slice(start, end);
});

const paginationInfo = computed(() => {
  const start = ((pagination.value.currentPage - 1) * pagination.value.pageSize) + 1;
  const end = Math.min(start + pagination.value.pageSize - 1, spendingLimits.value.length);
  return {
    start,
    end,
    total: spendingLimits.value.length
  };
});

// Update pagination based on data length
const updatePagination = () => {
  pagination.value.totalPages = Math.ceil(spendingLimits.value.length / pagination.value.pageSize);
  // Reset to page 1 if current page exceeds total pages
  if (pagination.value.currentPage > pagination.value.totalPages) {
    pagination.value.currentPage = 1;
  }
};

// Handle page change
const handlePageChange = (newPage) => {
  if (newPage >= 1 && newPage <= pagination.value.totalPages) {
    pagination.value.currentPage = newPage;
  }
};

// Computed properties for summary
const totalLimit = computed(() => {
  return spendingLimits.value.reduce((sum, limit) => sum + limit.limitAmount, 0);
});

const totalSpent = computed(() => {
  return spendingLimits.value.reduce((sum, limit) => sum + limit.spentAmount, 0);
});

const totalRemaining = computed(() => {
  return totalLimit.value - totalSpent.value;
});

// Sample data for demonstration - replace with API call
const fetchData = async () => {
  loading.value = true;
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Sample data - Replace with actual API data
    spendingLimits.value = [
      {
        category: 'Ăn uống',
        limitAmount: 3000000,
        spentAmount: 2500000,
        remainingAmount: 500000,
        status: 'Cảnh báo',
        percentUsed: 83.3
      },
      {
        category: 'Đi lại',
        limitAmount: 1000000,
        spentAmount: 500000,
        remainingAmount: 500000,
        status: 'Tốt',
        percentUsed: 50
      },
      {
        category: 'Mua sắm',
        limitAmount: 2000000,
        spentAmount: 2500000,
        remainingAmount: -500000,
        status: 'Vượt hạn mức',
        percentUsed: 125
      },
      {
        category: 'Giải trí',
        limitAmount: 1500000,
        spentAmount: 900000,
        remainingAmount: 600000,
        status: 'Tốt',
        percentUsed: 60
      },
      {
        category: 'Hóa đơn',
        limitAmount: 1800000,
        spentAmount: 1750000,
        remainingAmount: 50000,
        status: 'Cảnh báo',
        percentUsed: 97.2
      },
      {
        category: 'Y tế',
        limitAmount: 2000000,
        spentAmount: 600000,
        remainingAmount: 1400000,
        status: 'Tốt',
        percentUsed: 30
      },
      {
        category: 'Giáo dục',
        limitAmount: 1500000,
        spentAmount: 900000,
        remainingAmount: 600000,
        status: 'Tốt',
        percentUsed: 60
      },
      {
        category: 'Đồ gia dụng',
        limitAmount: 1000000,
        spentAmount: 1200000,
        remainingAmount: -200000,
        status: 'Vượt hạn mức',
        percentUsed: 120
      },
      {
        category: 'Quà tặng',
        limitAmount: 800000,
        spentAmount: 500000,
        remainingAmount: 300000,
        status: 'Tốt',
        percentUsed: 62.5
      },
      {
        category: 'Thú cưng',
        limitAmount: 600000,
        spentAmount: 520000,
        remainingAmount: 80000,
        status: 'Cảnh báo',
        percentUsed: 86.7
      },
      {
        category: 'Khác',
        limitAmount: 500000,
        spentAmount: 100000,
        remainingAmount: 400000,
        status: 'Tốt',
        percentUsed: 20
      },
      {
        category: 'Đầu tư',
        limitAmount: 5000000,
        spentAmount: 4000000,
        remainingAmount: 1000000,
        status: 'Tốt',
        percentUsed: 80
      }
    ];
    
    // Update pagination
    updatePagination();
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
};

const handleFilterReset = () => {
  filters.value = {
    timeOption: 'Theo tháng',
    account: ['all'],
    expenseCategory: ['all'],
    customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)]
  };
  fetchData();
};

// Format helpers
const formatAmount = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Export to Excel
const exportToExcel = () => {
  // Implement Excel export logic here
  console.log('Exporting data to Excel...');
  alert('Tính năng xuất Excel đang được phát triển');
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