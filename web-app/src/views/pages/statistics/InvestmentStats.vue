<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  investmentPortfolio: [
    { name: 'Cổ phiếu A', amount: 50000000, percentage: 40 },
    { name: 'Cổ phiếu B', amount: 30000000, percentage: 24 },
    { name: 'Quỹ đầu tư', amount: 20000000, percentage: 16 },
    { name: 'Bất động sản', amount: 25000000, percentage: 20 }
  ],
  investmentPerformance: {
    'Cổ phiếu A': [50000000, 52000000, 51000000, 53000000, 55000000],
    'Cổ phiếu B': [30000000, 31000000, 30500000, 31500000, 32000000],
    'Quỹ đầu tư': [20000000, 20500000, 21000000, 21500000, 22000000],
    'Bất động sản': [25000000, 25500000, 26000000, 26500000, 27000000]
  }
})

// Chart options
const investmentPortfolioChart = ref({
  series: transactionData.value.investmentPortfolio.map(inv => inv.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.investmentPortfolio.map(inv => inv.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444'],
  legend: {
    position: 'bottom'
  }
})

const investmentPerformanceChart = ref({
  series: Object.entries(transactionData.value.investmentPerformance).map(([name, data]) => ({
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
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444']
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
      <!-- Investment Portfolio -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Danh mục đầu tư</h3>
        <apexchart
          type="donut"
          height="350"
          :options="investmentPortfolioChart"
          :series="investmentPortfolioChart.series"
        />
      </div>

      <!-- Investment Performance -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Hiệu suất đầu tư</h3>
        <apexchart
          type="line"
          height="350"
          :options="investmentPerformanceChart"
          :series="investmentPerformanceChart.series"
        />
      </div>
    </div>
  </div>
</template> 