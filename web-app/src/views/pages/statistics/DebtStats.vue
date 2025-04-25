<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  debtBreakdown: [
    { name: 'Vay mua nhà', amount: 500000000, percentage: 50 },
    { name: 'Vay mua xe', amount: 300000000, percentage: 30 },
    { name: 'Vay tiêu dùng', amount: 200000000, percentage: 20 }
  ],
  debtTrends: {
    'Vay mua nhà': [500000000, 450000000, 400000000, 350000000, 300000000],
    'Vay mua xe': [300000000, 250000000, 200000000, 150000000, 100000000],
    'Vay tiêu dùng': [200000000, 150000000, 100000000, 50000000, 0]
  }
})

// Chart options
const debtBreakdownChart = ref({
  series: transactionData.value.debtBreakdown.map(debt => debt.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.debtBreakdown.map(debt => debt.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
  legend: {
    position: 'bottom'
  }
})

const debtTrendsChart = ref({
  series: Object.entries(transactionData.value.debtTrends).map(([name, data]) => ({
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
  colors: ['#3B82F6', '#10B981', '#F59E0B']
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
      <!-- Debt Breakdown -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân tích khoản nợ</h3>
        <apexchart
          type="donut"
          height="350"
          :options="debtBreakdownChart"
          :series="debtBreakdownChart.series"
        />
      </div>

      <!-- Debt Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng trả nợ</h3>
        <apexchart
          type="line"
          height="350"
          :options="debtTrendsChart"
          :series="debtTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 