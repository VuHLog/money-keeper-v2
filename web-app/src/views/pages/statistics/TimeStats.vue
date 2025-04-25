<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  dailyTrends: [100, 150, 200, 180, 250, 300, 280, 320, 350, 400, 380, 420],
  weeklyTrends: [1500, 1800, 2000, 2200, 2500, 2800, 3000],
  monthlyTrends: [8000, 9000, 10000, 11000, 12000, 13000],
  yearlyTrends: [100000, 120000, 140000, 160000]
})

// Chart options
const dailyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.dailyTrends
  }],
  chart: {
    type: 'line',
    height: 350
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']
  },
  colors: ['#3B82F6']
})

const weeklyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.weeklyTrends
  }],
  chart: {
    type: 'line',
    height: 350
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật']
  },
  colors: ['#10B981']
})

const monthlyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.monthlyTrends
  }],
  chart: {
    type: 'line',
    height: 350
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6']
  },
  colors: ['#F59E0B']
})

const yearlyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.yearlyTrends
  }],
  chart: {
    type: 'line',
    height: 350
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['2020', '2021', '2022', '2023']
  },
  colors: ['#EF4444']
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
      <!-- Daily Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo ngày</h3>
        <apexchart
          type="line"
          height="350"
          :options="dailyTrendsChart"
          :series="dailyTrendsChart.series"
        />
      </div>

      <!-- Weekly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo tuần</h3>
        <apexchart
          type="line"
          height="350"
          :options="weeklyTrendsChart"
          :series="weeklyTrendsChart.series"
        />
      </div>

      <!-- Monthly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo tháng</h3>
        <apexchart
          type="line"
          height="350"
          :options="monthlyTrendsChart"
          :series="monthlyTrendsChart.series"
        />
      </div>

      <!-- Yearly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo năm</h3>
        <apexchart
          type="line"
          height="350"
          :options="yearlyTrendsChart"
          :series="yearlyTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 