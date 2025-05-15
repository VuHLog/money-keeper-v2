<template>
  <div class="transaction-history-report">
    <FilterOptions 
      @filter-change="handleFilterChange" 
      @filter-reset="handleFilterReset"
      @apply-filter="fetchData"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Lịch sử giao dịch</h2>
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
      <div v-else-if="paginatedTransactions.length === 0" class="py-10 text-center text-gray-500">
        Không có dữ liệu giao dịch phù hợp với bộ lọc
      </div>
      
      <!-- Data table -->
      <div v-else class="overflow-auto max-h-[500px]">
        <table class="min-w-full">
          <thead class="bg-gray-50 sticky top-0">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngày</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nội dung</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Danh mục</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số tiền</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Loại</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ghi chú</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="(transaction, index) in paginatedTransactions" 
              :key="index"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600">{{ formatDate(transaction.date) }}</td>
              <td class="px-4 py-3 text-sm text-gray-900">{{ transaction.content }}</td>
              <td class="px-4 py-3 text-sm text-gray-600">
                <div class="flex items-center">
                  <div 
                    class="w-2 h-2 rounded-full mr-2"
                    :class="{
                      'bg-red-500': transaction.type === 'expense',
                      'bg-green-500': transaction.type === 'revenue'
                    }"
                  ></div>
                  {{ transaction.category }}
                </div>
              </td>
              <td 
                class="px-4 py-3 whitespace-nowrap text-sm font-medium"
                :class="{
                  'text-red-500': transaction.type === 'expense',
                  'text-green-600': transaction.type === 'revenue'
                }"
              >
                {{ formatAmount(transaction.amount) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <span 
                  class="px-2 py-1 text-xs rounded-full"
                  :class="{
                    'bg-red-50 text-red-600': transaction.type === 'expense',
                    'bg-green-50 text-green-600': transaction.type === 'revenue'
                  }"
                >
                  {{ transaction.type === 'expense' ? 'Chi tiêu' : 'Thu nhập' }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-500">{{ transaction.note }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination section -->
      <div v-if="transactions.length > 0" class="mt-4 flex justify-between items-center px-4 py-3 border-t border-gray-200">
        <!-- Pagination info -->
        <div class="text-sm text-gray-500">
          Hiển thị {{ paginationInfo.start }} đến {{ paginationInfo.end }}
          trên tổng số {{ transactions.length }} giao dịch
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
const transactions = ref([]);
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

// Computed properties
const paginatedTransactions = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return transactions.value.slice(start, end);
});

const paginationInfo = computed(() => {
  const start = ((pagination.value.currentPage - 1) * pagination.value.pageSize) + 1;
  const end = Math.min(start + pagination.value.pageSize - 1, transactions.value.length);
  return {
    start,
    end,
    total: transactions.value.length
  };
});

// Sample data for demonstration - replace with API call
const fetchData = async () => {
  loading.value = true;
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Sample data - Replace with actual API data
    transactions.value = [
      {
        date: '2023-05-15',
        content: 'Mua sắm tại siêu thị',
        category: 'Mua sắm',
        amount: 250000,
        type: 'expense',
        note: 'Mua thực phẩm và đồ dùng'
      },
      {
        date: '2023-05-12',
        content: 'Tiền lương tháng 5',
        category: 'Lương',
        amount: 15000000,
        type: 'revenue',
        note: 'Lương cơ bản'
      },
      {
        date: '2023-05-10',
        content: 'Tiền điện tháng 5',
        category: 'Hóa đơn',
        amount: 500000,
        type: 'expense',
        note: 'Hóa đơn điện tháng 5'
      },
      {
        date: '2023-05-08',
        content: 'Tiền thưởng dự án',
        category: 'Thưởng',
        amount: 2000000,
        type: 'revenue',
        note: 'Thưởng hoàn thành dự án'
      },
      {
        date: '2023-05-05',
        content: 'Đổ xăng',
        category: 'Đi lại',
        amount: 200000,
        type: 'expense',
        note: 'Đổ xăng xe máy'
      },
      {
        date: '2023-05-03',
        content: 'Ăn tối nhà hàng',
        category: 'Ăn uống',
        amount: 350000,
        type: 'expense',
        note: 'Ăn tối với gia đình'
      },
      {
        date: '2023-05-01',
        content: 'Mua sách',
        category: 'Giáo dục',
        amount: 180000,
        type: 'expense',
        note: 'Sách học tiếng Anh'
      },
      {
        date: '2023-04-28',
        content: 'Tiền lãi ngân hàng',
        category: 'Tiền lãi',
        amount: 50000,
        type: 'revenue',
        note: 'Lãi tiết kiệm'
      },
      {
        date: '2023-04-25',
        content: 'Mua thuốc',
        category: 'Y tế',
        amount: 120000,
        type: 'expense',
        note: 'Thuốc cảm cúm'
      },
      {
        date: '2023-04-22',
        content: 'Tiền nước',
        category: 'Hóa đơn',
        amount: 150000,
        type: 'expense',
        note: 'Hóa đơn nước tháng 4'
      },
      {
        date: '2023-04-20',
        content: 'Mua quần áo',
        category: 'Mua sắm',
        amount: 500000,
        type: 'expense',
        note: 'Quần áo mùa hè'
      },
      {
        date: '2023-04-18',
        content: 'Thưởng KPI',
        category: 'Thưởng',
        amount: 1000000,
        type: 'revenue',
        note: 'Thưởng KPI quý 1'
      }
    ];
    
    // Update pagination
    updatePagination();
  } catch (error) {
    console.error('Error fetching transaction data:', error);
    transactions.value = [];
  } finally {
    loading.value = false;
  }
};

// Update pagination based on data length
const updatePagination = () => {
  pagination.value.totalPages = Math.ceil(transactions.value.length / pagination.value.pageSize);
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
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};

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
.transaction-history-report {
  @apply transition-all duration-300;
}

/* Ensure smooth transitions for filter changes */
.transaction-history-report > div {
  @apply transition-opacity duration-300;
}
</style> 