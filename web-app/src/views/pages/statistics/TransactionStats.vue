<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  categoryBreakdown: [
    { name: 'Ăn uống', amount: 5000000, percentage: 25 },
    { name: 'Di chuyển', amount: 3000000, percentage: 15 },
    { name: 'Giải trí', amount: 2000000, percentage: 10 },
    { name: 'Mua sắm', amount: 10000000, percentage: 50 }
  ],
  monthlyTrends: {
    'Thu nhập': [10000000, 12000000, 15000000, 13000000, 14000000],
    'Chi tiêu': [8000000, 9000000, 10000000, 9500000, 11000000],
    'Tiết kiệm': [2000000, 3000000, 5000000, 3500000, 3000000]
  }
})

// Chart options
const categoryBreakdownChart = ref({
  series: transactionData.value.categoryBreakdown.map(cat => cat.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.categoryBreakdown.map(cat => cat.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444'],
  legend: {
    position: 'bottom'
  }
})

const monthlyTrendsChart = ref({
  series: Object.entries(transactionData.value.monthlyTrends).map(([name, data]) => ({
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
  colors: ['#3B82F6', '#EF4444', '#10B981']
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
      <!-- Category Breakdown -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân tích theo danh mục</h3>
        <apexchart
          type="donut"
          height="350"
          :options="categoryBreakdownChart"
          :series="categoryBreakdownChart.series"
        />
      </div>

      <!-- Monthly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng thu chi</h3>
        <apexchart
          type="line"
          height="350"
          :options="monthlyTrendsChart"
          :series="monthlyTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 