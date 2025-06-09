<template>
  <div class="category-report">
    <FilterOptions 
      :show-account="false"
      :show-transaction-type="false"
      :default-open="false"
      @filter-change="handleFilterChange"
      @filter-reset="handleFilterReset"
      @apply-filter="handleApplyFilter"
    />
    
    <div class="bg-white rounded-lg shadow-sm p-4 mt-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium text-gray-800">Báo cáo theo danh mục</h2>
        <button 
          @click="exportExcel" 
          class="px-3 py-2 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition-colors flex items-center text-sm"
          v-if="paginatedCategoryData.length > 0"
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
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Số giao dịch</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Tổng số tiền</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Tỷ lệ (%)</th>
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
              <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-600 text-end">{{ category.transactionCount }}</td>
              <td 
                class="px-4 py-3 whitespace-nowrap text-sm font-medium text-end"
                :class="{
                  'text-red-500': category.type === 'expense',
                  'text-green-600': category.type === 'revenue'
                }"
              >
                {{ formatAmount(category.totalAmount) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center justify-end">
                  {{ category.percentage.toFixed(1) }}%
                </div>
              </td>
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
import { useReportStore } from '@/store/ReportStore';
import { useDictionaryExpenseStore } from '@stores/DictionaryExpenseStore.js'
import { useDictionaryRevenueStore } from '@stores/DictionaryRevenueStore.js'

// Register Font Awesome icons
library.add(faFileExcel, faChevronLeft, faChevronRight);

// Store
const reportStore = useReportStore();
const expenseStore = useDictionaryExpenseStore();
const revenueStore = useDictionaryRevenueStore();

// Data state
const categoryData = ref([]);
const loading = ref(true);
const filters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: ['all'],
  revenueCategory: ['all'],
  customTimeRange: null
});
const excelFilters = ref({
  timeOption: '',
  transactionType: 'all',
  account: [],
  expenseCategory: ['all'],
  revenueCategory: ['all'],
  customTimeRange: null
})

// Totals for percentage calculation
const totalExpenseAmount = ref(0);
const totalRevenueAmount = ref(0);
// Pagination state
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  totalPages: 1,
  totalElements: 0
});

// Computed properties for pagination
const paginatedCategoryData = computed(() => {
  // Add percentage to each category based on type
  return categoryData.value.map(category => {
    const total = category.type === 'expense' ? totalExpenseAmount.value : totalRevenueAmount.value;
    const percentage = total > 0 ? (category.totalAmount / total) * 100 : 0;
    
    return {
      ...category,
      percentage,
      transactionCount: category.totalTransaction // Map API field to UI field
    };
  });
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

// Load data from API
const loadData = async () => {
  loading.value = true;
  
  try {
    // Get category data
    const response = await reportStore.getReportCategory(filters.value, {
      pageNumber: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    });

    
    if (response) {
      categoryData.value = response.content;
      pagination.value.totalElements = response.totalElements;
      pagination.value.totalPages = response.totalPages;
    }
    const responseNoPaging = await reportStore.getReportCategoryNoPaging(filters.value);
    // Calculate totals for percentage calculations
    totalExpenseAmount.value = responseNoPaging
    .filter(item => item.type === 'expense')
    .reduce((sum, item) => sum + item.totalAmount, 0);
    
    totalRevenueAmount.value = responseNoPaging
    .filter(item => item.type === 'revenue')
    .reduce((sum, item) => sum + item.totalAmount, 0);
  } catch (error) {
    console.error('Error fetching category data:', error);
    categoryData.value = [];
  } finally {
    loading.value = false;
  }
};

// Handle filter changes
const handleFilterChange = (filterOptions) => {
  filters.value = {
    ...filters.value,
    ...filterOptions
  };
  console.log('Filters changed:', filters.value);
};

const handleFilterReset = async () => {
  filters.value = {
    timeOption: '',
    transactionType: 'all',
    account: [],
    customTimeRange: null,
    expenseCategory: reportStore.expenseCategoriesId,
    revenueCategory: reportStore.revenueCategoriesId,
  }
  pagination.value.currentPage = 1;
  await loadData();
};

const handleApplyFilter = async () => {
  excelFilters.value = filters.value;
  pagination.value.currentPage = 1;
  await loadData();
};

// Format helpers
const formatAmount = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Export to Excel
const exportExcel = async () => {
  try {
    await reportStore.exportExcelForReportCategory(excelFilters.value);
  } catch (error) {
    console.error('Error exporting to Excel:', error);
  }
};

// Lifecycle hooks
onMounted(async () => {
  reportStore.expenseCategoriesId = (await expenseStore.getMyExpenseCategories()).map(item => item.id);
  reportStore.revenueCategoriesId = (await revenueStore.getMyRevenueCategories()).map(item => item.id);
  filters.value.expenseCategory = reportStore.expenseCategoriesId;
  filters.value.revenueCategory = reportStore.revenueCategoriesId;
  await loadData();
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