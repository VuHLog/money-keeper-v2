<template>
  <div class="category-report">
    <FilterOptions 
      @filter-change="handleFilterChange" 
      @filter-reset="handleFilterReset"
      @apply-filter="fetchData"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo theo danh mục</h2>
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
      <div v-else-if="paginatedCategoryData.length === 0" class="py-10 text-center text-gray-500">
        Không có dữ liệu danh mục phù hợp với bộ lọc
      </div>
      
      <!-- Data table -->
      <div v-else class="overflow-auto max-h-[500px]">
        <table class="min-w-full">
          <thead class="bg-gray-50 sticky top-0">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Danh mục</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Loại</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số giao dịch</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tổng số tiền</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tỷ lệ (%)</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ghi chú</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="(category, index) in paginatedCategoryData" 
              :key="index"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 text-sm text-gray-900">
                <div class="flex items-center">
                  <div 
                    class="w-2 h-2 rounded-full mr-2"
                    :class="{
                      'bg-red-500': category.type === 'expense',
                      'bg-green-500': category.type === 'revenue'
                    }"
                  ></div>
                  {{ category.name }}
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <span 
                  class="px-2 py-1 text-xs rounded-full"
                  :class="{
                    'bg-red-50 text-red-600': category.type === 'expense',
                    'bg-green-50 text-green-600': category.type === 'revenue'
                  }"
                >
                  {{ category.type === 'expense' ? 'Chi tiêu' : 'Thu nhập' }}
                </span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">{{ category.transactionCount }}</td>
              <td 
                class="px-4 py-3 whitespace-nowrap text-sm font-medium"
                :class="{
                  'text-red-500': category.type === 'expense',
                  'text-green-600': category.type === 'revenue'
                }"
              >
                {{ formatAmount(category.totalAmount) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center">
                  <div class="w-16 bg-gray-200 rounded-full h-2 mr-2">
                    <div 
                      class="h-2 rounded-full" 
                      :class="{
                        'bg-red-500': category.type === 'expense',
                        'bg-green-500': category.type === 'revenue'
                      }"
                      :style="{ width: category.percentage + '%' }"
                    ></div>
                  </div>
                  {{ category.percentage.toFixed(1) }}%
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-gray-500">{{ category.note }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div v-if="categoryData.length > 0" class="mt-4 flex justify-between items-center px-4 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ categoryData.length }} danh mục
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
const categoryData = ref([]);
const loading = ref(true);
const filters = ref({
  timeOption: 'Theo tháng',
  account: ['all'],
  expenseCategory: ['all'],
  revenueCategory: ['all'],
  customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)],
  transactionType: ''
});

// Pagination state
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  totalPages: 1
});

// Computed properties for pagination
const paginatedCategoryData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return categoryData.value.slice(start, end);
});

const paginationInfo = computed(() => {
  const start = ((pagination.value.currentPage - 1) * pagination.value.pageSize) + 1;
  const end = Math.min(start + pagination.value.pageSize - 1, categoryData.value.length);
  return {
    start,
    end,
    total: categoryData.value.length
  };
});

// Update pagination based on data length
const updatePagination = () => {
  pagination.value.totalPages = Math.ceil(categoryData.value.length / pagination.value.pageSize);
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

// Sample data for demonstration - replace with API call
const fetchData = async () => {
  loading.value = true;
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Sample data - Replace with actual API data
    categoryData.value = [
      {
        name: 'Mua sắm',
        type: 'expense',
        transactionCount: 15,
        totalAmount: 2500000,
        percentage: 25.0,
        note: 'Tổng hợp chi tiêu mua sắm'
      },
      {
        name: 'Hóa đơn',
        type: 'expense',
        transactionCount: 8,
        totalAmount: 1500000,
        percentage: 15.0,
        note: 'Điện, nước, internet'
      },
      {
        name: 'Đi lại',
        type: 'expense',
        transactionCount: 20,
        totalAmount: 1000000,
        percentage: 10.0,
        note: 'Xăng, vé xe'
      },
      {
        name: 'Lương',
        type: 'revenue',
        transactionCount: 1,
        totalAmount: 15000000,
        percentage: 88.2,
        note: 'Lương tháng'
      },
      {
        name: 'Thưởng',
        type: 'revenue',
        transactionCount: 1,
        totalAmount: 2000000,
        percentage: 11.8,
        note: 'Thưởng dự án'
      },
      {
        name: 'Ăn uống',
        type: 'expense',
        transactionCount: 25,
        totalAmount: 2000000,
        percentage: 20.0,
        note: 'Ăn uống hàng ngày'
      },
      {
        name: 'Giải trí',
        type: 'expense',
        transactionCount: 5,
        totalAmount: 800000,
        percentage: 8.0,
        note: 'Phim, nhạc, game'
      },
      {
        name: 'Sức khỏe',
        type: 'expense',
        transactionCount: 3,
        totalAmount: 600000,
        percentage: 6.0,
        note: 'Khám bệnh, thuốc men'
      },
      {
        name: 'Giáo dục',
        type: 'expense',
        transactionCount: 2,
        totalAmount: 500000,
        percentage: 5.0,
        note: 'Sách vở, khóa học'
      },
      {
        name: 'Tặng quà',
        type: 'expense',
        transactionCount: 2,
        totalAmount: 400000,
        percentage: 4.0,
        note: 'Quà sinh nhật, lễ tết'
      },
      {
        name: 'Đầu tư',
        type: 'expense',
        transactionCount: 1,
        totalAmount: 1000000,
        percentage: 10.0,
        note: 'Đầu tư cổ phiếu'
      },
      {
        name: 'Tiền lãi',
        type: 'revenue',
        transactionCount: 1,
        totalAmount: 500000,
        percentage: 2.9,
        note: 'Lãi tiết kiệm'
      }
    ];
    
    // Update pagination
    updatePagination();
  } catch (error) {
    console.error('Error fetching category data:', error);
    categoryData.value = [];
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
    revenueCategory: ['all'],
    customTimeRange: [(new Date()).toISOString().slice(0, 7), new Date().toISOString().slice(0, 7)],
    transactionType: ''
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
.category-report {
  @apply transition-all duration-300;
}

/* Ensure smooth transitions for filter changes */
.category-report > div {
  @apply transition-opacity duration-300;
}
</style> 