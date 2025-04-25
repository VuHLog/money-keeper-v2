<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  incomeCategories: [
    { name: 'Lương', amount: 20000000, percentage: 50 },
    { name: 'Đầu tư', amount: 10000000, percentage: 25 },
    { name: 'Kinh doanh', amount: 10000000, percentage: 25 }
  ],
  expenseCategories: [
    { name: 'Ăn uống', amount: 5000000, percentage: 25 },
    { name: 'Nhà ở', amount: 8000000, percentage: 40 },
    { name: 'Di chuyển', amount: 3000000, percentage: 15 },
    { name: 'Giải trí', amount: 4000000, percentage: 20 }
  ],
  categoryTrends: {
    'Ăn uống': [4000000, 4500000, 5000000, 4800000, 5000000],
    'Nhà ở': [7000000, 7500000, 8000000, 7800000, 8000000],
    'Di chuyển': [2500000, 2800000, 3000000, 2900000, 3000000],
    'Giải trí': [3500000, 3800000, 4000000, 3900000, 4000000]
  }
})

// Chart options
const incomeCategoriesChart = ref({
  series: transactionData.value.incomeCategories.map(category => category.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.incomeCategories.map(category => category.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
  legend: {
    position: 'bottom'
  }
})

const expenseCategoriesChart = ref({
  series: transactionData.value.expenseCategories.map(category => category.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.expenseCategories.map(category => category.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#F97316'],
  legend: {
    position: 'bottom'
  }
})

const categoryTrendsChart = ref({
  series: Object.entries(transactionData.value.categoryTrends).map(([name, data]) => ({
    name,
    data
  })),
  chart: {
    type: 'line',
    height: 350,
    stacked: false
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5']
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#F97316']
})

const handleFilterChange = (filters) => {
  // TODO: Call API with new filters and update charts
  console.log('Filters changed:', filters)
}
</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions
      :show-time-range="true"
      :show-account="true"
      :show-category="true"
      @filter-change="handleFilterChange"
    />

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Income Categories -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ thu nhập</h3>
        <apexchart
          type="donut"
          height="350"
          :options="incomeCategoriesChart"
          :series="incomeCategoriesChart.series"
        />
      </div>

      <!-- Expense Categories -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ chi tiêu</h3>
        <apexchart
          type="donut"
          height="350"
          :options="expenseCategoriesChart"
          :series="expenseCategoriesChart.series"
        />
      </div>

      <!-- Category Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo danh mục</h3>
        <apexchart
          type="line"
          height="350"
          :options="categoryTrendsChart"
          :series="categoryTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 