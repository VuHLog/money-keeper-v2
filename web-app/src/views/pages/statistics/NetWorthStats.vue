<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  netWorthBreakdown: [
    { name: 'Tài sản lưu động', amount: 50000000, percentage: 40 },
    { name: 'Đầu tư', amount: 30000000, percentage: 24 },
    { name: 'Bất động sản', amount: 25000000, percentage: 20 },
    { name: 'Nợ', amount: -20000000, percentage: -16 }
  ],
  netWorthTrends: {
    'Tổng tài sản': [80000000, 85000000, 90000000, 95000000, 100000000],
    'Tổng nợ': [25000000, 23000000, 22000000, 21000000, 20000000],
    'Tài sản ròng': [55000000, 62000000, 68000000, 74000000, 80000000]
  }
})

// Chart options
const netWorthBreakdownChart = ref({
  series: transactionData.value.netWorthBreakdown.map(item => item.amount),
  chart: {
    type: 'bar',
    height: 350
  },
  plotOptions: {
    bar: {
      horizontal: true,
      dataLabels: {
        position: 'top'
      }
    }
  },
  dataLabels: {
    enabled: true,
    formatter: function(val) {
      return val.toLocaleString() + 'đ'
    }
  },
  xaxis: {
    categories: transactionData.value.netWorthBreakdown.map(item => item.name)
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444']
})

const netWorthTrendsChart = ref({
  series: Object.entries(transactionData.value.netWorthTrends).map(([name, data]) => ({
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
    <div class="grid grid-cols-1 gap-6">
      <!-- Net Worth Breakdown -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân tích tài sản ròng</h3>
        <apexchart
          type="bar"
          height="350"
          :options="netWorthBreakdownChart"
          :series="netWorthBreakdownChart.series"
        />
      </div>

      <!-- Net Worth Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng tài sản ròng</h3>
        <apexchart
          type="line"
          height="350"
          :options="netWorthTrendsChart"
          :series="netWorthTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 