<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  dailyTrends: {
    'Thu nhập': [1000000, 1200000, 1500000, 1300000, 1400000, 1600000, 1800000],
    'Chi tiêu': [800000, 900000, 1000000, 950000, 1100000, 1200000, 1300000],
    'Tiết kiệm': [200000, 300000, 500000, 350000, 300000, 400000, 500000]
  },
  weeklyTrends: {
    'Thu nhập': [7000000, 8400000, 10500000, 9100000, 9800000],
    'Chi tiêu': [5600000, 6300000, 7000000, 6650000, 7700000],
    'Tiết kiệm': [1400000, 2100000, 3500000, 2450000, 2100000]
  }
})

// Chart options
const dailyTrendsChart = ref({
  series: Object.entries(transactionData.value.dailyTrends).map(([name, data]) => ({
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
    categories: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật']
  },
  colors: ['#3B82F6', '#EF4444', '#10B981']
})

const weeklyTrendsChart = ref({
  series: Object.entries(transactionData.value.weeklyTrends).map(([name, data]) => ({
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
    categories: ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4', 'Tuần 5']
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
      <!-- Daily Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng theo ngày</h3>
        <apexchart
          type="line"
          height="350"
          :options="dailyTrendsChart"
          :series="dailyTrendsChart.series"
        />
      </div>

      <!-- Weekly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng theo tuần</h3>
        <apexchart
          type="line"
          height="350"
          :options="weeklyTrendsChart"
          :series="weeklyTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 